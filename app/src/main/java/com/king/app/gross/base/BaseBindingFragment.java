package com.king.app.gross.base;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 16:19
 */
public abstract class BaseBindingFragment<T extends ViewDataBinding> extends BaseFragment {

    protected T mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, getContentLayoutRes(), container, false);
        initView();
        return mBinding.getRoot();
    }

    protected abstract void initView();
}
