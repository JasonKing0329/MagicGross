package com.king.app.gross.page.gross;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.IFragmentHolder;
import com.king.app.gross.base.MvvmFragment;
import com.king.app.gross.conf.RankType;
import com.king.app.gross.conf.Region;
import com.king.app.gross.databinding.FragmentRankBinding;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.page.EditMarketGrossActivity;
import com.king.app.gross.page.MarketRankActivity;
import com.king.app.gross.page.MovieGrossActivity;
import com.king.app.gross.page.adapter.RankItemAdapter;
import com.king.app.gross.page.adapter.TagRegionAdapter;
import com.king.app.gross.viewmodel.RankViewModel;

/**
 * @desc
 * @auth 景阳
 * @time 2018/6/10 0010 21:31
 */

public class RankFragment extends MvvmFragment<FragmentRankBinding, RankViewModel> {

    private RankItemAdapter rankItemAdapter;

    @Override
    protected void bindFragmentHolder(IFragmentHolder holder) {

    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.fragment_rank;
    }

    @Override
    protected RankViewModel createViewModel() {
        return ViewModelProviders.of(this).get(RankViewModel.class);
    }

    @Override
    protected void onCreate(View view) {
        mBinding.setModel(mModel);

        mBinding.rvRegion.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mBinding.rvType.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mBinding.rvMovies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    protected void onCreateData() {
        mModel.regionTagsObserver.observe(this, regionTags -> {
            TagRegionAdapter adapter = new TagRegionAdapter();
            adapter.setList(regionTags);
            adapter.setOnItemClickListener((view, position, data) -> {
                Region region = (Region) data.getBean();
                if (region == Region.MARKET) {
                    showMarketPage();
                }
                else {
                    mModel.changeRegion((Region) data.getBean());
                }
            });
            mBinding.rvRegion.setAdapter(adapter);
        });
        mModel.typeTagsObserver.observe(this, typeTags -> {
            TagRegionAdapter adapter = new TagRegionAdapter();
            adapter.setList(typeTags);
            adapter.setOnItemClickListener((view, position, data) -> mModel.changeRankType((RankType) data.getBean()));
            mBinding.rvType.setAdapter(adapter);
        });
        mModel.itemsObserver.observe(this, items -> {
            if (rankItemAdapter == null) {
                rankItemAdapter = new RankItemAdapter();
                rankItemAdapter.setList(items);
                rankItemAdapter.setOnItemClickListener((view, position, data) -> onClickMovie(data.getMovie()));
                mBinding.rvMovies.setAdapter(rankItemAdapter);
            }
            else {
                rankItemAdapter.setList(items);
                rankItemAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showMarketPage() {
        Intent intent = new Intent().setClass(getActivity(), MarketRankActivity.class);
        startActivity(intent);
    }

    private void onClickMovie(Movie movie) {
        Intent intent = new Intent().setClass(getContext(), MovieGrossActivity.class);
        intent.putExtra(MovieGrossActivity.EXTRA_MOVIE_ID, movie.getId());
        startActivity(intent);
    }
}
