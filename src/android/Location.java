package com.microfountain.location;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class Location extends CordovaPlugin {

    private static final int REQUEST_CROP_IMAGE = 1000;

    CallbackContext callbackContext;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        SDKInitializer.initialize(cordova.getActivity().getApplicationContext());
        SDKInitializer.setCoordType(CoordType.GCJ02);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("getLocation")) {
            this.getPosition(callbackContext);
            return true;
        } else if (action.equals("openLocation")) {
            this.openPosition(args.getString(0), callbackContext);
            return true;
        }
        return false;
    }

    private void getPosition(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        cordova.startActivityForResult(this, new Intent(cordova.getActivity(), LocationActivity.class), REQUEST_CROP_IMAGE);
    }

    private void openPosition(String message, CallbackContext callbackContext) {
        if (message != null) {
            try {
                Intent intent = new Intent(cordova.getActivity(), LocationDetailActivity.class);
                JSONObject data = new JSONObject(message);
                String longitude = data.optString("longitude");
                String latitude = data.optString("latitude");
                String label = data.optString("label");
                String content = data.optString("content");
                Bundle bundle = new Bundle();
                bundle.putString("longitude", longitude);
                bundle.putString("latitude", latitude);
                bundle.putString("label", label);
                bundle.putString("content", content);
                intent.putExtra("location", bundle);
                cordova.getActivity().startActivity(intent);
                callbackContext.success();
            } catch (JSONException e) {
                e.printStackTrace();
                callbackContext.error("Parameter error");
            }
        } else {
            callbackContext.error("Parameter error");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (REQUEST_CROP_IMAGE == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                JSONObject result = new JSONObject();
                try {
                    result.put("CoordType", intent.getStringExtra("CoordType"));
                    result.put("latitude", intent.getStringExtra("latitude"));
                    result.put("longitude", intent.getStringExtra("longitude"));
                    result.put("label", intent.getStringExtra("label"));
                    result.put("address", intent.getStringExtra("address"));
                    callbackContext.success(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
