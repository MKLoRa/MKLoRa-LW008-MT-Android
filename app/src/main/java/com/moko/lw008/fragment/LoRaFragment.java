package com.moko.lw008.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moko.lw008.R;
import com.moko.lw008.R2;
import com.moko.lw008.activity.DeviceInfoActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoRaFragment extends Fragment {
    private static final String TAG = LoRaFragment.class.getSimpleName();
    @BindView(R2.id.tv_lora_status)
    TextView tvLoraStatus;
    @BindView(R2.id.tv_lora_info)
    TextView tvLoraInfo;


    private DeviceInfoActivity activity;

    public LoRaFragment() {
    }


    public static LoRaFragment newInstance() {
        LoRaFragment fragment = new LoRaFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.lw008_fragment_lora, container, false);
        ButterKnife.bind(this, view);
        activity = (DeviceInfoActivity) getActivity();
        return view;
    }

    public void setLoRaInfo(String loraInfo) {
        tvLoraInfo.setText(loraInfo);
    }

    public void setLoraStatus(int networkCheck) {
        String networkCheckDisPlay = "";
        switch (networkCheck) {
            case 0:
                networkCheckDisPlay = "Connecting";
                break;
            case 1:
                networkCheckDisPlay = "Connected";
                break;
        }
        tvLoraStatus.setText(networkCheckDisPlay);
    }
}
