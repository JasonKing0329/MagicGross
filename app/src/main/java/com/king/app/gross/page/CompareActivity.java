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
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.Region;
import com.king.app.gross.databinding.ActivityCompareBinding;
import com.king.app.gross.model.compare.CompareChart;
import com.king.app.gross.model.compare.CompareInstance;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.gross.ChartModel;
import com.king.app.gross.model.setting.SettingProperty;
import com.king.app.gross.page.adapter.CompareItemAdapter;
import com.king.app.gross.page.adapter.CompareMovieAdapter;
import com.king.app.gross.utils.ListUtil;
import com.king.app.gross.view.dialog.AlertDialogFragment;
import com.king.app.gross.view.widget.chart.adapter.IAxis;
import com.king.app.gross.view.widget.chart.adapter.LineChartAdapter;
import com.king.app.gross.view.widget.chart.adapter.LineData;
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

        mBinding.ivFull.setOnClickListener(v -> startActivity(new Intent(CompareActivity.this, FullChartActivity.class)));

        mBinding.rvMovies.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                if (parent.getChildAdapterPosition(view) > 0) {
                    outRect.left = 1;
                }
            }
        });

        mBinding.tvRegion.setOnClickListener(view -> selectRegion());

        mBinding.actionbar.setOnBackListener(() -> onBackPressed());
        mBinding.actionbar.setOnMenuItemListener(menuId -> {
            switch (menuId) {
                case R.id.menu_chart:
                    if (mBinding.chart.getVisibility() == View.VISIBLE) {
                        mBinding.chart.setVisibility(View.GONE);
                        mBinding.ivFull.setVisibility(View.GONE);
                    }
                    else {
                        mBinding.chart.setVisibility(View.VISIBLE);
                        mBinding.ivFull.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.menu_change_color:
                    movieAdapter.refreshColors();
                    movieAdapter.notifyDataSetChanged();
                    updateChart(mModel.chartObserver.getValue());
                    break;
                case R.id.menu_type:
                    int type = SettingProperty.getCompareType();
                    if (type == AppConstants.COMPARE_TYPE_ACCU) {
                        SettingProperty.setCompareType(AppConstants.COMPARE_TYPE_DAILY);
                        mBinding.actionbar.updateMenuText(R.id.menu_type, "Accumulated");
                    }
                    else {
                        SettingProperty.setCompareType(AppConstants.COMPARE_TYPE_ACCU);
                        mBinding.actionbar.updateMenuText(R.id.menu_type, "Daily");
                    }
                    mModel.loadCompareItems();
                    break;
            }
        });
        int type = SettingProperty.getCompareType();
        if (type == AppConstants.COMPARE_TYPE_ACCU) {
            mBinding.actionbar.updateMenuText(R.id.menu_type, "Daily");
        }
        else {
            mBinding.actionbar.updateMenuText(R.id.menu_type, "Accumulated");
        }
    }

    private void selectRegion() {
        new AlertDialogFragment()
            .setItems(getResources().getStringArray(R.array.compare_region), (dialog, which) -> {
                switch (which) {
                    case 0:
                        mModel.changeRegion(Region.NA);
                        break;
                    case 1:
                        mModel.changeRegion(Region.CHN);
                        break;
                    case 2:
                        mModel.changeRegion(Region.MARKET);
                        break;
                    case 3:
                        mModel.changeRegion(Region.MARKET_OPEN);
                        break;
                }
            })
            .show(getSupportFragmentManager(), "AlertDialogFragment");
    }

    @Override
    protected CompareViewModel createViewModel() {
        return ViewModelProviders.of(this).get(CompareViewModel.class);
    }

    @Override
    protected void initData() {

        mModel.compareItemsObserver.observe(this, compareItems -> showCompareItems(compareItems));

        mModel.chartObserver.observe(this, data -> {
            mBinding.chart.setVisibility(View.VISIBLE);
            mBinding.ivFull.setVisibility(View.VISIBLE);
            updateChart(data);
        });

        mModel.hideChart.observe(this, data -> {
            mBinding.chart.setVisibility(View.GONE);
            mBinding.ivFull.setVisibility(View.GONE);
        });

        if (!ListUtil.isEmpty(CompareInstance.getInstance().getMovieList())) {
            GridLayoutManager manager = new GridLayoutManager(this, CompareInstance.getInstance().getMovieList().size());
            mBinding.rvMovies.setLayoutManager(manager);

            movieAdapter = new CompareMovieAdapter();
            movieAdapter.setOnItemClickListener((view, position, data) -> showMoviePage(data));
            movieAdapter.setList(CompareInstance.getInstance().getMovieList());
            ChartDataProvider.setMovieList(CompareInstance.getInstance().getMovieList());
            ChartDataProvider.setColorList(movieAdapter.getColorList());
            mBinding.rvMovies.setAdapter(movieAdapter);

            mModel.loadCompareItems();
        }
    }

    private void showMoviePage(Movie data) {
        Intent intent = new Intent().setClass(this, MovieActivity.class);
        intent.putExtra(MovieActivity.EXTRA_MOVIE_ID, data.getId());
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

    private void updateChart(CompareChart data) {
        if (data.getLineDataList() == null) {
            mBinding.chart.setVisibility(View.GONE);
            return;
        }
        ChartDataProvider.setChartData(data);
        ChartDataProvider.setRegion(mModel.getRegion());
        mBinding.chart.setVisibility(View.VISIBLE);
        mBinding.chart.setDrawAxisY(true);
        mBinding.chart.setDegreeCombine(1);
        mBinding.chart.setDrawDashGrid(SettingProperty.getCompareType() == AppConstants.COMPARE_TYPE_ACCU);
        mBinding.chart.setAxisX(new IAxis() {
            @Override
            public int getDegreeCount() {
                return data.getxCount();
            }

            @Override
            public int getTotalWeight() {
                return data.getxCount();
            }

            @Override
            public int getWeightAt(int position) {
                return position;
            }

            @Override
            public String getTextAt(int position) {
                return data.getxTextList().get(position);
            }

            @Override
            public boolean isNotDraw(int position) {
                return false;
            }
        });
        mBinding.chart.setAxisY(new IAxis() {
            @Override
            public int getDegreeCount() {
                return data.getyCount();
            }

            @Override
            public int getTotalWeight() {
                return data.getyCount();
            }

            @Override
            public int getWeightAt(int position) {
                return position;
            }

            @Override
            public String getTextAt(int position) {
                if (SettingProperty.getCompareType() == AppConstants.COMPARE_TYPE_ACCU) {
                    return ChartModel.formatAccumulatedAxis(mModel.getRegion(), position);
                }
                else {
                    return ChartModel.formatDailyAxis(mModel.getRegion(), position);
                }
            }

            @Override
            public boolean isNotDraw(int position) {
                // y轴每50个显示一个点
                if (position % 50 == 0) {
                    return false;
                }
                return true;
            }
        });
        mBinding.chart.setAdapter(new LineChartAdapter() {
            @Override
            public int getLineCount() {
                return data.getLineDataList() == null ? 0:data.getLineDataList().size();
            }

            @Override
            public LineData getLineData(int lineIndex) {
                LineData lineData = data.getLineDataList().get(lineIndex);
                lineData.setColor(movieAdapter.getColorList().get(lineIndex));
                return lineData;
            }
        });
    }

    @Override
    protected void onDestroy() {
        ChartDataProvider.release();
        super.onDestroy();
    }
}
