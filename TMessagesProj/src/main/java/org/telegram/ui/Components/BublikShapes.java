package org.telegram.ui.Components;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;

/** Bublik Pro: the 9-wave "cookie" avatar shape from Android 17's Pixel apps. */
public final class BublikShapes {
    private static final int WAVES = 9;
    private static final float DEPTH = 0.07f;
    private static final int STEPS = 144;

    // Built once in a 1x1 box and scaled per call: avatar sizes change every frame while animating,
    // so caching a path per size would grow without bound.
    private static final Path UNIT = build(1f, 1f);
    private static final ThreadLocal<Path> work = new ThreadLocal<Path>() {
        @Override
        protected Path initialValue() {
            return new Path();
        }
    };
    private static final ThreadLocal<Matrix> matrix = new ThreadLocal<Matrix>() {
        @Override
        protected Matrix initialValue() {
            return new Matrix();
        }
    };

    private BublikShapes() {
    }

    /** Cookie outline filling {@code rect}. The returned path is reused per thread; don't keep it. */
    public static Path cookie(RectF rect) {
        final Matrix m = matrix.get();
        m.setScale(rect.width(), rect.height());
        m.postTranslate(rect.left, rect.top);
        final Path path = work.get();
        UNIT.transform(m, path);
        return path;
    }

    private static Path build(float w, float h) {
        final Path path = new Path();
        final float cx = w / 2f, cy = h / 2f;
        final float rx = w / 2f, ry = h / 2f;
        for (int i = 0; i <= STEPS; i++) {
            final double t = 2 * Math.PI * i / STEPS;
            final double k = (1 - DEPTH) + DEPTH * Math.cos(WAVES * t);
            final float x = (float) (cx + rx * k * Math.sin(t));
            final float y = (float) (cy - ry * k * Math.cos(t));
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
        return path;
    }
}
