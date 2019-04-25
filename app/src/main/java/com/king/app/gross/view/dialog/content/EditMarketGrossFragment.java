package com.king.app.gross.view.dialog.content;

import android.text.TextUtils;

import com.king.app.gross.R;
import com.king.app.gross.base.IFragmentHolder;
import com.king.app.gross.databinding.FragmentEditMarketBinding;
import com.king.app.gross.model.entity.MarketGross;

public class EditMarketGrossFragment extends DraggableContentFragment<FragmentEditMarketBinding> {

    private MarketGross marketGross;

    private OnUpdateListener onUpdateListener;

    @Override
    protected void bindFragmentHolder(IFragmentHolder holder) {

    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.fragment_edit_market;
    }

    public void setMarketGross(MarketGross marketGross) {
        this.marketGross = marketGross;
    }

    public void setOnUpdateListener(OnUpdateListener onUpdateListener) {
        this.onUpdateListener = onUpdateListener;
    }

    @Override
    protected void initView() {
        mBinding.setBean(marketGross);
        if (TextUtils.isEmpty(marketGross.getMovie().getSubName())) {
            mBinding.tvMovie.setText(marketGross.getMovie().getName());
        }
        else {
            mBinding.tvMovie.setText(marketGross.getMovie().getName() + ":" + marketGross.getMovie().getSubName());
        }

        mBinding.tvOk.setOnClickListener(e -> {
            try {
                marketGross.setGross(Long.parseLong(mBinding.etTotal.getText().toString()));
            } catch (Exception ex) {}
            try {
                marketGross.setOpening(Long.parseLong(mBinding.etOpening.getText().toString()));
            } catch (Exception ex) {}
            marketGross.setDebut(mBinding.etDebut.getText().toString());
            marketGross.setEndDate(mBinding.etEnd.getText().toString());
            if (onUpdateListener != null) {
                onUpdateListener.onUpdate(marketGross);
            }
            dismissAllowingStateLoss();
        });
    }

    public interface OnUpdateListener {
        void onUpdate(MarketGross gross);
    }
}
