package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.page.bean.EditMarketGrossBean;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/4/23 17:16
 */
public class EditMarketGrossViewModel extends BaseViewModel {

    public MutableLiveData<List<EditMarketGrossBean>> marketsObserver = new MutableLiveData<>();

    public EditMarketGrossViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadData() {
        loadData(null);
    }

    private void loadData(ArrayList<CharSequence> refMovieIds) {
        getData(refMovieIds)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<EditMarketGrossBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<EditMarketGrossBean> list) {
                        marketsObserver.setValue(list);
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

    public void onReferenceChanged(ArrayList<CharSequence> list) {
        loadData(list);
    }

    private Observable<List<EditMarketGrossBean>> getData(ArrayList<CharSequence> refMovieIds) {
        return Observable.create(e -> {
            for (CharSequence idStr:refMovieIds) {
                long movieId = Long.parseLong(idStr.toString());
            }
        });
    }
}
