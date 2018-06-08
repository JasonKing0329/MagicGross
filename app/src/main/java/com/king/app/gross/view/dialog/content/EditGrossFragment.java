package com.king.app.gross.view.dialog.content;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;

import com.king.app.gross.R;
import com.king.app.gross.base.IFragmentHolder;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.Region;
import com.king.app.gross.databinding.FragmentEditGrossBinding;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.model.setting.EditGrossPref;
import com.king.app.gross.model.setting.SettingProperty;
import com.king.app.gross.utils.FormatUtil;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/7 14:56
 */
public class EditGrossFragment extends DraggableContentFragment<FragmentEditGrossBinding> {

    private Gross mGross;

    private Movie mMovie;

    public OnGrossListener onGrossListener;

    @Override
    protected void bindFragmentHolder(IFragmentHolder holder) {

    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.fragment_edit_gross;
    }

    @Override
    protected void initView() {
        EditGrossPref pref = SettingProperty.getEditGrossPref();
        mBinding.tvGrossUs.setVisibility(View.GONE);
        if (mGross == null) {
            mBinding.etDay.setText(String.valueOf(pref.getDay() + 1));
            if (pref.getDayOfWeekIndex() == 6) {
                mBinding.spDayOfWeek.setSelection(0);
            }
            else {
                mBinding.spDayOfWeek.setSelection(pref.getDayOfWeekIndex() + 1);
            }
            mBinding.spRegion.setSelection(pref.getRegionIndex());
            mBinding.spSymbol.setSelection(pref.getSymbolIndex());
            mBinding.spUnit.setSelection(pref.getUnitIndex());
            mBinding.cbLeft.setChecked(false);
            mBinding.etLeftDay.setVisibility(View.INVISIBLE);
        }
        else {
            mBinding.etDay.setText(String.valueOf(mGross.getDay()));
            mBinding.spDayOfWeek.setSelection(mGross.getDayOfWeek() - 1);
            mBinding.spRegion.setSelection(mGross.getRegion());
            mBinding.spSymbol.setSelection(mGross.getSymbol());
            if (mGross.getRegion() == Region.CHN.ordinal()) {
                mBinding.etGross.setText(getChnGross(mGross));
                mBinding.spSymbol.setSelection(1);
            }
            else {
                mBinding.etGross.setText(getUsGross(mGross));
                mBinding.spSymbol.setSelection(0);
            }
            mBinding.spUnit.setSelection(0);
            if (mGross.getIsLeftAfterDay() > 0) {
                mBinding.cbLeft.setChecked(true);
                mBinding.etLeftDay.setVisibility(View.VISIBLE);
                mBinding.etLeftDay.setText(String.valueOf(mGross.getIsLeftAfterDay()));
            }
            else {
                mBinding.cbLeft.setChecked(false);
                mBinding.etLeftDay.setVisibility(View.INVISIBLE);
            }
        }

        mBinding.tvOk.setOnClickListener(view -> onConfirm());
        mBinding.cbLeft.setOnCheckedChangeListener((buttonView, isChecked) -> mBinding.etLeftDay.setVisibility(isChecked ? View.VISIBLE:View.INVISIBLE));

        mBinding.etGross.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onGrossChanged(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBinding.spSymbol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onGrossChanged(mBinding.etGross.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void onGrossChanged(String gross) {
        // 人民币同步显示美元
        if (mBinding.spSymbol.getSelectedItemPosition() == 1) {
            if (TextUtils.isEmpty(gross.trim())) {
                mBinding.tvGrossUs.setVisibility(View.GONE);
            }
            else {
                mBinding.tvGrossUs.setVisibility(View.VISIBLE);
                mBinding.tvGrossUs.setText(FormatUtil.formatUsGross((long) (convertToGross() / mMovie.getUsToYuan())));
            }
        }
        else {
            mBinding.tvGrossUs.setVisibility(View.GONE);
        }
    }

    public void setMovie(Movie mMovie) {
        this.mMovie = mMovie;
    }

    public void setGross(Gross mGross) {
        this.mGross = mGross;
    }

    public void setOnGrossListener(OnGrossListener onGrossListener) {
        this.onGrossListener = onGrossListener;
    }

    /**
     * 货币：人民币。单位：万
     * @param mGross
     * @return
     */
    private String getChnGross(Gross mGross) {
        return FormatUtil.pointZZ((double) mGross.getGross() / (double)10000);
    }

    /**
     * 货币：美元。单位：万
     * @param mGross
     * @return
     */
    private String getUsGross(Gross mGross) {
        return FormatUtil.pointZZ((double) mGross.getGross() / (double)10000);
    }

    private void onConfirm() {
        if (mGross == null) {
            mGross = new Gross();
            mGross.setMovieId(mMovie.getId());
        }
        String day = mBinding.etDay.getText().toString();
        if (TextUtils.isEmpty(day)) {
            showMessageShort("Day cannot be empty");
            return;
        }
        String gross = mBinding.etGross.getText().toString();
        if (TextUtils.isEmpty(gross)) {
            showMessageShort("Gross cannot be empty");
            return;
        }
        if (mBinding.cbLeft.isChecked()) {
            String leftDay = mBinding.etLeftDay.getText().toString();
            if (TextUtils.isEmpty(leftDay)) {
                showMessageShort("After day cannot be empty");
                return;
            }
            mGross.setIsLeftAfterDay(Integer.parseInt(leftDay));
        }

        mGross.setDay(Integer.parseInt(day));
        mGross.setDayOfWeek(mBinding.spDayOfWeek.getSelectedItemPosition() + 1);
        mGross.setRegion(mBinding.spRegion.getSelectedItemPosition());
        mGross.setSymbol(mBinding.spSymbol.getSelectedItemPosition());
        try {
            mGross.setGross(convertToGross());
        } catch (Exception e) {
            showMessageShort("Wrong gross number");
            return;
        }

        MApplication.getInstance().getDaoSession().getGrossDao().insertOrReplace(mGross);

        // update pref
        EditGrossPref pref = SettingProperty.getEditGrossPref();
        pref.setDay(mGross.getDay());
        pref.setDayOfWeekIndex(mBinding.spDayOfWeek.getSelectedItemPosition());
        pref.setRegionIndex(mBinding.spRegion.getSelectedItemPosition());
        pref.setSymbolIndex(mBinding.spSymbol.getSelectedItemPosition());
        pref.setUnitIndex(mBinding.spUnit.getSelectedItemPosition());
        SettingProperty.setEditGrossPref(pref);

        dismiss();

        if (onGrossListener != null) {
            onGrossListener.onSaved(mGross);
        }
    }

    private long convertToGross() throws RuntimeException {
        long result;
        String inputGross = mBinding.etGross.getText().toString();
        if (inputGross.endsWith(".")) {
            inputGross = inputGross.substring(0, inputGross.indexOf("."));
        }
        double price = Double.parseDouble(inputGross);
        switch (mBinding.spUnit.getSelectedItemPosition()) {
            // 万
            case 0:
                result = (long) (price * 10000);
                break;
            // 百万
            case 1:
                result = (long) (price * 1000000);
                break;
            // 个位
            default:
                result = (long) price;
                break;
        }
        return result;
    }

    public interface OnGrossListener {
        void onSaved(Gross gross);
    }
}
