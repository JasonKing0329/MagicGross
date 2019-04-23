package com.king.app.gross.page;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseRecyclerAdapter;
import com.king.app.gross.base.MvvmActivity;
import com.king.app.gross.databinding.ActivityEditMarketGrossBinding;
import com.king.app.gross.page.adapter.EditMarketGrossAdapter;
import com.king.app.gross.page.bean.EditMarketGrossBean;
import com.king.app.gross.utils.FormatUtil;
import com.king.app.gross.view.dialog.SimpleDialogs;
import com.king.app.gross.viewmodel.EditMarketGrossViewModel;
import com.king.app.jactionbar.OnConfirmListener;

import java.util.ArrayList;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/4/23 17:16
 */
public class EditMarketGrossActivity extends MvvmActivity<ActivityEditMarketGrossBinding, EditMarketGrossViewModel> {

    private final int ID_CONFIRM = 0;

    public static final String EXTRA_MOVIE_ID = "movie_id";

    public static final int REQUEST_SELECT_REF = 501;

    private EditMarketGrossAdapter adapter;

    @Override
    protected int getContentView() {
        return R.layout.activity_edit_market_gross;
    }

    @Override
    protected void initView() {
        mBinding.setModel(mModel);

        mBinding.actionbar.showConfirmStatus(ID_CONFIRM);
        mBinding.actionbar.setOnConfirmListener(new OnConfirmListener() {
            @Override
            public boolean disableInstantDismissConfirm() {
                return false;
            }

            @Override
            public boolean disableInstantDismissCancel() {
                return true;
            }

            @Override
            public boolean onConfirm(int actionId) {
                mModel.executeUpdate();
                return false;
            }

            @Override
            public boolean onCancel(int actionId) {
                cancelAndExit();
                return false;
            }
        });
        mBinding.tvRef.setOnClickListener(v -> selectRef());
        mBinding.tvRef1.setOnClickListener(v -> selectRef());
        mBinding.tvRef2.setOnClickListener(v -> selectRef());
        mBinding.rvMarkets.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private void cancelAndExit() {
        showConfirmCancelMessage("Data will not be saved if cancel, continue?"
                , (dialogInterface, i) -> finish()
                , null);
    }

    @Override
    public void onBackPressed() {
        cancelAndExit();
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
        mModel.movieObserver.observe(this, movie -> {
            if (TextUtils.isEmpty(movie.getSubName())) {
                mBinding.actionbar.setTitle(movie.getName());
            }
            else {
                mBinding.actionbar.setTitle(movie.getName() + ":" + movie.getSubName());
            }
        });
        mModel.marketsObserver.observe(this, list -> {
            if (adapter == null) {
                adapter = new EditMarketGrossAdapter();
                adapter.setOnItemClickListener((view, position, data) -> editMarketGross(position, data));
                adapter.setList(list);
                mBinding.rvMarkets.setAdapter(adapter);
            }
            else {
                adapter.setList(list);
                adapter.notifyDataSetChanged();
            }
        });

        mModel.loadData(getMovieId());
    }

    private void editMarketGross(int position, EditMarketGrossBean data) {
        new SimpleDialogs().openInputDialog(this, data.getMarket(), name -> {
            try {
                long gross = Long.parseLong(name);
                data.setEdited(true);
                data.setGross(gross);
                data.setGrossText(FormatUtil.formatUsGross(gross));
                if (gross == 0) {
                    data.setMarkColor(Color.WHITE);
                }
                else {
                    data.setMarkColor(Color.parseColor("#ffff00"));
                }
                adapter.notifyItemChanged(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, String.valueOf(data.getGross()));
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
