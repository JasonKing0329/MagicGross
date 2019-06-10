package com.king.app.gross.view.dialog.content;

import com.king.app.gross.R;
import com.king.app.gross.base.IFragmentHolder;
import com.king.app.gross.databinding.FragmentRankDateBinding;
import com.king.app.gross.view.dialog.DatePickerFragment;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/6/10 16:28
 */
public class RankDateRangeFragment extends DraggableContentFragment<FragmentRankDateBinding> {

    private String mStartDate;

    private String mEndDate;

    private OnDateRangeListener onDateRangeListener;

    @Override
    protected void bindFragmentHolder(IFragmentHolder holder) {

    }

    public void setOnDateRangeListener(OnDateRangeListener onDateRangeListener) {
        this.onDateRangeListener = onDateRangeListener;
    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.fragment_rank_date;
    }

    @Override
    protected void initView() {
        if (mStartDate != null) {
            mBinding.cbStart.setChecked(true);
            mBinding.btnStart.setText(mStartDate);
        }
        if (mEndDate != null) {
            mBinding.cbEnd.setChecked(true);
            mBinding.btnEnd.setText(mEndDate);
        }
        mBinding.btnStart.setOnClickListener(v -> selectDate(0));
        mBinding.btnEnd.setOnClickListener(v -> selectDate(1));
        mBinding.tvOk.setOnClickListener(v -> {
            if (onDateRangeListener != null) {
                String start = null;
                if (mBinding.cbStart.isChecked()) {
                    start = mStartDate;
                }
                String end = null;
                if (mBinding.cbEnd.isChecked()) {
                    end = mEndDate;
                }
                onDateRangeListener.onSetDateRange(start, end);
            }
        });
    }

    private void selectDate(int type) {
        DatePickerFragment fragment = new DatePickerFragment();
        if (type == 0) {
            fragment.setDate(mStartDate);
        }
        else {
            fragment.setDate(mEndDate);
        }
        fragment.setOnDateSetListener((view, year, month, dayOfMonth) -> {
            StringBuffer buffer = new StringBuffer();
            buffer.append(year).append("-");
            month = month + 1;
            if (month < 10) {
                buffer.append("0").append(month);
            }
            else {
                buffer.append(month);
            }
            buffer.append("-");
            if (dayOfMonth < 10) {
                buffer.append("0").append(dayOfMonth);
            }
            else {
                buffer.append(dayOfMonth);
            }
            if (type == 0) {
                mStartDate = buffer.toString();
                mBinding.btnStart.setText(mStartDate);
            }
            else {
                mEndDate = buffer.toString();
                mBinding.btnEnd.setText(mEndDate);
            }
        });
        fragment.show(getChildFragmentManager(), "DatePickerFragment");
    }

    public void initDate(String mStartDate, String mEndDate) {
        this.mStartDate = mStartDate;
        this.mEndDate = mEndDate;
    }

    public interface OnDateRangeListener {
        void onSetDateRange(String start, String end);
    }
}
