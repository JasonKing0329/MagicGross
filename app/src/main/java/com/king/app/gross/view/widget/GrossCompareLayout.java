package com.king.app.gross.view.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.king.app.gross.R;
import com.king.app.gross.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/11/27 9:55
 */
public class GrossCompareLayout extends LinearLayout {

    private List<TextView> textViews;

    public GrossCompareLayout(Context context) {
        super(context);
        init();
    }

    public GrossCompareLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        textViews = new ArrayList<>();
    }

    public void addCell(String text) {
        // 最左侧不加分割线
        if (getChildCount() > 0) {
            Drawable drawable = getResources().getDrawable(R.drawable.shape_divider_hor);
            View divider = new View(getContext());
            divider.setBackground(drawable);
            addView(divider, new LayoutParams(ScreenUtils.dp2px(1), LayoutParams.MATCH_PARENT));
        }

        LayoutParams params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        TextView view = new TextView(getContext());
        view.setText(text);
        view.setGravity(Gravity.RIGHT);
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        view.setPadding(0, 0, ScreenUtils.dp2px(5), 0);
        addView(view, params);
        textViews.add(view);
    }

    public void setCell(int index, String text) {
        if (index >= textViews.size()) {
            for (int i = textViews.size(); i < index + 1; i ++) {
                if (i == index) {
                    addCell(text);
                }
                else {
                    addCell("");
                }
            }
        }
        else {
            textViews.get(index).setText(text);
        }
    }

    public void setCellTextColor(int index, int color) {
        textViews.get(index).setTextColor(color);
    }
}
