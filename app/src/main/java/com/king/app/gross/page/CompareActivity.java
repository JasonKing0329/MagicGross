package com.king.app.gross.page;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.MvvmActivity;
import com.king.app.gross.databinding.ActivityCompareBinding;
import com.king.app.gross.model.compare.CompareInstance;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.page.adapter.CompareItemAdapter;
import com.king.app.gross.page.adapter.CompareMovieAdapter;
import com.king.app.gross.utils.ListUtil;
import com.king.app.gross.view.dialog.AlertDialogFragment;
import com.king.app.gross.viewmodel.CompareViewModel;
import com.king.app.gross.viewmodel.bean.CompareItem;

import java.util.List;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/13 10:07
 */
public class CompareActivity extends MvvmActivity<ActivityCompareBinding, CompareViewModel> {

    private CompareMovieAdapter movieAdapter;

    private CompareItemAdapter itemAdapter;

    @Override
    protected int getContentView() {
        return R.layout.activity_compare;
    }

    @Override
    protected void initView() {

        mBinding.setModel(mModel);

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mBinding.rvCompare.setLayoutManager(manager);

        mBinding.rvCompare.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.top = 1;
            }
        });

        mBinding.rvMovies.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                if (parent.getChildAdapterPosition(view) > 0) {
                    outRect.left = 1;
                }
            }
        });

        mBinding.tvRegion.setOnClickListener(view -> selectRegion());
    }

    private void selectRegion() {
        new AlertDialogFragment()
            .setItems(getResources().getStringArray(R.array.region), (dialog, which) -> mModel.changeRegion(which))
            .show(getSupportFragmentManager(), "AlertDialogFragment");
    }

    @Override
    protected CompareViewModel createViewModel() {
        return ViewModelProviders.of(this).get(CompareViewModel.class);
    }

    @Override
    protected void initData() {
        if (!ListUtil.isEmpty(CompareInstance.getInstance().getMovieList())) {
            GridLayoutManager manager = new GridLayoutManager(this, CompareInstance.getInstance().getMovieList().size());
            mBinding.rvMovies.setLayoutManager(manager);

            movieAdapter = new CompareMovieAdapter();
            movieAdapter.setOnItemClickListener((view, position, data) -> showMoviePage(data));
            movieAdapter.setList(CompareInstance.getInstance().getMovieList());
            mBinding.rvMovies.setAdapter(movieAdapter);

            mModel.loadCompareItems();
        }

        mModel.compareItemsObserver.observe(this, compareItems -> showCompareItems(compareItems));
    }

    private void showMoviePage(Movie data) {
        Intent intent = new Intent().setClass(this, MovieGrossActivity.class);
        intent.putExtra(MovieGrossActivity.EXTRA_MOVIE_ID, data.getId());
        startActivity(intent);
    }

    private void showCompareItems(List<CompareItem> compareItems) {
        if (itemAdapter == null) {
            itemAdapter = new CompareItemAdapter();
            itemAdapter.setList(compareItems);
            mBinding.rvCompare.setAdapter(itemAdapter);
        }
        else {
            itemAdapter.setList(compareItems);
            itemAdapter.notifyDataSetChanged();
        }
    }
}
