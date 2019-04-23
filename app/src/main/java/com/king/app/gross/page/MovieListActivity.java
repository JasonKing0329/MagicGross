package com.king.app.gross.page;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.PopupMenu;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseRecyclerAdapter;
import com.king.app.gross.base.MvvmActivity;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.databinding.ActivityMovieListBinding;
import com.king.app.gross.model.compare.CompareInstance;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.page.adapter.AbsMovieListAdapter;
import com.king.app.gross.page.adapter.MovieListAdapter;
import com.king.app.gross.page.adapter.MovieListWildAdapter;
import com.king.app.gross.page.adapter.SelectedMovieAdapter;
import com.king.app.gross.utils.ScreenUtils;
import com.king.app.gross.view.dialog.AlertDialogFragment;
import com.king.app.gross.view.dialog.DraggableDialogFragment;
import com.king.app.gross.view.dialog.content.EditMovieFragment;
import com.king.app.gross.viewmodel.MovieListViewModel;
import com.king.app.gross.viewmodel.bean.MovieGridItem;
import com.king.app.jactionbar.OnConfirmListener;

import java.util.List;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 15:09
 */
public class MovieListActivity extends MvvmActivity<ActivityMovieListBinding, MovieListViewModel> {

    private final int ID_SELECT_MODE = 0;

    public static final String SELECT_MODE = "select_mode";
    public static final String RESP_SELECT_RESULT = "resp_select_result";


//    private MovieListAdapter adapter;

    private AbsMovieListAdapter adapter;

    private boolean isEditMode;

    private SelectedMovieAdapter selectedMovieAdapter;

    @Override
    protected int getContentView() {
        return R.layout.activity_movie_list;
    }

    @Override
    protected void initView() {
        mBinding.setModel(mModel);
        mBinding.executePendingBindings();

        mBinding.groupSelectContainer.setVisibility(View.GONE);

        mBinding.rvMovies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mBinding.rvMovies.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                // 预留空位应对弹出compare选择框
                int size = mModel.getMovieNumber() % 2 == 1 ? mModel.getMovieNumber() - 1 : mModel.getMovieNumber() - 2;
                if (parent.getChildAdapterPosition(view) >= size) {
                    outRect.bottom = getResources().getDimensionPixelOffset(R.dimen.select_movie_height);
                }
            }
        });
//        GridLayoutManager manager = new GridLayoutManager(this, 2);
////        mBinding.rvMovies.setLayoutManager(manager);
////        mBinding.rvMovies.addItemDecoration(new RecyclerView.ItemDecoration() {
////            @Override
////            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
////                // 预留空位应对弹出compare选择框
////                int size = mModel.getMovieNumber() % 2 == 1 ? mModel.getMovieNumber() - 1 : mModel.getMovieNumber() - 2;
////                if (parent.getChildAdapterPosition(view) >= size) {
////                    outRect.bottom = getResources().getDimensionPixelOffset(R.dimen.select_movie_height);
////                }
////            }
////        });

        mBinding.actionbar.setOnBackListener(() -> onBackPressed());
        mBinding.actionbar.setOnMenuItemListener(menuId -> {
            switch (menuId) {
                case R.id.menu_add:
                    editMovie(null);
                    break;
                case R.id.menu_delete:
                    adapter.setSelectionMode(true);
                    adapter.notifyDataSetChanged();
                    mBinding.actionbar.showConfirmStatus(menuId);
                    break;
                case R.id.menu_edit:
                    isEditMode = true;
                    mBinding.actionbar.showConfirmStatus(menuId);
                    break;
                case R.id.menu_compare:
                    showCompare();
                    mBinding.actionbar.showConfirmStatus(menuId);
                    break;
                case R.id.menu_gross_type:
                    selectGrossType();
                    break;
            }
        });
        mBinding.actionbar.setOnConfirmListener(new OnConfirmListener() {
            @Override
            public boolean disableInstantDismissConfirm() {
                return false;
            }

            @Override
            public boolean disableInstantDismissCancel() {
                return false;
            }

            @Override
            public boolean onConfirm(int actionId) {
                switch (actionId) {
                    case ID_SELECT_MODE:
                        setSelectResult();
                        finish();
                        break;
                    case R.id.menu_delete:
                        mModel.delete();
                        return false;
                    case R.id.menu_edit:
                        isEditMode = false;
                        break;
                    case R.id.menu_compare:
                        // 目前最多只支持3个
                        int compareSize = CompareInstance.getInstance().getMovieList().size();
                        if (compareSize < 2) {
                            showMessageShort("Please select at least 2 movies to compare");
                            return false;
                        }
                        startActivity(new Intent().setClass(MovieListActivity.this, CompareActivity.class));
                        hideCompare();
                        break;
                }
                return true;
            }

            @Override
            public boolean onCancel(int actionId) {
                switch (actionId) {
                    case ID_SELECT_MODE:
                        finish();
                        break;
                    case R.id.menu_delete:
                        adapter.setSelectionMode(false);
                        adapter.notifyDataSetChanged();
                        mBinding.actionbar.cancelConfirmStatus();
                        break;
                    case R.id.menu_edit:
                        isEditMode = false;
                        break;
                    case R.id.menu_compare:
                        hideCompare();
                        mBinding.actionbar.cancelConfirmStatus();
                        break;
                }
                return true;
            }
        });

        mBinding.actionbar.registerPopupMenu(R.id.menu_sort);
        mBinding.actionbar.setPopupMenuProvider((iconMenuId, anchorView) -> {
            switch (iconMenuId) {
                case R.id.menu_sort:
                    return createSortPopup(anchorView);
            }
            return null;
        });

        if (isSelectMode()) {
            mBinding.actionbar.showConfirmStatus(ID_SELECT_MODE);
        }
    }

    private void setSelectResult() {
        Intent intent = new Intent();
        intent.putCharSequenceArrayListExtra(RESP_SELECT_RESULT, mModel.getSelectedItems());
        setResult(RESULT_OK, intent);
    }

    private boolean isSelectMode() {
        return getIntent().getBooleanExtra(SELECT_MODE, false);
    }

    private void selectGrossType() {
        new AlertDialogFragment()
                .setItems(getResources().getStringArray(R.array.region), (dialog, which) -> mModel.updateRegionInList(which))
                .show(getSupportFragmentManager(), "AlertDialogFragment");
    }

    private void showCompare() {
        if (mBinding.groupSelectContainer.getVisibility() == View.GONE) {
            initCompare();
            mBinding.groupSelectContainer.setVisibility(View.VISIBLE);

            TranslateAnimation animation = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0
                , TranslateAnimation.RELATIVE_TO_SELF, 1, TranslateAnimation.RELATIVE_TO_SELF, 0);
            animation.setDuration(500);
            animation.setInterpolator(new BounceInterpolator());
            mBinding.groupSelectContainer.startAnimation(animation);
        }
    }

    private void hideCompare() {
        TranslateAnimation animation = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0
                , TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 1);
        animation.setDuration(500);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mBinding.groupSelectContainer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mBinding.groupSelectContainer.startAnimation(animation);
    }

    private void initCompare() {
        if (selectedMovieAdapter == null) {
            mBinding.ivCloseSelect.setOnClickListener(view -> {
                hideCompare();
                mBinding.actionbar.cancelConfirmStatus();
            });
            mBinding.ivCloseSelect.setColorFilter(getResources().getColor(R.color.actionbar_bg), PorterDuff.Mode.SRC_IN);
            LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            mBinding.rvSelectedMovies.setLayoutManager(manager);
            selectedMovieAdapter = new SelectedMovieAdapter();
            selectedMovieAdapter.setOnDeleteListener(movie -> {
                CompareInstance.getInstance().removeCompareMovie(movie);
                selectedMovieAdapter.setList(CompareInstance.getInstance().getMovieList());
                selectedMovieAdapter.notifyDataSetChanged();
            });
            mBinding.rvSelectedMovies.setAdapter(selectedMovieAdapter);
        }
    }

    private PopupMenu createSortPopup(View anchorView) {
        PopupMenu menu = new PopupMenu(this, anchorView);
        menu.getMenuInflater().inflate(R.menu.movie_sort, menu.getMenu());
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_sort_date:
                    mModel.changeSortType(AppConstants.MOVIE_SORT_DATE);
                    break;
                case R.id.menu_sort_name:
                    mModel.changeSortType(AppConstants.MOVIE_SORT_NAME);
                    break;
            }
            return true;
        });
        return menu;
    }

    @Override
    protected MovieListViewModel createViewModel() {
        return ViewModelProviders.of(this).get(MovieListViewModel.class);
    }

    @Override
    protected void initData() {
        mModel.moviesObserver.observe(this, list -> showMovies(list));
        mModel.deleteObserver.observe(this, delete -> {
            adapter.setSelectionMode(false);
            mBinding.actionbar.cancelConfirmStatus();
            mModel.loadMovies();
        });
        mModel.loadMovies();
    }

    private void showMovies(List<MovieGridItem> list) {
        if (adapter == null) {
            adapter = new MovieListWildAdapter();
            adapter.setSelectionMode(isSelectMode());
            adapter.setList(list);
            adapter.setCheckMap(mModel.getCheckMap());
            adapter.setOnItemClickListener((BaseRecyclerAdapter.OnItemClickListener<MovieGridItem>) (view, position, data) -> {
                if (isEditMode) {
                    editMovie(data.getBean());
                }
                else if (mBinding.groupSelectContainer.getVisibility() == View.VISIBLE) {
                    CompareInstance.getInstance().addCompareMovie(data.getBean());
                    selectedMovieAdapter.setList(CompareInstance.getInstance().getMovieList());
                    selectedMovieAdapter.notifyDataSetChanged();
                }
                else {
                    showMovieGross(data.getBean());
                }
            });
            mBinding.rvMovies.setAdapter(adapter);
        }
        else {
            adapter.setList(list);
            adapter.notifyDataSetChanged();
        }
    }

    private void editMovie(Movie movie) {
        EditMovieFragment content = new EditMovieFragment();
        content.setEditMovie(movie);
        DraggableDialogFragment dialog = new DraggableDialogFragment.Builder()
                .setTitle(movie == null ? "New movie":"Edit movie")
                .setMaxHeight(ScreenUtils.getScreenHeight() * 4 / 5)
                .setContentFragment(content)
                .build();
        dialog.setOnDismissListener(dialog1 -> mModel.loadMovies());
        dialog.show(getSupportFragmentManager(), "EditMovie");
    }

    private void showMovieGross(Movie data) {
        Intent intent = new Intent().setClass(this, MovieGrossActivity.class);
        intent.putExtra(MovieGrossActivity.EXTRA_MOVIE_ID, data.getId());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CompareInstance.getInstance().destroy();
    }
}
