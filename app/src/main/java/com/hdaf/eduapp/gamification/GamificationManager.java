package com.hdaf.eduapp.gamification;

import android.content.Context;

import com.hdaf.eduapp.quiz.QuizResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Main gamification service managing XP, badges, streaks, and challenges.
 * Singleton pattern for app-wide access.
 */
public class GamificationManager {

    private static GamificationManager instance;
    private final Context context;
    private final GamificationStorage storage;
    private final BadgeRegistry badgeRegistry;

    private GamificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.storage = GamificationStorage.getInstance(context);
        this.badgeRegistry = BadgeRegistry.getInstance();
    }

    public static synchronized GamificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new GamificationManager(context);
        }
        return instance;
    }

    /**
     * Award XP to the user and check for new badges.
     */
    public AwardResult awardXP(int xp, String reason) {
        UserProgress progress = storage.loadUserProgress();
        int previousLevel = progress.getCurrentLevel();

        boolean leveledUp = progress.addXP(xp);
        progress.updateLastActiveDate();

        storage.saveUserProgress(progress);

        List<Badge> newBadges = checkAndAwardBadges();

        AwardResult.Builder resultBuilder = new AwardResult.Builder()
                .xpAwarded(xp)
                .reason(reason)
                .newTotalXP(progress.getTotalXP())
                .newLevel(progress.getCurrentLevel())
                .leveledUp(leveledUp)
                .previousLevel(previousLevel);

        for (Badge badge : newBadges) {
            resultBuilder.addBadge(badge);
        }

        AwardResult result = resultBuilder.build();

        if (leveledUp) {
            result.setMessage(LevelSystem.getLevelUpMessage(progress.getCurrentLevel()));
        }

        return result;
    }

    /**
     * Check all badge conditions and award any newly earned badges.
     */
    public List<Badge> checkAndAwardBadges() {
        List<Badge> newBadges = new ArrayList<>();
        UserProgress progress = storage.loadUserProgress();

        if (!progress.hasBadge(BadgeRegistry.BADGE_FIRST_QUIZ) && progress.getQuizzesCompleted() >= 1) {
            if (awardBadge(progress, BadgeRegistry.BADGE_FIRST_QUIZ)) {
                newBadges.add(badgeRegistry.getBadge(BadgeRegistry.BADGE_FIRST_QUIZ));
            }
        }

        if (!progress.hasBadge(BadgeRegistry.BADGE_QUIZ_MASTER) && progress.getQuizzesCompleted() >= 10) {
            if (awardBadge(progress, BadgeRegistry.BADGE_QUIZ_MASTER)) {
                newBadges.add(badgeRegistry.getBadge(BadgeRegistry.BADGE_QUIZ_MASTER));
            }
        }

        if (!progress.hasBadge(BadgeRegistry.BADGE_PERFECT_SCORE) && progress.getPerfectScores() >= 1) {
            if (awardBadge(progress, BadgeRegistry.BADGE_PERFECT_SCORE)) {
                newBadges.add(badgeRegistry.getBadge(BadgeRegistry.BADGE_PERFECT_SCORE));
            }
        }

        int streak = progress.getCurrentStreak();

        if (!progress.hasBadge(BadgeRegistry.BADGE_STREAK_3) && streak >= 3) {
            if (awardBadge(progress, BadgeRegistry.BADGE_STREAK_3)) {
                newBadges.add(badgeRegistry.getBadge(BadgeRegistry.BADGE_STREAK_3));
            }
        }

        if (!progress.hasBadge(BadgeRegistry.BADGE_STREAK_7) && streak >= 7) {
            if (awardBadge(progress, BadgeRegistry.BADGE_STREAK_7)) {
                newBadges.add(badgeRegistry.getBadge(BadgeRegistry.BADGE_STREAK_7));
            }
        }

        if (!progress.hasBadge(BadgeRegistry.BADGE_STREAK_30) && streak >= 30) {
            if (awardBadge(progress, BadgeRegistry.BADGE_STREAK_30)) {
                newBadges.add(badgeRegistry.getBadge(BadgeRegistry.BADGE_STREAK_30));
            }
        }

        if (!progress.hasBadge(BadgeRegistry.BADGE_MATH_EXPERT) && progress.getMathQuizzesCompleted() >= 5) {
            if (awardBadge(progress, BadgeRegistry.BADGE_MATH_EXPERT)) {
                newBadges.add(badgeRegistry.getBadge(BadgeRegistry.BADGE_MATH_EXPERT));
            }
        }

        if (!progress.hasBadge(BadgeRegistry.BADGE_SCIENCE_EXPERT) && progress.getScienceQuizzesCompleted() >= 5) {
            if (awardBadge(progress, BadgeRegistry.BADGE_SCIENCE_EXPERT)) {
                newBadges.add(badgeRegistry.getBadge(BadgeRegistry.BADGE_SCIENCE_EXPERT));
            }
        }

        for (Badge badge : newBadges) {
            progress.addXP(badge.getXpReward());
        }

        if (!newBadges.isEmpty()) {
            storage.saveUserProgress(progress);
        }

        return newBadges;
    }

    private boolean awardBadge(UserProgress progress, String badgeId) {
        return progress.addBadge(badgeId);
    }

    /**
     * Update the daily streak.
     */
    public void updateStreak() {
        UserProgress progress = storage.loadUserProgress();
        String today = UserProgress.getTodayDateString();
        String lastActive = progress.getLastActiveDate();

        if (lastActive == null) {
            progress.setCurrentStreak(1);
            progress.setLongestStreak(1);
        } else if (lastActive.equals(today)) {
            return;
        } else if (isYesterday(lastActive)) {
            int newStreak = progress.getCurrentStreak() + 1;
            progress.setCurrentStreak(newStreak);
            if (newStreak > progress.getLongestStreak()) {
                progress.setLongestStreak(newStreak);
            }
        } else {
            progress.setCurrentStreak(1);
        }

        progress.updateLastActiveDate();
        storage.saveUserProgress(progress);

        checkAndAwardBadges();
    }

    private boolean isYesterday(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date lastDate = sdf.parse(dateString);

            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);
            resetTime(yesterday);

            Calendar lastCal = Calendar.getInstance();
            lastCal.setTime(lastDate);
            resetTime(lastCal);

            return lastCal.equals(yesterday);
        } catch (ParseException e) {
            return false;
        }
    }

    private void resetTime(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    public UserProgress getUserProgress() {
        return storage.loadUserProgress();
    }

    /**
     * Record quiz completion and award XP.
     */
    public AwardResult onQuizCompleted(QuizResult result) {
        UserProgress progress = storage.loadUserProgress();

        progress.incrementQuizzesCompleted();

        if (result.getQuiz() != null && result.getQuiz().getSubject() != null) {
            progress.incrementSubjectQuizCount(result.getQuiz().getSubject());
        }

        boolean isPerfect = result.getScore() == 100;
        if (isPerfect) {
            progress.incrementPerfectScores();
        }

        storage.saveUserProgress(progress);

        int xp = LevelSystem.calculateQuizXP(
                result.getScore(),
                result.getTotalQuestions(),
                isPerfect
        );

        return awardXP(xp, isPerfect ? "perfect quiz score" : "quiz completion");
    }

    /**
     * Record lesson completion and award XP.
     */
    public AwardResult onLessonCompleted(String subject, String chapter) {
        UserProgress progress = storage.loadUserProgress();
        progress.setLessonsCompleted(progress.getLessonsCompleted() + 1);
        storage.saveUserProgress(progress);

        return awardXP(20, "lesson completion");
    }

    /**
     * Get or generate today's daily challenge.
     */
    public DailyChallenge getDailyChallenge() {
        DailyChallenge challenge = storage.loadDailyChallenge();

        if (challenge == null) {
            challenge = generateDailyChallenge();
            storage.saveDailyChallenge(challenge);
        }

        return challenge;
    }

    private DailyChallenge generateDailyChallenge() {
        Random random = new Random();
        int type = random.nextInt(4);

        switch (type) {
            case 0: return DailyChallenge.Templates.quizChallenge();
            case 1: return DailyChallenge.Templates.perfectScoreChallenge();
            case 2: return DailyChallenge.Templates.mathChallenge();
            case 3: return DailyChallenge.Templates.scienceChallenge();
            default: return DailyChallenge.Templates.streakChallenge();
        }
    }

    /**
     * Complete daily challenge and award XP.
     */
    public AwardResult completeDailyChallenge() {
        DailyChallenge challenge = getDailyChallenge();
        if (challenge != null && !challenge.isCompleted()) {
            challenge.complete();
            storage.saveDailyChallenge(challenge);
            return awardXP(challenge.getXpReward(), "daily challenge");
        }
        return new AwardResult.Builder().build();
    }

    public boolean isDailyChallengeCompleted() {
        DailyChallenge challenge = storage.loadDailyChallenge();
        return challenge != null && challenge.isCompleted();
    }

    public List<BadgeStatus> getAllBadgesWithStatus() {
        List<BadgeStatus> result = new ArrayList<>();
        UserProgress progress = getUserProgress();

        for (Badge badge : badgeRegistry.getAllBadges()) {
            boolean earned = progress.hasBadge(badge.getId());
            result.add(new BadgeStatus(badge, earned));
        }

        return result;
    }

    public static class BadgeStatus {
        public final Badge badge;
        public final boolean earned;

        public BadgeStatus(Badge badge, boolean earned) {
            this.badge = badge;
            this.earned = earned;
        }
    }
}
