package com.segunfrancis.newsfeed.di

import com.segunfrancis.newsfeed.repository.NewsFeedRepository
import com.segunfrancis.newsfeed.repository.NewsFeedRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class OtherModule {

    @Binds
    abstract fun bindRepository(repositoryImpl: NewsFeedRepositoryImpl): NewsFeedRepository
}
