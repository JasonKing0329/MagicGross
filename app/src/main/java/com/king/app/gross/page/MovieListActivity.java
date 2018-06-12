package com.king.app.gross.page;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.king.app.gross.R;
import com.king.app.gross.base.MvvmActivity;
import com.king.app.gross.conf.AppConstants;
import com.king.app.gross.databinding.ActivityMovieListBinding;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.page.adapter.MovieListAdapter;
import com.king.app.gross.view.dialog.DraggableDialogFragment;
import com.king.app.gross.view.dialog.content.EditMovieFragment;
import com.king.app.gross.viewmodel.MovieListViewModel;
import com.king.app.jactionbar.OnConfirmListener;
import com.king.app.jactionbar.PopupMenuProvider;

import java.util.List;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 15:09
 */
public class MovieListActivity extends MvvmActivity<ActivityMovieListBinding, MovieListViewModel> {

    private MovieListAdapter adapter;

    private boolean isEditMode;

    @Override
    protected int getContentView() {
        return R.layout.activity_movie_list;
    }

    @Override
    protected void initView() {
        mBinding.setModel(mModel);
        mBinding.executePendingBindings();

        GridLayoutManager manager = new GridLayoutManager(this, 2);
        mBinding.rvMovies.setLayoutManager(manager);

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
                    case R.id.menu_delete:
                        mModel.delete();
                        return false;
                    case R.id.menu_edit:
                        isEditMode = false;
                        break;
                }
                return true;
            }

            @Override
            public boolean onCancel(int actionId) {
                switch (actionId) {
                    case R.id.menu_delete:
                        adapter.setSelectionMode(false);
                        adapter.notifyDataSetChanged();
                        mBinding.actionbar.cancelConfirmStatus();
                        break;
                    case R.id.menu_edit:
                        isEditMode = false;
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

    private void showMovies(List<Movie> list) {
        if (adapter == null) {
            adapter = new MovieListAdapter();
            adapter.setList(list);
            adapter.setCheckMap(mModel.getCheckMap());
            adapter.setOnItemClickListener((view, position, data) -> {
                if (isEditMode) {
                    editMovie(data);
                }
                else {
                    showMovieGross(data);
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

}
