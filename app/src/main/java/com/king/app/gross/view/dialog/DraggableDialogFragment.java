package com.king.app.gross.view.dialog;

import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.king.app.gross.R;
import com.king.app.gross.databinding.DialogBaseBinding;
import com.king.app.gross.utils.DebugLog;
import com.king.app.gross.utils.ScreenUtils;
import com.king.app.gross.view.dialog.content.DraggableContentFragment;

/**
 * 描述: 可拖拽移动的base dialog框架
 * <p/>作者：景阳
 * <p/>创建时间: 2017/7/20 11:45
 */
public class DraggableDialogFragment extends BaseDialogFragment implements DraggableHolder {

    private Point startPoint, touchPoint;

    private String title;

    private int backgroundColor;

    private boolean hideClose;

    private boolean showDelete;

    private DraggableContentFragment contentFragment;

    private int maxHeight;

    private View.OnClickListener onDeleteListener;

    private DialogBaseBinding binding;

    protected DialogInterface.OnDismissListener onDismissListener;

    @Override
    protected View onSubCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DialogBaseBinding.inflate(inflater);
        initView();
        return binding.getRoot();
    }

    protected void initView() {

        if (title != null) {
            binding.tvTitle.setText(title);
        }
        if (backgroundColor != 0) {
            GradientDrawable drawable = (GradientDrawable) binding.groupDialog.getBackground();
            drawable.setColor(backgroundColor);
        }
        if (hideClose) {
            binding.ivClose.setVisibility(View.GONE);
        }
        if (showDelete) {
            binding.ivDelete.setVisibility(View.VISIBLE);
            binding.ivDelete.setOnClickListener(onDeleteListener);
        }

        initDragParams();

        if (contentFragment != null) {
            contentFragment.setDialogHolder(this);
            replaceContentFragment(contentFragment, "ContentView");
        }

        binding.groupFtContainer.post(() -> {
            DebugLog.e("groupFtContent height=" + binding.groupFtContainer.getHeight());
            limitMaxHeight();
        });

        binding.ivClose.setOnClickListener(v -> dismissAllowingStateLoss());
    }

    protected void replaceContentFragment(DraggableContentFragment target, String tag) {
        if (target != null) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.replace(R.id.group_ft_container, target, tag);
            ft.commit();
        }
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

    private void limitMaxHeight() {
        int maxContentHeight = getMaxHeight();
        if (binding.groupFtContainer.getHeight() > maxContentHeight) {
            ViewGroup.LayoutParams params = binding.groupFtContainer.getLayoutParams();
            params.height = maxContentHeight;
            binding.groupFtContainer.setLayoutParams(params);
        }
    }

    /**
     * 子类可选择覆盖
     * @return
     */
    protected int getMaxHeight() {
        if (maxHeight != 0) {
            return maxHeight;
        }
        else {
            return ScreenUtils.getScreenHeight() * 3 / 5;
        }
    }

    private void initDragParams() {
        touchPoint = new Point();
        startPoint = new Point();
        binding.groupDialog.setOnTouchListener(new DialogTouchListener());
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setHideClose(boolean hideClose) {
        this.hideClose = hideClose;
    }

    public void setContentFragment(DraggableContentFragment contentFragment) {
        this.contentFragment = contentFragment;
    }

    public void setShowDelete(boolean showDelete) {
        this.showDelete = showDelete;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public void setOnDeleteListener(View.OnClickListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    private class Point {
        float x;
        float y;
    }

    /**
     * move dialog
     */
    private class DialogTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    float x = event.getRawX();//
                    float y = event.getRawY();
                    startPoint.x = x;
                    startPoint.y = y;
                    DebugLog.d("ACTION_DOWN x=" + x + ", y=" + y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    x = event.getRawX();
                    y = event.getRawY();
                    touchPoint.x = x;
                    touchPoint.y = y;
                    float dx = touchPoint.x - startPoint.x;
                    float dy = touchPoint.y - startPoint.y;

                    move((int) dx, (int) dy);

                    startPoint.x = x;
                    startPoint.y = y;
                    break;
                case MotionEvent.ACTION_UP:
                    break;

                default:
                    break;
            }
            return true;
        }
    }

    public static class Builder {

        private String title;

        private int backgroundColor;

        private boolean hideClose;

        private int maxHeight;

        private boolean showDelete;

        private View.OnClickListener onDeleteListener;

        private DraggableContentFragment contentFragment;

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder setHideClose(boolean hideClose) {
            this.hideClose = hideClose;
            return this;
        }

        public Builder setShowDelete(boolean showDelete) {
            this.showDelete = showDelete;
            return this;
        }

        public Builder setContentFragment(DraggableContentFragment contentFragment) {
            this.contentFragment = contentFragment;
            return this;
        }

        public Builder setMaxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
            return this;
        }

        public Builder setOnDeleteListener(View.OnClickListener onDeleteListener) {
            this.onDeleteListener = onDeleteListener;
            return this;
        }

        public DraggableDialogFragment build() {
            DraggableDialogFragment fragment = new DraggableDialogFragment();
            fragment.setHideClose(hideClose);
            fragment.setShowDelete(showDelete);
            fragment.setOnDeleteListener(onDeleteListener);
            if (title != null) {
                fragment.setTitle(title);
            }
            if (backgroundColor != 0) {
                fragment.setBackgroundColor(backgroundColor);
            }
            if (contentFragment != null) {
                fragment.setContentFragment(contentFragment);
            }
            if (maxHeight != 0) {
                fragment.setMaxHeight(maxHeight);
            }
            return fragment;
        }
    }
}
