package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.model.setting.SettingProperty;
import com.king.app.gross.utils.DBExportor;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @desc
 * @auth 景阳
 * @time 2018/6/10 0010 21:25
 */

public class HomeViewModel extends BaseViewModel {

    public MutableLiveData<Boolean> onEnableVirtualChanged = new MutableLiveData<>();

    private boolean isEnableVirtualMovie;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        isEnableVirtualMovie = SettingProperty.isEnableVirtualMovie();
    }

    public void saveDatabase() {
        copyDatabase()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        messageObserver.setValue("Save success");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private Observable<Boolean> copyDatabase() {
        return Observable.create(e -> {
            DBExportor.exportAsHistory();
            e.onNext(true);
        });
    }

    public void checkVirtualEnable() {
        boolean enable = SettingProperty.isEnableVirtualMovie();
        if (isEnableVirtualMovie != enable) {
            isEnableVirtualMovie = enable;
            onEnableVirtualChanged.setValue(true);
        }
    }
}
