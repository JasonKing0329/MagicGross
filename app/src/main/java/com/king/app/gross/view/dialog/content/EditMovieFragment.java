package com.king.app.gross.view.dialog.content;

import android.arch.lifecycle.ViewModelProviders;
import android.text.TextUtils;
import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.IFragmentHolder;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConfig;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.databinding.FragmentEditMovieBinding;
import com.king.app.gross.model.StatCreator;
import com.king.app.gross.model.entity.GrossStat;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.setting.SettingProperty;
import com.king.app.gross.view.dialog.DatePickerFragment;
import com.king.app.gross.viewmodel.EditMovieViewModel;

import java.io.File;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 16:16
 */
public class EditMovieFragment extends DraggableContentFragment<FragmentEditMovieBinding> {

    private Movie mEditMovie;

    private String mDebutDate;

    protected OnConfirmListener onConfirmListener;

    private EditMovieViewModel mModel;

    public void setOnConfirmListener(OnConfirmListener onConfirmListener) {
        this.onConfirmListener = onConfirmListener;
    }

    @Override
    protected void bindFragmentHolder(IFragmentHolder holder) {

    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.fragment_edit_movie;
    }

    @Override
    protected void initView() {
        mModel = ViewModelProviders.of(this).get(EditMovieViewModel.class);
        mModel.loadingObserver.observe(this, show -> {
            if (show) {
                showProgress("loading...");
            }
            else {
                dismissProgress();
            }
        });
        mModel.messageObserver.observe(this, msg -> showMessageShort(msg));

        mBinding.tvOk.setOnClickListener(view -> updateMovie());
        if (mEditMovie != null) {
            mBinding.etName.setText(mEditMovie.getName());
            mBinding.etNameSub.setText(mEditMovie.getSubName());
            mBinding.etNameChn.setText(mEditMovie.getNameChn());
            mBinding.etNameChnSub.setText(mEditMovie.getSubChnName());
            mBinding.etBudget.setText(String.valueOf(mEditMovie.getBudget()));
            mBinding.etExchange.setText(String.valueOf(mEditMovie.getUsToYuan()));
            mBinding.etMojo.setText(mEditMovie.getMojoId());
            mDebutDate = mEditMovie.getDebut();
            mBinding.btnDebut.setText(mEditMovie.getDebut());
            mBinding.cbIsReal.setChecked(mEditMovie.getIsReal() == AppConstants.MOVIE_REAL);
        }
        mBinding.btnDebut.setOnClickListener(view -> selectDate());

        if (!SettingProperty.isEnableVirtualMovie()) {
            mBinding.cbIsReal.setChecked(true);
            mBinding.cbIsReal.setVisibility(View.GONE);
        }

        mBinding.ivDownload.setOnClickListener(e -> mModel.fetchMovie(mBinding.etMojo.getText().toString()));
        mModel.mojoMovie.observe(this, movie -> {
            if (!TextUtils.isEmpty(movie.getName())) {
                mBinding.etName.setText(movie.getName().trim());
            }
            if (!TextUtils.isEmpty(movie.getSubName())) {
                mBinding.etNameSub.setText(movie.getSubName().trim());
            }
            mBinding.etBudget.setText(String.valueOf(movie.getBudget()));
            mDebutDate = movie.getDebut();
            mBinding.btnDebut.setText(movie.getDebut());
        });
    }

    private void selectDate() {
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setDate(mDebutDate);
        fragment.setOnDateSetListener((view, year, month, dayOfMonth) -> {
            StringBuffer buffer = new StringBuffer();
            buffer.append(year).append("-");
            month = month + 1;
            if (month < 10) {
                buffer.append("0").append(month);
            }
            else {
                buffer.append(month);
            }
            buffer.append("-");
            if (dayOfMonth < 10) {
                buffer.append("0").append(dayOfMonth);
            }
            else {
                buffer.append(dayOfMonth);
            }
            mDebutDate = buffer.toString();
            mBinding.btnDebut.setText(mDebutDate);
        });
        fragment.show(getChildFragmentManager(), "DatePickerFragment");
    }

    public void setEditMovie(Movie mEditMovie) {
        this.mEditMovie = mEditMovie;
    }

    private void updateMovie() {
        String name = mBinding.etName.getText().toString();
        String nameChn = mBinding.etNameChn.getText().toString();
        if (TextUtils.isEmpty(name.trim()) && TextUtils.isEmpty(nameChn.trim())) {
            showMessageShort("英文名与中文名不能同时为空");
            return;
        }
        double exchange;
        try {
            exchange = Double.parseDouble(mBinding.etExchange.getText().toString());
        } catch (Exception e) {
            showMessageShort("Wrong exchange");
            return;
        }
        if (exchange == 0) {
            showMessageShort("Wrong exchange");
            return;
        }
        if (TextUtils.isEmpty(mDebutDate)) {
            showMessageShort("Please select date");
            return;
        }

        long budget;
        try {
            budget = Long.parseLong(mBinding.etBudget.getText().toString());
        } catch (Exception e) {
            budget = 0;
        }

        if (mEditMovie == null) {
            mEditMovie = new Movie();
        }
        mEditMovie.setName(name);
        mEditMovie.setNameChn(nameChn);
        mEditMovie.setSubName(mBinding.etNameSub.getText().toString());
        mEditMovie.setSubChnName(mBinding.etNameChnSub.getText().toString());
        mEditMovie.setUsToYuan(exchange);
        mEditMovie.setIsReal(mBinding.cbIsReal.isChecked() ? AppConstants.MOVIE_REAL:AppConstants.MOVIE_VIRTUAL);
        mEditMovie.setDebut(mDebutDate);
        mEditMovie.setBudget(budget);
        mEditMovie.setYear(Integer.parseInt(mDebutDate.substring(0, 4)));
        mEditMovie.setMojoId(mBinding.etMojo.getText().toString());

        createImageFolder();
        boolean isInsert = mEditMovie.getId() == null;
        MApplication.getInstance().getDaoSession().getMovieDao().insertOrReplace(mEditMovie);

        if (isInsert) {
            GrossStat stat = new GrossStat();
            stat.setMovieId(mEditMovie.getId());
            MApplication.getInstance().getDaoSession().getGrossStatDao().insert(stat);
        }
        else {
            // 可能修改了汇率，重新统计
            new StatCreator().statisticMovieInstant(mEditMovie);
        }

        if (onConfirmListener == null) {
            dismiss();
        }
        else {
            if (isInsert) {
                if (onConfirmListener.onMovieInserted(mEditMovie)) {
                    dismiss();
                }
            }
            else {
                if (onConfirmListener.onMovieUpdated(mEditMovie)) {
                    dismiss();
                }
            }
        }
    }

    private void createImageFolder() {
        String folder = AppConfig.IMG_MOVIE + "/" + mEditMovie.getName();
        if (!TextUtils.isEmpty(mEditMovie.getSubName())) {
            folder = folder + "_" + mEditMovie.getSubName();
        }
        File file = new File(folder);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public interface OnConfirmListener {
        boolean onMovieInserted(Movie movie);
        boolean onMovieUpdated(Movie movie);
    }
}
