package com.hdaf.eduapp.analytics;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AnalyticsManagerTest {

    private Context appContext;
    private AnalyticsManager manager;
    private AnalyticsStorage storage;

    @Before
    public void setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        storage = AnalyticsStorage.getInstance(appContext);
        storage.clearAllData();

        manager = AnalyticsManager.getInstance(appContext);
    }

    @Test
    public void testStartAndEndSession() {
        manager.startSession(LearningSession.ActivityType.QUIZ, "Math");

        manager.endSession();

        List<LearningSession> logs = storage.loadSessionLogs();
        assertEquals(0, logs.size());
    }

    @Test
    public void testLogQuizResult() {
        manager.logQuizResult("Science", 85,
                java.util.Arrays.asList("Photosynthesis", "Cells"));

        List<LearningSession> logs = storage.loadSessionLogs();
        assertEquals(1, logs.size());

        LearningSession session = logs.get(0);
        assertEquals("Science", session.getSubject());
        assertEquals(LearningSession.ActivityType.QUIZ, session.getActivityType());
        assertEquals("85", session.getMetadata().get("score"));
        assertTrue(session.getMetadata().get("weak_topics")
                .contains("Photosynthesis"));
    }

    @Test
    public void testParentPinOperations() {
        assertFalse(storage.isPinSet());

        storage.saveParentPin("1234");

        assertTrue(storage.isPinSet());
        assertTrue(storage.verifyPin("1234"));
        assertFalse(storage.verifyPin("0000"));
    }
}
