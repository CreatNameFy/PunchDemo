package com.demo.PunchDemo;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.fence.GeoFenceClient;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.DPoint;
import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;


/**
 * Created by: 方燚
 * Time: 2019/3/1
 * Details:
 */
public class LocationUtil implements AMapLocationListener {
    public static final String GEOFENCE_BROADCAST_ACTION = "com.demo.punch.broadcast";
    private AMapLocationClient aMapLocationClient;
    private AMapLocationClientOption clientOption;
    private ILocationCallBack callBack;

    public void onDestroy() {
        if (aMapLocationClient != null)
            aMapLocationClient.onDestroy();
    }

    public void startLocate(Context context) {
        aMapLocationClient = new AMapLocationClient(context);

        //设置监听回调
        aMapLocationClient.setLocationListener(this);

        //初始化定位参数
        clientOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        clientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        //设置是否返回地址信息（默认返回地址信息）
        clientOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        clientOption.setOnceLocation(true);
        //设置是否强制刷新WIFI，默认为强制刷新
        clientOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        clientOption.setMockEnable(false);
        //设置定位间隔
//        clientOption.setInterval(1000);
        aMapLocationClient.setLocationOption(clientOption);

        aMapLocationClient.startLocation();
    }
    public void setLocationCallBack(ILocationCallBack callBack) {
        this.callBack = callBack;
    }
    //完成定位回调
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //定位成功完成回调
                String country = aMapLocation.getCountry();
                String province = aMapLocation.getProvince();
                String city = aMapLocation.getCity();
                String district = aMapLocation.getDistrict();
                String street = aMapLocation.getStreet();
                String address = aMapLocation.getAddress();
                double lat = aMapLocation.getLatitude();
                double lgt = aMapLocation.getLongitude();
                callBack.callBack(country + province + city + district + street, lat, lgt, aMapLocation);
                Log.d("weizhi", country + province + city + district + street + address);
                Log.d("jingweidu", lat + "------" + lgt);
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }

    /**
     * 自定义图标
     *
     * @return
     */
    public MarkerOptions getMarkerOption(String str, double lat, double lgt) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(lat, lgt));
        markerOptions.period(100);
//        markerOptions.draggable(true);
        return markerOptions;
    }

    //绘制围栏
    public void circle(AMap aMap, double v1, double v2, int m) {
        aMap.removecache();
        LatLng latLng = new LatLng(v1, v2);
        aMap.addCircle(new CircleOptions().center(latLng)
                .radius(m/2).strokeColor(Color.parseColor("#3F51B5"))
                .fillColor(Color.argb(100, 29, 161, 242)).strokeWidth(5));
    }

    //    隐藏定位显示的圆圈
    public void set(AMap aMap) {
        MyLocationStyle locationStyle = new MyLocationStyle();
        locationStyle.strokeColor(Color.argb(0, 0, 0, 0));
        locationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
        locationStyle.strokeWidth(0);
        aMap.setMyLocationStyle(locationStyle);
    }

    public interface ILocationCallBack {
        void callBack(String str, double lat, double lgt, AMapLocation aMapLocation);
    }



    //    访问网络====================================================================

}
