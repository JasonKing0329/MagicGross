package com.king.app.gross.base;

import android.arch.lifecycle.Observer;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 描述:
 * <p/>作者：景阳
 * <p/>创建时间: 2018/4/4 10:51
 */
public abstract class MvvmFragment<T extends ViewDataBinding, VM extends BaseViewModel> extends BaseFragment {

    protected T mBinding;

    protected VM mModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mBinding = DataBindingUtil.inflate(inflater, getContentLayoutRes(), container, false);
        mModel = createViewModel();
        if (mModel != null) {
            mModel.loadingObserver.observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean show) {
                    if (show) {
                        showProgress("loading...");
                    }
                    else {
                        dismissProgress();
                    }
                }
            });
            mModel.messageObserver.observe(this, new Observer<String>() {
                @Override
                public void onChanged(@Nullable String message) {
                    showMessageLong(message);
                }
            });
        }

        View view = mBinding.getRoot();
        onCreate(view);
        onCreateData();
        return view;
    }

    protected abstract VM createViewModel();

    protected abstract void onCreate(View view);

    protected abstract void onCreateData();

    @Override
    public void onDestroyView() {
        if (mModel != null) {
            mModel.onDestroy();
        }
        super.onDestroyView();
    }

}
