package com.king.app.gross.page.rating;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;

import com.king.app.gross.R;
import com.king.app.gross.base.MvvmActivity;
import com.king.app.gross.conf.RatingSystem;
import com.king.app.gross.databinding.ActivityRatingPageBinding;
import com.king.app.gross.page.MovieActivity;
import com.king.app.gross.page.bean.RatingMovie;
import com.king.app.gross.utils.ScreenUtils;
import com.king.app.gross.view.dialog.DraggableDialogFragment;
import com.king.app.gross.view.dialog.content.EditRatingFragment;
import com.king.app.jactionbar.OnConfirmListener;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/7/26 10:46
 */
public class RatingPageActivity extends MvvmActivity<ActivityRatingPageBinding, RatingViewModel> {

    public static final String RATING_SYSTEM = "rating_system";

    private RatingMovieAdapter ratedAdapter;
    private RatingRottenMovieAdapter rottenAdapter;
    private UnratedMovieAdapter unRatedAdapter;
    private RatingMovieAdapter unRatedFullAdapter;

    private boolean isUnratedLarge;

    private boolean isEditScore;

    @Override
    protected int getContentView() {
        return R.layout.activity_rating_page;
    }

    @Override
    protected void initView() {

        mBinding.actionbar.setOnMenuItemListener(menuId -> {
            switch (menuId) {
                case R.id.menu_edit:
                    isEditScore = true;
                    mBinding.actionbar.showConfirmStatus(menuId);
                    break;
            }
        });
        mBinding.actionbar.setOnConfirmListener(new OnConfirmListener() {
            @Override
            public boolean disableInstantDismissConfirm() {
                return false;
            }

            @Override
            public boolean disableInstantDismissCancel() {
                return false;
            }

            @Override
            public boolean onConfirm(int actionId) {
                isEditScore = false;
                return true;
            }

            @Override
            public boolean onCancel(int actionId) {
                isEditScore = false;
                return true;
            }
        });

        mBinding.rvMovies.setLayoutManager(new GridLayoutManager(this, 2));
        mBinding.rvUnrated.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        mBinding.ivFull.setOnClickListener(v -> {
            if (isUnratedLarge) {
                shrinkUnratedList();
            }
            else {
                expandUnratedList();
            }
        });
    }

    private void expandUnratedList() {
        isUnratedLarge = true;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mBinding.rvUnrated.getLayoutParams();
        params.height = ScreenUtils.dp2px(360);
        mBinding.rvUnrated.setLayoutParams(params);
        mBinding.rvUnrated.requestLayout();
        mBinding.ivFull.setImageResource(R.drawable.ic_fullscreen_exit_red_a200_36dp);
        mBinding.rvUnrated.setLayoutManager(new GridLayoutManager(this, 2));
        if (unRatedFullAdapter == null) {
            unRatedFullAdapter = new RatingMovieAdapter();
            unRatedFullAdapter.setOnItemClickListener((view, position, data) -> onClickMovie(data));
        }
        unRatedFullAdapter.setList(mModel.unratedMovies.getValue());
        mBinding.rvUnrated.setAdapter(unRatedFullAdapter);
    }

    private void shrinkUnratedList() {
        isUnratedLarge = false;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mBinding.rvUnrated.getLayoutParams();
        params.height = ScreenUtils.dp2px(100);
        mBinding.rvUnrated.setLayoutParams(params);
        mBinding.rvUnrated.requestLayout();
        mBinding.ivFull.setImageResource(R.drawable.ic_fullscreen_red_a200_36dp);
        mBinding.rvUnrated.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        unRatedAdapter.setList(mModel.unratedMovies.getValue());
        mBinding.rvUnrated.setAdapter(unRatedAdapter);
    }

    @Override
    protected RatingViewModel createViewModel() {
        return ViewModelProviders.of(this).get(RatingViewModel.class);
    }

    private long getSystemId() {
        return getIntent().getLongExtra(RATING_SYSTEM, 0);
    }

    @Override
    protected void initData() {
        mBinding.actionbar.setTitle(RatingSystem.getTitle(getSystemId()));

        mModel.ratedMovies.observe(this, list -> {
            if (mModel.isRottenSystem()) {
                if (rottenAdapter == null) {
                    rottenAdapter = new RatingRottenMovieAdapter();
                    rottenAdapter.setList(list);
                    rottenAdapter.setOnItemClickListener((view, position, data) -> onClickMovie(data));
                    mBinding.rvMovies.setAdapter(rottenAdapter);
                }
                else {
                    rottenAdapter.setList(list);
                    rottenAdapter.notifyDataSetChanged();
                }
            }
            else {
                if (ratedAdapter == null) {
                    ratedAdapter = new RatingMovieAdapter();
                    ratedAdapter.setList(list);
                    ratedAdapter.setSystemId(getSystemId());
                    ratedAdapter.setOnItemClickListener((view, position, data) -> onClickMovie(data));
                    mBinding.rvMovies.setAdapter(ratedAdapter);
                }
                else {
                    ratedAdapter.setList(list);
                    ratedAdapter.notifyDataSetChanged();
                }
            }
        });
        mModel.unratedMovies.observe(this, list -> {
            if (unRatedAdapter == null) {
                unRatedAdapter = new UnratedMovieAdapter();
                unRatedAdapter.setList(list);
                unRatedAdapter.setOnItemClickListener((view, position, data) -> onClickMovie(data));
                mBinding.rvUnrated.setAdapter(unRatedAdapter);
            }
            else {
                unRatedAdapter.setList(list);
                unRatedAdapter.notifyDataSetChanged();
            }

            if (unRatedFullAdapter != null) {
                unRatedFullAdapter.setList(list);
                unRatedFullAdapter.notifyDataSetChanged();
            }
        });

        mModel.loadRatings(getSystemId());
    }

    private void onClickMovie(RatingMovie data) {
        if (isEditScore) {
            EditRatingFragment content = new EditRatingFragment();
            content.setRating(data);
            if (mModel.isRottenSystem()) {
                content.setRottenSystem(true);
                content.setOnRateRottenListener((scorePro, personPro, scoreAud, personAud) -> mModel.updateRottenScore(data, scorePro, personPro, scoreAud, personAud));
            }
            else {
                content.setOnRatingListener((score, person) -> mModel.updateScore(data, score, person));
            }
            DraggableDialogFragment dialog = new DraggableDialogFragment.Builder()
                    .setTitle("Update Score")
                    .setContentFragment(content)
                    .build();
            dialog.show(getSupportFragmentManager(), "EditRatingFragment");
        }
        else {
            Intent intent = new Intent().setClass(this, MovieActivity.class);
            intent.putExtra(MovieActivity.EXTRA_MOVIE_ID, data.getMovie().getId());
            startActivity(intent);
        }
    }
}
