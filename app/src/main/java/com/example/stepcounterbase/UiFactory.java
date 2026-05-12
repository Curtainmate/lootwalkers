package com.example.stepcounterbase;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

final class UiFactory {
    private final Context context;

    UiFactory(Context context) {
        this.context = context;
    }

    TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setLineSpacing(dp(2), 1.0f);
        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return view;
    }

    LinearLayout card() {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(16), dp(15), dp(16), dp(15));
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(255, 251, 239));
        background.setCornerRadius(dp(8));
        background.setStroke(dp(1), Color.rgb(220, 205, 176));
        layout.setBackground(background);
        LinearLayout.LayoutParams params = fullWidthWrapContent();
        params.setMargins(0, 0, 0, dp(12));
        layout.setLayoutParams(params);
        return layout;
    }

    LinearLayout darkCard() {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(14), dp(12), dp(14), dp(12));
        layout.setBackground(cardBackground(Color.rgb(30, 25, 18), Color.rgb(126, 82, 37)));
        LinearLayout.LayoutParams params = fullWidthWrapContent();
        params.setMargins(0, 0, 0, dp(8));
        layout.setLayoutParams(params);
        return layout;
    }

    Button actionButton(String label, boolean primary) {
        Button button = new Button(context);
        button.setText(label);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setTextColor(primary ? Color.WHITE : Color.rgb(46, 34, 28));
        button.setBackgroundResource(primary ? R.drawable.button_primary : R.drawable.button_secondary);
        return button;
    }

    TextView sectionTitle(String label) {
        TextView title = text(label, 24, Color.rgb(245, 224, 177), true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(10));
        return title;
    }

    LinearLayout adventureCard(String tag, String title, String body, View.OnClickListener listener) {
        return adventureCard(0, tag, title, body, listener);
    }

    LinearLayout adventureCard(int drawableRes, String title, String body, View.OnClickListener listener) {
        return adventureCard(drawableRes, "", title, body, listener);
    }

    LinearLayout iconAdventureCard(int drawableRes, String title, String body, View.OnClickListener listener) {
        return adventureCard(drawableRes, "", title, body, listener, true);
    }

    LinearLayout adventureCard(int drawableRes, String tag, String title, String body, View.OnClickListener listener) {
        return adventureCard(drawableRes, tag, title, body, listener, false);
    }

    private LinearLayout adventureCard(int drawableRes, String tag, String title, String body,
                                      View.OnClickListener listener, boolean showCopyWithImage) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(drawableRes == 0 || showCopyWithImage ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        card.setGravity(drawableRes == 0 || showCopyWithImage ? Gravity.CENTER_VERTICAL : Gravity.CENTER);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));
        card.setMinimumHeight(drawableRes == 0 ? dp(116) : 0);
        card.setBackground(cardBackground(Color.rgb(25, 42, 24), Color.rgb(126, 82, 37)));
        if (listener != null) {
            card.setClickable(true);
            card.setOnClickListener(listener);
        }

        if (drawableRes == 0) {
            card.addView(placeholderIcon(tag, dp(78)), new LinearLayout.LayoutParams(dp(82), dp(82)));
        } else {
            ImageView image = new ImageView(context);
            image.setImageResource(drawableRes);
            image.setAdjustViewBounds(true);
            image.setScaleType(showCopyWithImage ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
            image.setBackgroundColor(Color.rgb(13, 16, 12));
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                    showCopyWithImage ? dp(82) : LinearLayout.LayoutParams.MATCH_PARENT,
                    showCopyWithImage ? dp(82) : dp(140)
            );
            if (showCopyWithImage) {
                imageParams.setMargins(0, 0, dp(12), 0);
            }
            card.addView(image, imageParams);
        }

        if (drawableRes == 0 || showCopyWithImage) {
            LinearLayout copy = new LinearLayout(context);
            copy.setOrientation(LinearLayout.VERTICAL);
            copy.setPadding(drawableRes == 0 ? dp(12) : 0, 0, 0, 0);
            TextView titleView = text(title, 21, Color.rgb(245, 224, 177), true);
            TextView bodyView = text(body, 14, Color.rgb(226, 205, 163), false);
            bodyView.setPadding(0, dp(5), 0, 0);
            copy.addView(titleView);
            copy.addView(bodyView);
            card.addView(copy, weightedWidth(1.0f));
        }
        card.setLayoutParams(buttonLayoutParams());
        return card;
    }

    LinearLayout detailRow(String label, String value) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(12), dp(9), dp(12), dp(9));
        row.setBackground(cardBackground(Color.rgb(36, 30, 22), Color.rgb(80, 58, 35)));
        TextView labelView = text(label, 13, Color.rgb(192, 157, 100), true);
        TextView valueView = text(value, 15, Color.rgb(245, 224, 177), false);
        valueView.setPadding(0, dp(3), 0, 0);
        row.addView(labelView);
        row.addView(valueView);
        row.setLayoutParams(buttonLayoutParams());
        return row;
    }

    LinearLayout meterCard(String title, String tag, ProgressBar bar, TextView valueView) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));
        card.setBackground(cardBackground(Color.rgb(24, 21, 17), Color.rgb(80, 58, 35)));

        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(placeholderIcon(tag, dp(34)), new LinearLayout.LayoutParams(dp(38), dp(38)));
        TextView titleView = text(title, 13, Color.rgb(245, 224, 177), true);
        titleView.setPadding(dp(8), 0, 0, 0);
        header.addView(titleView, weightedWidth(1.0f));
        card.addView(header);

        card.addView(bar, progressLayoutParams());
        valueView.setTextColor(Color.rgb(192, 157, 100));
        valueView.setTextSize(12);
        valueView.setGravity(Gravity.CENTER);
        valueView.setPadding(0, dp(4), 0, 0);
        card.addView(valueView);
        return card;
    }

    TextView placeholderIcon(String label, int size) {
        TextView icon = text(label, 13, Color.rgb(245, 224, 177), true);
        icon.setGravity(Gravity.CENTER);
        icon.setSingleLine(false);
        icon.setBackground(cardBackground(Color.rgb(52, 42, 28), Color.rgb(192, 125, 44)));
        icon.setMinWidth(size);
        icon.setMinHeight(size);
        return icon;
    }

    Button menuButton(String label) {
        Button button = actionButton(label, false);
        button.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        button.setMinHeight(dp(92));
        button.setPadding(dp(14), dp(10), dp(14), dp(10));
        return button;
    }

    Button backButton(View.OnClickListener listener) {
        Button button = actionButton("Back", false);
        button.setOnClickListener(listener);
        return button;
    }

    void addLockedRow(LinearLayout parent, String label) {
        Button button = menuButton(label + "\nComing later");
        button.setEnabled(false);
        parent.addView(button, buttonLayoutParams());
    }

    Button mainNavButton(String label, int drawableRes, View.OnClickListener listener) {
        Button button = actionButton(label, false);
        button.setTextSize(12);
        button.setSingleLine(false);
        button.setPadding(dp(2), 0, dp(2), 0);
        Drawable icon = context.getResources().getDrawable(drawableRes, context.getTheme());
        int size = dp(28);
        icon.setBounds(0, 0, size, size);
        button.setCompoundDrawables(null, icon, null, null);
        button.setCompoundDrawablePadding(dp(2));
        button.setOnClickListener(listener);
        return button;
    }

    void styleTabButton(Button button, boolean selected) {
        if (button == null) {
            return;
        }
        button.setTextColor(selected ? Color.WHITE : Color.rgb(245, 224, 177));
        button.setBackground(cardBackground(
                selected ? Color.rgb(129, 83, 31) : Color.rgb(36, 30, 22),
                selected ? Color.rgb(240, 174, 55) : Color.rgb(126, 82, 37)
        ));
    }

    LinearLayout.LayoutParams fullWidthWrapContent() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    LinearLayout.LayoutParams weightedWidth(float weight) {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight);
    }

    LinearLayout.LayoutParams progressLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(14)
        );
        params.setMargins(0, dp(2), 0, dp(4));
        return params;
    }

    LinearLayout.LayoutParams sceneLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(260)
        );
        params.setMargins(0, 0, 0, dp(10));
        return params;
    }

    LinearLayout.LayoutParams buttonLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(10));
        return params;
    }

    int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private GradientDrawable cardBackground(int fill, int stroke) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(fill);
        background.setCornerRadius(dp(4));
        background.setStroke(dp(2), stroke);
        return background;
    }
}
