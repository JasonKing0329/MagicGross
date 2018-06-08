package com.king.app.gross.page.gross;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingFragment;
import com.king.app.gross.base.IFragmentHolder;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.Region;
import com.king.app.gross.databinding.FragmentGrossTabBinding;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/7 17:18
 */
public class GrossTabFragment extends BaseBindingFragment<FragmentGrossTabBinding> {

    private SimplePagerAdapter pagerAdapter;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.fragment_gross_tab;
    }

    @Override
    protected void bindFragmentHolder(IFragmentHolder holder) {

    }

    @Override
    protected void initView() {
        pagerAdapter = new SimplePagerAdapter(getChildFragmentManager());
        pagerAdapter.addFragment(GrossSimpleFragment.newInstance(Region.NA.ordinal())
            , AppConstants.REGION_TITLES[Region.NA.ordinal()]);
        pagerAdapter.addFragment(GrossSimpleFragment.newInstance(Region.CHN.ordinal())
                , AppConstants.REGION_TITLES[Region.CHN.ordinal()]);
        pagerAdapter.addFragment(GrossSimpleFragment.newInstance(Region.OVERSEA_NO_CHN.ordinal())
                , AppConstants.REGION_TITLES[Region.OVERSEA_NO_CHN.ordinal()]);
        pagerAdapter.addFragment(GrossSimpleFragment.newInstance(Region.OVERSEA.ordinal())
                , AppConstants.REGION_TITLES[Region.OVERSEA.ordinal()]);
        pagerAdapter.addFragment(GrossSimpleFragment.newInstance(Region.WORLDWIDE.ordinal())
                , AppConstants.REGION_TITLES[Region.WORLDWIDE.ordinal()]);
        mBinding.viewpager.setAdapter(pagerAdapter);

        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText(AppConstants.REGION_TITLES[Region.NA.ordinal()]));
        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText(AppConstants.REGION_TITLES[Region.CHN.ordinal()]));
        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText(AppConstants.REGION_TITLES[Region.OVERSEA_NO_CHN.ordinal()]));
        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText(AppConstants.REGION_TITLES[Region.OVERSEA.ordinal()]));
        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText(AppConstants.REGION_TITLES[Region.WORLDWIDE.ordinal()]));
        mBinding.tabLayout.setupWithViewPager(mBinding.viewpager);

    }
}
