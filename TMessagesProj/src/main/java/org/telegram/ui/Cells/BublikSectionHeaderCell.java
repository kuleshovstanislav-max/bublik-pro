package org.telegram.ui.Cells;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

/** Bublik Pro: small uppercase label with an icon above a group of chats ("Pinned", "All chats"). */
public class BublikSectionHeaderCell extends FrameLayout {
    private final ImageView iconView;
    private final TextView textView;
    private final Theme.ResourcesProvider resourcesProvider;

    public BublikSectionHeaderCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;

        iconView = new ImageView(context);
        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        addView(iconView, LayoutHelper.createFrame(15, 15, Gravity.LEFT | Gravity.CENTER_VERTICAL, 24, 4, 0, 0));

        textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        textView.setLetterSpacing(0.1f);
        textView.setAllCaps(true);
        textView.setSingleLine();
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTypeface(AndroidUtilities.bold());
        addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 45, 4, 24, 0));

        updateColors();
    }

    public void set(CharSequence text, @DrawableRes int icon) {
        textView.setText(text);
        iconView.setImageResource(icon);
    }

    public void updateColors() {
        final int color = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider);
        textView.setTextColor(color);
        iconView.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(dp(40), MeasureSpec.EXACTLY));
    }
}
