package com.moko.lw008.dialog;

import android.content.Context;
import android.widget.SeekBar;

import com.moko.lw008.databinding.Lw008DialogSensitivityBinding;

public class TriggerSensitivityDialog extends BaseDialog<Lw008DialogSensitivityBinding> implements SeekBar.OnSeekBarChangeListener {
    private int sensitivity;


    public TriggerSensitivityDialog(Context context) {
        super(context);
    }

    @Override
    protected Lw008DialogSensitivityBinding getViewBind() {
        return Lw008DialogSensitivityBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate() {
        int progress = sensitivity;
        String value = String.valueOf(progress);
        mBind.tvSensitivityValue.setText(value);
        mBind.sbSensitivity.setProgress(progress);
        mBind.sbSensitivity.setOnSeekBarChangeListener(this);
        mBind.tvCancel.setOnClickListener(v -> {
            dismiss();
        });
        mBind.tvEnsure.setOnClickListener(v -> {
            int sensitivity = mBind.sbSensitivity.getProgress();
            dismiss();
            if (sensitivityListener != null)
                sensitivityListener.onEnsure(sensitivity);
        });
    }

    private SensitivityListener sensitivityListener;

    public void setOnSensitivityClicked(SensitivityListener sensitivityListener) {
        this.sensitivityListener = sensitivityListener;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        String value = String.valueOf(progress);
        mBind.tvSensitivityValue.setText(value);
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

    public void setData(int sensitivity) {
        this.sensitivity = sensitivity;
    }

}
