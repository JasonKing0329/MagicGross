package com.king.app.gross.base;

import android.arch.lifecycle.Observer;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * 描述:
 * <p/>作者：景阳
 * <p/>创建时间: 2018/4/2 15:50
 */
public abstract class MvvmActivity<T extends ViewDataBinding, VM extends BaseViewModel> extends BaseActivity {

    protected T binding;

    protected VM viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, getContentView());
        viewModel = createViewModel();
        if (viewModel != null) {
            viewModel.loadingObserver.observe(this, new Observer<Boolean>() {
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
            viewModel.messageObserver.observe(this, new Observer<String>() {
                @Override
                public void onChanged(@Nullable String message) {
                    showMessageLong(message);
                }
            });
        }

        initView();
        initData();
    }

    /**
     * 仅LoginActivity不应用，单独覆写
     * @return
     */
    protected boolean updateStatusBarColor() {
        return true;
    }

    protected abstract VM createViewModel();

    protected abstract void initData();

    @Override
    protected void onDestroy() {
        if (viewModel != null) {
            viewModel.onDestroy();
        }
        super.onDestroy();
    }
}
