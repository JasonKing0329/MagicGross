package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConfig;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.entity.GrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.entity.MovieDao;
import com.king.app.gross.model.gross.DailyModel;
import com.king.app.gross.model.setting.SettingProperty;
import com.king.app.gross.utils.ColorUtil;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.viewmodel.bean.MovieGridItem;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    
    public MutableLiveData<List<MovieGridItem>> moviesObserver = new MutableLiveData<>();

    public MutableLiveData<Boolean> deleteObserver = new MutableLiveData<>();

    public MutableLiveData<Integer> notifyUpdatePosition = new MutableLiveData<>();

    public MutableLiveData<Integer> scrollToPosition = new MutableLiveData<>();

    private List<MovieGridItem> mMovieList;

    private Map<Long, Boolean> checkMap;

    private int mSortType;

    private int mRegionInList;

    private int mTempMoviePosition;

    public MovieListViewModel(@NonNull Application application) {
        super(application);
        checkMap = new HashMap<>();
        mSortType = AppConstants.MOVIE_SORT_DATE;
        mRegionInList = SettingProperty.getRegionTypeInMovieList();
    }

    public void loadMovies() {
        loadMovies(null);
    }

    public void loadMovies(Movie showMovie) {
        loadingObserver.setValue(true);
        queryMovies()
                .flatMap(list -> toGridItems(list))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<MovieGridItem>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(List<MovieGridItem> movies) {
                        mMovieList = movies;
                        loadingObserver.setValue(false);
                        moviesObserver.setValue(movies);
                        if (showMovie != null) {
                            for (int i = 0; i < movies.size(); i ++) {
                                if (movies.get(i).getBean().getId() == showMovie.getId()) {
                                    scrollToPosition.setValue(i);
                                    break;
                                }
                            }
                        }
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
            // debut与name可以在这里直接排，gross需要在赋值后排
            if (mSortType == AppConstants.MOVIE_SORT_DATE) {
                builder.orderAsc(MovieDao.Properties.Debut);
            }
            else if (mSortType == AppConstants.MOVIE_SORT_NAME) {
                builder.orderAsc(MovieDao.Properties.Name);
            }
            List<Movie> list = builder.build().list();
            e.onNext(list);
        });
    }

    private MovieGridItem convertMovie(Movie movie, Random random) {
        MovieGridItem item = new MovieGridItem();
        item.setBean(movie);
        item.setName(movie.getName());
        item.setSubName(movie.getSubName());
        String folder = AppConfig.IMG_MOVIE + "/" + movie.getName();
        if (!TextUtils.isEmpty(movie.getSubName())) {
            folder = folder + "_" + movie.getSubName();
        }
        File file = new File(folder);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files.length > 0) {
                item.setImageUrl(files[Math.abs(random.nextInt()) % files.length].getPath());
            }
        }
        item.setDate(movie.getDebut());
        item.setIndexColor(ColorUtil.randomWhiteTextBgColor());
        if (TextUtils.isEmpty(movie.getSubChnName())) {
            item.setChnName(movie.getNameChn());
        }
        else {
            item.setChnName(movie.getNameChn() + "：" + movie.getSubChnName());
        }
        if (mSortType == AppConstants.MOVIE_SORT_DATE) {
            if (!TextUtils.isEmpty(movie.getDebut())) {
                item.setFlag(movie.getDebut().substring(0, 4));
            }
            else {
                item.setFlag("#");
            }
        }
        else {
            if (!TextUtils.isEmpty(movie.getName())) {
                item.setFlag(movie.getName().substring(0, 1));
            }
            else {
                item.setFlag("#");
            }
        }
        DailyModel model = new DailyModel(movie);
        if (mRegionInList == Region.CHN.ordinal()) {
            item.setGross(FormatUtil.formatChnGross(model.queryTotalGross(mRegionInList)));
        }
        else {
            item.setGross(FormatUtil.formatUsGross(model.queryTotalGross(mRegionInList)));
        }
        long gross = model.queryTotalGross(Region.CHN.ordinal());
        if (gross > 0) {
            item.setGrossCnNum(gross);
            item.setGrossCn(FormatUtil.formatChnGross(gross));
        }
        gross = model.queryTotalGross(Region.NA.ordinal());
        if (gross > 0) {
            item.setGrossUsNum(gross);
            item.setGrossUs(FormatUtil.formatUsGross(gross));
        }
        gross = model.queryTotalGross(Region.WORLDWIDE.ordinal());
        if (gross > 0) {
            item.setGrossWorldNum(gross);
            item.setGrossWorld(FormatUtil.formatUsGross(gross));
        }
        return item;
    }

    private Observable<List<MovieGridItem>> toGridItems(List<Movie> list) {
        return Observable.create(e -> {
            List<MovieGridItem> results = new ArrayList<>();
            Random random = new Random();
            for (Movie movie:list) {
                MovieGridItem item = convertMovie(movie, random);
                results.add(item);
            }
            if (mSortType == AppConstants.MOVIE_SORT_NA || mSortType == AppConstants.MOVIE_SORT_CHN || mSortType == AppConstants.MOVIE_SORT_WW) {
                Collections.sort(results, new GrossComparator(mSortType));
            }
            e.onNext(results);
        });
    }

    public void reloadMovie(Movie movie) {
        if (moviesObserver.getValue() != null) {
            MovieGridItem item = convertMovie(movie, new Random());
            for (int i = 0; i < moviesObserver.getValue().size(); i ++) {
                MovieGridItem mi = moviesObserver.getValue().get(i);
                if (mi.getBean().getId() == movie.getId()) {
                    moviesObserver.getValue().set(i, item);
                    notifyUpdatePosition.setValue(i);
                    break;
                }
            }
        }
    }

    private class GrossComparator implements Comparator<MovieGridItem> {

        private int sortType;

        public GrossComparator(int sortType) {
            this.sortType = sortType;
        }

        @Override
        public int compare(MovieGridItem left, MovieGridItem right) {
            long lv;
            long rv;
            switch (sortType) {
                case AppConstants.MOVIE_SORT_CHN:
                    lv = left.getGrossCnNum();
                    rv = right.getGrossCnNum();
                    break;
                case AppConstants.MOVIE_SORT_WW:
                    lv = left.getGrossWorldNum();
                    rv = right.getGrossWorldNum();
                    break;
                default:
                    lv = left.getGrossUsNum();
                    rv = right.getGrossUsNum();
                    break;
            }
            long result = rv - lv;
            if (result < 0) {
                return -1;
            }
            else if (result > 0) {
                return 1;
            }
            else {
                return 0;
            }
        }
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

    public ArrayList<CharSequence> getSelectedItems() {
        ArrayList<CharSequence> list = new ArrayList<>();
        for (MovieGridItem movie:mMovieList) {
            if (checkMap.get(movie.getBean().getId()) != null) {
                list.add(String.valueOf(movie.getBean().getId()));
            }
        }
        return list;
    }

    private Observable<Boolean> deleteMovies() {
        return Observable.create(e -> {
            List<Movie> list = new ArrayList<>();
            if (mMovieList != null) {
                for (MovieGridItem movie:mMovieList) {
                    if (checkMap.get(movie.getBean().getId()) != null) {
                        list.add(movie.getBean());
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

    public int getSortType() {
        return mSortType;
    }

    public int getMovieNumber() {
        return mMovieList == null ? 0:mMovieList.size();
    }

    public void updateRegionInList(int regionInList) {
        if (mRegionInList != regionInList) {
            mRegionInList = regionInList;
            SettingProperty.setRegionTypeInMovieList(regionInList);
            loadMovies();
        }
    }

    public void setMoviePosition(int mTempMoviePosition) {
        this.mTempMoviePosition = mTempMoviePosition;
    }

    public void refreshLastMovie() {
        reloadMovie(moviesObserver.getValue().get(mTempMoviePosition).getBean());
    }
}
