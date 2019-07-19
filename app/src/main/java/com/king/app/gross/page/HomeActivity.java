package com.king.app.gross.page;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.king.app.gross.R;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.base.MvvmActivity;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.databinding.ActivityHomeBinding;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.single.DateRangeInstance;
import com.king.app.gross.page.gross.RankFragment;
import com.king.app.gross.utils.ScreenUtils;
import com.king.app.gross.view.dialog.DraggableDialogFragment;
import com.king.app.gross.view.dialog.content.EditMovieFragment;
import com.king.app.gross.view.dialog.content.LoadFromFragment;
import com.king.app.gross.view.dialog.content.RankDateRangeFragment;
import com.king.app.gross.viewmodel.HomeViewModel;

/**
 * @desc
 * @auth 景阳
 * @time 2018/6/10 0010 22:16
 */

public class HomeActivity extends MvvmActivity<ActivityHomeBinding, HomeViewModel> {

    private DraggableDialogFragment dateRangeDialog;

    private final int REQUEST_SETTING = 34920;

    @Override
    protected int getContentView() {
        return R.layout.activity_home;
    }

    @Override
    protected void initView() {
        mBinding.actionbar.setOnMenuItemListener(menuId -> {
            switch (menuId) {
                case R.id.menu_movies:
                    startMovies();
                    break;
                case R.id.menu_date:
                    setDateRange();
                    break;
                case R.id.menu_load_from:
                    showLoadFrom();
                    break;
                case R.id.menu_save:
                    mModel.saveDatabase();
                    break;
                case R.id.menu_setting:
                    startActivityForResult(new Intent(HomeActivity.this, SettingsActivity.class), REQUEST_SETTING);
                    break;
            }
        });

        mModel.onEnableVirtualChanged.observe(this, changed -> {
            if (changed) {
                initData();
            }
        });
    }

    private void setDateRange() {
        RankDateRangeFragment content = new RankDateRangeFragment();
        content.initDate(DateRangeInstance.getInstance().getStartDate(), DateRangeInstance.getInstance().getEndDate());
        content.setOnDateRangeListener((start, end) -> {
            DateRangeInstance.getInstance().setStartDate(start);
            DateRangeInstance.getInstance().setEndDate(end);
            dateRangeDialog.dismissAllowingStateLoss();
            showRankFragment();
        });
        dateRangeDialog = new DraggableDialogFragment.Builder()
                .setTitle("Set Date Range")
                .setMaxHeight(ScreenUtils.getScreenHeight() * 3 / 5)
                .setContentFragment(content)
                .build();
        dateRangeDialog.show(getSupportFragmentManager(), "EditMovie");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_SETTING) {
            mModel.checkVirtualEnable();
        }
    }

    private void showLoadFrom() {
        LoadFromFragment content = new LoadFromFragment();
        content.setOnDatabaseChangedListener(() -> {
            MApplication.getInstance().reCreateGreenDao();
            initData();
        });
        DraggableDialogFragment editDialog = new DraggableDialogFragment.Builder()
                .setTitle("Load from")
                .setShowDelete(false)
                .setContentFragment(content)
                .build();
        editDialog.show(getSupportFragmentManager(), "LoadFromFragment");
    }

    @Override
    protected HomeViewModel createViewModel() {
        return ViewModelProviders.of(this).get(HomeViewModel.class);
    }

    @Override
    protected void initData() {
        mModel.openMarketRankPage.observe(this, show -> showMarketPage());
        showRankFragment();
    }

    private void showMarketPage() {
        Intent intent = new Intent().setClass(this, MarketRankActivity.class);
        startActivity(intent);
    }

    private void showRankFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.group_ft, RankFragment.newInstance(), "RankFragment")
                .commit();
    }

    private void startMovies() {
        Intent intent = new Intent(this, MovieListActivity.class);
        startActivity(intent);
    }
}
