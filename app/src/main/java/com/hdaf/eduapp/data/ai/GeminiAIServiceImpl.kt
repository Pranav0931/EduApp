package com.hdaf.eduapp.data.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.generationConfig
import com.hdaf.eduapp.BuildConfig
import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.ai.AnswerResult
import com.hdaf.eduapp.domain.ai.EduAIService
import com.hdaf.eduapp.domain.ai.ExplanationResult
import com.hdaf.eduapp.domain.ai.IntentParseResult
import com.hdaf.eduapp.domain.ai.LearningRecommendation
import com.hdaf.eduapp.domain.ai.PracticeProblem
import com.hdaf.eduapp.domain.ai.RecommendationType
import com.hdaf.eduapp.domain.ai.UserIntent
import com.hdaf.eduapp.domain.model.Quiz
import com.hdaf.eduapp.domain.model.QuizDifficulty
import com.hdaf.eduapp.domain.model.QuizQuestion
import com.hdaf.eduapp.domain.model.Subject
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of EduAIService using Google Gemini AI.
 * Provides AI-powered educational features.
 */
@Singleton
class GeminiAIServiceImpl @Inject constructor() : EduAIService {

    private val gson = Gson()
    
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 8192
            }
        )
    }
    
    private val strictModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.3f
                topK = 20
                topP = 0.8f
                maxOutputTokens = 4096
            }
        )
    }

    override suspend fun generateQuiz(
        chapterContent: String,
        subject: Subject,
        numberOfQuestions: Int,
        difficulty: QuizDifficulty
    ): Resource<Quiz> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildQuizPrompt(chapterContent, subject, numberOfQuestions, difficulty)
            val response = strictModel.generateContent(prompt)
            val quiz = parseQuizResponse(response, subject, difficulty)
            Resource.Success(quiz)
        } catch (e: Exception) {
            Timber.e(e, "Error generating quiz")
            Resource.Error("Failed to generate quiz: ${e.message}")
        }
    }

    override suspend fun generateAdaptiveQuiz(
        subject: Subject,
        weakTopics: List<String>,
        numberOfQuestions: Int
    ): Resource<Quiz> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                आप एक शिक्षा विशेषज्ञ हैं। छात्र के कमजोर विषयों के आधार पर एक अभ्यास प्रश्नोत्तरी बनाएं।
                
                विषय: ${subject.displayName}
                कमजोर विषय: ${weakTopics.joinToString(", ")}
                प्रश्नों की संख्या: $numberOfQuestions
                
                JSON प्रारूप में उत्तर दें:
                {
                    "title": "अभ्यास प्रश्नोत्तरी",
                    "questions": [
                        {
                            "question": "प्रश्न पाठ",
                            "options": ["विकल्प 1", "विकल्प 2", "विकल्प 3", "विकल्प 4"],
                            "correctIndex": 0,
                            "explanation": "स्पष्टीकरण",
                            "topic": "विषय नाम"
                        }
                    ]
                }
            """.trimIndent()
            
            val response = strictModel.generateContent(prompt)
            val quiz = parseQuizResponse(response, subject, QuizDifficulty.MEDIUM)
            Resource.Success(quiz)
        } catch (e: Exception) {
            Timber.e(e, "Error generating adaptive quiz")
            Resource.Error("Failed to generate adaptive quiz: ${e.message}")
        }
    }

    override suspend fun explainConcept(
        concept: String,
        subject: Subject,
        classLevel: Int
    ): Resource<ExplanationResult> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                आप एक मित्रवत शिक्षक हैं जो कक्षा $classLevel के छात्रों को पढ़ाते हैं।
                
                कृपया निम्नलिखित अवधारणा को सरल हिंदी में समझाएं:
                
                विषय: ${subject.displayName}
                अवधारणा: $concept
                
                JSON प्रारूप में उत्तर दें:
                {
                    "explanation": "सरल व्याख्या",
                    "examples": ["उदाहरण 1", "उदाहरण 2"],
                    "relatedTopics": ["संबंधित विषय 1", "संबंधित विषय 2"],
                    "funFact": "एक रोचक तथ्य"
                }
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            val explanation = parseExplanationResponse(response)
            Resource.Success(explanation)
        } catch (e: Exception) {
            Timber.e(e, "Error explaining concept")
            Resource.Error("Failed to explain concept: ${e.message}")
        }
    }

    override suspend fun answerQuestion(
        question: String,
        context: String?,
        subject: Subject?
    ): Resource<AnswerResult> = withContext(Dispatchers.IO) {
        try {
            val contextPart = context?.let { "संदर्भ: $it\n\n" } ?: ""
            val subjectPart = subject?.let { "विषय: ${it.displayName}\n" } ?: ""
            
            val prompt = """
                आप एक सहायक शिक्षक हैं। छात्र का प्रश्न का उत्तर दें।
                
                $subjectPart$contextPart
                छात्र का प्रश्न: $question
                
                JSON प्रारूप में उत्तर दें:
                {
                    "answer": "विस्तृत उत्तर",
                    "confidence": 0.95,
                    "followUpQuestions": ["आगे का प्रश्न 1", "आगे का प्रश्न 2"]
                }
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            val answer = parseAnswerResponse(response)
            Resource.Success(answer)
        } catch (e: Exception) {
            Timber.e(e, "Error answering question")
            Resource.Error("Failed to answer question: ${e.message}")
        }
    }

    override suspend fun summarizeChapter(
        content: String,
        maxLength: Int
    ): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                निम्नलिखित अध्याय सामग्री का सारांश हिंदी में लिखें। 
                सारांश $maxLength शब्दों से कम होना चाहिए।
                मुख्य बिंदुओं को बुलेट पॉइंट में लिखें।
                
                अध्याय सामग्री:
                $content
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            val summary = response.text ?: "सारांश उपलब्ध नहीं है"
            Resource.Success(summary)
        } catch (e: Exception) {
            Timber.e(e, "Error summarizing chapter")
            Resource.Error("Failed to summarize: ${e.message}")
        }
    }

    override suspend fun generatePracticeProblems(
        topic: String,
        subject: Subject,
        difficulty: QuizDifficulty,
        count: Int
    ): Resource<List<PracticeProblem>> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                ${subject.displayName} विषय में "$topic" पर $count अभ्यास प्रश्न बनाएं।
                कठिनाई स्तर: ${difficulty.displayName}
                
                JSON प्रारूप में उत्तर दें:
                {
                    "problems": [
                        {
                            "question": "प्रश्न",
                            "hints": ["संकेत 1", "संकेत 2"],
                            "solution": "समाधान",
                            "explanation": "व्याख्या"
                        }
                    ]
                }
            """.trimIndent()
            
            val response = strictModel.generateContent(prompt)
            val problems = parsePracticeProblemsResponse(response)
            Resource.Success(problems)
        } catch (e: Exception) {
            Timber.e(e, "Error generating practice problems")
            Resource.Error("Failed to generate problems: ${e.message}")
        }
    }

    override suspend fun getRecommendations(
        userId: String,
        weakTopics: Map<Subject, List<String>>,
        completedChapters: List<String>
    ): Resource<List<LearningRecommendation>> = withContext(Dispatchers.IO) {
        try {
            // Generate recommendations based on weak topics
            val recommendations = mutableListOf<LearningRecommendation>()
            
            weakTopics.forEach { (subject, topics) ->
                topics.take(2).forEachIndexed { index, topic ->
                    recommendations.add(
                        LearningRecommendation(
                            type = if (index % 2 == 0) RecommendationType.TOPIC_TO_REVIEW 
                                   else RecommendationType.PRACTICE_PROBLEMS,
                            title = "$topic का अभ्यास करें",
                            description = "${subject.displayName} में $topic को और समझने की जरूरत है",
                            priority = index
                        )
                    )
                }
            }
            
            Resource.Success(recommendations)
        } catch (e: Exception) {
            Timber.e(e, "Error getting recommendations")
            Resource.Error("Failed to get recommendations: ${e.message}")
        }
    }

    override suspend fun translateContent(
        content: String,
        targetLanguage: String
    ): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Translate the following content to $targetLanguage.
                Keep the meaning and tone appropriate for students.
                
                Content:
                $content
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            Resource.Success(response.text ?: content)
        } catch (e: Exception) {
            Timber.e(e, "Error translating content")
            Resource.Error("Translation failed: ${e.message}")
        }
    }

    override suspend fun parseIntent(
        userInput: String
    ): Resource<IntentParseResult> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Parse the user's voice command and determine their intent.
                
                User input: "$userInput"
                
                Possible intents: READ_CHAPTER, TAKE_QUIZ, ASK_QUESTION, EXPLAIN_CONCEPT, 
                GO_HOME, GO_BACK, REPEAT, PAUSE, RESUME, NEXT, PREVIOUS, SELECT_ITEM,
                OPEN_SETTINGS, OPEN_PROFILE, UNKNOWN
                
                Respond in JSON:
                {
                    "intent": "INTENT_NAME",
                    "entities": {"key": "value"},
                    "confidence": 0.95
                }
            """.trimIndent()
            
            val response = strictModel.generateContent(prompt)
            val result = parseIntentResponse(response)
            Resource.Success(result)
        } catch (e: Exception) {
            Timber.e(e, "Error parsing intent")
            Resource.Success(IntentParseResult(UserIntent.UNKNOWN, emptyMap(), 0f))
        }
    }

    // ==================== Helper Functions ====================

    private fun buildQuizPrompt(
        content: String,
        subject: Subject,
        numberOfQuestions: Int,
        difficulty: QuizDifficulty
    ): String {
        return """
            आप एक शिक्षा विशेषज्ञ हैं। निम्नलिखित पाठ्य सामग्री के आधार पर एक प्रश्नोत्तरी बनाएं।
            
            विषय: ${subject.displayName}
            कठिनाई: ${difficulty.displayName}
            प्रश्नों की संख्या: $numberOfQuestions
            
            पाठ्य सामग्री:
            $content
            
            JSON प्रारूप में उत्तर दें:
            {
                "title": "प्रश्नोत्तरी शीर्षक",
                "questions": [
                    {
                        "question": "प्रश्न पाठ",
                        "options": ["विकल्प 1", "विकल्प 2", "विकल्प 3", "विकल्प 4"],
                        "correctIndex": 0,
                        "explanation": "सही उत्तर का स्पष्टीकरण",
                        "topic": "विषय"
                    }
                ]
            }
            
            महत्वपूर्ण:
            - प्रत्येक प्रश्न के 4 विकल्प होने चाहिए
            - correctIndex 0-3 के बीच होना चाहिए
            - प्रश्न पाठ्य सामग्री से संबंधित होने चाहिए
        """.trimIndent()
    }

    private fun parseQuizResponse(
        response: GenerateContentResponse,
        subject: Subject,
        difficulty: QuizDifficulty
    ): Quiz {
        val text = response.text ?: throw Exception("Empty response")
        val jsonText = extractJson(text)
        val json = JsonParser.parseString(jsonText).asJsonObject
        
        val title = json.get("title")?.asString ?: "Quiz"
        val questionsArray = json.getAsJsonArray("questions")
        
        val questions = questionsArray.mapIndexed { index, element ->
            val q = element.asJsonObject
            QuizQuestion(
                id = UUID.randomUUID().toString(),
                quizId = "",
                questionText = q.get("question").asString,
                options = q.getAsJsonArray("options").map { it.asString },
                correctAnswerIndex = q.get("correctIndex").asInt,
                explanation = q.get("explanation")?.asString,
                topic = q.get("topic")?.asString,
                difficulty = difficulty,
                orderIndex = index
            )
        }
        
        return Quiz(
            id = UUID.randomUUID().toString(),
            title = title,
            subject = subject,
            difficulty = difficulty,
            totalQuestions = questions.size,
            timeLimitMinutes = questions.size * 1, // 1 minute per question
            isAiGenerated = true,
            questions = questions
        )
    }

    private fun parseExplanationResponse(response: GenerateContentResponse): ExplanationResult {
        val text = response.text ?: throw Exception("Empty response")
        return try {
            val jsonText = extractJson(text)
            val json = JsonParser.parseString(jsonText).asJsonObject
            ExplanationResult(
                explanation = json.get("explanation")?.asString ?: text,
                examples = json.getAsJsonArray("examples")?.map { it.asString } ?: emptyList(),
                relatedTopics = json.getAsJsonArray("relatedTopics")?.map { it.asString } ?: emptyList(),
                funFact = json.get("funFact")?.asString
            )
        } catch (e: Exception) {
            ExplanationResult(explanation = text)
        }
    }

    private fun parseAnswerResponse(response: GenerateContentResponse): AnswerResult {
        val text = response.text ?: throw Exception("Empty response")
        return try {
            val jsonText = extractJson(text)
            val json = JsonParser.parseString(jsonText).asJsonObject
            AnswerResult(
                answer = json.get("answer")?.asString ?: text,
                confidence = json.get("confidence")?.asFloat ?: 0.8f,
                followUpQuestions = json.getAsJsonArray("followUpQuestions")?.map { it.asString } ?: emptyList()
            )
        } catch (e: Exception) {
            AnswerResult(answer = text, confidence = 0.7f)
        }
    }

    private fun parsePracticeProblemsResponse(response: GenerateContentResponse): List<PracticeProblem> {
        val text = response.text ?: throw Exception("Empty response")
        val jsonText = extractJson(text)
        val json = JsonParser.parseString(jsonText).asJsonObject
        val problemsArray = json.getAsJsonArray("problems")
        
        return problemsArray.map { element ->
            val p = element.asJsonObject
            PracticeProblem(
                question = p.get("question").asString,
                hints = p.getAsJsonArray("hints")?.map { it.asString } ?: emptyList(),
                solution = p.get("solution").asString,
                explanation = p.get("explanation").asString
            )
        }
    }

    private fun parseIntentResponse(response: GenerateContentResponse): IntentParseResult {
        val text = response.text ?: throw Exception("Empty response")
        val jsonText = extractJson(text)
        val json = JsonParser.parseString(jsonText).asJsonObject
        
        val intentStr = json.get("intent")?.asString ?: "UNKNOWN"
        val intent = try {
            UserIntent.valueOf(intentStr)
        } catch (e: Exception) {
            UserIntent.UNKNOWN
        }
        
        val entities = json.getAsJsonObject("entities")?.entrySet()?.associate { 
            it.key to it.value.asString 
        } ?: emptyMap()
        
        val confidence = json.get("confidence")?.asFloat ?: 0f
        
        return IntentParseResult(intent, entities, confidence)
    }

    private fun extractJson(text: String): String {
        // Find JSON in the response (may be wrapped in markdown code blocks)
        val jsonPattern = Regex("""```json\s*([\s\S]*?)\s*```|(\{[\s\S]*\})""")
        val match = jsonPattern.find(text)
        return match?.groupValues?.find { it.startsWith("{") } ?: text.trim()
    }
}
