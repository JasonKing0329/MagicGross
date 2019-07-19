package com.king.app.gross.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.Region;
import com.king.app.gross.model.ImageUrlProvider;
import com.king.app.gross.model.entity.GrossDao;
import com.king.app.gross.model.entity.MarketGrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.entity.MovieDao;
import com.king.app.gross.model.setting.SettingProperty;
import com.king.app.gross.utils.ColorUtil;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.viewmodel.bean.MovieGridItem;
import com.king.app.gross.viewmodel.bean.RankTag;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
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

    public MutableLiveData<List<String>> indexListObserver = new MutableLiveData<>();

    public MutableLiveData<List<RankTag>> yearsObserver = new MutableLiveData<>();

    private List<MovieGridItem> mMovieList;

    private Map<Long, Boolean> checkMap;

    private Map<String, Integer> indexMap;

    private int mSortType;

    private int mRegionInList;

    private int mTempMoviePosition;

    private List<String> indexList;

    private String mYear;

    private String mKeywords;

    public MovieListViewModel(@NonNull Application application) {
        super(application);
        checkMap = new HashMap<>();
        indexMap = new HashMap<>();
        mSortType = AppConstants.MOVIE_SORT_DATE;
        mRegionInList = SettingProperty.getRegionTypeInMovieList();
    }

    /**
     * 第一次加载，创建年份
     */
    public void loadMovies() {
        mYear = AppConstants.TAG_YEAR_ALL;
        loadMovies(null, true, true);
    }

    /**
     * 改变了排序方式、标签，不需要重新创建标签
     */
    public void onParamsChanged(boolean showLoading) {
        loadMovies(null, false, showLoading);
    }

    /**
     * 新增movie，重新加载列表以及年份，最后滚动到新增movie
     * @param showMovie
     */
    public void onMovieInserted(Movie showMovie) {
        mYear = AppConstants.TAG_YEAR_ALL;
        loadMovies(showMovie, true, true);
    }

    private void loadMovies(Movie showMovie, boolean createTags, boolean showLoading) {
        if (showLoading) {
            loadingObserver.setValue(true);
        }
        queryMovies()
                .flatMap(list -> filterKeywords(list))
                .flatMap(list -> toGridItems(list))
                .flatMap(list -> {
                    if (createTags) {
                        return createYears(list).flatMap(list1 -> createIndex(list1));
                    }
                    else {
                        return createIndex(list);
                    }
                })
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
                        if (indexList != null) {
                            indexListObserver.setValue(indexList);
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
            if (!SettingProperty.isEnableVirtualMovie()) {
                builder.where(MovieDao.Properties.IsReal.eq(AppConstants.MOVIE_REAL));
            }
            // 过滤年份
            if (mYear.endsWith(AppConstants.TAG_YEAR_AFTER)) {
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                builder.where(MovieDao.Properties.Year.gt(currentYear));
            }
            else if (!mYear.equals(AppConstants.TAG_YEAR_ALL)) {
                builder.where(MovieDao.Properties.Year.eq(Integer.parseInt(mYear)));
            }
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
        item.setImageUrl(ImageUrlProvider.getMovieImageRandom(movie, random));
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
        if (mRegionInList == Region.CHN.ordinal()) {
            item.setGross(FormatUtil.formatChnGross(movie.getGrossStat().getChn()));
        }
        else if (mRegionInList == Region.NA.ordinal()) {
            item.setGross(FormatUtil.formatUsGross(movie.getGrossStat().getUs()));
        }
        else {
            item.setGross(FormatUtil.formatUsGross(movie.getGrossStat().getWorld()));
        }
        long gross = movie.getGrossStat().getChn();
        if (gross > 0) {
            item.setGrossCnNum(gross);
            item.setGrossCn(FormatUtil.formatChnGross(gross));
        }
        gross = movie.getGrossStat().getUs();
        if (gross > 0) {
            item.setGrossUsNum(gross);
            item.setGrossUs(FormatUtil.formatUsGross(gross));
        }
        gross = movie.getGrossStat().getWorld();
        if (gross > 0) {
            item.setGrossWorldNum(gross);
            item.setGrossWorld(FormatUtil.formatUsGross(gross));
        }
        return item;
    }

    private ObservableSource<List<Movie>> filterKeywords(List<Movie> list) {
        return observer -> {
            if (mKeywords == null || mKeywords.trim().length() == 0) {
                observer.onNext(list);
            }
            else {
                List<Movie> results = new ArrayList<>();
                for (Movie movie:list) {
                    if (isContainKeywords(movie.getName(), mKeywords) || isContainKeywords(movie.getNameChn(), mKeywords)
                            || isContainKeywords(movie.getSubName(), mKeywords) || isContainKeywords(movie.getSubChnName(), mKeywords)) {
                        results.add(movie);
                    }
                }
                observer.onNext(results);
            }
        };
    }

    private boolean isContainKeywords(String target, String keywords) {
        if (TextUtils.isEmpty(target)) {
            return false;
        }
        return target.toLowerCase().contains(keywords.toLowerCase());
    }

    private ObservableSource<List<MovieGridItem>> toGridItems(List<Movie> list) {
        return observer -> {
            List<MovieGridItem> results = new ArrayList<>();
            Random random = new Random();
            for (Movie movie : list) {
                MovieGridItem item = convertMovie(movie, random);
                results.add(item);
            }
            if (mSortType == AppConstants.MOVIE_SORT_NA || mSortType == AppConstants.MOVIE_SORT_CHN || mSortType == AppConstants.MOVIE_SORT_WW) {
                Collections.sort(results, new GrossComparator(mSortType));
            }
            observer.onNext(results);
        };
    }

    /**
     *
     * @param list 已按year升序排序
     * @return
     */
    private Observable<List<MovieGridItem>> createYears(List<MovieGridItem> list) {
        return Observable.create(observer -> {
            List<RankTag> tags = new ArrayList<>();

            int currentYear = Calendar.getInstance().get(Calendar.YEAR);

            Map<String, Boolean> yearMap = new HashMap<>();
            for (MovieGridItem item:list) {
                int year = item.getBean().getYear();
                String key;
                if (year > currentYear) {
                    key = currentYear + AppConstants.TAG_YEAR_AFTER;
                }
                else {
                    key = String.valueOf(year);
                }
                if (yearMap.get(key) == null) {
                    yearMap.put(key, true);
                    RankTag tag = new RankTag();
                    tag.setTag(key);
                    tags.add(tag);
                }
            }

            // 年份由近及远
            Collections.reverse(tags);

            RankTag all = new RankTag();
            all.setTag(AppConstants.TAG_YEAR_ALL);
            tags.add(0, all);

            yearsObserver.postValue(tags);
            observer.onNext(list);
        });
    }

    private ObservableSource<List<MovieGridItem>> createIndex(List<MovieGridItem> list) {
        return observer -> {
            indexMap.clear();

            indexList = new ArrayList<>();
            for (int i = 0; i < list.size(); i ++) {
                MovieGridItem item = list.get(i);
                String key;
                switch (mSortType) {
                    case AppConstants.MOVIE_SORT_DATE:
                        key = String.valueOf(item.getBean().getYear());
                        break;
                    case AppConstants.MOVIE_SORT_NAME:
                        key = String.valueOf(item.getBean().getName().charAt(0));
                        break;
                    default:
                        key = convertIndexKey(i);
                        break;
                }
                if (indexMap.get(key) == null) {
                    indexList.add(key);
                    indexMap.put(key, i);
                }
            }

            observer.onNext(list);
        };
    }

    private String convertIndexKey(int position) {
        if (position < 10) {
            return "Top 10";
        }
        // 10个一组
        else if (position < 100) {
            int start = position / 10 * 10;
            int end = start + 10;
            return String.valueOf(start + 1) + " - " + end;
        }
        // 50个一组
        else {
            int i = (position - 100)/50;
            int start = 100 + 50 * i;
            int end = start + 50;
            return String.valueOf(start + 1) + " - " + end;
        }
    }

    public void reloadMovie(Movie movie) {
//        movie.refresh();
        // 这里用movie.refresh没用，因为StatCreator里detach movie的操作影响不了实体本身，必须重新加载才行
        movie = getDaoSession().getMovieDao().load(movie.getId());
        if (moviesObserver.getValue() != null) {
            MovieGridItem item = convertMovie(movie, new Random());
            for (int i = 0; i < moviesObserver.getValue().size(); i ++) {
                MovieGridItem mi = moviesObserver.getValue().get(i);
                if (mi.getBean().getId().longValue() == movie.getId().longValue()) {
                    moviesObserver.getValue().set(i, item);
                    notifyUpdatePosition.setValue(i);
                    break;
                }
            }
        }
    }

    public int getIndexPosition(String data) {
        return indexMap.get(data);
    }

    public void filterYear(String year) {
        mYear = year;
        onParamsChanged(true);
    }

    public void onFilterWordsChanged(String words) {
        mKeywords = words;
        onParamsChanged(false);
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
                // delete data in table gross, market
                for (Movie movie:list) {
                    MApplication.getInstance().getDaoSession().getGrossDao()
                            .queryBuilder()
                            .where(GrossDao.Properties.MovieId.eq(movie.getId()))
                            .buildDelete()
                            .executeDeleteWithoutDetachingEntities();
                    MApplication.getInstance().getDaoSession().getMarketGrossDao()
                            .queryBuilder()
                            .where(MarketGrossDao.Properties.MovieId.eq(movie.getId()))
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
            onParamsChanged(true);
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
            onParamsChanged(true);
        }
    }

    public void setMoviePosition(int mTempMoviePosition) {
        this.mTempMoviePosition = mTempMoviePosition;
    }

    public void refreshLastMovie() {
        reloadMovie(moviesObserver.getValue().get(mTempMoviePosition).getBean());
    }
}
