package com.hdaf.eduapp.data.local.dao

import androidx.room.*
import com.hdaf.eduapp.data.local.entity.NoteEntity
import com.hdaf.eduapp.data.local.entity.TextHighlightEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for notes operations.
 */
@Dao
interface NotesDao {
    
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE chapterId = :chapterId ORDER BY createdAt DESC")
    fun getNotesByChapter(chapterId: String): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE bookId = :bookId ORDER BY createdAt DESC")
    fun getNotesByBook(bookId: String): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): NoteEntity?
    
    @Query("SELECT * FROM notes WHERE content LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)
    
    @Update
    suspend fun updateNote(note: NoteEntity)
    
    @Delete
    suspend fun deleteNote(note: NoteEntity)
    
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: String)
    
    @Query("DELETE FROM notes WHERE chapterId = :chapterId")
    suspend fun deleteNotesByChapter(chapterId: String)
    
    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNotesCount(): Int
    
    @Query("SELECT * FROM notes WHERE isSynced = 0")
    suspend fun getUnsyncedNotes(): List<NoteEntity>
    
    @Query("UPDATE notes SET isSynced = 1 WHERE id IN (:noteIds)")
    suspend fun markAsSynced(noteIds: List<String>)
}

// Note: BookmarkDao is already defined in StudyToolsDao.kt
// We reuse that DAO for bookmarks. See BookmarkDao in StudyToolsDao.kt

/**
 * DAO for text highlights operations.
 */
@Dao
interface TextHighlightsDao {
    
    @Query("SELECT * FROM text_highlights WHERE chapterId = :chapterId ORDER BY startOffset ASC")
    fun getHighlightsByChapter(chapterId: String): Flow<List<TextHighlightEntity>>
    
    @Query("SELECT * FROM text_highlights WHERE id = :highlightId")
    suspend fun getHighlightById(highlightId: String): TextHighlightEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: TextHighlightEntity)
    
    @Update
    suspend fun updateHighlight(highlight: TextHighlightEntity)
    
    @Delete
    suspend fun deleteHighlight(highlight: TextHighlightEntity)
    
    @Query("DELETE FROM text_highlights WHERE id = :highlightId")
    suspend fun deleteHighlightById(highlightId: String)
    
    @Query("DELETE FROM text_highlights WHERE chapterId = :chapterId")
    suspend fun deleteHighlightsByChapter(chapterId: String)
    
    @Query("SELECT COUNT(*) FROM text_highlights WHERE chapterId = :chapterId")
    suspend fun getHighlightsCountByChapter(chapterId: String): Int
}
