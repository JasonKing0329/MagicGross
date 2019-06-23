package com.king.app.gross.page;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;

import com.king.app.gross.R;
import com.king.app.gross.base.MvvmActivity;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.Region;
import com.king.app.gross.databinding.ActivityMovieBinding;
import com.king.app.gross.model.entity.Market;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.page.adapter.MovieMarketsAdapter;
import com.king.app.gross.utils.ScreenUtils;
import com.king.app.gross.view.dialog.DraggableDialogFragment;
import com.king.app.gross.view.dialog.content.EditMovieFragment;
import com.king.app.gross.view.dialog.content.EditTotalFragment;
import com.king.app.gross.view.dialog.content.ParseMojoFragment;
import com.king.app.gross.viewmodel.MovieViewModel;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/5/27 9:42
 */
public class MovieActivity extends MvvmActivity<ActivityMovieBinding, MovieViewModel> {

    public static final String EXTRA_MOVIE_ID = "movie_id";

    private final int REQUEST_MARKET = 1231;
    private final int REQUEST_DAILY = 1232;

    private MovieMarketsAdapter marketAdapter;

    @Override
    protected int getContentView() {
        return R.layout.activity_movie;
    }

    @Override
    protected MovieViewModel createViewModel() {
        return ViewModelProviders.of(this).get(MovieViewModel.class);
    }

    @Override
    protected void initView() {
        mBinding.setModel(mModel);

        mBinding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        mBinding.rvMarkets.setLayoutManager(layoutManager);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return marketAdapter.getSpanSize(position);
            }
        });
    }

        /**
         * 不设置statusbar背景，因为该页面运用了系统statusbar浮于head图片之上的效果
         * @return
         */
    @Override
    protected void updateStatusBarColor() {
        // 覆盖父类实现
    }

    private boolean isRealMovie() {
        return mModel.movieObserver.getValue().getIsReal() == AppConstants.MOVIE_REAL;
    }

    @Override
    protected void initData() {

        mModel.pageDataObserver.observe(this, list -> {
            if (marketAdapter == null) {
                marketAdapter = new MovieMarketsAdapter();
                marketAdapter.setList(list);
                marketAdapter.setOnClickItemListener((view, position, item) -> {
                    showMarketPage(item.getMarket());
                });
                marketAdapter.setOnBasicDataListener(new MovieMarketsAdapter.OnBasicDataListener() {
                    @Override
                    public void onClickEdit() {
                        editMovie(mModel.movieObserver.getValue());
                    }

                    @Override
                    public void onClickMojoId() {
                        if (isRealMovie()) {
                            parseMojo();
                        }
                    }

                    @Override
                    public void onClickGrossNa() {
                        showDailyPage(mModel.movieObserver.getValue(), Region.NA);
                    }

                    @Override
                    public void onClickGrossChn() {
                        showDailyPage(mModel.movieObserver.getValue(), Region.CHN);
                    }

                    @Override
                    public void onClickGrossOversea() {
                        if (isRealMovie()) {
                            editTotal(mModel.movieObserver.getValue(), Region.OVERSEA);
                        }
                    }

                    @Override
                    public void onClickGrossWorld() {
                        if (isRealMovie()) {
                            editTotal(mModel.movieObserver.getValue(), Region.WORLDWIDE);
                        }
                    }

                    @Override
                    public void onClickGrossMarket() {
                        showMarketPage();
                    }
                });

                mBinding.rvMarkets.setAdapter(marketAdapter);
            }
            else {
                marketAdapter.setList(list);
                marketAdapter.notifyDataSetChanged();
            }
        });

        mModel.loadMovie(getMovieId());
    }

    private void showMarketPage(Market market) {
        Intent intent = new Intent(this, MarketPageActivity.class);
        intent.putExtra(MarketPageActivity.EXTRA_MARKET_ID, market.getId());
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        mModel.loadMovie(getMovieId());
    }

    private long getMovieId() {
        return getIntent().getLongExtra(EXTRA_MOVIE_ID, -1);
    }

    private void editMovie(Movie movie) {
        final double oldExchange = movie.getUsToYuan();
        EditMovieFragment content = new EditMovieFragment();
        content.setEditMovie(movie);
        content.setOnConfirmListener(new EditMovieFragment.OnConfirmListener() {
            @Override
            public boolean onMovieInserted(Movie movie) {
                return true;
            }

            @Override
            public boolean onMovieUpdated(Movie movie) {
                if (movie.getUsToYuan() != oldExchange && movie.getIsReal() == AppConstants.MOVIE_VIRTUAL) {
                    mModel.statVirtualChn();
                }
                else {
                    mModel.loadMovie(getMovieId());
                }
                return true;
            }
        });
        DraggableDialogFragment dialog = new DraggableDialogFragment.Builder()
                .setTitle(movie == null ? "New movie":"Edit movie")
                .setMaxHeight(ScreenUtils.getScreenHeight() * 4 / 5)
                .setContentFragment(content)
                .build();
        dialog.show(getSupportFragmentManager(), "EditMovie");
    }

    private void showMarketPage() {
        Intent intent = new Intent().setClass(this, MarketActivity.class);
        intent.putExtra(MarketActivity.EXTRA_MOVIE_ID, getMovieId());
        startActivityForResult(intent, REQUEST_MARKET);
    }

    private void parseMojo() {
        ParseMojoFragment content = new ParseMojoFragment();
        content.setMovie(mModel.movieObserver.getValue());
        content.setOnDailyDataChangedListener(() -> mModel.loadMovie(getMovieId()));
        content.setOnTotalDataChangedListener(() -> mModel.loadMovie(getMovieId()));
        DraggableDialogFragment editDialog = new DraggableDialogFragment.Builder()
                .setTitle("Gross")
                .setContentFragment(content)
                .build();
        editDialog.show(getSupportFragmentManager(), "EditGross");
    }

    private void editTotal(Movie movie, Region region) {
        EditTotalFragment content = new EditTotalFragment();
        content.setMovie(movie);
        content.setRegion(region);
        content.setOnDataChangedListener(() -> mModel.loadMovie(getMovieId()));
        DraggableDialogFragment dialog = new DraggableDialogFragment.Builder()
                .setTitle("Edit")
                .setContentFragment(content)
                .build();
        dialog.show(getSupportFragmentManager(), "EditTotalFragment");
    }

    private void showDailyPage(Movie data, Region region) {
        Intent intent = new Intent().setClass(this, MovieGrossActivity.class);
        intent.putExtra(MovieGrossActivity.EXTRA_MOVIE_ID, data.getId());
        intent.putExtra(MovieGrossActivity.EXTRA_MOVIE_REGION, region.ordinal());
        startActivityForResult(intent, REQUEST_DAILY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_MARKET) {
            mModel.loadMovie(getMovieId());
        }
        else if (requestCode == REQUEST_DAILY) {
            mModel.loadMovie(getMovieId());
        }
    }
}
