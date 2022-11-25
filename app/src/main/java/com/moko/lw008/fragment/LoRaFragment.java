package com.moko.lw008.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.lw008.activity.DeviceInfoActivity;
import com.moko.lw008.databinding.Lw008FragmentLoraBinding;

public class LoRaFragment extends Fragment {
    private static final String TAG = LoRaFragment.class.getSimpleName();
    private Lw008FragmentLoraBinding mBind;


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
        mBind = Lw008FragmentLoraBinding.inflate(inflater, container, false);
        activity = (DeviceInfoActivity) getActivity();
        return mBind.getRoot();
    }

    public void setLoRaInfo(String loraInfo) {
        mBind.tvLoraInfo.setText(loraInfo);
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
        mBind.tvLoraStatus.setText(networkCheckDisPlay);
    }
}
