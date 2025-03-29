package com.ordering.blinkit_clone.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.ordering.blinkit_clone.R;

public class CheckMarkView extends View {
    private Paint circlePaint;
    private Paint checkMarkPaint;
    private Path checkMarkPath;
    private float progress = 0;

    public CheckMarkView(Context context) {
        super(context);
        init();
    }

    public CheckMarkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        circlePaint = new Paint();
        circlePaint.setColor(getResources().getColor(R.color.blinkit_green_color)); // Green color
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(10);
        circlePaint.setAntiAlias(true);

        checkMarkPaint = new Paint();
        checkMarkPaint.setColor(getResources().getColor(R.color.white)); // White color
        checkMarkPaint.setStyle(Paint.Style.STROKE);
        checkMarkPaint.setStrokeWidth(10);
        checkMarkPaint.setAntiAlias(true);

        checkMarkPath = new Path();
        checkMarkPath.moveTo(50, 100);
        checkMarkPath.lineTo(100, 150);
        checkMarkPath.lineTo(200, 50);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2 - 20;

        // Draw the circle
        canvas.drawCircle(width / 2, height / 2, radius, circlePaint);

        // Draw the check mark
        canvas.save();
        canvas.translate((width - 250) / 2, (height - 200) / 2);
        canvas.drawPath(checkMarkPath, checkMarkPaint);
        canvas.restore();
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }
}
