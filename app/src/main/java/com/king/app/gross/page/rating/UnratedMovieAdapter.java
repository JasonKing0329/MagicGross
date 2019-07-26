package com.king.app.gross.page.rating;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseBindingAdapter;
import com.king.app.gross.databinding.AdapterRatingMovieBinding;
import com.king.app.gross.databinding.AdapterUnratedMovieBinding;
import com.king.app.gross.page.bean.RatingMovie;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/7/26 10:44
 */
public class UnratedMovieAdapter extends BaseBindingAdapter<AdapterUnratedMovieBinding, RatingMovie> {

    @Override
    protected int getItemLayoutRes() {
        return R.layout.adapter_unrated_movie;
    }

    @Override
    protected void onBindItem(AdapterUnratedMovieBinding binding, int position, RatingMovie bean) {
        binding.setBean(bean);
    }
}
