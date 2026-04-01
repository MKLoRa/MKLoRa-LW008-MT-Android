package com.moko.lw008.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.ble.lib.task.OrderTask;
import com.moko.lw008.activity.DeviceInfoActivity;
import com.moko.lw008.databinding.Lw008FragmentGeneralBinding;
import com.moko.lw008.utils.Utils;
import com.moko.support.lw008.LoRaLW008MokoSupport;
import com.moko.support.lw008.OrderTaskAssembler;

import java.util.ArrayList;
import java.util.List;

public class GeneralFragment extends Fragment {
    private static final String TAG = GeneralFragment.class.getSimpleName();
    private Lw008FragmentGeneralBinding mBind;

    private DeviceInfoActivity activity;

    public GeneralFragment() {
    }


    public static GeneralFragment newInstance() {
        GeneralFragment fragment = new GeneralFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = Lw008FragmentGeneralBinding.inflate(inflater, container, false);
        activity = (DeviceInfoActivity) getActivity();
        return mBind.getRoot();
    }

    public void setHeartbeatInterval(int interval) {
        mBind.etHeartbeatInterval.setText(String.valueOf(interval));
    }

    public void setAxisEnable(int enable) {
        mBind.cbAxisEnable.setChecked(enable == 1);
    }

    public boolean isValid() {
        final String intervalStr = mBind.etHeartbeatInterval.getText().toString();
        if (TextUtils.isEmpty(intervalStr))
            return false;
        final int interval = Integer.parseInt(intervalStr);
        if (interval < 300 || interval > 86400) {
            return false;
        }
        return true;
    }

    public void saveParams() {
        final String intervalStr = mBind.etHeartbeatInterval.getText().toString();
        final int interval = Integer.parseInt(intervalStr);
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setHeartBeatInterval(interval));
        if (Utils.isNewFunction(activity.mVersion, "V1.0.9"))
            orderTasks.add(OrderTaskAssembler.setAxisEnable(mBind.cbAxisEnable.isChecked() ? 1 : 0));
        LoRaLW008MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setAxisShown(String version) {
        if (Utils.isNewFunction(version, "V1.0.9"))
            mBind.cbAxisEnable.setVisibility(View.VISIBLE);
    }
}
