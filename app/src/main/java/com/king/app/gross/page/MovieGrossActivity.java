package com.king.app.gross.page;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.PopupMenu;

import com.king.app.gross.R;
import com.king.app.gross.base.MvvmActivity;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.GrossDateType;
import com.king.app.gross.conf.Region;
import com.king.app.gross.databinding.ActivityMovieGrossBinding;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.page.gross.GrossTabFragment;
import com.king.app.gross.view.dialog.DraggableDialogFragment;
import com.king.app.gross.view.dialog.content.EditGrossFragment;
import com.king.app.gross.view.dialog.content.ParseMojoFragment;
import com.king.app.gross.viewmodel.MovieGrossViewModel;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/7 15:15
 */
public class MovieGrossActivity extends MvvmActivity<ActivityMovieGrossBinding, MovieGrossViewModel> {

    public static final String EXTRA_MOVIE_ID = "movie_id";

    private DraggableDialogFragment editDialog;

    private GrossTabFragment ftTab;

    @Override
    protected int getContentView() {
        return R.layout.activity_movie_gross;
    }

    @Override
    protected void initView() {
        mBinding.actionbar.setOnBackListener(() -> onBackPressed());
        mBinding.actionbar.setOnMenuItemListener(menuId -> {
            switch (menuId) {
                case R.id.menu_add:
                    editGross(null);
                    break;
                case R.id.menu_chart:
                    ftTab.toggleChart();
                    break;
                case R.id.menu_market:
                    showMarketPage();
                    break;
                case R.id.menu_fetch:
                    if (mModel.getMovie().getIsReal() == AppConstants.MOVIE_VIRTUAL) {
                        showMessageShort("Virtual movie doesn't have Mojo data");
                        return;
                    }
                    if (TextUtils.isEmpty(mModel.getMovie().getMojoId())) {
                        showMessageShort("Mojo id is null");
                        return;
                    }
                    parseMojo();
                    break;
            }
        });
        mBinding.actionbar.registerPopupMenu(R.id.menu_date);
        mBinding.actionbar.setPopupMenuProvider((iconMenuId, anchorView) -> {
            switch (iconMenuId) {
                case R.id.menu_date:
                    return createDateMenu(anchorView);
            }
            return null;
        });
    }

    private PopupMenu createDateMenu(View anchorView) {
        PopupMenu menu = new PopupMenu(this, anchorView);
        menu.getMenuInflater().inflate(R.menu.gross_date, menu.getMenu());
        menu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.menu_date_daily:
                    mModel.setDateType(GrossDateType.DAILY);
                    ftTab.onDateTypeChanged();
                    break;
                case R.id.menu_date_weekend:
                    mModel.setDateType(GrossDateType.WEEKEND);
                    ftTab.onDateTypeChanged();
                    break;
                case R.id.menu_date_weekly:
                    mModel.setDateType(GrossDateType.WEEKLY);
                    ftTab.onDateTypeChanged();
                    break;
            }
            return true;
        });
        return menu;
    }

    @Override
    protected MovieGrossViewModel createViewModel() {
        return ViewModelProviders.of(this).get(MovieGrossViewModel.class);
    }

    @Override
    protected void initData() {
        mModel.titleText.observe(this, s -> mBinding.actionbar.setTitle(s));

        mModel.loadMovie(getMovieId());

        mModel.editObserver.observe(this, gross -> editGross(gross));

        ftTab = new GrossTabFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.group_ft, ftTab, "GrossTabFragment")
                .commit();
    }

    private void parseMojo() {
        ParseMojoFragment content = new ParseMojoFragment();
        content.setMovie(mModel.getMovie());
        content.setOnDailyDataChangedListener(() -> mModel.loadRegion(Region.NA));
        DraggableDialogFragment editDialog = new DraggableDialogFragment.Builder()
                .setTitle("Gross")
                .setContentFragment(content)
                .build();
        editDialog.show(getSupportFragmentManager(), "EditGross");
    }

    private void editGross(Gross gross) {
        EditGrossFragment content = new EditGrossFragment();
        content.setGross(gross);
        content.setMovie(mModel.getMovie());
        content.setOnGrossListener(gross1 -> mModel.onGrossChanged(gross1));
        editDialog = new DraggableDialogFragment.Builder()
                .setTitle("Gross")
                .setShowDelete(gross != null)
                .setOnDeleteListener(view -> warningDelete(gross))
                .setContentFragment(content)
                .build();
        editDialog.show(getSupportFragmentManager(), "EditGross");
    }

    private void warningDelete(Gross gross) {
        showConfirmCancelMessage("Are you sure to delete?"
                , (dialogInterface, i) -> {
                    mModel.deleteGross(gross);
                    editDialog.dismissAllowingStateLoss();
                }
                , null);
    }

    private long getMovieId() {
        return getIntent().getLongExtra(EXTRA_MOVIE_ID, -1);
    }

    private void showMarketPage() {
        Intent intent = new Intent().setClass(this, MarketActivity.class);
        intent.putExtra(MarketActivity.EXTRA_MOVIE_ID, getMovieId());
        startActivity(intent);
    }
}
