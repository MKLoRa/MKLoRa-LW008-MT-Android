package com.moko.lw008.dialog;

import android.content.Context;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.lw008.R;
import com.moko.lw008.R2;

import butterknife.BindView;
import butterknife.OnClick;

public class TriggerSensitivityDialog extends BaseDialog<Integer> implements SeekBar.OnSeekBarChangeListener {

    @BindView(R2.id.sb_sensitivity)
    SeekBar sbSensitivity;
    @BindView(R2.id.tv_sensitivity_value)
    TextView tvSensitivityValue;

    public TriggerSensitivityDialog(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.lw008_dialog_sensitivity;
    }

    @Override
    protected void renderConvertView(View convertView, Integer sensitivity) {
        int progress = sensitivity;
        String value = String.valueOf(progress);
        tvSensitivityValue.setText(value);
        sbSensitivity.setProgress(progress);
        sbSensitivity.setOnSeekBarChangeListener(this);
    }

    private SensitivityListener sensitivityListener;

    public void setOnSensitivityClicked(SensitivityListener sensitivityListener) {
        this.sensitivityListener = sensitivityListener;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        String value = String.valueOf(progress);
        tvSensitivityValue.setText(value);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public interface SensitivityListener {

        void onEnsure(int sensitivity);
    }

    @OnClick(R2.id.tv_cancel)
    public void onCancel(View view) {
        dismiss();
    }

    @OnClick(R2.id.tv_ensure)
    public void onEnsure(View view) {
        int progress = sbSensitivity.getProgress();
        int sensitivity = progress;
        dismiss();
        if (sensitivityListener != null)
            sensitivityListener.onEnsure(sensitivity);
    }
}
