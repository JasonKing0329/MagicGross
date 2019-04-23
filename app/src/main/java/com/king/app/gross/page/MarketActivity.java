package com.king.app.gross.page;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.PopupMenu;

import com.king.app.gross.R;
import com.king.app.gross.base.MvvmActivity;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.databinding.ActivityMovieMarketBinding;
import com.king.app.gross.page.adapter.MarketGrossAdapter;
import com.king.app.gross.page.adapter.MarketGroupAdapter;
import com.king.app.gross.viewmodel.MojoViewModel;

public class MarketActivity extends MvvmActivity<ActivityMovieMarketBinding, MojoViewModel> {

    public static final String EXTRA_MOVIE_ID = "movie_id";

    private final int REQUEST_EDIT = 1201;

    private MarketGrossAdapter adapter;
    private MarketGroupAdapter groupAdapter;

    @Override
    protected int getContentView() {
        return R.layout.activity_movie_market;
    }

    @Override
    protected void initView() {
        mBinding.rvMarkets.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mBinding.actionbar.setOnMenuItemListener(menuId -> {
            switch (menuId) {
                case R.id.menu_download:
                    showConfirmCancelMessage("Fetch html will remove data in database, continue?"
                            , (dialogInterface, i) -> mModel.fetchForeignData()
                            , null);
                    break;
                case R.id.menu_edit:
                    editMarketGross();
                    break;
                case R.id.menu_type:
                    mModel.changeGroup();
                    break;
            }
        });

        mBinding.actionbar.registerPopupMenu(R.id.menu_sort);
        mBinding.actionbar.setPopupMenuProvider((iconMenuId, anchorView) -> {
            switch (iconMenuId) {
                case R.id.menu_sort:
                    return createSortPopup(anchorView);
            }
            return null;
        });
        mBinding.setTotal(mModel.getMarketTotal());
    }

    private void editMarketGross() {
        Intent intent = new Intent().setClass(this, EditMarketGrossActivity.class);
        intent.putExtra(EditMarketGrossActivity.EXTRA_MOVIE_ID, getMovieId());
        startActivityForResult(intent, REQUEST_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_EDIT:
                mModel.loadMovie(getMovieId());
                break;
        }
    }

    private PopupMenu createSortPopup(View anchorView) {
        PopupMenu menu = new PopupMenu(this, anchorView);
        menu.getMenuInflater().inflate(R.menu.market_gross_sort, menu.getMenu());
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_sort_total:
                    mModel.changeSortType(AppConstants.MARKET_GROSS_SORT_TOTAL);
                    break;
                case R.id.menu_sort_opening:
                    mModel.changeSortType(AppConstants.MARKET_GROSS_SORT_OPENING);
                    break;
                case R.id.menu_sort_debut:
                    mModel.changeSortType(AppConstants.MARKET_GROSS_SORT_DEBUT);
                    break;
                default:
                    mModel.changeSortType(AppConstants.MARKET_GROSS_SORT_MARKET);
                    break;
            }
            return true;
        });
        return menu;
    }

    private long getMovieId() {
        return getIntent().getLongExtra(EXTRA_MOVIE_ID, -1);
    }

    @Override
    protected MojoViewModel createViewModel() {
        return ViewModelProviders.of(this).get(MojoViewModel.class);
    }

    @Override
    protected void initData() {
        mModel.movieObserver.observe(this, movie -> {
            if (TextUtils.isEmpty(movie.getSubName())) {
                mBinding.actionbar.setTitle(movie.getName());
            }
            else {
                mBinding.actionbar.setTitle(movie.getName() + ":" + movie.getSubName());
            }
        });
        mModel.grossObserver.observe(this, list -> {
            if (adapter == null || mBinding.rvMarkets.getAdapter() == groupAdapter) {
                adapter = new MarketGrossAdapter();
                adapter.setList(list);
                mBinding.rvMarkets.setAdapter(adapter);
            }
            else {
                adapter.setList(list);
                adapter.notifyDataSetChanged();
            }
        });
        mModel.groupObserver.observe(this, list -> {
            if (groupAdapter == null || mBinding.rvMarkets.getAdapter() == adapter) {
                groupAdapter = new MarketGroupAdapter();
                groupAdapter.setList(list);
                mBinding.rvMarkets.setAdapter(groupAdapter);
            }
            else {
                groupAdapter.setList(list);
                groupAdapter.notifyDataSetChanged();
            }
        });

        mModel.loadMovie(getMovieId());
    }
}
