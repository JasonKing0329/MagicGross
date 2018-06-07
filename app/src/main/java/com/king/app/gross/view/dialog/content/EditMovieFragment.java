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
        binding.tvOk.setOnClickListener(view -> updateMovie());
        if (mEditMovie != null) {
            binding.etName.setText(mEditMovie.getName());
            binding.etNameSub.setText(mEditMovie.getSubName());
            binding.etNameChn.setText(mEditMovie.getNameChn());
            binding.etNameChnSub.setText(mEditMovie.getSubChnName());
        }
    }

    public void setEditMovie(Movie mEditMovie) {
        this.mEditMovie = mEditMovie;
    }

    private void updateMovie() {
        String name = binding.etName.getText().toString();
        String nameChn = binding.etNameChn.getText().toString();
        if (TextUtils.isEmpty(name.trim()) && TextUtils.isEmpty(nameChn.trim())) {
            showMessageShort("英文名与中文名不能同时为空");
            return;
        }

        if (mEditMovie == null) {
            mEditMovie = new Movie();
        }
        mEditMovie.setName(name);
        mEditMovie.setNameChn(nameChn);
        mEditMovie.setSubName(binding.etNameSub.getText().toString());
        mEditMovie.setSubChnName(binding.etNameChnSub.getText().toString());
        MApplication.getInstance().getDaoSession().getMovieDao().insertOrReplace(mEditMovie);

        dismiss();
    }
}
