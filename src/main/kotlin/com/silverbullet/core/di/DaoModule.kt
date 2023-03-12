package com.silverbullet.core.di

import com.silverbullet.core.databse.dao.RefreshTokenDao
import com.silverbullet.core.databse.dao.RefreshTokenDaoImpl
import com.silverbullet.core.databse.dao.UserDao
import com.silverbullet.core.databse.dao.UserDaoImpl
import org.koin.dsl.module

val daoModule = module {

    single<UserDao> {
        UserDaoImpl()
    }

    single<RefreshTokenDao> {
        RefreshTokenDaoImpl(get())
    }

}