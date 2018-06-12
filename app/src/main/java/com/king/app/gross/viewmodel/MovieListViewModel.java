package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.model.entity.GrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.entity.MovieDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 14:59
 */
public class MovieListViewModel extends BaseViewModel {
    
    public MutableLiveData<List<Movie>> moviesObserver = new MutableLiveData<>();

    public MutableLiveData<Boolean> deleteObserver = new MutableLiveData<>();

    private List<Movie> mMovieList;

    private Map<Long, Boolean> checkMap;

    private int mSortType;
    
    public MovieListViewModel(@NonNull Application application) {
        super(application);
        checkMap = new HashMap<>();
        mSortType = AppConstants.MOVIE_SORT_DATE;
    }

    public void loadMovies() {
        loadingObserver.setValue(true);
        queryMovies()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<Movie>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<Movie> movies) {
                        mMovieList = movies;
                        loadingObserver.setValue(false);
                        moviesObserver.setValue(movies);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        loadingObserver.setValue(false);
                        messageObserver.setValue(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private Observable<List<Movie>> queryMovies() {
        return Observable.create(e -> {
            QueryBuilder<Movie> builder = MApplication.getInstance().getDaoSession().getMovieDao()
                    .queryBuilder();
            if (mSortType == AppConstants.MOVIE_SORT_DATE) {
                builder.orderAsc(MovieDao.Properties.Debut);
            }
            else {
                builder.orderAsc(MovieDao.Properties.Name);
            }
            List<Movie> list = builder.build().list();
            e.onNext(list);
        });
    }

    public Map<Long, Boolean> getCheckMap() {
        return checkMap;
    }

    public void delete() {
        loadingObserver.setValue(true);
        deleteMovies()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(Boolean movies) {
                        loadingObserver.setValue(false);
                        deleteObserver.setValue(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        loadingObserver.setValue(false);
                        messageObserver.setValue(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private Observable<Boolean> deleteMovies() {
        return Observable.create(e -> {
            List<Movie> list = new ArrayList<>();
            if (mMovieList != null) {
                for (Movie movie:mMovieList) {
                    if (checkMap.get(movie.getId()) != null) {
                        list.add(movie);
                    }
                }
            }
            if (list.size() > 0) {
                // delete from table movie
                MApplication.getInstance().getDaoSession().getMovieDao().deleteInTx(list);
                // delete data in table gross
                for (Movie movie:list) {
                    MApplication.getInstance().getDaoSession().getGrossDao()
                            .queryBuilder()
                            .where(GrossDao.Properties.MovieId.eq(movie.getId()))
                            .buildDelete()
                            .executeDeleteWithoutDetachingEntities();
                }
                MApplication.getInstance().getDaoSession().getMovieDao().detachAll();
                MApplication.getInstance().getDaoSession().getGrossDao().detachAll();
            }
            e.onNext(true);
        });
    }

    public void changeSortType(int sortType) {
        if (mSortType != sortType) {
            mSortType = sortType;
            loadMovies();
        }
    }
}
