package com.moko.lw008.dialog;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.moko.lw008.R;
import com.moko.lw008.R2;
import com.moko.lw008.utils.ToastUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class PasswordDialog extends BaseDialog<String> {
    private final String FILTER_ASCII = "[ -~]*";
    @BindView(R2.id.et_password)
    EditText etPassword;

    public PasswordDialog(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.lw008_dialog_password;
    }

    @Override
    protected void renderConvertView(View convertView, String password) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (!(source + "").matches(FILTER_ASCII)) {
                    return "";
                }

                return null;
            }
        };
        etPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8), filter});
        if (!TextUtils.isEmpty(password)) {
            etPassword.setText(password);
            etPassword.setSelection(password.length());
        }
    }

    @OnClick(R2.id.tv_password_cancel)
    public void onCancel(View view) {
        dismiss();
        if (passwordClickListener != null) {
            passwordClickListener.onDismiss();
        }
    }

    @OnClick(R2.id.tv_password_ensure)
    public void onEnsure(View view) {
        if (TextUtils.isEmpty(etPassword.getText().toString())) {
            ToastUtils.showToast(getContext(), getContext().getString(R.string.password_null));
            return;
        }
        if (etPassword.getText().toString().length() != 8) {
            ToastUtils.showToast(getContext(), getContext().getString(R.string.main_password_length));
            return;
        }
        dismiss();
        if (passwordClickListener != null)
            passwordClickListener.onEnsureClicked(etPassword.getText().toString());
    }

    private PasswordClickListener passwordClickListener;

    public void setOnPasswordClicked(PasswordClickListener passwordClickListener) {
        this.passwordClickListener = passwordClickListener;
    }

    public interface PasswordClickListener {

        void onEnsureClicked(String password);

        void onDismiss();
    }

    public void showKeyboard() {
        if (etPassword != null) {
            //设置可获得焦点
            etPassword.setFocusable(true);
            etPassword.setFocusableInTouchMode(true);
            //请求获得焦点
            etPassword.requestFocus();
            //调用系统输入法
            InputMethodManager inputManager = (InputMethodManager) etPassword
                    .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(etPassword, 0);
        }
    }
}
