package com.demo.PunchDemo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.amap.api.fence.GeoFence;
import com.amap.api.fence.GeoFenceClient;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.DPoint;
import com.amap.api.maps.AMap;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;

/**
 * Created by: 方燚
 * Time: 2019/5/14
 * Details:
 */
public abstract class BaseActivity extends AppCompatActivity {
    //    判断本地时间是否和网络时间相同
    protected boolean isTime=false;
    //    添加围栏
    protected GeoFenceListener mGeoFenceListener ;
    //    权限返回码
    private final int REQ_LOCATION = 0x12;
    //定义接收广播的action字符串
    public static final String GEOFENCE_BROADCAST_ACTION = "com.demo.punch.broadcast";
    //    围栏广播
    protected MyBroadcastReceiver mBReceiver;
    protected String TAG;
    private GeoFenceClient mGeoFenceClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyPermission();
        super.onCreate(savedInstanceState);
        TAG=getClass().getSimpleName();
    }
    protected void applyPermission() {
        if (!PermissionUtils.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION))
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQ_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                initData();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    protected boolean compareTime(long time,long netTime){
        if(compareMin(time,netTime)>3){
            isTime=false;
            return false;
        }else{
            isTime=true;
            return true;
        }
    }
    ///    注册围栏状态广播
    protected   void registerReceiver(){
        //接受定位广播
        IntentFilter filter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(GEOFENCE_BROADCAST_ACTION);
        mBReceiver = new MyBroadcastReceiver();
        registerReceiver(mBReceiver, filter);
    }
//    用户进入围栏接收器
    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)) {
                //解析广播内容
                Bundle bundle = intent.getExtras();
                //获取当前有触发的围栏对象：
                GeoFence fence = bundle.getParcelable(GeoFence.BUNDLE_KEY_FENCE);
                int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
                StringBuffer sb = new StringBuffer();
                switch (status) {
                    case GeoFence.STATUS_LOCFAIL:
//                        定位失败
                        break;
                    case GeoFence.STATUS_IN:
                        enter();
                        Log.e(TAG, "进入围栏");
                        break;
                    case GeoFence.STATUS_OUT:
                        out();
                        Log.e(TAG, "离开围栏");
                        break;
                    case GeoFence.STATUS_STAYED:
                        Log.e(TAG, "停留围栏");
                        break;
                    default:
                        break;
                }
            }
        }
    }
    //    添加围栏
    public void add(Context context,GeoFenceListener mGeoFenceListener,int m,double latitude,double longitude) {
        if (mGeoFenceClient!=null){
            mGeoFenceClient.removeGeoFence();
        }else{
            mGeoFenceClient = new GeoFenceClient(context);
        }
        //创建一个中心点坐标
        DPoint centerPoint = new DPoint();
//设置中心点纬度
        centerPoint.setLatitude(latitude);
//设置中心点经度
        centerPoint.setLongitude(longitude);
//执行添加围栏的操作
        mGeoFenceClient.addGeoFence (centerPoint,m/2,"公司打卡");
        mGeoFenceClient.setGeoFenceListener(mGeoFenceListener);
        mGeoFenceClient.setActivateAction(GeoFenceClient.GEOFENCE_IN| GeoFenceClient.GEOFENCE_OUT| GeoFenceClient.GEOFENCE_STAYED);
        mGeoFenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);

    }
    protected abstract void enter();
    protected abstract void out ();
    /**
     * 两个时间点的间隔时长（分钟）
     * @param before 开始时间
     * @param after 结束时间
     * @return 两个时间点的间隔时长（分钟）
     */
    protected    long compareMin(long before, long after) {
        long dif = 0;
        if (after  >= before ) {
            dif = (after - before) / (1000 * 60);
//            dif = after  - before ;
        } else if (after  < before ) {
            dif = (before - after) / (1000 * 60);
//            dif = after  + 86400000 - before ;
        }
//        dif = Math.abs(dif);
//        return dif  / 60000;
        return dif;

    }
}
