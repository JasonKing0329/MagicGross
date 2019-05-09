package com.king.app.gross.page;

import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseViewModel;
import com.king.app.gross.base.MvvmActivity;
import com.king.app.gross.databinding.ActivityFullChartBinding;
import com.king.app.gross.model.compare.CompareChart;
import com.king.app.gross.model.compare.CompareInstance;
import com.king.app.gross.model.gross.ChartModel;
import com.king.app.gross.page.adapter.CompareMovieAdapter;
import com.king.app.gross.view.widget.chart.adapter.IAxis;
import com.king.app.gross.view.widget.chart.adapter.LineChartAdapter;
import com.king.app.gross.view.widget.chart.adapter.LineData;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/5/9 17:10
 */
public class FullChartActivity extends MvvmActivity<ActivityFullChartBinding, BaseViewModel> {

    private CompareMovieAdapter movieAdapter;

    @Override
    protected BaseViewModel createViewModel() {
        return null;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected int getContentView() {
        return R.layout.activity_full_chart;
    }

    @Override
    protected void initView() {
        mBinding.ivClose.setOnClickListener(v -> finish());

        GridLayoutManager manager = new GridLayoutManager(this, CompareInstance.getInstance().getMovieList().size());
        mBinding.rvMovies.setLayoutManager(manager);

        movieAdapter = new CompareMovieAdapter();
//        movieAdapter.setOnItemClickListener((view, position, data) -> showMoviePage(data));
        movieAdapter.setList(ChartDataProvider.getMovieList());
        mBinding.rvMovies.setAdapter(movieAdapter);

        updateChart(ChartDataProvider.getChartData());
    }

    private void updateChart(CompareChart data) {
        if (data.getLineDataList() == null) {
            mBinding.chart.setVisibility(View.GONE);
            return;
        }
        mBinding.chart.setVisibility(View.VISIBLE);
        mBinding.chart.setDrawAxisY(true);
        mBinding.chart.setDegreeCombine(1);
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
                return ChartModel.formatAxisString(ChartDataProvider.getRegion(), position);
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

}
