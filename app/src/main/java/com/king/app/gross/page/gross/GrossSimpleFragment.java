package com.king.app.gross.page.gross;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingFragment;
import com.king.app.gross.base.BaseRecyclerAdapter;
import com.king.app.gross.base.IFragmentHolder;
import com.king.app.gross.conf.Region;
import com.king.app.gross.databinding.FragmentGrossSimpleBinding;
import com.king.app.gross.page.adapter.GrossSimpleAdapter;
import com.king.app.gross.utils.DebugLog;
import com.king.app.gross.viewmodel.MovieGrossViewModel;
import com.king.app.gross.viewmodel.bean.GrossPage;
import com.king.app.gross.viewmodel.bean.SimpleGross;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/7 17:16
 */
public class GrossSimpleFragment extends BaseBindingFragment<FragmentGrossSimpleBinding> {

    private static final String ARG_REGION = "arg_region";

    private GrossSimpleAdapter adapter;

    private MovieGrossViewModel mModel;

    private Region mRegion;

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
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mBinding.rvGross.setLayoutManager(manager);

        mRegion = Region.values()[getArguments().getInt(ARG_REGION)];

        mModel = ViewModelProviders.of(getActivity()).get(MovieGrossViewModel.class);

        DebugLog.e("region " + mRegion);
        mModel.grossObserver.observe(this, grossPage -> onReceiveData(grossPage));
        mModel.loadGross(mRegion);
    }

    private void onReceiveData(GrossPage grossPage) {
        DebugLog.e("region=" + grossPage.region + ", size=" + grossPage.list.size());
        if (grossPage.region == mRegion) {
            if (adapter == null) {
                adapter = new GrossSimpleAdapter();
                adapter.setList(grossPage.list);
                adapter.setOnItemClickListener((view, position, data) -> mModel.editGross(data));
                mBinding.rvGross.setAdapter(adapter);
            }
            else {
                adapter.setList(grossPage.list);
//                adapter.notifyDataSetChanged();
                mBinding.rvGross.setAdapter(adapter);
            }
        }
    }
}
