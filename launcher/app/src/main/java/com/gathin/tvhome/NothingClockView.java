package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.Calendar;

/**
 * Custom Nothing OS style analog clock with dot ticks, sleek hands, and red center dot.
 */
public class NothingClockView extends View {

    private final Paint dialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint minutePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint redDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final float density;
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            invalidate();
            postDelayed(this, 1000L);
        }
    };

    public NothingClockView(Context context) {
        this(context, null);
    }

    public NothingClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;

        dialPaint.setStyle(Paint.Style.STROKE);
        dialPaint.setStrokeWidth(1.5f * density);
        dialPaint.setColor(Color.parseColor("#33FFFFFF"));

        tickPaint.setStyle(Paint.Style.FILL);
        tickPaint.setColor(Color.parseColor("#80FFFFFF"));

        hourPaint.setStyle(Paint.Style.STROKE);
        hourPaint.setStrokeCap(Paint.Cap.ROUND);
        hourPaint.setStrokeWidth(3.5f * density);
        hourPaint.setColor(Color.WHITE);

        minutePaint.setStyle(Paint.Style.STROKE);
        minutePaint.setStrokeCap(Paint.Cap.ROUND);
        minutePaint.setStrokeWidth(2.0f * density);
        minutePaint.setColor(Color.WHITE);

        redDotPaint.setStyle(Paint.Style.FILL);
        redDotPaint.setColor(Color.parseColor("#D71920"));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(ticker);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(ticker);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;
        float radius = Math.min(cx, cy) - (4f * density);

        // Draw dial circle
        canvas.drawCircle(cx, cy, radius, dialPaint);

        // Draw 12 dot ticks
        float tickRadius = 1.6f * density;
        float tickDist = radius - (6f * density);
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30.0);
            float tx = (float) (cx + Math.sin(angle) * tickDist);
            float ty = (float) (cy - Math.cos(angle) * tickDist);
            canvas.drawCircle(tx, ty, tickRadius, tickPaint);
        }

        // Get time
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);

        float hourAngle = (hour + minute / 60f) * 30f;
        float minuteAngle = (minute + second / 60f) * 6f;

        // Draw hour hand
        float hourLen = radius * 0.48f;
        double hrRad = Math.toRadians(hourAngle);
        canvas.drawLine(cx, cy, (float) (cx + Math.sin(hrRad) * hourLen), (float) (cy - Math.cos(hrRad) * hourLen), hourPaint);

        // Draw minute hand
        float minLen = radius * 0.72f;
        double minRad = Math.toRadians(minuteAngle);
        canvas.drawLine(cx, cy, (float) (cx + Math.sin(minRad) * minLen), (float) (cy - Math.cos(minRad) * minLen), minutePaint);

        // Draw Nothing signature red center dot
        canvas.drawCircle(cx, cy, 3.8f * density, redDotPaint);
    }
}
