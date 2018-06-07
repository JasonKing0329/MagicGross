package com.king.app.gross.page;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.king.app.gross.R;
import com.king.app.gross.base.BaseRecyclerAdapter;
import com.king.app.gross.base.MvvmActivity;
import com.king.app.gross.databinding.ActivityMovieListBinding;
import com.king.app.gross.model.entity.Movie;
import com.king.app.gross.page.adapter.MovieListAdapter;
import com.king.app.gross.view.dialog.DraggableDialogFragment;
import com.king.app.gross.view.dialog.content.EditMovieFragment;
import com.king.app.gross.viewmodel.MovieListViewModel;
import com.king.app.jactionbar.OnConfirmListener;

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
        binding.setModel(viewModel);
        binding.executePendingBindings();

        GridLayoutManager manager = new GridLayoutManager(this, 2);
        binding.rvMovies.setLayoutManager(manager);

        binding.actionbar.setOnBackListener(() -> onBackPressed());
        binding.actionbar.setOnMenuItemListener(menuId -> {
            switch (menuId) {
                case R.id.menu_add:
                    editMovie(null);
                    break;
                case R.id.menu_delete:
                    adapter.setSelectionMode(true);
                    adapter.notifyDataSetChanged();
                    binding.actionbar.showConfirmStatus(menuId);
                    break;
                case R.id.menu_edit:
                    isEditMode = true;
                    binding.actionbar.showConfirmStatus(menuId);
                    break;
            }
        });
        binding.actionbar.setOnConfirmListener(new OnConfirmListener() {
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
                        viewModel.delete();
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
                        binding.actionbar.cancelConfirmStatus();
                        break;
                    case R.id.menu_edit:
                        isEditMode = false;
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected MovieListViewModel createViewModel() {
        return ViewModelProviders.of(this).get(MovieListViewModel.class);
    }

    @Override
    protected void initData() {
        viewModel.moviesObserver.observe(this, list -> showMovies(list));
        viewModel.deleteObserver.observe(this, delete -> {
            adapter.setSelectionMode(false);
            binding.actionbar.cancelConfirmStatus();
            viewModel.loadMovies();
        });
        viewModel.loadMovies();
    }

    private void showMovies(List<Movie> list) {
        if (adapter == null) {
            adapter = new MovieListAdapter();
            adapter.setList(list);
            adapter.setCheckMap(viewModel.getCheckMap());
            adapter.setOnItemClickListener((view, position, data) -> {
                if (isEditMode) {
                    editMovie(data);
                }
            });
            binding.rvMovies.setAdapter(adapter);
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
        dialog.setOnDismissListener(dialog1 -> viewModel.loadMovies());
        dialog.show(getSupportFragmentManager(), "EditMovie");
    }

}
