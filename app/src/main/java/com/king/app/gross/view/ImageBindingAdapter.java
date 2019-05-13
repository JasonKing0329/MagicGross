package com.king.app.gross.view;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.king.app.gross.R;
import com.king.app.gross.model.AppGlide;
import com.king.app.gross.utils.DebugLog;

public class ImageBindingAdapter {

    /**
     * 处于recyclerView中的菜品图片（提前下单、点餐页、购物车）
     * @param view
     * @param url
     */
    @BindingAdapter({"movieCover"})
    public static void setMovieCover(ImageView view, String url) {
        DebugLog.e(url);
        AppGlide.with(view.getContext())
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.bg_movie_default)
                .into(view);
    }

}