package com.king.app.gross.page;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;

import com.king.app.gross.R;
import com.king.app.gross.base.MvvmActivity;
import com.king.app.gross.databinding.ActivityMarketPageBinding;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.page.adapter.MarketPageAdapter;
import com.king.app.gross.viewmodel.MarketPageViewModel;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/5/30 11:00
 */
public class MarketPageActivity extends MvvmActivity<ActivityMarketPageBinding, MarketPageViewModel> {

    public static final String EXTRA_MARKET_ID = "market_id";

    private MarketPageAdapter adapter;

    @Override
    protected int getContentView() {
        return R.layout.activity_market_page;
    }

    /**
     * 不设置statusbar背景，因为该页面运用了系统statusbar浮于head图片之上的效果
     * @return
     */
    @Override
    protected void updateStatusBarColor() {
        // 覆盖父类实现
    }

    @Override
    protected void initView() {
        mBinding.setModel(mModel);

        mBinding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mBinding.rvMovie.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    protected MarketPageViewModel createViewModel() {
        return ViewModelProviders.of(this).get(MarketPageViewModel.class);
    }

    private long getMarketId() {
        return getIntent().getLongExtra(EXTRA_MARKET_ID, -1);
    }

    @Override
    protected void initData() {
        mModel.moviesObserver.observe(this, list -> {
            adapter = new MarketPageAdapter();
            adapter.setList(list);
            adapter.setOnItemClickListener((view, position, data) -> showMovie(data.getMovie()));
            mBinding.rvMovie.setAdapter(adapter);
        });
        mModel.loadMarket(getMarketId());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        mModel.loadMarket(getMarketId());
    }

    private void showMovie(Movie data) {
        Intent intent = new Intent().setClass(this, MovieActivity.class);
        intent.putExtra(MovieActivity.EXTRA_MOVIE_ID, data.getId());
        startActivity(intent);
    }

}
