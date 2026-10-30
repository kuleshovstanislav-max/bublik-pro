package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;

/** Draws the legacy-style round-video recording progress. */
public final class RoundVideoProgressView extends FrameLayout {
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
        @Override
        public void setAlpha(int alpha) {
            super.setAlpha(alpha);
            backgroundPaint.setAlpha(Math.round(alpha * 0.20f));
            budgetPaint.setAlpha(alpha);
            invalidate();
        }
    };
    // Bublik Pro: thin accent ring outside the time ring: how much of the Auto size budget is used.
    private final Paint budgetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float budgetProgress = -1f;
    private final RectF arcBounds = new RectF();
    private final float strokeWidth;
    private final float ringInset;

    private float progress;

    /** Creates a round-video progress view matching the legacy camera ring. */
    public RoundVideoProgressView(Context context) {
        super(context);
        strokeWidth = AndroidUtilities.dp(3);
        ringInset = AndroidUtilities.dp(5) + strokeWidth * 0.5f;
        configureStroke(backgroundPaint, 0.20f);
        configureStroke(progressPaint, 1f);
        configureStroke(budgetPaint, 1f);
        budgetPaint.setStrokeWidth(AndroidUtilities.dp(2));
        budgetPaint.setColor(0xFF2E9BF0);
        setWillNotDraw(false);
    }

    /** Returns the paint controlling the whole ring opacity. */
    public Paint getPaint() {
        return progressPaint;
    }

    /** Sets normalized recorded progress. */
    public void setProgress(float progress) {
        float value = clamp(progress, 0f, 1f);
        if (this.progress == value) return;
        this.progress = value;
        invalidate();
    }

    /** Bublik Pro: share of the Auto bitrate size budget written so far, -1 hides the ring. */
    public void setBudgetProgress(float progress) {
        float value = progress < 0 ? -1f : clamp(progress, 0f, 1f);
        if (budgetProgress == value) return;
        budgetProgress = value;
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        arcBounds.set(
                ringInset,
                ringInset,
                getWidth() - ringInset,
                getHeight() - ringInset
        );
        canvas.drawOval(arcBounds, backgroundPaint);
        canvas.drawArc(arcBounds, -90f, progress * 360f, false, progressPaint);
        if (budgetProgress > 0) {
            final float inset = AndroidUtilities.dp(1) + budgetPaint.getStrokeWidth() * 0.5f;
            arcBounds.set(inset, inset, getWidth() - inset, getHeight() - inset);
            canvas.drawArc(arcBounds, -90f, budgetProgress * 360f, false, budgetPaint);
        }
    }

    private static void configureStroke(Paint paint, float alpha) {
        paint.setColor(Color.WHITE);
        paint.setAlpha(Math.round(255f * alpha));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(AndroidUtilities.dp(3));
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
