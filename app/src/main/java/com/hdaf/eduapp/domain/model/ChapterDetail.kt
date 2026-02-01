package com.hdaf.eduapp.domain.model

/**
 * Domain model for chapter detail (for playback).
 */
data class ChapterDetail(
    val id: String,
    val title: String,
    val bookTitle: String,
    val contentUrl: String,
    val signLanguageUrl: String?,
    val duration: Int, // in seconds
    val lastPosition: Int, // last played position in seconds
    val progress: Int, // 0-100
    val hasSignLanguage: Boolean,
    val transcriptUrl: String?
)
