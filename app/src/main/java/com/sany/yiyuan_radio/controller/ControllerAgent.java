package com.sany.yiyuan_radio.controller;

import android.content.Context;
import android.util.Log;

import androidx.annotation.IntDef;

import com.sany.data.server.canframe.CanFrameCollector;
import com.sany.data.server.netframe.NetFrameCollector;
import com.sany.fuel_client.FuelDataDevice;
import com.sany.fuel_client.FuelDataListener;
import com.sany.zn.tccp.ser.service.CanInfoDevice;
import com.sany.zn.tccp.ser.service.DataByteBean;
import com.sany.zn.tccp.ser.service.IDataListener;
import com.sany.zn.tccp.ser.service.NetInfoDevice;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 日期：2021/1/19 10:04
 * <br>
 * 描述：连接控制器的代理
 *
 * @author
 */
public class ControllerAgent {
    private static final String TAG = "ControllerAgent";

    private final Context mContext;
    /**
     * 控制器进程网口数据
     */
    private NetInfoDevice mNetInfoDevice;
    private final IDataListener mNetDataListener = new IDataListener() {
        @Override
        public void strCallback(String s) {
            Log.d(TAG, "[NAIPIQ]mNetDataListener.strCallback: " + s);
        }

        @Override
        public void dataCallback(DataByteBean dataByteBean) {
            if (dataByteBean == null) {
                Log.e(TAG, "[NAIPIQ]mNetDataListener.dataCallback: dataByteBean is null.");
                return;
            }
            NetFrameCollector.setRawBytes(dataByteBean.getBdata());
        }
    };

    public ControllerAgent(Context mContext) {
        this.mContext = mContext;
    }


    /**
     * 连接控制器进程
     */
    public void connectControllerService(@DataType int type) {
        Log.d(TAG, "[NAIPIQ]connectControllerService: type=" + type);
        switch (type) {
            case DataType.NET:
                mNetInfoDevice = NetInfoDevice.getInstance(mContext);
                mNetInfoDevice.registerListener(mNetDataListener);
                break;
            default:
        }
    }

    public void unbindControllerService(@DataType int type) {
        Log.d(TAG, "[NAIPIQ]unbindControllerService: type=" + type);
        switch (type) {
            case DataType.NET:
                mNetInfoDevice.unregisterListener(mNetDataListener);
                mNetInfoDevice.unBindService();
                break;
            default:
        }

    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DataType.NET})
    public @interface DataType {
        /**
         * 网口
         */
        int NET = 0;
    }
}
