package com.king.app.gross.page.gross;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingFragment;
import com.king.app.gross.base.IFragmentHolder;
import com.king.app.gross.conf.GrossDateType;
import com.king.app.gross.conf.Region;
import com.king.app.gross.databinding.FragmentGrossSimpleBinding;
import com.king.app.gross.model.gross.ChartModel;
import com.king.app.gross.page.adapter.GrossSimpleAdapter;
import com.king.app.gross.page.adapter.GrossWeekAdapter;
import com.king.app.gross.utils.DebugLog;
import com.king.app.gross.utils.ListUtil;
import com.king.app.gross.view.widget.chart.adapter.IAxis;
import com.king.app.gross.view.widget.chart.adapter.LineChartAdapter;
import com.king.app.gross.view.widget.chart.adapter.LineData;
import com.king.app.gross.viewmodel.MovieGrossViewModel;
import com.king.app.gross.viewmodel.bean.GrossPage;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/7 17:16
 */
public class GrossSimpleFragment extends BaseBindingFragment<FragmentGrossSimpleBinding> {

    private static final String ARG_REGION = "arg_region";

    private GrossSimpleAdapter adapter;

    private GrossWeekAdapter weeklyAdapter;

    private GrossWeekAdapter weekendAdapter;

    private MovieGrossViewModel mModel;

    private Region mRegion;

    private GrossDateType mDateType;

    public static GrossSimpleFragment newInstance(int region) {
        GrossSimpleFragment fragment = new GrossSimpleFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_REGION, region);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void bindFragmentHolder(IFragmentHolder holder) {

    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.fragment_gross_simple;
    }

    @Override
    protected void initView() {

        mBinding.groupWeek.setVisibility(View.INVISIBLE);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mBinding.rvGross.setLayoutManager(manager);

        mRegion = Region.values()[getArguments().getInt(ARG_REGION)];

        mModel = ViewModelProviders.of(getActivity()).get(MovieGrossViewModel.class);

        mDateType = mModel.getDateType();

        DebugLog.e("region " + mRegion);
        mModel.grossObserver.observe(this, grossPage -> onReceiveData(grossPage));
        mModel.loadRegion(mRegion);
    }

    private void onReceiveData(GrossPage grossPage) {
        DebugLog.e("region=" + grossPage.region + ", date type=" + grossPage.dateType);
        if (grossPage.region == mRegion) {
            switch (grossPage.dateType) {
                case WEEKEND:
                    updateWeekendData(grossPage);
                    break;
                case WEEKLY:
                    updateWeeklyData(grossPage);
                    break;
                default:
                    updateDailyData(grossPage);
                    // 目前仅支持daily chart，选择WEEKEND和WEEKLY后，也不用因此chart，仍然显示daily时的chart就行
                    updateChart(grossPage);
                    break;
            }
            mBinding.tvOpening.setText(grossPage.opening);
            mBinding.tvTotal.setText(grossPage.total);
            mBinding.tvRate.setText(grossPage.rate);
        }
    }

    /**
     * value以十万(美元)/百万(人民币)描点
     * y轴以5百万(美元)/5千万(人民币)描点
     * @param page
     */
    private void updateChart(GrossPage page) {
        if (page.lineData == null || ListUtil.isEmpty(page.lineData.getValues())) {
            mBinding.chartWeek.setVisibility(View.GONE);
            return;
        }
        mBinding.chartWeek.setVisibility(View.VISIBLE);
        mBinding.chartWeek.setDrawAxisY(true);
        mBinding.chartWeek.setDegreeCombine(1);
        mBinding.chartWeek.setAxisX(new IAxis() {
            @Override
            public int getDegreeCount() {
                return page.list.size();
            }

            @Override
            public int getTotalWeight() {
                return page.list.size();
            }

            @Override
            public int getWeightAt(int position) {
                return position;
            }

            @Override
            public String getTextAt(int position) {
                return page.list.get(position).getDay();
            }

            @Override
            public boolean isNotDraw(int position) {
                return false;
            }
        });
        mBinding.chartWeek.setAxisY(new IAxis() {
            @Override
            public int getDegreeCount() {
                return page.axisYData.total;
            }

            @Override
            public int getTotalWeight() {
                return page.axisYData.total;
            }

            @Override
            public int getWeightAt(int position) {
                return position;
            }

            @Override
            public String getTextAt(int position) {

                return ChartModel.formatAxisString(page.region, position);
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
        mBinding.chartWeek.setAdapter(new LineChartAdapter() {
            @Override
            public int getLineCount() {
                return 1;
            }

            @Override
            public LineData getLineData(int lineIndex) {
                return page.lineData;
            }
        });
    }

    private void updateWeekendData(GrossPage grossPage) {
        mBinding.groupDaily.setVisibility(View.INVISIBLE);
        mBinding.groupWeek.setVisibility(View.VISIBLE);

        weekendAdapter = new GrossWeekAdapter();
        weekendAdapter.setList(grossPage.weekList);
//            adapter.setOnItemClickListener((view, position, data) -> mModel.editGross(data));
        mBinding.rvGross.setAdapter(weekendAdapter);
    }

    private void updateWeeklyData(GrossPage grossPage) {
        mBinding.groupDaily.setVisibility(View.INVISIBLE);
        mBinding.groupWeek.setVisibility(View.VISIBLE);

        weeklyAdapter = new GrossWeekAdapter();
        weeklyAdapter.setList(grossPage.weekList);
//            adapter.setOnItemClickListener((view, position, data) -> mModel.editGross(data));
        mBinding.rvGross.setAdapter(weeklyAdapter);
    }

    private void updateDailyData(GrossPage grossPage) {
        mBinding.groupDaily.setVisibility(View.VISIBLE);
        mBinding.groupWeek.setVisibility(View.INVISIBLE);

        adapter = new GrossSimpleAdapter();
        adapter.setList(grossPage.list);
        adapter.setOnItemClickListener((view, position, data) -> mModel.editGross(data));
        mBinding.rvGross.setAdapter(adapter);
    }

    public void onDateTypeChanged() {
        if (mModel.getDateType() != mDateType) {
            mModel.loadRegion(mRegion);
        }
    }

    public void onPageSelected() {
        mModel.loadRegion(mRegion);
    }

    public void toggleChart() {
        if (mBinding.chartWeek.getVisibility() == View.VISIBLE) {
            mBinding.chartWeek.setVisibility(View.GONE);
        }
        else {
            mBinding.chartWeek.setVisibility(View.VISIBLE);
        }
    }
}
