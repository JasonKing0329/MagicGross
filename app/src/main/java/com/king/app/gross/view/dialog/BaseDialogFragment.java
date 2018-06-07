package com.king.app.gross.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.king.app.gross.R;

/**
 * 描述:DialogFragment基类
 *
 * <br/>创建时间: 2017/4/19
 */
public abstract class BaseDialogFragment extends DialogFragment {

    private WindowManager.LayoutParams windowParams;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(getCancelable());
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseDialogFragment);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        windowParams = getDialog().getWindow().getAttributes();
        return onSubCreateView(inflater, container, savedInstanceState);
    }

    protected abstract View onSubCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 是否可以点击空白处取消
     *
     * @return
     */
    public boolean getCancelable() {
        return true;
    }

    public void setWidth(int width) {
        windowParams.width = width;
        getDialog().getWindow().setAttributes(windowParams);
    }

    public void setHeight(int height) {
        windowParams.height = height;
        getDialog().getWindow().setAttributes(windowParams);
    }

    /**
     * 设置dialog的偏移位置
     *
     * @param x 负数向左，正数向右
     * @param y 负数向上，正数向下
     */
    public void setPositionOffset(int x, int y) {

        windowParams.x = x;
        windowParams.y = y;
        getDialog().getWindow().setAttributes(windowParams);
    }

    /**
     * move dialog
     *
     * @param x
     * @param y
     */
    protected void move(int x, int y) {

        windowParams.x += x;
        windowParams.y += y;
        getDialog().getWindow().setAttributes(windowParams);//must have
    }
}
