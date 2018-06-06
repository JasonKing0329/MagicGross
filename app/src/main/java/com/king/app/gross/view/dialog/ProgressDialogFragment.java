package com.king.app.gross.view.dialog;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.king.app.gross.R;
import com.king.app.gross.databinding.DialogLoadingBinding;

/**
 * 描述:
 * <p/>作者：景阳
 * <p/>创建时间: 2018/1/29 13:37
 */
public class ProgressDialogFragment extends DialogFragment {

    DialogLoadingBinding mBinding;

    private String message;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setCancelable(true);
        setStyle(android.app.DialogFragment.STYLE_NORMAL, R.style.LoadingDialog);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_loading, container, false);
        mBinding.tvMessage.setText(message);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().setGravity(Gravity.CENTER);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        FragmentTransaction ft = manager.beginTransaction();
        if (isAdded()) {
            ft.show(this);
        } else {
            ft.add(this, tag);
        }
        ft.commitAllowingStateLoss();
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
