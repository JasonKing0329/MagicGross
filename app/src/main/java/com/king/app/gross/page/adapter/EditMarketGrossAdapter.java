package com.king.app.gross.page.adapter;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.databinding.AdapterEditMarketGrossBinding;
import com.king.app.gross.page.bean.EditMarketGrossBean;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/4/23 17:14
 */
public class EditMarketGrossAdapter extends BaseBindingAdapter<AdapterEditMarketGrossBinding, EditMarketGrossBean> {

    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_edit_market_gross;
    }

    @Override
    protected void onBindItem(AdapterEditMarketGrossBinding binding, int position, EditMarketGrossBean bean) {
        binding.setBean(bean);
    }
}
