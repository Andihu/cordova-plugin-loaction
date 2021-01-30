package com.microfountain.location;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;

/**
 * Copyright (C), 2015-2021
 * FileName: LocationBaseActivity
 * Author: hujian
 * Date: 2021/1/16 12:49
 * History:
 * <author> <time> <version> <desc>
 */
public abstract class LocationBaseActivity extends Activity {

    private static final int OPEN_SET_REQUEST_CODE = 100;

    private final String[] permissions = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,};

    private LocationClient locationClient;

    private final BDAbstractLocationListener bdAbstractLocationListener = new BDAbstractLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location.getLocType() == BDLocation.TypeNetWorkLocation || location.getLocType() == BDLocation.TypeGpsLocation || location.getLocType() == BDLocation.TypeOffLineLocation) {
                onReceiveLocationSuccess(location);
            } else {
                onReceiveLocationFailed(location);
                Toast.makeText(LocationBaseActivity.this, "定位失败！", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public abstract void initView();
    public abstract void onStartLocation();
    public abstract void onReceiveLocationSuccess(BDLocation location);
    public abstract void onReceiveLocationFailed(BDLocation location);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeStatusBarTransparent(this);
        initView();
        initPermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == OPEN_SET_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "未拥有相应权限", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                startLocation();
            }
        }
    }
    public void makeStatusBarTransparent(Activity activity) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int option = window.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        window.getDecorView().setSystemUiVisibility(option);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    protected void startLocation() {
        onStartLocation();
        locationClient = new LocationClient(this);
        locationClient.registerLocationListener(bdAbstractLocationListener);
        LocationClientOption locationClientOption = new LocationClientOption();
        locationClientOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        locationClientOption.setCoorType(CoordType.GCJ02.name());
        locationClientOption.setOpenGps(true);
        locationClientOption.setIsNeedLocationDescribe(true);
        locationClientOption.setOnceLocation(true);
        locationClientOption.setIsNeedLocationPoiList(true);
        locationClientOption.setIsNeedAddress(true);
        locationClient.setLocOption(locationClientOption);
        locationClient.start();
    }

    private void stopLocation() {
        if (null != locationClient) {
            locationClient.stop();
        }
    }

    private void initPermissions() {
        if (lacksPermission(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, OPEN_SET_REQUEST_CODE);
        } else {
            startLocation();
        }
    }

    public boolean lacksPermission(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

}
