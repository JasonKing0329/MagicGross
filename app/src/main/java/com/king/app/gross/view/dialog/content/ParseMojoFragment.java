package com.king.app.gross.view.dialog.content;

import android.arch.lifecycle.ViewModelProviders;

import com.king.app.gross.R;
import com.king.app.gross.base.IFragmentHolder;
import com.king.app.gross.databinding.FragmentMojoBinding;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.viewmodel.ParseMojoViewModel;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/5/14 16:21
 */
public class ParseMojoFragment extends DraggableContentFragment<FragmentMojoBinding> {

    private ParseMojoViewModel mModel;

    private Movie movie;

    private OnDailyDataChangedListener onDailyDataChangedListener;

    private OnTotalDataChangedListener onTotalDataChangedListener;

    @Override
    protected void bindFragmentHolder(IFragmentHolder holder) {

    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.fragment_mojo;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public void setOnDailyDataChangedListener(OnDailyDataChangedListener onDailyDataChangedListener) {
        this.onDailyDataChangedListener = onDailyDataChangedListener;
    }

    public void setOnTotalDataChangedListener(OnTotalDataChangedListener onTotalDataChangedListener) {
        this.onTotalDataChangedListener = onTotalDataChangedListener;
    }

    @Override
    protected void initView() {
        mModel = ViewModelProviders.of(this).get(ParseMojoViewModel.class);
        mModel.loadingObserver.observe(this, show -> {
            if (show) {
                showProgress("loading...");
            }
            else {
                dismissProgress();
            }
        });
        mModel.messageObserver.observe(this, message -> showMessageShort(message));
        mBinding.setModel(mModel);

        mBinding.btnDaily.setOnClickListener(v -> {
            showConfirmCancelMessage("Fetch Mojo data will remove local data, continue?"
                    , (dialogInterface, i) -> mModel.fetchDailyData()
                    , null);
        });
        mBinding.btnMarket.setOnClickListener(v -> {
            showConfirmCancelMessage("Fetch Mojo data will remove local data, continue?"
                    , (dialogInterface, i) -> mModel.fetchMarketData()
                    , null);
        });
        mBinding.btnDefault.setOnClickListener(v -> mModel.fetchDefaultData());
        mBinding.btnParseDomestic.setOnClickListener(v -> mModel.insertLeft());
        mBinding.btnParseTotal.setOnClickListener(v -> mModel.insertTotal());

        mModel.notifyDailyDataChanged.observe(this, success -> {
            if (onDailyDataChangedListener != null) {
                onDailyDataChangedListener.onDailyDataChanged();
            }
        });
        mModel.notifyTotalDataChanged.observe(this, success -> {
            if (onTotalDataChangedListener != null) {
                onTotalDataChangedListener.onTotalDataChanged();
            }
        });
        mModel.insertLeftPopup.observe(this, msg -> {
            showConfirmCancelMessage(msg
                    , (dialogInterface, i) -> mModel.confirmInsertLeft()
                    , null);
        });

        mModel.loadDefaultData(movie);
    }

    public interface OnDailyDataChangedListener {
        void onDailyDataChanged();
    }

    public interface OnTotalDataChangedListener {
        void onTotalDataChanged();
    }
}
