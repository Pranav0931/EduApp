package com.hdaf.eduapp.core.accessibility

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Audio Description Manager for Blind Students.
 * 
 * Provides rich audio descriptions for visual content:
 * - Image descriptions using AI
 * - Chart/graph verbal explanations
 * - Diagram walkthroughs
 * - Video scene descriptions
 * - Mathematical equation reading
 * 
 * All descriptions available in Hindi and English.
 */
@Singleton
class AudioDescriptionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()
    
    private val _currentDescription = MutableStateFlow<AudioDescription?>(null)
    val currentDescription: StateFlow<AudioDescription?> = _currentDescription.asStateFlow()
    
    /**
     * Describe an image for blind users.
     */
    fun describeImage(
        imageType: ImageType,
        mainSubject: String,
        details: List<String> = emptyList(),
        isHindi: Boolean = true
    ): AudioDescription {
        val description = buildImageDescription(imageType, mainSubject, details, isHindi)
        val descriptionObj = AudioDescription(
            type = DescriptionType.IMAGE,
            shortDescription = mainSubject,
            fullDescription = description,
            language = if (isHindi) "hi" else "en"
        )
        _currentDescription.value = descriptionObj
        return descriptionObj
    }
    
    /**
     * Describe a chart or graph.
     */
    fun describeChart(
        chartType: ChartType,
        title: String,
        dataPoints: List<ChartDataPoint>,
        trend: String? = null,
        isHindi: Boolean = true
    ): AudioDescription {
        val description = buildChartDescription(chartType, title, dataPoints, trend, isHindi)
        val descriptionObj = AudioDescription(
            type = DescriptionType.CHART,
            shortDescription = title,
            fullDescription = description,
            language = if (isHindi) "hi" else "en"
        )
        _currentDescription.value = descriptionObj
        return descriptionObj
    }
    
    /**
     * Read a mathematical equation aloud.
     */
    fun describeMathEquation(
        equation: String,
        isHindi: Boolean = true
    ): AudioDescription {
        val spoken = convertMathToSpoken(equation, isHindi)
        val descriptionObj = AudioDescription(
            type = DescriptionType.MATH,
            shortDescription = equation,
            fullDescription = spoken,
            language = if (isHindi) "hi" else "en"
        )
        _currentDescription.value = descriptionObj
        return descriptionObj
    }
    
    /**
     * Describe a diagram step-by-step.
     */
    fun describeDiagram(
        diagramType: DiagramType,
        title: String,
        components: List<DiagramComponent>,
        isHindi: Boolean = true
    ): AudioDescription {
        val description = buildDiagramDescription(diagramType, title, components, isHindi)
        val descriptionObj = AudioDescription(
            type = DescriptionType.DIAGRAM,
            shortDescription = title,
            fullDescription = description,
            language = if (isHindi) "hi" else "en"
        )
        _currentDescription.value = descriptionObj
        return descriptionObj
    }
    
    /**
     * Describe video scene for audio description track.
     */
    fun describeVideoScene(
        scene: String,
        characters: List<String> = emptyList(),
        action: String,
        setting: String? = null,
        isHindi: Boolean = true
    ): AudioDescription {
        val description = buildVideoSceneDescription(scene, characters, action, setting, isHindi)
        val descriptionObj = AudioDescription(
            type = DescriptionType.VIDEO_SCENE,
            shortDescription = scene,
            fullDescription = description,
            language = if (isHindi) "hi" else "en"
        )
        _currentDescription.value = descriptionObj
        return descriptionObj
    }
    
    // ========== Private Helper Methods ==========
    
    private fun buildImageDescription(
        imageType: ImageType,
        mainSubject: String,
        details: List<String>,
        isHindi: Boolean
    ): String {
        val typeLabel = getImageTypeLabel(imageType, isHindi)
        val intro = if (isHindi) "यह $typeLabel है" else "This is a $typeLabel"
        val subject = if (isHindi) "जिसमें $mainSubject दिख रहा है" else "showing $mainSubject"
        
        val detailStr = if (details.isNotEmpty()) {
            val prefix = if (isHindi) "विवरण: " else "Details: "
            prefix + details.joinToString(if (isHindi) ", " else ", ")
        } else ""
        
        return "$intro $subject। $detailStr"
    }
    
    private fun buildChartDescription(
        chartType: ChartType,
        title: String,
        dataPoints: List<ChartDataPoint>,
        trend: String?,
        isHindi: Boolean
    ): String {
        val typeLabel = getChartTypeLabel(chartType, isHindi)
        val intro = if (isHindi) 
            "यह $typeLabel है जिसका शीर्षक है: $title" 
        else 
            "This is a $typeLabel titled: $title"
        
        val dataDesc = if (dataPoints.isNotEmpty()) {
            val prefix = if (isHindi) "मुख्य आंकड़े: " else "Key data: "
            prefix + dataPoints.take(5).joinToString("; ") { 
                "${it.label}: ${it.value}" 
            }
        } else ""
        
        val trendDesc = trend?.let {
            if (isHindi) "प्रवृत्ति: $it" else "Trend: $it"
        } ?: ""
        
        return "$intro। $dataDesc। $trendDesc"
    }
    
    private fun convertMathToSpoken(equation: String, isHindi: Boolean): String {
        // Convert mathematical notation to spoken form
        var spoken = equation
        
        if (isHindi) {
            spoken = spoken
                .replace("+", " जोड़ ")
                .replace("-", " घटा ")
                .replace("*", " गुणा ")
                .replace("×", " गुणा ")
                .replace("/", " भाग ")
                .replace("÷", " भाग ")
                .replace("=", " बराबर ")
                .replace("^2", " वर्ग ")
                .replace("^3", " घन ")
                .replace("√", " वर्गमूल ")
                .replace("π", " पाई ")
                .replace("∞", " अनंत ")
                .replace("≤", " से कम या बराबर ")
                .replace("≥", " से अधिक या बराबर ")
                .replace("<", " से कम ")
                .replace(">", " से अधिक ")
                .replace("(", " कोष्ठक खोलें ")
                .replace(")", " कोष्ठक बंद ")
        } else {
            spoken = spoken
                .replace("+", " plus ")
                .replace("-", " minus ")
                .replace("*", " times ")
                .replace("×", " times ")
                .replace("/", " divided by ")
                .replace("÷", " divided by ")
                .replace("=", " equals ")
                .replace("^2", " squared ")
                .replace("^3", " cubed ")
                .replace("√", " square root of ")
                .replace("π", " pi ")
                .replace("∞", " infinity ")
                .replace("≤", " less than or equal to ")
                .replace("≥", " greater than or equal to ")
                .replace("<", " less than ")
                .replace(">", " greater than ")
                .replace("(", " open parenthesis ")
                .replace(")", " close parenthesis ")
        }
        
        return spoken.trim()
    }
    
    private fun buildDiagramDescription(
        diagramType: DiagramType,
        title: String,
        components: List<DiagramComponent>,
        isHindi: Boolean
    ): String {
        val typeLabel = getDiagramTypeLabel(diagramType, isHindi)
        val intro = if (isHindi)
            "यह $typeLabel है: $title"
        else
            "This is a $typeLabel: $title"
        
        val componentDesc = components.mapIndexed { index, comp ->
            val num = index + 1
            if (isHindi)
                "$num. ${comp.name}: ${comp.description}"
            else
                "$num. ${comp.name}: ${comp.description}"
        }.joinToString(" ")
        
        return "$intro। $componentDesc"
    }
    
    private fun buildVideoSceneDescription(
        scene: String,
        characters: List<String>,
        action: String,
        setting: String?,
        isHindi: Boolean
    ): String {
        val characterStr = if (characters.isNotEmpty()) {
            if (isHindi)
                "पात्र: ${characters.joinToString(", ")}"
            else
                "Characters: ${characters.joinToString(", ")}"
        } else ""
        
        val settingStr = setting?.let {
            if (isHindi) "स्थान: $it" else "Setting: $it"
        } ?: ""
        
        val actionStr = if (isHindi) "क्रिया: $action" else "Action: $action"
        
        return "$scene। $characterStr। $settingStr। $actionStr"
    }
    
    private fun getImageTypeLabel(type: ImageType, isHindi: Boolean): String = when (type) {
        ImageType.PHOTOGRAPH -> if (isHindi) "फोटो" else "photograph"
        ImageType.ILLUSTRATION -> if (isHindi) "चित्र" else "illustration"
        ImageType.ICON -> if (isHindi) "आइकन" else "icon"
        ImageType.MAP -> if (isHindi) "नक्शा" else "map"
        ImageType.SCREENSHOT -> if (isHindi) "स्क्रीनशॉट" else "screenshot"
    }
    
    private fun getChartTypeLabel(type: ChartType, isHindi: Boolean): String = when (type) {
        ChartType.BAR -> if (isHindi) "बार चार्ट" else "bar chart"
        ChartType.LINE -> if (isHindi) "लाइन ग्राफ" else "line graph"
        ChartType.PIE -> if (isHindi) "पाई चार्ट" else "pie chart"
        ChartType.SCATTER -> if (isHindi) "स्कैटर प्लॉट" else "scatter plot"
        ChartType.HISTOGRAM -> if (isHindi) "हिस्टोग्राम" else "histogram"
    }
    
    private fun getDiagramTypeLabel(type: DiagramType, isHindi: Boolean): String = when (type) {
        DiagramType.FLOWCHART -> if (isHindi) "फ्लोचार्ट" else "flowchart"
        DiagramType.ANATOMY -> if (isHindi) "शारीरिक आरेख" else "anatomy diagram"
        DiagramType.CIRCUIT -> if (isHindi) "सर्किट आरेख" else "circuit diagram"
        DiagramType.MOLECULAR -> if (isHindi) "आणविक संरचना" else "molecular structure"
        DiagramType.GEOMETRIC -> if (isHindi) "ज्यामितीय आकृति" else "geometric figure"
        DiagramType.LIFECYCLE -> if (isHindi) "जीवन चक्र" else "life cycle"
    }
    
    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
    }
}

/**
 * Types of images.
 */
enum class ImageType {
    PHOTOGRAPH, ILLUSTRATION, ICON, MAP, SCREENSHOT
}

/**
 * Types of charts.
 */
enum class ChartType {
    BAR, LINE, PIE, SCATTER, HISTOGRAM
}

/**
 * Types of diagrams.
 */
enum class DiagramType {
    FLOWCHART, ANATOMY, CIRCUIT, MOLECULAR, GEOMETRIC, LIFECYCLE
}

/**
 * Chart data point.
 */
data class ChartDataPoint(
    val label: String,
    val value: String
)

/**
 * Diagram component.
 */
data class DiagramComponent(
    val name: String,
    val description: String,
    val position: String? = null
)

/**
 * Description types.
 */
enum class DescriptionType {
    IMAGE, CHART, MATH, DIAGRAM, VIDEO_SCENE
}

/**
 * Audio description data.
 */
data class AudioDescription(
    val type: DescriptionType,
    val shortDescription: String,
    val fullDescription: String,
    val language: String
)
