package com.king.app.gross.view.dialog.content;

import com.king.app.gross.R;
import com.king.app.gross.base.IFragmentHolder;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.Region;
import com.king.app.gross.databinding.FragmentEditTotalBinding;
import com.king.app.gross.model.gross.StatModel;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.model.entity.GrossDao;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.utils.DebugLog;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/5/27 15:21
 */
public class EditTotalFragment extends DraggableContentFragment<FragmentEditTotalBinding> {

    private Movie movie;

    private Region region;

    private Gross opening;

    private Gross total;

    private OnDataChangedListener onDataChangedListener;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.fragment_edit_total;
    }

    @Override
    protected void bindFragmentHolder(IFragmentHolder holder) {

    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public void setOnDataChangedListener(OnDataChangedListener onDataChangedListener) {
        this.onDataChangedListener = onDataChangedListener;
    }

    @Override
    protected void initView() {
        initOpening();
        initTotal();

        mBinding.tvOk.setOnClickListener(v -> onSave());
    }

    private void onSave() {
        long nOpening;
        long nTotal;
        try {
            nOpening = Long.parseLong(mBinding.etOpen.getText().toString());
        } catch (Exception e) {
            showMessageShort("Wrong opening");
            return;
        }
        try {
            nTotal = Long.parseLong(mBinding.etTotal.getText().toString());
        } catch (Exception e) {
            showMessageShort("Wrong total");
            return;
        }

        GrossDao grossDao = MApplication.getInstance().getDaoSession().getGrossDao();
        if (nOpening == 0 && opening.getId() != null) {
            grossDao.delete(opening);
            DebugLog.e("delete opening");
        }
        else {
            opening.setGross(nOpening);
            grossDao.insertOrReplace(opening);
            DebugLog.e("insert or replace opening");
        }
        if (nTotal == 0 && total.getId() != null) {
            grossDao.delete(total);
            DebugLog.e("delete total");
        }
        else {
            total.setGross(nTotal);
            grossDao.insertOrReplace(total);
            DebugLog.e("insert or replace total");
        }
        grossDao.detachAll();
        // 修改统计表
        new StatModel().statisticMovieInstant(movie);

        if (onDataChangedListener != null) {
            onDataChangedListener.onDataChanged();
        }
        dismissAllowingStateLoss();
    }

    private void initOpening() {
        GrossDao grossDao = MApplication.getInstance().getDaoSession().getGrossDao();
        opening = grossDao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(movie.getId()))
                .where(GrossDao.Properties.Region.eq(region.ordinal()))
                .where(GrossDao.Properties.IsTotal.eq(AppConstants.GROSS_IS_OPENING))
                .build().unique();
        if (opening == null) {
            opening = new Gross();
            opening.setMovieId(movie.getId());
            opening.setRegion(region.ordinal());
            opening.setIsTotal(AppConstants.GROSS_IS_OPENING);
            mBinding.etOpen.setText("0");
        }
        else {
            mBinding.etOpen.setText(String.valueOf(opening.getGross()));
        }
    }

    private void initTotal() {
        GrossDao grossDao = MApplication.getInstance().getDaoSession().getGrossDao();
        total = grossDao.queryBuilder()
                .where(GrossDao.Properties.MovieId.eq(movie.getId()))
                .where(GrossDao.Properties.Region.eq(region.ordinal()))
                .where(GrossDao.Properties.IsTotal.eq(AppConstants.GROSS_IS_TOTAL))
                .build().unique();
        if (total == null) {
            total = new Gross();
            total.setMovieId(movie.getId());
            total.setRegion(region.ordinal());
            total.setIsTotal(AppConstants.GROSS_IS_TOTAL);
            mBinding.etTotal.setText("0");
        }
        else {
            mBinding.etTotal.setText(String.valueOf(total.getGross()));
        }
    }

    public interface OnDataChangedListener {
        void onDataChanged();
    }
}
