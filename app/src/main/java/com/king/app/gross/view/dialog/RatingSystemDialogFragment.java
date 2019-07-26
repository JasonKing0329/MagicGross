package com.king.app.gross.view.dialog;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.king.app.gross.R;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.conf.RatingSystem;
import com.king.app.gross.databinding.DialogLoadingBinding;
import com.king.app.gross.databinding.DialogRatingSystemBinding;
import com.king.app.gross.page.rating.RatingPageActivity;

/**
 * 描述:
 * <p/>作者：景阳
 * <p/>创建时间: 2018/1/29 13:37
 */
public class RatingSystemDialogFragment extends DialogFragment {

    DialogRatingSystemBinding mBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setCancelable(true);
        setStyle(android.app.DialogFragment.STYLE_NORMAL, R.style.RatingSystemDialog);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_rating_system, container, false);
        mBinding.ivImdb.setOnClickListener(v -> openRatingPage(RatingSystem.IMDB));
        mBinding.ivRotten.setOnClickListener(v -> openRatingPage(RatingSystem.ROTTEN_PRO));
        mBinding.ivMeta.setOnClickListener(v -> openRatingPage(RatingSystem.META));
        mBinding.ivDouban.setOnClickListener(v -> openRatingPage(RatingSystem.DOUBAN));
        mBinding.ivMaoyan.setOnClickListener(v -> openRatingPage(RatingSystem.MAOYAN));
        mBinding.ivTpp.setOnClickListener(v -> openRatingPage(RatingSystem.TAOPP));
        return mBinding.getRoot();
    }

    private void openRatingPage(long id) {
        Intent intent = new Intent(getContext(), RatingPageActivity.class);
        intent.putExtra(RatingPageActivity.RATING_SYSTEM, id);
        startActivity(intent);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().setGravity(Gravity.CENTER);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        FragmentTransaction ft = manager.beginTransaction();
        if (isAdded()) {
            ft.show(this);
        } else {
            ft.add(this, tag);
        }
        ft.commitAllowingStateLoss();
    }
}
