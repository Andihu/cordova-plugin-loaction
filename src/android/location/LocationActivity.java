package com.microfountain.location;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;


public class LocationActivity extends LocationBaseActivity implements BaiduMap.OnMapStatusChangeListener
        , OnGetGeoCoderResultListener, OnGetSuggestionResultListener {
    private static final String TAG = "LocationActivity";

    private SlidingUpPanelLayout slidingUpPanelLayout;
    private View vSearch;
    private View vContentSearch;
    private View vContentPoi;
    private MapView mvMapView;
    private BaiduMap baiduMap;
    private EditText editText;
    private TextView tvCancel;
    private TextView tvBack;
    private TextView tvOk;
    private ImageView ivClear;
    private ImageView ivCollapsed;
    private ImageView ivCenter;
    private ImageButton ibReset;
    private ProgressBar mPoiProgressBar;
    private ProgressBar mSuggestProgressBar;
    private BitmapDescriptor bitmap;
    private SuggestionSearch mSuggestionSearch;
    private RecyclerView rvLocationList;
    private RecyclerView rvSearchList;
    private com.microfountain.location.SearchListAdapter searchListAdapter;
    private com.microfountain.location.LocationListAdapter locationListAdapter;
    private GeoCoder mGeoCoder = null;
    private PoiInfo mCurrentPoiInfo;
    private Marker marker = null;
    private String city = "北京";
    private String mKeyword;
    private boolean mIsScrollMap = false;

    private static final int sDefaultRGCRadius = 500;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private View mMapArea;

    @Override
    public void initView() {
        findID();
        init();
    }

    @Override
    public void onStartLocation() {
        mPoiProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onReceiveLocationSuccess(BDLocation location) {
        city = location.getCity();
        getLocationMsg(location);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        moveToPoiInfo(latLng);
        reverseRequest(latLng);
        addOneLocMarker(latLng);
    }

    @Override
    public void onReceiveLocationFailed(BDLocation location) {
        Toast.makeText(LocationActivity.this, "定位失败！", Toast.LENGTH_SHORT).show();
        mPoiProgressBar.setVisibility(View.GONE);
        new com.microfountain.location.RequestDialog
                .Builder()
                .Context(LocationActivity.this)
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

    private void init() {
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(18.0f);
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);
        baiduMap = mvMapView.getMap();
        baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        mvMapView.showZoomControls(false);
        baiduMap.setOnMapStatusChangeListener(this);
        locationListAdapter = new com.microfountain.location.LocationListAdapter();
        rvLocationList.setLayoutManager(new LinearLayoutManager(this));
        rvLocationList.setAdapter(locationListAdapter);

        searchListAdapter = new com.microfountain.location.SearchListAdapter();
        rvSearchList.setLayoutManager(new LinearLayoutManager(this));
        rvSearchList.setAdapter(searchListAdapter);
        baiduMap.setOnMapTouchListener(motionEvent -> mIsScrollMap = true);
        tvBack.setOnClickListener(v -> onBackPressed());
        ibReset.setOnClickListener(v -> {
            startLocation();
            rvLocationList.scrollToPosition(0);
        });
        locationListAdapter.setItemClickListener(poiInfo -> {
            moveToPoiInfo(poiInfo.location);
            this.mCurrentPoiInfo = poiInfo;
        });
        searchListAdapter.setItemClickListener(suggestionInfo -> {
            LatLng latLng = new LatLng(suggestionInfo.getPt().latitude, suggestionInfo.getPt().longitude);
            moveToPoiInfo(latLng);
            PoiInfo poiInfo = new PoiInfo();
            poiInfo.setLocation(latLng);
            poiInfo.setName(suggestionInfo.getKey());
            poiInfo.setAddress(suggestionInfo.getAddress());
            this.mCurrentPoiInfo = poiInfo;
        });

        tvOk.setOnClickListener(v -> {
            Toast.makeText(LocationActivity.this, mCurrentPoiInfo.toString(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra("CoordType", CoordType.GCJ02.name());
            intent.putExtra("latitude", String.valueOf(mCurrentPoiInfo.location.latitude));
            intent.putExtra("longitude", String.valueOf(mCurrentPoiInfo.location.longitude));
            intent.putExtra("label", mCurrentPoiInfo.name);
            intent.putExtra("address", mCurrentPoiInfo.address);
            setResult(RESULT_OK, intent);
            finish();
        });

        vSearch.setOnClickListener(v -> {
            vContentPoi.setVisibility(View.GONE);
            vContentSearch.setVisibility(View.VISIBLE);
            slidingUpPanelLayout.setScrollableView(rvSearchList);
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            showSoftInputFromWindow(editText);
        });

        ivClear.setOnClickListener(v -> {
            editText.setText("");
            searchListAdapter.clear();
        });

        tvCancel.setOnClickListener(v -> {
            hideInput();
            vContentPoi.setVisibility(View.VISIBLE);
            vContentSearch.setVisibility(View.GONE);
            searchListAdapter.clear();
            editText.setText("");
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            slidingUpPanelLayout.setScrollableView(rvLocationList);
        });

        ivCollapsed.setOnClickListener(v -> {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        });
        final int[] maxPanelTop = new int[1];
        mMapArea.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            maxPanelTop[0] = mMapArea.getBottom();
        });
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                mMapArea.scrollTo(0, (maxPanelTop[0] - panel.getTop()) / 2);
                ViewGroup.LayoutParams layoutParams = ivCollapsed.getLayoutParams();
                layoutParams.height = (int) (slideOffset * dp2px(32));
                ivCollapsed.setLayoutParams(layoutParams);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    if (!TextUtils.isEmpty(city)) {
                        requestSuggestion(s.toString());
                        mKeyword = s.toString();
                    } else {
                        Toast.makeText(LocationActivity.this, "定位失败！", Toast.LENGTH_SHORT).show();
                    }

                    ivClear.setVisibility(View.VISIBLE);
                } else {
                    ivClear.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public static int dp2px(float dpValue) {
        return (int) (0.5f + dpValue * Resources.getSystem().getDisplayMetrics().density);
    }

    private void moveToPoiInfo(LatLng latLng) {
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
        baiduMap.animateMapStatus(mapStatusUpdate);
        startShakeAnimation(ivCenter);
    }

    /**
     * EditText获取焦点并显示软键盘
     */
    public void showSoftInputFromWindow(EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    protected void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View v = getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    private void findID() {
        Context appContext = getApplicationContext();
        Resources resource = appContext.getResources();
        String pkgName = appContext.getPackageName();
        int id_layout = resource.getIdentifier("activity_location", "layout", pkgName);
        int id_res_map = resource.getIdentifier("map", "id", pkgName);
        int id_res_tv_ok = resource.getIdentifier("tv_ok", "id", pkgName);
        int id_res_tv_back = resource.getIdentifier("tv_back", "id", pkgName);
        int id_res_iv_center = resource.getIdentifier("iv_center", "id", pkgName);
        int id_res_tv_reset = resource.getIdentifier("ib_reset", "id", pkgName);
        int id_res_fl_search = resource.getIdentifier("fl_search", "id", pkgName);
        int id_res_lv_location = resource.getIdentifier("lv_location", "id", pkgName);
        int id_res_rv_search_list = resource.getIdentifier("search_list", "id", pkgName);
        int id_res_v_search_content = resource.getIdentifier("search_content", "id", pkgName);
        int id_res_v_poi_content = resource.getIdentifier("poi_content", "id", pkgName);
        int id_res_et_edit_text = resource.getIdentifier("edit_text", "id", pkgName);
        int id_res_tv_cancel = resource.getIdentifier("cancel", "id", pkgName);
        int id_res_tv_clear = resource.getIdentifier("clear", "id", pkgName);
        int id_res_iv_back_poi = resource.getIdentifier("back_poi", "id", pkgName);
        int id_res_slide_layout = resource.getIdentifier("slid_layout", "id", pkgName);
        int id_res_poi_progressbar = resource.getIdentifier("progressbar", "id", pkgName);
        int id_res_suggest_progressbar = resource.getIdentifier("suggest_progressbar", "id", pkgName);
        int id_res_map_area = resource.getIdentifier("map_area", "id", pkgName);

        int id_res_bitmap = resource.getIdentifier("icon_location_current", "mipmap", pkgName);

        setContentView(id_layout);
        tvOk = findViewById(id_res_tv_ok);
        tvBack = findViewById(id_res_tv_back);
        ivCenter = findViewById(id_res_iv_center);
        ibReset = findViewById(id_res_tv_reset);
        vSearch = findViewById(id_res_fl_search);
        mvMapView = findViewById(id_res_map);
        rvLocationList = findViewById(id_res_lv_location);

        vContentSearch = findViewById(id_res_v_search_content);
        vContentPoi = findViewById(id_res_v_poi_content);
        editText = findViewById(id_res_et_edit_text);
        tvCancel = findViewById(id_res_tv_cancel);
        ivClear = findViewById(id_res_tv_clear);
        ivCollapsed = findViewById(id_res_iv_back_poi);
        slidingUpPanelLayout = findViewById(id_res_slide_layout);
        rvSearchList = findViewById(id_res_rv_search_list);
        mPoiProgressBar = findViewById(id_res_poi_progressbar);
        mSuggestProgressBar = findViewById(id_res_suggest_progressbar);
        bitmap = BitmapDescriptorFactory.fromResource(id_res_bitmap);
        mMapArea = findViewById(id_res_map_area);
        ViewGroup.LayoutParams layoutParams = ivCollapsed.getLayoutParams();
        layoutParams.height = 0;
        ivCollapsed.setLayoutParams(layoutParams);
    }

    private void requestSuggestion(String keyword) {
        mSuggestProgressBar.setVisibility(View.VISIBLE);
        mSuggestionSearch.requestSuggestion(new SuggestionSearchOption()
                .citylimit(false)
                .city(city)
                .keyword(keyword));
    }

    /**
     * 逆地理编码请求
     */
    private void reverseRequest(LatLng latLng) {
        if (null == latLng) {
            return;
        }
        ReverseGeoCodeOption reverseGeoCodeOption = new ReverseGeoCodeOption().location(latLng)
                .newVersion(0)
                .radius(sDefaultRGCRadius);
        if (null == mGeoCoder) {
            mGeoCoder = GeoCoder.newInstance();
        }

        mGeoCoder.setOnGetGeoCodeResultListener(this);
        mGeoCoder.reverseGeoCode(reverseGeoCodeOption);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mGeoCoder) {
            mGeoCoder.destroy();
        }

        if (null != mvMapView) {
            mvMapView.onDestroy();
        }
        if (mSuggestionSearch != null) {
            mSuggestionSearch.destroy();
        }
    }

    private void addOneLocMarker(LatLng latLng) {
        if (null != marker) {
            marker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(bitmap);
        marker = (Marker) baiduMap.addOverlay(markerOptions);
    }

    private void updateUI(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult.getPoiList() != null) {
            LatLng latLng = reverseGeoCodeResult.getLocation();
            PoiInfo poiInfo = new PoiInfo();
            poiInfo.setLocation(latLng);
            poiInfo.setName(reverseGeoCodeResult.getSematicDescription());
            poiInfo.setAddress(reverseGeoCodeResult.getAddress());
            reverseGeoCodeResult.getPoiList().add(0, poiInfo);
            mCurrentPoiInfo = poiInfo;
            locationListAdapter.setPoiInfoList(reverseGeoCodeResult.getPoiList());
        }
    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {
    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus, int i) {
    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {
    }

    @Override
    public void onGetReverseGeoCodeResult(final ReverseGeoCodeResult reverseGeoCodeResult) {
        mPoiProgressBar.setVisibility(View.GONE);
        if (null == reverseGeoCodeResult) {
            return;
        }
        mHandler.post(() -> updateUI(reverseGeoCodeResult));
    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {
        LatLng target = mapStatus.target;
        if (mIsScrollMap) {
            mPoiProgressBar.setVisibility(View.VISIBLE);
            reverseRequest(target);
        }
        mIsScrollMap = false;
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
    }

    public void startShakeAnimation(View view) {
        Animation translateAnimation = new RotateAnimation(0, 10, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        translateAnimation.setInterpolator(new CycleInterpolator(5));
        translateAnimation.setDuration(500);
        view.startAnimation(translateAnimation);
    }

    @Override
    public void onGetSuggestionResult(SuggestionResult suggestionResult) {
        mSuggestProgressBar.setVisibility(View.GONE);
        if (suggestionResult.getAllSuggestions() == null) {
            Toast.makeText(LocationActivity.this, "未查询到内容！", Toast.LENGTH_SHORT).show();
        } else {
            mHandler.post(() -> updateSearchUI(suggestionResult));
        }
    }

    private void updateSearchUI(SuggestionResult suggestionResult) {
        searchListAdapter.setKeyword(mKeyword);
        searchListAdapter.setSuggestionsList(suggestionResult.getAllSuggestions());
    }

    private void getLocationMsg(BDLocation location) {
        StringBuilder sb = new StringBuilder(256);
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
}