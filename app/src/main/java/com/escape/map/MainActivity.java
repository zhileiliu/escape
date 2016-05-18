package com.escape.map;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnGetRoutePlanResultListener {

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private BitmapDescriptor mCurrentMarker;
    RoutePlanSearch mSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        initLocation();

        mLocationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // 开启定位图层
            mBaiduMap.setMyLocationEnabled(true);
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(0).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            MyLocationConfiguration config = new MyLocationConfiguration(null, true, null);

            LatLng ll = new LatLng(location.getLatitude(),
                    location.getLongitude());
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(18.0f);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            mBaiduMap.setMyLocationConfigeration(config);
//            少荃湖公园	117.38325906 	31.95642583
//            杏花公园	117.26798905 	31.87116907
//            天鹅湖公园	117.21349624 	31.81524444
//            城市森林公园	117.21554610 	31.89156738
//            省体育馆	117.29379103 	31.85184863
//            工大南区	117.20005435 	31.77702497
//            大蜀山公园	117.16956807 	31.84357684
//            陶冲湖公园	117.32672969 	31.93419389
//            十五里河公园	117.33940780 	31.75784740
//            张桥中心避灾公园	117.3052528	32.02827097
//            陶冲湖中心避灾公园	117.3285617	31.93353189
//            店埠中心避灾公园	117.4574496	31.8875055
//            潭冲湖中心避灾公园	117.1277693	31.69814251

            LatLng locate = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng p1 = new LatLng(31.95642583, 117.38325906);
            LatLng p2 = new LatLng(31.87116907, 117.26798905);
            LatLng p3 = new LatLng(31.81524444, 117.21349624);
            LatLng p4 = new LatLng(31.89156738, 117.21554610);
            LatLng p5 = new LatLng(31.85184863, 117.29379103);
            LatLng p6 = new LatLng(31.77702497, 117.20005435);
            LatLng p7 = new LatLng(31.84357684, 117.16956807);
            LatLng p8 = new LatLng(31.93419389, 117.32672969);
            LatLng p9 = new LatLng(31.75784740, 117.33940780);
            LatLng p10 = new LatLng(32.02827097, 117.3052528);
            LatLng p11 = new LatLng(31.93353189, 117.3285617);
            LatLng p12 = new LatLng(31.8875055, 117.4574496);
            LatLng p13 = new LatLng(31.69814251, 117.1277693);
            double d1 = DistanceUtil.getDistance(locate, p1);
            Log.e("BaiduLocationApiDem", "d1:" + d1);
            double d2 = DistanceUtil.getDistance(locate, p2);
            Log.e("BaiduLocationApiDem", "d2:" + d2);
            double d3 = DistanceUtil.getDistance(locate, p3);
            Log.e("BaiduLocationApiDem", "d3:" + d3);
            double d4 = DistanceUtil.getDistance(locate, p4);
            Log.e("BaiduLocationApiDem", "d4:" + d4);
            double d5 = DistanceUtil.getDistance(locate, p5);
            Log.e("BaiduLocationApiDem", "d5:" + d5);
            double d6 = DistanceUtil.getDistance(locate, p6);
            Log.e("BaiduLocationApiDem", "d6:" + d6);
            double d7 = DistanceUtil.getDistance(locate, p7);
            Log.e("BaiduLocationApiDem", "d7:" + d7);
            double d8 = DistanceUtil.getDistance(locate, p8);
            Log.e("BaiduLocationApiDem", "d8:" + d8);
            double d9 = DistanceUtil.getDistance(locate, p9);
            Log.e("BaiduLocationApiDem", "d9:" + d9);
            double d10 = DistanceUtil.getDistance(locate, p10);
            Log.e("BaiduLocationApiDem", "d10:" + d10);
            double d11 = DistanceUtil.getDistance(locate, p11);
            Log.e("BaiduLocationApiDem", "d4:" + d4);
            double d12 = DistanceUtil.getDistance(locate, p12);
            Log.e("BaiduLocationApiDem", "d4:" + d4);
            double d13 = DistanceUtil.getDistance(locate, p13);
            Log.e("BaiduLocationApiDem", "d4:" + d4);

            BitmapDescriptor bd = BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_gcoding);
            MarkerOptions oo1 = new MarkerOptions().position(p1).icon(bd)
                    .draggable(true).title("少荃湖公园");
            mBaiduMap.addOverlay(oo1);
            MarkerOptions oo2 = new MarkerOptions().position(p2).icon(bd)
                    .draggable(true).title("杏花公园");
            mBaiduMap.addOverlay(oo2);
            MarkerOptions oo3 = new MarkerOptions().position(p3).icon(bd)
                    .draggable(true).title("天鹅湖公园");
            mBaiduMap.addOverlay(oo3);
            MarkerOptions oo4 = new MarkerOptions().position(p4).icon(bd)
                    .draggable(true).title("城市森林公园");
            mBaiduMap.addOverlay(oo4);
            MarkerOptions oo5 = new MarkerOptions().position(p5).icon(bd)
                    .draggable(true).title("省体育馆");
            mBaiduMap.addOverlay(oo5);
            MarkerOptions oo6 = new MarkerOptions().position(p6).icon(bd)
                    .draggable(true).title("工大南区");
            mBaiduMap.addOverlay(oo6);
            MarkerOptions oo7 = new MarkerOptions().position(p7).icon(bd)
                    .draggable(true).title("大蜀山公园");
            mBaiduMap.addOverlay(oo7);
            MarkerOptions oo8 = new MarkerOptions().position(p8).icon(bd)
                    .draggable(true).title("陶冲湖公园");
            mBaiduMap.addOverlay(oo8);
            MarkerOptions oo9 = new MarkerOptions().position(p9).icon(bd)
                    .draggable(true).title("十五里河公园");
            mBaiduMap.addOverlay(oo9);
            MarkerOptions oo10 = new MarkerOptions().position(p10).icon(bd)
                    .draggable(true).title("张桥中心避灾公园");
            mBaiduMap.addOverlay(oo10);
            MarkerOptions oo11 = new MarkerOptions().position(p11).icon(bd)
                    .draggable(true).title("陶冲湖中心避灾公园");
            mBaiduMap.addOverlay(oo11);
            MarkerOptions oo12 = new MarkerOptions().position(p12).icon(bd)
                    .draggable(true).title("店埠中心避灾公园");
            mBaiduMap.addOverlay(oo12);
            MarkerOptions oo13 = new MarkerOptions().position(p13).icon(bd)
                    .draggable(true).title("潭冲湖中心避灾公园");
            mBaiduMap.addOverlay(oo13);

            LatLng near = p1;
            double neard = d1;
            if (neard > d2) {
                near = p2;
                neard = d2;
            }
            if (neard > d3) {
                near = p3;
                neard = d3;
            }
            if (neard > d4) {
                near = p4;
                neard = d4;
            }
            if (neard > d5) {
                near = p5;
                neard = d5;
            }
            if (neard > d6) {
                near = p6;
                neard = d6;
            }
            if (neard > d7) {
                near = p7;
                neard = d7;
            }
            if (neard > d8) {
                near = p8;
                neard = d8;
            }
            if (neard > d9) {
                near = p9;
                neard = d9;
            }
            if (neard > d10) {
                near = p10;
                neard = d10;
            }
            if (neard > d11) {
                near = p11;
                neard = d11;
            }
            if (neard > d12) {
                near = p12;
                neard = d12;
            }
            if (neard > d13) {
                near = p13;
                neard = d13;
            }

            mSearch = RoutePlanSearch.newInstance();

            mSearch.walkingSearch(new WalkingRoutePlanOption().from(PlanNode.withLocation(locate)).to(PlanNode.withLocation(near)));

            mSearch.setOnGetRoutePlanResultListener(MainActivity.this);


// 当不需要定位图层时关闭定位图层
//            mBaiduMap.setMyLocationEnabled(false);
//            LatLng pt1 = new LatLng(location.getLatitude(), location.getLongitude());
//            LatLng pt2 = new LatLng(31.9564258311, 117.3832590563);
//            NaviParaOption para = new NaviParaOption()
//                    .startPoint(pt1).endPoint(pt2)
//                    .startName("天安门").endName("百度大厦");
//
//            try {
//// 调起百度地图步行导航
//                BaiduMapNavigation.openBaiduMapWalkNavi(para, MainActivity.this);
//            } catch (BaiduMapAppNotSupportNaviException e) {
//                e.printStackTrace();
//            }
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i("BaiduLocationApiDem", sb.toString());
            mLocationClient.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
        }
    }
}
