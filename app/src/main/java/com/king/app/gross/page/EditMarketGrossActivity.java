package com.king.app.gross.page;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;

import com.king.app.gross.R;
import com.king.app.gross.base.MvvmActivity;
import com.king.app.gross.databinding.ActivityEditMarketGrossBinding;
import com.king.app.gross.viewmodel.EditMarketGrossViewModel;

import java.util.ArrayList;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/4/23 17:16
 */
public class EditMarketGrossActivity extends MvvmActivity<ActivityEditMarketGrossBinding, EditMarketGrossViewModel> {

    public static final String EXTRA_MOVIE_ID = "movie_id";

    public static final int REQUEST_SELECT_REF = 501;

    @Override
    protected int getContentView() {
        return R.layout.activity_edit_market_gross;
    }

    @Override
    protected void initView() {
        mBinding.tvRef.setOnClickListener(v -> selectRef());
    }

    private void selectRef() {
        Intent intent = new Intent().setClass(this, MovieListActivity.class);
        intent.putExtra(MovieListActivity.SELECT_MODE, true);
        startActivityForResult(intent, REQUEST_SELECT_REF);
    }

    @Override
    protected EditMarketGrossViewModel createViewModel() {
        return ViewModelProviders.of(this).get(EditMarketGrossViewModel.class);
    }

    @Override
    protected void initData() {

    }

    private long getMovieId() {
        return getIntent().getLongExtra(EXTRA_MOVIE_ID, -1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_REF ) {
            if (resultCode == RESULT_OK) {
                ArrayList<CharSequence> list = data.getCharSequenceArrayListExtra(MovieListActivity.RESP_SELECT_RESULT);
                mModel.onReferenceChanged(list);
            }
        }
    }

}
