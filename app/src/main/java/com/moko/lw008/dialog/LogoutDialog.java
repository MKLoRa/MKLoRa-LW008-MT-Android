package com.moko.lw008.dialog;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.moko.lw008.AppConstants;
import com.moko.lw008.R;
import com.moko.lw008.databinding.DialogLogoutBinding;
import com.moko.lw008.utils.SPUtiles;

public class LogoutDialog extends MokoBaseDialog<DialogLogoutBinding> {
    public static final String TAG = LogoutDialog.class.getSimpleName();


    @Override
    protected DialogLogoutBinding getViewBind(LayoutInflater inflater, ViewGroup container) {
        return DialogLogoutBinding.inflate(inflater, container, false);
    }

    @Override
    protected void onCreateView() {
        String acc = SPUtiles.getStringValue(getContext(), AppConstants.SP_LOGIN_ACCOUNT, "");
        mBind.tvUsername.setText(acc);
        mBind.tvCancel.setOnClickListener(v -> {
            dismiss();
        });
        mBind.tvExit.setOnClickListener(v -> {
            dismiss();
            SPUtiles.setStringValue(getContext(), AppConstants.SP_LOGIN_PASSWORD, "");
            if (LogoutClickListener != null)
                LogoutClickListener.onExit();
        });
    }

    @Override
    public int getDialogStyle() {
        return R.style.LW008CenterDialog;
    }

    @Override
    public int getGravity() {
        return Gravity.CENTER;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public float getDimAmount() {
        return 0.7f;
    }

    @Override
    public boolean getCancelOutside() {
        return false;
    }

    @Override
    public boolean getCancellable() {
        return true;
    }

    private LogoutClickListener LogoutClickListener;

    public void setOnLogoutClicked(LogoutClickListener LogoutClickListener) {
        this.LogoutClickListener = LogoutClickListener;
    }

    public interface LogoutClickListener {

        void onExit();
    }
}
