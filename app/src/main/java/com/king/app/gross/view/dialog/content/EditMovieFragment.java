package com.king.app.gross.view.dialog.content;

import android.text.TextUtils;

import com.king.app.gross.R;
import com.king.app.gross.base.IFragmentHolder;
import com.king.app.gross.base.MApplication;
import com.king.app.gross.databinding.FragmentEditMovieBinding;
import com.king.app.gross.model.entity.Movie;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 16:16
 */
public class EditMovieFragment extends DraggableContentFragment<FragmentEditMovieBinding> {

    private Movie mEditMovie;

    @Override
    protected void bindFragmentHolder(IFragmentHolder holder) {

    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.fragment_edit_movie;
    }

    @Override
    protected void initView() {
        mBinding.tvOk.setOnClickListener(view -> updateMovie());
        if (mEditMovie != null) {
            mBinding.etName.setText(mEditMovie.getName());
            mBinding.etNameSub.setText(mEditMovie.getSubName());
            mBinding.etNameChn.setText(mEditMovie.getNameChn());
            mBinding.etNameChnSub.setText(mEditMovie.getSubChnName());
            mBinding.etExchange.setText(String.valueOf(mEditMovie.getUsToYuan()));
        }
    }

    public void setEditMovie(Movie mEditMovie) {
        this.mEditMovie = mEditMovie;
    }

    private void updateMovie() {
        String name = mBinding.etName.getText().toString();
        String nameChn = mBinding.etNameChn.getText().toString();
        if (TextUtils.isEmpty(name.trim()) && TextUtils.isEmpty(nameChn.trim())) {
            showMessageShort("英文名与中文名不能同时为空");
            return;
        }
        double exchange;
        try {
            exchange = Double.parseDouble(mBinding.etExchange.getText().toString());
        } catch (Exception e) {
            showMessageShort("Wrong exchange");
            return;
        }
        if (exchange == 0) {
            showMessageShort("Wrong exchange");
            return;
        }

        if (mEditMovie == null) {
            mEditMovie = new Movie();
        }
        mEditMovie.setName(name);
        mEditMovie.setNameChn(nameChn);
        mEditMovie.setSubName(mBinding.etNameSub.getText().toString());
        mEditMovie.setSubChnName(mBinding.etNameChnSub.getText().toString());
        mEditMovie.setUsToYuan(exchange);
        MApplication.getInstance().getDaoSession().getMovieDao().insertOrReplace(mEditMovie);

        dismiss();
    }
}
