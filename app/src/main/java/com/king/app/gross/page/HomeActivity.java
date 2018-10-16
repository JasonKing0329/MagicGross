package com.king.app.gross.page;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;

import com.king.app.gross.R;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.base.MvvmActivity;
import com.king.app.gross.databinding.ActivityHomeBinding;
import com.king.app.gross.page.gross.RankFragment;
import com.king.app.gross.view.dialog.DraggableDialogFragment;
import com.king.app.gross.view.dialog.content.LoadFromFragment;
import com.king.app.gross.viewmodel.HomeViewModel;

/**
 * @desc
 * @auth 景阳
 * @time 2018/6/10 0010 22:16
 */

public class HomeActivity extends MvvmActivity<ActivityHomeBinding, HomeViewModel> {

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
                case R.id.menu_load_from:
                    showLoadFrom();
                    break;
                case R.id.menu_save:
                    mModel.saveDatabase();
                    break;
            }
        });
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
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.group_ft, new RankFragment(), "RankFragment")
                .commit();
    }

    private void startMovies() {
        Intent intent = new Intent(this, MovieListActivity.class);
        startActivity(intent);
    }
}
