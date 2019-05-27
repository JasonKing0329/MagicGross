package com.king.app.gross.base;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Toast;

import com.king.app.gross.R;
import com.king.app.gross.utils.ScreenUtils;
import com.king.app.gross.view.dialog.ProgressDialogFragment;

/**
 * 描述:
 * <p/>作者：景阳
 * <p/>创建时间: 2018/1/29 13:05
 */
public abstract class BaseActivity extends AppCompatActivity {

    private ProgressDialogFragment progressDialogFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //prevent from task manager take screenshot
        //also prevent from system screenshot
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        updateStatusBarColor();

        super.onCreate(savedInstanceState);
    }

    /**
     * 子类可单独覆写
     * @return
     */
    protected void updateStatusBarColor() {
        ScreenUtils.setStatusBarColor(this, getResources().getColor(R.color.status_bar_bg));
    }

    protected abstract int getContentView();

    protected abstract void initView();

    public void showConfirmMessage(String msg, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(this)
                .setTitle(null)
                .setMessage(msg)
                .setPositiveButton(getString(R.string.ok), listener)
                .show();
    }

    public void showConfirmCancelMessage(String msg, DialogInterface.OnClickListener okListener
            , DialogInterface.OnClickListener cancelListener) {
        new AlertDialog.Builder(this)
                .setTitle(null)
                .setMessage(msg)
                .setPositiveButton(getString(R.string.ok), okListener)
                .setNegativeButton(getString(R.string.cancel), cancelListener)
                .show();
    }

    public void showYesNoMessage(String msg, DialogInterface.OnClickListener okListener
            , DialogInterface.OnClickListener cancelListener) {
        new AlertDialog.Builder(this)
                .setTitle(null)
                .setMessage(msg)
                .setPositiveButton(getString(R.string.yes), okListener)
                .setNegativeButton(getString(R.string.no), cancelListener)
                .show();
    }

    public void showProgress(String msg) {
        progressDialogFragment = new ProgressDialogFragment();
        if (TextUtils.isEmpty(msg)) {
            msg = getResources().getString(R.string.loading);
        }
        progressDialogFragment.setMessage(msg);
        progressDialogFragment.show(getSupportFragmentManager(), "ProgressDialogFragment");
    }

    public void dismissProgress() {
        if (progressDialogFragment != null) {
            progressDialogFragment.dismissAllowingStateLoss();
        }
    }

    public void showMessageShort(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void showMessageLong(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}
