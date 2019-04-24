package com.king.app.gross.page;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.widget.LinearLayoutManager;

import com.king.app.gross.R;
import com.king.app.gross.base.MvvmActivity;
import com.king.app.gross.databinding.ActivityMarketRankBinding;
import com.king.app.gross.page.adapter.MarketRandAdapter;
import com.king.app.gross.page.adapter.MarketTextAdapter;
import com.king.app.gross.viewmodel.MarketRankViewModel;

public class MarketRankActivity extends MvvmActivity<ActivityMarketRankBinding, MarketRankViewModel> {

    private MarketTextAdapter marketAdapter;
    private MarketRandAdapter rankAdapter;

    @Override
    protected int getContentView() {
        return R.layout.activity_market_rank;
    }

    @Override
    protected void initView() {
        mBinding.rvMarket.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mBinding.rvMovie.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    protected MarketRankViewModel createViewModel() {
        return ViewModelProviders.of(this).get(MarketRankViewModel.class);
    }

    @Override
    protected void initData() {

        mModel.marketsObserver.observe(this, list -> {
            if (marketAdapter == null) {
                marketAdapter = new MarketTextAdapter();
                marketAdapter.setList(list);
                marketAdapter.setOnItemClickListener((view, position, data) -> mModel.loadMarketRank(data));
                mBinding.rvMarket.setAdapter(marketAdapter);
            }
            else {
                marketAdapter.setList(list);
                marketAdapter.notifyDataSetChanged();
            }
        });

        mModel.rankObserver.observe(this, list -> {
            if (rankAdapter == null) {
                rankAdapter = new MarketRandAdapter();
                rankAdapter.setList(list);
                mBinding.rvMovie.setAdapter(rankAdapter);
            }
            else {
                rankAdapter.setList(list);
                rankAdapter.notifyDataSetChanged();
            }
        });

        mModel.loadMarkets();
    }
}
