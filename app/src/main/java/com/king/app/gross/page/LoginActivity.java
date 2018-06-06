package com.king.app.gross.page;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.king.app.gross.R;
import com.king.app.gross.base.MvvmActivity;
import com.king.app.gross.databinding.ActivityLoginBinding;
import com.king.app.gross.model.FingerPrintController;
import com.king.app.gross.viewmodel.LoginViewModel;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class LoginActivity extends MvvmActivity<ActivityLoginBinding, LoginViewModel> {

    private FingerPrintController fingerPrint;

    @Override
    protected int getContentView() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView() {
        binding.setModel(viewModel);
        viewModel.fingerprintObserver.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                fingerPrint = new FingerPrintController(LoginActivity.this);
                if (fingerPrint.isSupported()) {
                    if (fingerPrint.hasRegistered()) {
                        startFingerPrintDialog();
                    } else {
                        showMessageLong("设备未注册指纹");
                    }
                    return;
                } else {
                    showMessageLong("设备不支持指纹识别");
                }
            }
        });
        viewModel.loginObserver.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean success) {
                if (success) {
                    startHome();
                }
            }
        });
    }

    @Override
    protected LoginViewModel createViewModel() {
        return ViewModelProviders.of(this).get(LoginViewModel.class);
    }

    @Override
    protected void initData() {
        new RxPermissions(this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isGranted) throws Exception {
                        if (isGranted) {
                            initCreate();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        finish();
                    }
                });
    }

    private void initCreate() {
        viewModel.initCreate();
    }

    private void startFingerPrintDialog() {
        if (fingerPrint.hasRegistered()) {
            boolean withPW = false;
            fingerPrint.showIdentifyDialog(withPW, new FingerPrintController.SimpleIdentifyListener() {

                @Override
                public void onSuccess() {
                    startHome();
                }

                @Override
                public void onFail() {

                }

                @Override
                public void onCancel() {
                    finish();
                }
            });
        } else {
            showMessageLong(getString(R.string.login_finger_not_register));
        }
    }

    private void startHome() {
//        Intent intent = new Intent(this, StoryListActivity.class);
//        startActivity(intent);
//        finish();
    }
}
