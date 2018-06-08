package com.king.app.gross.page;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;

import com.king.app.gross.R;
import com.king.app.gross.base.MvvmActivity;
import com.king.app.gross.databinding.ActivityMovieGrossBinding;
import com.king.app.gross.model.entity.Gross;
import com.king.app.gross.page.gross.GrossTabFragment;
import com.king.app.gross.view.dialog.DraggableDialogFragment;
import com.king.app.gross.view.dialog.content.EditGrossFragment;
import com.king.app.gross.viewmodel.MovieGrossViewModel;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/7 15:15
 */
public class MovieGrossActivity extends MvvmActivity<ActivityMovieGrossBinding, MovieGrossViewModel> {

    public static final String EXTRA_MOVIE_ID = "movie_id";

    @Override
    protected int getContentView() {
        return R.layout.activity_movie_gross;
    }

    @Override
    protected void initView() {
        mBinding.actionbar.setOnBackListener(() -> onBackPressed());
        mBinding.actionbar.setOnMenuItemListener(menuId -> {
            switch (menuId) {
                case R.id.menu_add:
                    editGross(null);
                    break;
                case R.id.menu_delete:
                    mBinding.actionbar.showConfirmStatus(menuId);
                    break;
                case R.id.menu_edit:
                    mBinding.actionbar.showConfirmStatus(menuId);
                    break;
            }
        });
    }

    @Override
    protected MovieGrossViewModel createViewModel() {
        return ViewModelProviders.of(this).get(MovieGrossViewModel.class);
    }

    @Override
    protected void initData() {
        mModel.titleText.observe(this, s -> mBinding.actionbar.setTitle(s));

        long movieId = getIntent().getLongExtra(EXTRA_MOVIE_ID, -1);
        mModel.loadMovie(movieId);

        mModel.editObserver.observe(this, gross -> editGross(gross));

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.group_ft, new GrossTabFragment(), "GrossTabFragment")
                .commit();
    }

    private void editGross(Gross gross) {
        EditGrossFragment content = new EditGrossFragment();
        content.setGross(gross);
        content.setMovie(mModel.getMovie());
        content.setOnGrossListener(gross1 -> mModel.onGrossChanged(gross1));
        DraggableDialogFragment dialog = new DraggableDialogFragment.Builder()
                .setTitle("Gross")
                .setContentFragment(content)
                .build();
        dialog.show(getSupportFragmentManager(), "EditGross");
    }

}
