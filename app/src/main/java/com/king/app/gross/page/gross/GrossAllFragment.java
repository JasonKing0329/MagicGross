package com.king.app.gross.page.gross;

import android.support.v7.widget.LinearLayoutManager;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingFragment;
import com.king.app.gross.base.IFragmentHolder;
import com.king.app.gross.databinding.FragmentGrossAllBinding;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.page.adapter.GrossAllAdapter;

import java.util.ArrayList;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/7 15:37
 */
public class GrossAllFragment extends BaseBindingFragment<FragmentGrossAllBinding> {

    private GrossAllAdapter adapter;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.fragment_gross_all;
    }

    @Override
    protected void initView() {
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mBinding.rvGross.setLayoutManager(manager);

        adapter = new GrossAllAdapter();
        adapter.setList(new ArrayList<>());
        for (int i = 0; i < 50; i ++) {
            adapter.getList().add(new Gross());
        }
        mBinding.rvGross.setAdapter(adapter);
    }

    @Override
    protected void bindFragmentHolder(IFragmentHolder holder) {

    }
}
