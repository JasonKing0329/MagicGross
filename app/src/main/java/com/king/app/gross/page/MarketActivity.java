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
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.page.adapter.MarketGrossAdapter;
import com.king.app.gross.page.adapter.MarketGroupAdapter;
import com.king.app.gross.utils.ScreenUtils;
import com.king.app.gross.view.dialog.DraggableDialogFragment;
import com.king.app.gross.view.dialog.content.EditMarketGrossFragment;
import com.king.app.gross.viewmodel.MojoViewModel;

public class MarketActivity extends MvvmActivity<ActivityMovieMarketBinding, MojoViewModel> {

    public static final String EXTRA_MOVIE_ID = "movie_id";

    private final int REQUEST_EDIT = 1201;

    private MarketGrossAdapter adapter;
    private MarketGroupAdapter groupAdapter;
    private DraggableDialogFragment editDialog;

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
                adapter.setOnItemClickListener((view, position, data) -> editMarketGross(position, data));
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

    private void editMarketGross(int position, MarketGross gross) {
        EditMarketGrossFragment content = new EditMarketGrossFragment();
        content.setMarketGross(gross);
        content.setOnUpdateListener(marketGross -> {
            mModel.updateMarketGross(gross);
            adapter.notifyItemChanged(position);
        });
        editDialog = new DraggableDialogFragment.Builder()
                .setTitle(gross.getMarket().getName())
                .setMaxHeight(ScreenUtils.getScreenHeight() * 3 / 5)
                .setShowDelete(true)
                .setOnDeleteListener(view -> {
                    showConfirmCancelMessage("Are you sure to delete?"
                            , (dialogInterface, i) -> {
                                editDialog.dismissAllowingStateLoss();
                                mModel.removeItem(position);
                                adapter.notifyDataSetChanged();
                            }
                            , null);
                })
                .setContentFragment(content)
                .build();
        editDialog.show(getSupportFragmentManager(), "EditGross");
    }

    private void popupLongClickItem(View view, int position, MarketGross data) {
        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenuInflater().inflate(R.menu.popup_market_context, menu.getMenu());
        menu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.menu_market_delete:
                    break;
            }
            return false;
        });
        menu.show();
    }

}
