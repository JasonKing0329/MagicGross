package com.king.app.gross.page;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.widget.LinearLayoutManager;

import com.king.app.gross.R;
import com.king.app.gross.base.MvvmActivity;
import com.king.app.gross.databinding.ActivityMarketRankBinding;
import com.king.app.gross.model.entity.Market;
import com.king.app.gross.model.entity.MarketGross;
import com.king.app.gross.page.adapter.MarketRankAdapter;
import com.king.app.gross.page.adapter.MarketTextAdapter;
import com.king.app.gross.utils.ScreenUtils;
import com.king.app.gross.view.dialog.DraggableDialogFragment;
import com.king.app.gross.view.dialog.content.EditMarketGrossFragment;
import com.king.app.gross.viewmodel.MarketRankViewModel;

public class MarketRankActivity extends MvvmActivity<ActivityMarketRankBinding, MarketRankViewModel> {

    private MarketTextAdapter marketAdapter;
    private MarketRankAdapter rankAdapter;

    private DraggableDialogFragment editDialog;

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
                marketAdapter.setOnClickMarketListener(new MarketTextAdapter.OnClickMarketListener() {
                    @Override
                    public void onClickMarket(int position, Market market) {
                        mModel.loadMarketRank(market);
                    }

                    @Override
                    public void onClickContinent(int position, String continent) {
                        mModel.loadContinentRank(continent);
                    }
                });
                mBinding.rvMarket.setAdapter(marketAdapter);
            }
            else {
                marketAdapter.setList(list);
                marketAdapter.notifyDataSetChanged();
            }
        });

        mModel.rankObserver.observe(this, list -> {
            if (rankAdapter == null) {
                rankAdapter = new MarketRankAdapter();
                rankAdapter.setList(list);
                rankAdapter.setOnItemClickListener((view, position, data) -> {
                    if (marketAdapter.isMarketSelected()) {
                        editMarketGross(position, data.getData());
                    }
                });
                mBinding.rvMovie.setAdapter(rankAdapter);
            }
            else {
                rankAdapter.setList(list);
                rankAdapter.notifyDataSetChanged();
            }
        });

        mModel.loadMarkets();
    }

    private void editMarketGross(int position, MarketGross gross) {
        EditMarketGrossFragment content = new EditMarketGrossFragment();
        content.setMarketGross(gross);
        content.setOnUpdateListener(marketGross -> {
            mModel.updateMarketGross(position);
            rankAdapter.notifyItemChanged(position);
        });
        editDialog = new DraggableDialogFragment.Builder()
                .setTitle(gross.getMarket().getName())
                .setMaxHeight(ScreenUtils.getScreenHeight() * 3 / 5)
                .setShowDelete(true)
                .setOnDeleteListener(view -> {
                    showConfirmCancelMessage("Are you sure to delete?"
                            , (dialogInterface, i) -> {
                                editDialog.dismissAllowingStateLoss();
                                mModel.deleteMarketGross(position);
                                rankAdapter.notifyDataSetChanged();
                            }
                            , null);
                })
                .setContentFragment(content)
                .build();
        editDialog.show(getSupportFragmentManager(), "EditGross");
    }

}
