package com.hdaf.eduapp.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A basic custom view to draw bar charts for analytics.
 */
public class SimpleBarChartView extends View {

    private List<BarData> dataList = new ArrayList<>();
    private Paint barPaint;
    private Paint textPaint;
    private int maxVal = 100;
    private int barColor = Color.BLUE;

    public SimpleBarChartView(Context context) {
        super(context);
        init();
    }

    public SimpleBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint = new Paint();
        barPaint.setColor(barColor);
        barPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(30f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<BarData> data) {
        this.dataList = data;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (dataList == null || dataList.isEmpty()) return;

        int width = getWidth();
        int height = getHeight();
        int padding = 40;
        int barWidth = (width - (2 * padding)) / dataList.size();
        
        // Find max value to normalize height
        float apiMax = 0;
        for (BarData d : dataList) {
            if (d.value > apiMax) apiMax = d.value;
        }
        if (apiMax == 0) apiMax = 100; // prevent div by zero

        int availableHeight = height - 100; // reserve space for text

        for (int i = 0; i < dataList.size(); i++) {
            BarData data = dataList.get(i);
            float barHeight = (data.value / apiMax) * availableHeight;
            
            float left = padding + (i * barWidth) + 10;
            float right = left + barWidth - 20;
            float top = availableHeight - barHeight + 50;
            float bottom = availableHeight + 50;

            // Draw bar
            if (data.color != 0) barPaint.setColor(data.color);
            else barPaint.setColor(barColor);
            
            canvas.drawRect(left, top, right, bottom, barPaint);

            // Draw label
            canvas.drawText(data.label, (left + right) / 2, bottom + 40, textPaint);
            
            // Draw value
            canvas.drawText(String.valueOf((int)data.value), (left + right) / 2, top - 10, textPaint);
        }
    }

    public static class BarData {
        public String label;
        public float value;
        public int color;

        public BarData(String label, float value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }
}
