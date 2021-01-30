package com.microfountain.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.util.ArrayList;
import java.util.List;


import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;

public class LocationDetailActivity extends LocationBaseActivity implements OnGetGeoCoderResultListener {

    private static final String TAG = "LocationDetailActivity";

    private String longitude;
    private String latitude;
    private String label;
    private String content;
    private LatLng latLng;

    private View llDesc;
    private MapView mapView;

    private TextView tvName;
    private TextView tvDesc;
    private ImageButton ibBack;
    private ImageButton ibReset;

    private GeoCoder mCoder = null;
    private BaiduMap baiduMap;
    private BitmapDescriptor myLocationBitmapMarker;
    private BitmapDescriptor locationBitmapMarker;

    @Override
    public void initView() {
        View decorView = this.getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        getData();
        findId();
        init();
    }

    @Override
    public void onStartLocation() {

    }

    @Override
    public void onReceiveLocationSuccess(BDLocation location) {
        getLocationMsg(location);
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        addOneLocMarker(latLng, myLocationBitmapMarker);
    }

    @Override
    public void onReceiveLocationFailed(BDLocation location) {
        new com.microfountain.location.RequestDialog
                .Builder()
                .Context(LocationDetailActivity.this)
                .CancelText("取消")
                .ConfirmText("去设置")
                .Description("抱歉，现在无法确定您的位置， 请在位置设置中打开GPS和无线网络")
                .Listener(new com.microfountain.location.RequestDialog.OnDialogClickListener() {
                    @Override
                    public void onCancel() {
                        onBackPressed();
                    }

                    @Override
                    public void onConfirm() {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, 0); // 设置完成后返回到原来的界
                    }
                }).build().show();
    }

    private void getData() {
        Intent intent = getIntent();
        if (intent == null) return;
        Bundle extras = intent.getBundleExtra("location");
        longitude = extras.getString("longitude");
        latitude = extras.getString("latitude");
        label = extras.getString("label");
        content = extras.getString("content");
        if (TextUtils.isEmpty(longitude) || TextUtils.isEmpty(latitude)) {
            onBackPressed();
        }
    }

    private void findId() {
        Context appContext = getApplicationContext();
        Resources resource = appContext.getResources();
        String pkgName = appContext.getPackageName();
        int id_layout = resource.getIdentifier("activity_location_detail", "layout", pkgName);
        setContentView(id_layout);
        int id_map = resource.getIdentifier("map", "id", pkgName);
        int id_tv_ok = resource.getIdentifier("back", "id", pkgName);
        int id_reset = resource.getIdentifier("ib_reset", "id", pkgName);
        int id_title = resource.getIdentifier("title", "id", pkgName);
        int id_desc = resource.getIdentifier("desc", "id", pkgName);
        int id_ll_desc = resource.getIdentifier("ll_desc", "id", pkgName);
        int id_bitmap_location_current = resource.getIdentifier("icon_location_current", "mipmap", pkgName);
        int id_bitmap_location_marker = resource.getIdentifier("icon_location_marker", "mipmap", pkgName);

        ibBack = findViewById(id_tv_ok);
        ibReset = findViewById(id_reset);
        tvName = findViewById(id_title);
        tvDesc = findViewById(id_desc);
        mapView = findViewById(id_map);
        llDesc = findViewById(id_ll_desc);

        myLocationBitmapMarker = BitmapDescriptorFactory.fromResource(id_bitmap_location_current);
        locationBitmapMarker = BitmapDescriptorFactory.fromResource(id_bitmap_location_marker);
    }

    @SuppressLint("WrongConstant")
    private void init() {
        baiduMap = mapView.getMap();
        mapView.showZoomControls(false);
        ibBack.setOnClickListener(v ->
                onBackPressed());
        ibReset.setOnClickListener(v -> {
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
            baiduMap.animateMapStatus(mapStatusUpdate);
        });
        baiduMap.setOnMapLoadedCallback(() -> {
            LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            addOneLocMarker(latLng, locationBitmapMarker);
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
            baiduMap.animateMapStatus(mapStatusUpdate);
        });
        llDesc.setOnClickListener(v -> {
            Intent intent = com.microfountain.location.Utils.getBaiduOpenUri(latitude, longitude);
            List<ResolveInfo> apps = new ArrayList<>();
            apps = this.getPackageManager().queryIntentActivities(intent, COMPONENT_ENABLED_STATE_DEFAULT);
            if (apps.size() == 0) {
                intent = com.microfountain.location.Utils.getBaiduOpenUrl(latitude, longitude);
            }
            this.startActivity(intent);
        });

        if (TextUtils.isEmpty(label) && TextUtils.isEmpty(content)) {
            LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            reverseRequest(latLng);
        } else {
            if (!TextUtils.isEmpty(label)) {
                tvName.setText(label);
                tvName.setVisibility(View.VISIBLE);
            } else {
                tvName.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(content)) {
                tvDesc.setText(content);
                tvDesc.setVisibility(View.VISIBLE);
            } else {
                tvDesc.setVisibility(View.GONE);
            }
        }

    }

    /**
     * 逆地理编码请求
     */
    private void reverseRequest(LatLng latLng) {
        if (null == latLng) {
            return;
        }
        ReverseGeoCodeOption reverseGeoCodeOption = new ReverseGeoCodeOption().location(latLng)
                .newVersion(1)
                .radius(500);
        if (null == mCoder) {
            mCoder = GeoCoder.newInstance();
        }

        mCoder.setOnGetGeoCodeResultListener(this);
        mCoder.reverseGeoCode(reverseGeoCodeOption);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mapView) {
            mapView.onDestroy();
        }
        if (mCoder != null) {
            mCoder.destroy();
        }
    }

    private void addOneLocMarker(LatLng latLng, BitmapDescriptor bitmap) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(bitmap);
        baiduMap.addOverlay(markerOptions);
    }

    private void getLocationMsg(BDLocation location) {
        StringBuffer sb = new StringBuffer(256);
        if (location.getLocType() == BDLocation.TypeGpsLocation) {
            sb.append("gps定位成功");
        } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
            sb.append("网络定位成功");
        } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
            sb.append("离线定位成功");
        } else if (location.getLocType() == BDLocation.TypeServerError) {
            sb.append("服务端网络定位失败");
        } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
            sb.append("网络不同导致定位失败，请检查网络是否通畅");
        } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
            sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
        }
        Log.d(TAG, "onReceiveLocation: " + sb.append(com.microfountain.location.Utils.getLocationStr(location)));
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {}

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            llDesc.setVisibility(View.GONE);
            return;
        } else {
            llDesc.setVisibility(View.VISIBLE);
            String address = reverseGeoCodeResult.getAddress();
            tvDesc.setText(address);
            tvName.setText(reverseGeoCodeResult.getBusinessCircle());
        }
    }
}