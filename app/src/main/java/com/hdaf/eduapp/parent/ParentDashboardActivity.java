package com.hdaf.eduapp.parent;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hdaf.eduapp.R;
import com.hdaf.eduapp.analytics.SubjectPerformance;
import com.hdaf.eduapp.gamification.GamificationManager;
import com.hdaf.eduapp.gamification.UserProgress;
import com.hdaf.eduapp.ui.SimpleBarChartView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parent Zone Dashboard.
 * Displays child's progress, subject performance, and insights.
 */
public class ParentDashboardActivity extends AppCompatActivity {

    private TextView txtXP, txtLevel, txtStreak, txtInsights;
    private SimpleBarChartView chartView;
    private com.hdaf.eduapp.analytics.AnalyticsManager analyticsManager;
    private GamificationManager gamificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        analyticsManager = com.hdaf.eduapp.analytics.AnalyticsManager.getInstance(this);
        gamificationManager = GamificationManager.getInstance(this);

        initViews();
        loadData();
    }

    private void initViews() {
        txtXP = findViewById(R.id.txt_stat_xp);
        txtLevel = findViewById(R.id.txt_stat_level);
        txtStreak = findViewById(R.id.txt_stat_streak);
        txtInsights = findViewById(R.id.txt_insights);
        chartView = findViewById(R.id.chart_view);
    }

    private void loadData() {
        // 1. Load Summary Stats from Gamification
        UserProgress progress = gamificationManager.getUserProgress();
        if (progress != null) {
            txtXP.setText(String.valueOf(progress.getTotalXP()));
            txtLevel.setText(String.valueOf(progress.getCurrentLevel()));
            txtStreak.setText(String.valueOf(progress.getCurrentStreak()));
        }

        // 2. Load Subject Performance for Chart
        Map<String, SubjectPerformance> performanceMap = analyticsManager.getAllSubjectPerformance();
        List<SimpleBarChartView.BarData> chartData = new ArrayList<>();
        StringBuilder insights = new StringBuilder();

        for (SubjectPerformance sp : performanceMap.values()) {
            float accuracy = sp.getAccuracy();
            // Color based on accuracy: Green > 80, Yellow > 50, Red < 50
            int color = 0xFF4CAF50; // Green
            if (accuracy < 50) color = 0xFFF44336; // Red
            else if (accuracy < 80) color = 0xFFFFC107; // Amber

            chartData.add(new SimpleBarChartView.BarData(sp.getSubjectName(), accuracy, color));
            
            // Insight generation
            if (accuracy < 50) {
                 insights.append("• ").append(sp.getSubjectName()).append(" needs attention (Avg: ").append((int)accuracy).append("%)\n");
            } else if (accuracy > 90) {
                 insights.append("• doing excellent in ").append(sp.getSubjectName()).append("!\n");
            }
        }

        if (chartData.isEmpty()) {
            insights.append("No quiz data recorded yet. Encourage your child to take some quizzes!");
        }

        chartView.setData(chartData);
        txtInsights.setText(insights.toString());
    }
}
