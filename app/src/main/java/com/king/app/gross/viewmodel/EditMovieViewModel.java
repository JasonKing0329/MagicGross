package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.conf.AppConfig;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.http.mojo.MojoClient;
import com.king.app.gross.model.http.mojo.MojoParser;

import java.io.File;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class EditMovieViewModel extends BaseViewModel {

    private MojoParser mojoParser;

    public MutableLiveData<Movie> mojoMovie = new MutableLiveData<>();

    public MutableLiveData<Movie> getBudget = new MutableLiveData<>();

    public EditMovieViewModel(@NonNull Application application) {
        super(application);
        mojoParser = new MojoParser();
    }

    public void fetchMovie(String mojoId) {
        if (TextUtils.isEmpty(mojoId)) {
            messageObserver.setValue("empty mojo id");
            return;
        }
        loadingObserver.setValue(true);
        MojoClient.getInstance().getService().getHtmlPage(mojoParser.getMojoDefaultUrl(mojoId))
                .flatMap(responseBody -> mojoParser.saveFile(responseBody, AppConfig.FILE_HTML_DEFAULT))
                .flatMap(file -> mojoParser.parseDefaultMovie(file))
//        mojoParser.parseDefaultMovie(new File(AppConfig.FILE_HTML_DEFAULT))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Movie>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(Movie movie) {
                        loadingObserver.setValue(false);
                        mojoMovie.setValue(movie);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        loadingObserver.setValue(false);
                        messageObserver.setValue("下载失败" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void fetchBudget(String titleId, Movie movie) {
        loadingObserver.setValue(true);
        MojoClient.getInstance().getService().getHtmlPage(mojoParser.getMojoTitleSummaryUrl(titleId))
                .flatMap(responseBody -> mojoParser.saveFile(responseBody, AppConfig.FILE_HTML_TITLE_SUMMARY))
                .flatMap(file -> mojoParser.parseTitleSummary(file, movie))
//        mojoParser.parseTitleSummary(new File(AppConfig.FILE_HTML_TITLE_SUMMARY), movie)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Movie>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(Movie movie) {
                        loadingObserver.setValue(false);
                        getBudget.setValue(movie);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        loadingObserver.setValue(false);
                        messageObserver.setValue("下载失败" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
