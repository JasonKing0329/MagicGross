package com.king.app.gross.view.dialog.content;

import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;

import com.king.app.gross.R;
import com.king.app.gross.base.IFragmentHolder;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.Region;
import com.king.app.gross.databinding.FragmentEditMarketTotalBinding;
import com.king.app.gross.model.gross.StatModel;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.model.entity.GrossDao;
import com.king.app.gross.model.entity.GrossStat;
import com.king.app.gross.model.entity.GrossStatDao;
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.model.entity.MarketGrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.utils.DebugLog;
import com.king.app.gross.utils.FormatUtil;

import org.greenrobot.greendao.database.Database;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/5/27 11:38
 */
public class EditMarketTotalFragment extends DraggableContentFragment<FragmentEditMarketTotalBinding> {

    private Movie movie;

    private long grossNa;

    private long grossNaOpen;

    private long grossMarketTotal;

    private long grossMarketTotalOpen;

    private MarketGross undisclosed;

    private OnDataChangedListener onDataChangedListener;

    @Override
    protected void bindFragmentHolder(IFragmentHolder holder) {

    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.fragment_edit_market_total;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public void setOnDataChangedListener(OnDataChangedListener onDataChangedListener) {
        this.onDataChangedListener = onDataChangedListener;
    }

    @Override
    protected void initView() {

        mBinding.btnUpdate.setOnClickListener(v -> {
            save();
        });

        loadMarketGross();

        mBinding.etUn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    undisclosed.setGross(Long.parseLong(charSequence.toString()));
                } catch (Exception e) {
                    undisclosed.setGross(0);
                }
                onGrossChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mBinding.etUnOpen.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    undisclosed.setOpening(Long.parseLong(charSequence.toString()));
                } catch (Exception e) {
                    undisclosed.setOpening(0);
                }
                onGrossOpenChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void onGrossChanged() {
        mBinding.tvMarketTotal.setText(FormatUtil.formatUsGross(grossMarketTotal));
        mBinding.tvOversea.setText(FormatUtil.formatUsGross(grossMarketTotal + undisclosed.getGross()));
        mBinding.tvWw.setText(FormatUtil.formatUsGross(grossNa + grossMarketTotal + undisclosed.getGross()));
    }

    private void onGrossOpenChanged() {
        mBinding.tvMarketOpen.setText(FormatUtil.formatUsGross(grossMarketTotalOpen));
        mBinding.tvOverseaOpening.setText(FormatUtil.formatUsGross(grossMarketTotalOpen + undisclosed.getOpening()));
        mBinding.tvWwOpen.setText(FormatUtil.formatUsGross(grossNaOpen+ grossMarketTotalOpen + undisclosed.getOpening()));
    }

    private void loadMarketGross() {
        MarketGrossDao dao = MApplication.getInstance().getDaoSession().getMarketGrossDao();
        undisclosed = dao.queryBuilder()
                .where(MarketGrossDao.Properties.MovieId.eq(movie.getId()))
                .where(MarketGrossDao.Properties.MarketId.eq(AppConstants.MARKET_UNDISCLOSED_ID))
                .build().unique();
        if (undisclosed == null) {
            undisclosed = new MarketGross();
            undisclosed.setMarketId(AppConstants.MARKET_UNDISCLOSED_ID);
            undisclosed.setMovieId(movie.getId());
        }

        Database database = MApplication.getInstance().getDatabase();
        String sql = "SELECT SUM(opening),SUM(gross) FROM %s WHERE %s=? AND %s>?";
        sql = String.format(sql, MarketGrossDao.TABLENAME, MarketGrossDao.Properties.MovieId.columnName, MarketGrossDao.Properties.MarketId.columnName);
        Cursor cursor = database.rawQuery(sql, new String[]{String.valueOf(movie.getId()), String.valueOf(AppConstants.MARKET_TOTAL_ID)});
        if (cursor.moveToNext()) {
            grossMarketTotalOpen = cursor.getLong(0);
            grossMarketTotal = cursor.getLong(1);
        }

        GrossStat stat = MApplication.getInstance().getDaoSession().getGrossStatDao().queryBuilder()
                .where(GrossStatDao.Properties.MovieId.eq(movie.getId()))
                .build().unique();
        if (stat != null) {
            grossNa = stat.getUs();
            grossNaOpen = stat.getUsOpening();
        }

        mBinding.etUn.setText(String.valueOf(undisclosed.getGross()));
        mBinding.etUnOpen.setText(String.valueOf(undisclosed.getOpening()));

        onGrossChanged();
        onGrossOpenChanged();
    }

    private void save() {
        doSave()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (onDataChangedListener != null) {
                            onDataChangedListener.onDataChanged();
                        }
                        showMessageShort("Success");
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

    private Observable<Boolean> doSave() {
        return Observable.create(e -> {
            insertUndisclosed();

            // 插入oversea的total数据
            GrossDao grossDao = MApplication.getInstance().getDaoSession().getGrossDao();
            insertOversea(grossDao);

            // 插入world wide的total数据
            insertWorld(grossDao);

            grossDao.detachAll();

            // 修改统计表
            new StatModel().statisticMovieInstant(movie);
            e.onNext(true);
        });
    }

    private void insertUndisclosed() {
        MarketGrossDao dao = MApplication.getInstance().getDaoSession().getMarketGrossDao();
        dao.insertOrReplace(undisclosed);
    }

    private void insertWorld(GrossDao grossDao) {
        Gross world = grossDao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(movie.getId()))
                .where(GrossDao.Properties.Region.eq(Region.WORLDWIDE.ordinal()))
                .where(GrossDao.Properties.IsTotal.eq(AppConstants.GROSS_IS_TOTAL))
                .build().unique();
        if (world == null) {
            world = new Gross();
            world.setMovieId(movie.getId());
            world.setIsTotal(AppConstants.GROSS_IS_TOTAL);
            world.setRegion(Region.WORLDWIDE.ordinal());
            DebugLog.e("insert world");
        }
        else {
            DebugLog.e("update world");
        }
        world.setGross(grossMarketTotal + undisclosed.getGross() + grossNa);
        grossDao.insertOrReplace(world);

        world = grossDao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(movie.getId()))
                .where(GrossDao.Properties.Region.eq(Region.WORLDWIDE.ordinal()))
                .where(GrossDao.Properties.IsTotal.eq(AppConstants.GROSS_IS_OPENING))
                .build().unique();
        if (world == null) {
            world = new Gross();
            world.setMovieId(movie.getId());
            world.setIsTotal(AppConstants.GROSS_IS_OPENING);
            world.setRegion(Region.WORLDWIDE.ordinal());
            DebugLog.e("insert world opening");
        }
        else {
            DebugLog.e("update world opening");
        }
        world.setGross(grossMarketTotalOpen + undisclosed.getOpening() + grossNaOpen);
        grossDao.insertOrReplace(world);
    }

    private void insertOversea(GrossDao grossDao) {
        Gross oversea = grossDao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(movie.getId()))
                .where(GrossDao.Properties.Region.eq(Region.OVERSEA.ordinal()))
                .where(GrossDao.Properties.IsTotal.eq(AppConstants.GROSS_IS_TOTAL))
                .build().unique();
        if (oversea == null) {
            oversea = new Gross();
            oversea.setMovieId(movie.getId());
            oversea.setIsTotal(AppConstants.GROSS_IS_TOTAL);
            oversea.setRegion(Region.OVERSEA.ordinal());
            DebugLog.e("insert oversea");
        }
        else {
            DebugLog.e("update oversea");
        }
        oversea.setGross(grossMarketTotal + undisclosed.getGross());
        grossDao.insertOrReplace(oversea);

        oversea = grossDao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(movie.getId()))
                .where(GrossDao.Properties.Region.eq(Region.OVERSEA.ordinal()))
                .where(GrossDao.Properties.IsTotal.eq(AppConstants.GROSS_IS_OPENING))
                .build().unique();
        if (oversea == null) {
            oversea = new Gross();
            oversea.setMovieId(movie.getId());
            oversea.setIsTotal(AppConstants.GROSS_IS_OPENING);
            oversea.setRegion(Region.OVERSEA.ordinal());
            DebugLog.e("insert oversea opening");
        }
        else {
            DebugLog.e("update oversea opening");
        }
        oversea.setGross(grossMarketTotalOpen + undisclosed.getOpening());
        grossDao.insertOrReplace(oversea);
    }

    public interface OnDataChangedListener {
        void onDataChanged();
    }
}
