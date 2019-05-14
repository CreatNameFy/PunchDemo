package com.demo.PunchDemo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import com.demo.PunchDemo.R;
import com.amap.api.fence.GeoFence;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;

import java.util.List;

public class MainActivity extends BaseActivity implements LocationSource {
//    地图
    private MapView mMapView;
//    重新定位按钮
    private ImageView mLocationbtn;
//    签到按钮  用于更换背景颜色
    private RelativeLayout siginBg;
//    签到提示用于提示打卡状况
    private TextView sigin;
//    显示当前时间
    private TextClock timeView;

    private LocationSource.OnLocationChangedListener mListener = null;//定位监听器

    private AMap aMap;
    private LocationUtil locationUtil;
//    定位获取的经纬度  添加打卡地址直接用的是这个   正常是后台返回的经纬度
    private double mLat;
    private double mLgt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mMapView.onCreate(savedInstanceState);
//        初始化数据
        initData();
    }


//    每次进入该页面就判断一次时间  避免用户更改手机时间
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        initData();
    }
    private void initData() {
//        模拟获取网络时间   用于和本地时间比较避免用户改变手机时间
       long netTime=  System.currentTimeMillis();
//       手机当前时间
        long currentTime = System.currentTimeMillis();
        if (locationUtil==null)
            locationUtil=new LocationUtil();
//        判断本地时间和网络时间是否差别过大
        if (compareTime(currentTime, netTime)) {
//            时间正常
//            根据自己后台业务来判断是上下班打卡，迟到早退打卡
            siginBg.setVisibility(View.VISIBLE);
//            如果是迟到或者早退可以换其他颜色背景提示
            siginBg.setBackgroundResource(R.drawable.siginin_bg);
            sigin.setText("上班打卡");
//            时间没问题的话就初始化地图
            initMapView();
        } else {
//            时间不对你可以自己显示另一个页面提示用户
//            showToast("手机时间和网络时间相差太大，无法打卡！");
        }
    }
    private void initMapView() {
        if(aMap == null){
            aMap = mMapView.getMap();
        }
//        定位回调
        setLocationCallBack();

        //设置定位监听
        aMap.setLocationSource(this);
        //设置缩放级别
        aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
        //显示定位层并可触发，默认false
        aMap.setMyLocationEnabled(true);
    }
    private void setLocationCallBack(){
        locationUtil = new LocationUtil();
//        定位回调
        locationUtil.setLocationCallBack(new LocationUtil.ILocationCallBack() {
            @Override
            public void callBack(String str,double lat,double lgt,AMapLocation aMapLocation) {

                mLocationbtn.setVisibility(View.VISIBLE);
                mMapView.setVisibility(View.VISIBLE);
                //    隐藏定位显示的圆圈
                locationUtil.set(aMap);
                //根据获取的经纬度，将地图移动到定位位置
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(lat,lgt)));
                //添加定位图标
                aMap.addMarker(locationUtil.getMarkerOption(str,lat,lgt));
                mLat =lat;
                mLgt = lgt;
//                添加围栏也就是打卡范围  参数1上下文，添加结果回调 ，范围的直径也就是圆圈的直径 ，经纬度
                add(MainActivity.this,getListener(),100, lat, lgt);
            }
        });
    }
    private GeoFenceListener getListener() {
        return new GeoFenceListener() {
            @Override
            public void onGeoFenceCreateFinished(List<GeoFence> list, int errorCode, String s) {
                if (errorCode == GeoFence.ADDGEOFENCE_SUCCESS) {//判断围栏是否创建成功
                    Log.d("test", "添加围栏成功!!");
                    locationUtil.circle(aMap,mLat,mLgt,100);
                    //            添加围栏成功后注册广播监听进出围栏
                    registerReceiver();
                } else {
                    Log.d("test", "添加围栏失败!!");
                }
            }
        };
    }
    private void initView() {
        mMapView = findViewById(R.id.map);
        mLocationbtn = findViewById(R.id.locationbtn);
        siginBg = findViewById(R.id.sigin_bg);
        sigin = findViewById(R.id.sigin);
        timeView = findViewById(R.id.timeView);

        mLocationbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationUtil.startLocate(getApplicationContext());
            }
        });
    }
//    进入围栏
    @Override
    protected void enter() {

    }
//    离开围栏
    @Override
    protected void out() {

    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        locationUtil.startLocate(getApplicationContext());
    }

    @Override
    public void deactivate() {

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        locationUtil.onDestroy();//销毁定位客户端，同时销毁本地定位服务。
        if (mBReceiver!=null)
            unregisterReceiver(mBReceiver);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }
}
