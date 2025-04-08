package com.example.purrytify.data.mapper

import com.example.purrytify.data.model.SongEntity
import com.example.purrytify.domain.model.Song

fun SongEntity.toDomain(): Song = Song(
    id = id,
    title = title,
    artist = artist,
    artworkUri = artworkUri,
    songUri = songUri,
    duration = duration,
    isLiked = isLiked,
    username = username,
    lastPlayed = lastPlayed,
    createdAt = createdAt
)

fun Song.toEntity(): SongEntity = SongEntity(
    id = id,
    title = title,
    artist = artist,
    artworkUri = artworkUri,
    songUri = songUri,
    duration = duration,
    isLiked = isLiked,
    username = username,
    lastPlayed = lastPlayed,
    createdAt = createdAt
)
