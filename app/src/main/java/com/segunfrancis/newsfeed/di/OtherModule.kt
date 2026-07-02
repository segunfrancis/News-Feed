package com.segunfrancis.newsfeed.di

import com.segunfrancis.newsfeed.data.BookmarkRepositoryImpl
import com.segunfrancis.newsfeed.domain.NewsFeedRepository
import com.segunfrancis.newsfeed.data.NewsFeedRepositoryImpl
import com.segunfrancis.newsfeed.domain.BookmarkRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class OtherModule {

    @Binds
    abstract fun bindRepository(repositoryImpl: NewsFeedRepositoryImpl): NewsFeedRepository

    @Binds
    abstract fun bindBookmarkRepository(repositoryImpl: BookmarkRepositoryImpl): BookmarkRepository
}
