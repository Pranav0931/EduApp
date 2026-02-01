package com.hdaf.eduapp.accessibility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.core.di.IoDispatcher
import com.hdaf.eduapp.data.local.dao.OCRCacheDao
import com.hdaf.eduapp.data.local.entity.OCRCacheEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * OCR Engine for extracting text from images.
 * Uses ML Kit for on-device text recognition.
 * Optimized for blind users to read printed materials.
 */
@Singleton
class OCREngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ocrCacheDao: OCRCacheDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    /**
     * Process image bytes and extract text.
     */
    suspend fun processImage(imageBytes: ByteArray): Resource<OCRResult> = withContext(ioDispatcher) {
        try {
            // Check cache first
            val imageHash = computeHash(imageBytes)
            val cached = ocrCacheDao.getCachedResult(imageHash)
            if (cached != null) {
                return@withContext Resource.Success(
                    OCRResult(
                        text = cached.extractedText,
                        confidence = cached.confidence,
                        isFromCache = true
                    )
                )
            }
            
            // Decode image
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: return@withContext Resource.Error("Failed to decode image")
            
            // Process with ML Kit
            val result = recognizeText(bitmap)
            
            if (result is Resource.Success) {
                // Cache the result
                ocrCacheDao.insertCache(
                    OCRCacheEntity(
                        id = UUID.randomUUID().toString(),
                        imageHash = imageHash,
                        extractedText = result.data.text,
                        confidence = result.data.confidence
                    )
                )
            }
            
            result
        } catch (e: Exception) {
            Timber.e(e, "Error processing image for OCR")
            Resource.Error("Failed to process image: ${e.message}")
        }
    }
    
    /**
     * Process a bitmap directly.
     */
    suspend fun processBitmap(bitmap: Bitmap): Resource<OCRResult> = withContext(ioDispatcher) {
        recognizeText(bitmap)
    }
    
    /**
     * Recognize text from bitmap using ML Kit.
     */
    private suspend fun recognizeText(bitmap: Bitmap): Resource<OCRResult> {
        return suspendCancellableCoroutine { continuation ->
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            
            textRecognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val fullText = visionText.text
                    val blocks = visionText.textBlocks
                    
                    // Calculate average confidence
                    var totalConfidence = 0f
                    var lineCount = 0
                    blocks.forEach { block ->
                        block.lines.forEach { line ->
                            line.confidence?.let { conf ->
                                totalConfidence += conf
                                lineCount++
                            }
                        }
                    }
                    val avgConfidence = if (lineCount > 0) totalConfidence / lineCount else 0f
                    
                    // Extract structured text
                    val structuredText = buildString {
                        blocks.forEachIndexed { index, block ->
                            append(block.text)
                            if (index < blocks.size - 1) {
                                append("\n\n")
                            }
                        }
                    }
                    
                    if (continuation.isActive) {
                        continuation.resume(
                            Resource.Success(
                                OCRResult(
                                    text = structuredText.ifEmpty { fullText },
                                    confidence = avgConfidence,
                                    blockCount = blocks.size,
                                    lineCount = lineCount
                                )
                            )
                        )
                    }
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "ML Kit text recognition failed")
                    if (continuation.isActive) {
                        continuation.resume(Resource.Error("Text recognition failed: ${e.message}"))
                    }
                }
            
            continuation.invokeOnCancellation {
                // ML Kit handles its own lifecycle
            }
        }
    }
    
    /**
     * Compute SHA-256 hash for caching.
     */
    private fun computeHash(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(bytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Clean up resources.
     */
    fun release() {
        textRecognizer.close()
    }
}

/**
 * Result of OCR processing.
 */
data class OCRResult(
    val text: String,
    val confidence: Float = 0f,
    val blockCount: Int = 0,
    val lineCount: Int = 0,
    val isFromCache: Boolean = false
)
