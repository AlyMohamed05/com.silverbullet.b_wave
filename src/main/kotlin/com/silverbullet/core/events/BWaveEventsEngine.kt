package com.silverbullet.core.events

import com.silverbullet.core.databse.dao.ConnectionDao
import com.silverbullet.core.databse.dao.DmMessageDao
import com.silverbullet.core.databse.entity.DmMessageEntity
import com.silverbullet.core.events.client.ClientEvent
import com.silverbullet.core.events.client.SeenDmMessageEvent
import com.silverbullet.core.events.client.SendDmMessageEvent
import com.silverbullet.core.events.server.*
import com.silverbullet.core.mapper.toDmMessage
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class BWaveEventsEngine(
    private val serializer: Json,
    private val connectionDao: ConnectionDao,
    private val dmsDao: DmMessageDao
) : EventsEngine {

    private val connections = ConcurrentHashMap<Int, DefaultWebSocketServerSession>()

    override suspend fun handleClient(
        userId: Int,
        session: DefaultWebSocketServerSession
    ) {
        // prevent user from having multiple connections
        if (connections.containsKey(userId)) {
            session.close(
                reason = CloseReason(
                    code = CloseReason.Codes.VIOLATED_POLICY,
                    message = "You have more than connection"
                )
            )
            return
        }
        connections[userId] = session

        onUserConnected(userId)

        try {
            for (frame in session.incoming) {
                val eventJson = (frame as? Frame.Text)?.readText() ?: continue
                val event = try {
                    serializer.decodeFromString<ClientEvent>(eventJson)
                } catch (e: Exception) {
                    continue
                }
                // handle the client event in another coroutine and go back to watch for other events.
                coroutineScope { launch { handleUserEvent(userId, event) } }
            }
        } finally {
            connections.remove(userId)
            notifyFriendsWithOfflineStatus(userId)
        }
    }

    override suspend fun sendServerEvent(userId: Int, event: ServerEvent) {
        val userSession = connections[userId] ?: return
        val eventJson = serializer.encodeToString(event)
        try {
            userSession.send(Frame.Text(eventJson))
        } catch (e: ClosedReceiveChannelException) {
            connections.remove(userId)
        }
    }

    private suspend fun handleUserEvent(userId: Int, event: ClientEvent) {
        when (event) {
            is SendDmMessageEvent -> handleSendDmMessageEvent(userId, event)
            is SeenDmMessageEvent -> handleSeenDmMessageEvent(userId, event)
        }
    }

    private suspend fun handleSendDmMessageEvent(
        userId: Int,
        event: SendDmMessageEvent
    ) {
        val dmMessageEntity = DmMessageEntity
            .create(
                text = event.text,
                senderId = userId,
                receiverId = event.receiverId
            )

        dmsDao.insert(dmMessageEntity)

        val dmMessage = dmMessageEntity.toDmMessage()

        // notify the receiver user
        val receivedMessageServerEvent = ReceivedDmMessageEvent(message = dmMessage)
        sendServerEvent(userId = dmMessageEntity.receiverId, event = receivedMessageServerEvent)

        // notify sender that message is sent
        val senderDmSentEvent = DmSentEvent(
            message = dmMessage,
            provisionalId = event.provisionalId
        )
        sendServerEvent(userId = dmMessageEntity.senderId, event = senderDmSentEvent)
    }

    private suspend fun handleSeenDmMessageEvent(
        userId: Int,
        event: SeenDmMessageEvent
    ) {
        val updatedMessage = dmsDao
            .markMessageAsSeen(
                messageId = event.messageId,
                receiverId = userId
            ) ?: return
        // if the updated message is null then this message already doesn't exist.
        // notice than the user who's trying to mark message as seen must be the receiver always.

        // now we should notify the sender with the updated message.
        val updateMessageEvent = UpdateDmMessageEvent(updatedMessage.toDmMessage())

        sendServerEvent(userId = updatedMessage.senderId, updateMessageEvent)
    }

    /**
     * sends user default events he should receive once he's connected
     */
    private suspend fun onUserConnected(userId: Int) = coroutineScope {
        launch {
            // get all user friends and notify about them.
            val userConnections = connectionDao.getUserConnections(userId).data
            userConnections.forEach { friendId ->
                if (connections[friendId] != null) {
                    // notify the new connected user that his friend is online
                    val event1 = FriendOnlineStatusEvent(friendId = friendId, online = true)
                    sendServerEvent(userId, event1)

                    // notify his friend
                    val event2 = FriendOnlineStatusEvent(friendId = userId, online = true)
                    sendServerEvent(friendId, event2)
                }
            }
        }
    }

    private suspend fun notifyFriendsWithOfflineStatus(offlineUserId: Int) = coroutineScope {
        launch {
            val userFriends = connectionDao.getUserConnections(offlineUserId).data
            val offlineEvent = FriendOnlineStatusEvent(offlineUserId, online = false)
            userFriends.forEach { friendId ->
                sendServerEvent(friendId, offlineEvent)
            }
        }
    }

}