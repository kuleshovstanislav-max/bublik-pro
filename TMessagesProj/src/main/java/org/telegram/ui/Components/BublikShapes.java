package org.telegram.ui.Components;

import android.graphics.Path;
import android.graphics.RectF;

import java.util.concurrent.ConcurrentHashMap;

/** Bublik Pro: the 9-wave "cookie" avatar shape from Android 17's Pixel apps. */
public final class BublikShapes {
    private static final int WAVES = 9;
    private static final float DEPTH = 0.07f;
    private static final int STEPS = 144;

    private static final ConcurrentHashMap<Long, Path> cache = new ConcurrentHashMap<>();
    private static final ThreadLocal<Path> work = new ThreadLocal<Path>() {
        @Override
        protected Path initialValue() {
            return new Path();
        }
    };

    private BublikShapes() {
    }

    /** Cookie outline filling {@code rect}. The returned path is reused per thread; don't keep it. */
    public static Path cookie(RectF rect) {
        final int w = Math.round(rect.width());
        final int h = Math.round(rect.height());
        final long key = ((long) w << 32) | (h & 0xffffffffL);
        Path base = cache.get(key);
        if (base == null) {
            base = build(w, h);
            cache.put(key, base);
        }
        final Path path = work.get();
        path.set(base);
        path.offset(rect.left, rect.top);
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
