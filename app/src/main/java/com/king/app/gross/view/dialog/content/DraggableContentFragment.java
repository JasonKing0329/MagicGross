package com.king.app.gross.view.dialog.content;

import android.databinding.ViewDataBinding;

import com.king.app.gross.base.BaseBindingFragment;
import com.king.app.gross.view.dialog.DraggableHolder;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/7 10:05
 */
public abstract class DraggableContentFragment<T extends ViewDataBinding> extends BaseBindingFragment<T> {

    private DraggableHolder dialogHolder;

    public void setDialogHolder(DraggableHolder dialogHolder) {
        this.dialogHolder = dialogHolder;
    }

    protected void dismiss() {
        dialogHolder.dismiss();
    }
}
