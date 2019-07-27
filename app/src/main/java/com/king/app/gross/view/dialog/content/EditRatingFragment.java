package com.king.app.gross.view.dialog.content;

import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.IFragmentHolder;
import com.king.app.gross.databinding.FragmentEditRatingBinding;
import com.king.app.gross.page.bean.RatingMovie;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/7/26 11:38
 */
public class EditRatingFragment extends DraggableContentFragment<FragmentEditRatingBinding> {

    private OnRatingListener onRatingListener;

    private OnRateRottenListener onRateRottenListener;

    private RatingMovie mRating;
    private boolean isRottenSystem;

    public void setOnRatingListener(OnRatingListener onRatingListener) {
        this.onRatingListener = onRatingListener;
    }

    public void setOnRateRottenListener(OnRateRottenListener onRateRottenListener) {
        this.onRateRottenListener = onRateRottenListener;
    }

    @Override
    protected void bindFragmentHolder(IFragmentHolder holder) {

    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.fragment_edit_rating;
    }

    @Override
    protected void initView() {

        if (isRottenSystem) {
            if (mRating != null) {
                if (mRating.getRottenPro() != null) {
                    mBinding.etPerson.setText(String.valueOf(mRating.getRottenPro().getRating().getPerson()));
                    mBinding.etScore.setText(String.valueOf(mRating.getRottenPro().getRating().getScore()));
                }
                if (mRating.getRottenAud() != null) {
                    mBinding.etPersonAud.setText(String.valueOf(mRating.getRottenAud().getRating().getPerson()));
                    mBinding.etScoreAud.setText(String.valueOf(mRating.getRottenAud().getRating().getScore()));
                }
            }
        }
        else {
            mBinding.tvRottenPro.setVisibility(View.GONE);
            mBinding.tvRottenAud.setVisibility(View.GONE);
            mBinding.etPersonAud.setVisibility(View.GONE);
            mBinding.etScoreAud.setVisibility(View.GONE);
            mBinding.tvPersonAud.setVisibility(View.GONE);
            mBinding.tvScoreAud.setVisibility(View.GONE);
            if (mRating != null && mRating.getRating() != null) {
                mBinding.etPerson.setText(String.valueOf(mRating.getRating().getPerson()));
                mBinding.etScore.setText(String.valueOf(mRating.getRating().getScore()));
            }
        }

        mBinding.tvOk.setOnClickListener(v -> {
            if (isRottenSystem) {
                if (onRateRottenListener != null) {
                    try {
                        double score = Double.parseDouble(mBinding.etScore.getText().toString());
                        int person = Integer.parseInt(mBinding.etPerson.getText().toString());
                        double scoreAud = Double.parseDouble(mBinding.etScoreAud.getText().toString());
                        int personAud = Integer.parseInt(mBinding.etPersonAud.getText().toString());
                        onRateRottenListener.onUpdateRating(score, person, scoreAud, personAud);
                        dismissAllowingStateLoss();
                    } catch (Exception e) {
                        showMessageShort("Error number");
                    }
                }
            }
            else {
                if (onRatingListener != null) {
                    try {
                        double score = Double.parseDouble(mBinding.etScore.getText().toString());
                        int person = Integer.parseInt(mBinding.etPerson.getText().toString());
                        onRatingListener.onUpdateRating(score, person);
                        dismissAllowingStateLoss();
                    } catch (Exception e) {
                        showMessageShort("Error number");
                    }
                }
            }
        });
    }

    public void setRating(RatingMovie rating) {
        mRating = rating;
    }

    public void setRottenSystem(boolean isRottenSystem) {
        this.isRottenSystem = isRottenSystem;
    }

    public interface OnRatingListener {
        void onUpdateRating(double score, int person);
    }

    public interface OnRateRottenListener {
        void onUpdateRating(double scorePro, int personPro, double scoreAud, int personAud);
    }
}
