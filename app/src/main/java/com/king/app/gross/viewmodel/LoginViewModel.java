package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConfig;
import com.king.app.gross.model.SettingProperty;
import com.king.app.gross.utils.DBExportor;
import com.king.app.gross.utils.MD5Util;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 描述:
 * <p/>作者：景阳
 * <p/>创建时间: 2018/4/2 14:05
 */
public class LoginViewModel extends BaseViewModel {

    public ObservableField<String> etPwdText;

    public ObservableInt groupLoginVisibility;

    public MutableLiveData<Boolean> fingerprintObserver;

    public MutableLiveData<Boolean> loginObserver;

    private String mPwd;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        etPwdText = new ObservableField<>();
        groupLoginVisibility = new ObservableInt(View.INVISIBLE);
        fingerprintObserver = new MutableLiveData<>();
        loginObserver = new MutableLiveData<>();
    }

    public void onClickLogin(View view) {
        checkPassword(mPwd);
    }

    public void initCreate() {
        // 每次进入导出一次数据库
        DBExportor.execute();
        prepare();
    }

    public void prepare() {
        loadingObserver.setValue(true);
        prepareDatas()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(Object o) {
                        loadingObserver.setValue(false);

                        MApplication.getInstance().createGreenDao();
                        if (SettingProperty.isEnableFingerPrint()) {
                            fingerprintObserver.setValue(true);
                        }
                        else {
                            groupLoginVisibility.set(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        loadingObserver.setValue(false);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private Observable<Object> prepareDatas() {
        return Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {

                // 创建base目录
                for (String path: AppConfig.DIRS) {
                    File file = new File(path);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                }

                // init server url
//                BaseUrl.getInstance().setBaseUrl(SettingProperty.getServerBaseUrl());

                e.onNext(new Object());
                e.onComplete();
            }
        });
    }

    public TextWatcher getPwdTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mPwd = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }

    public void checkPassword(String pwd) {
        if ("38D08341D686315F".equals(MD5Util.get16MD5Capital(pwd))) {
            loginObserver.setValue(true);
        }
        else {
            loginObserver.setValue(false);
            messageObserver.setValue("密码错误");
        }
    }
}
