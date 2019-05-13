package com.king.lib.groupmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/5/13 9:25
 */
public class GroupMenuVer extends LinearLayout {

    private int verItemBackgroundColor;

    private int verItemSize;

    private int verItemPadding;

    private int verItemMargin;

    private GroupMenu groupMenu;

    private OnMenuItemListener onMenuItemListener;

    public GroupMenuVer(Context context) {
        super(context);
        init(null);
    }

    public GroupMenuVer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public GroupMenuVer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void setOnMenuItemListener(OnMenuItemListener onMenuItemListener) {
        this.onMenuItemListener = onMenuItemListener;
    }

    private void init(AttributeSet attrs) {
        setOrientation(VERTICAL);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.GroupMenuVer);
        verItemBackgroundColor = typedArray.getColor(R.styleable.GroupMenuVer_verItemBackgroundColor, Color.parseColor("#66ffffff"));
        verItemMargin = typedArray.getDimensionPixelOffset(R.styleable.GroupMenuVer_verItemMargin, ParamUtils.dp2px(10));
        verItemSize = typedArray.getDimensionPixelOffset(R.styleable.GroupMenuVer_verItemSize, ParamUtils.dp2px(40));
        verItemPadding = typedArray.getDimensionPixelOffset(R.styleable.GroupMenuVer_verItemPadding, ParamUtils.dp2px(8));
        initMenu(typedArray);
    }

    private void initMenu(TypedArray typedArray) {
        removeAllViews();
        int menuRes = typedArray.getResourceId(R.styleable.GroupMenuVer_menu, -1);
        if (menuRes != -1) {
            try {
                groupMenu = new MenuParser().inflate(getContext(), menuRes);
                showGroupMenus(groupMenu);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showGroupMenus(GroupMenu menu) {
        if (menu.getItemList() != null) {
            for (int i = 0; i < menu.getItemList().size(); i ++) {
                ImageView item = createMenuView(menu.getItemList().get(i));
                LayoutParams params = new LayoutParams(verItemSize, verItemSize);
                if (i > 0) {
                    params.topMargin = verItemMargin;
                }
                addView(item, params);
            }
        }
    }

    private ImageView createMenuView(GroupMenuItem item) {
        ImageView view = new ImageView(getContext());
        view.setPadding(verItemPadding, verItemPadding, verItemPadding, verItemPadding);
        view.setImageResource(item.getIconRes());
        view.setId(item.getId());
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(verItemBackgroundColor);
        view.setBackground(drawable);
        view.setOnClickListener(itemClickListener);
        return view;
    }

    private OnClickListener itemClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (onMenuItemListener != null) {
                onMenuItemListener.onClickMenuItem(id);
            }
        }
    };
}
