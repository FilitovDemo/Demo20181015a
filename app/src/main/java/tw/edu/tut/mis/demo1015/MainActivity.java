package tw.edu.tut.mis.demo1015;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.UUID;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final String TAG = "TUTIM";
    private FusedLocationProviderClient mLPC;
    private LocationRequest mLReq;
    @BindView(R.id.mapView) MapView mapView;
    private GoogleMap gMap = null;

    boolean isGPS_On;
    @BindView(R.id.gpsswitch) ImageButton gpsSwitchButton;

    double mLat=23.037851, mLon=120.239122; //目前所在的經緯度

    Handler timerHandler = new Handler();  //操作計時器的把柄

    String mUserID;
    //先產生一個暫時性的key
    String mTempUserID = UUID.randomUUID().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mLPC = LocationServices.getFusedLocationProviderClient(this);
        mLReq = new LocationRequest();
        mLReq.setInterval(10000);
        mLReq.setFastestInterval(5000);
        mLReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mapView.onCreate( savedInstanceState );
        mapView.getMapAsync(this);


        if( savedInstanceState!=null ){
            isGPS_On = savedInstanceState.getBoolean("GPS_ON", true);
            mUserID = savedInstanceState.getString("USER_ID", mTempUserID);
        }else{ //沒有之前儲存的狀態時，直接給預設值就好了
            isGPS_On = true;
            mUserID = mTempUserID;
        }

        if( isGPS_On ){
            gpsSwitchButton.setImageResource(R.drawable.location2);
        }else{
            gpsSwitchButton.setImageResource(R.drawable.location1);
        }

    }


    //需要儲存狀態時
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("GPS_ON", isGPS_On);
        outState.putString("USER_ID", mUserID);
        super.onSaveInstanceState(outState); //這行最後做!!
    }

    //需要取得狀態時
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isGPS_On = savedInstanceState.getBoolean("GPS_ON", true);
        mUserID = savedInstanceState.getString("USER_ID", mTempUserID); //TODO: 以後試著改好一點
    }

    @OnClick(R.id.gpsswitch)
    void onGPSSwitch(View v){
        if( isGPS_On ){
            isGPS_On = false;
            gpsSwitchButton.setImageResource(R.drawable.location1);
            stopLocation();
        } else {
            isGPS_On = true;
            gpsSwitchButton.setImageResource(R.drawable.location2);
            startLocation();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==9527) { //之前請求定位權限
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                startLocation();
            } else {
                Log.e(TAG, "no location permission granted!");
            }
        }
    }


    //回報取得位置的物件
    private LocationCallback mLCb = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            //super.onLocationResult(locationResult);
            if (locationResult!=null) {
                List<Location> locs = locationResult.getLocations();
                if (locs.size()>0) {
                    Location loc = locs.get( locs.size()-1 );
                    Log.i(TAG, "Locatoin callback: "+ loc.toString() );
                    mLat = loc.getLatitude();
                    mLon = loc.getLongitude();
                    //搬到定時處理那邊...
//                    LatLng pos = new LatLng( mLat, mLon );
//                    gMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
//                    gMap.addMarker(new MarkerOptions().position(pos).title("我在這兒"));
                }

            }
        }
    };


    //
    @Override
    protected void onResume() {
        Log.i(TAG, "GPS_ON: "+isGPS_On);
        super.onResume();
        if(isGPS_On){
            startLocation();
        }
        mapView.onResume();
        timerHandler.postDelayed( timerTask, 0 );  //啟動計時器task
    }

    //
    @Override
    protected void onPause() {
        Log.i(TAG, "GPS_ON: "+isGPS_On);
        super.onPause();
        if(isGPS_On) {
            stopLocation();
        }
        mapView.onPause();
        timerHandler.removeCallbacks( timerTask );  //停止計時器task
    }

    //
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    //
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    //啟動定位
    void startLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, perms, 9527 );
            return;
        }
        mLPC.requestLocationUpdates(mLReq, mLCb, null);
    }

    //停止定位
    void stopLocation() {
        mLPC.removeLocationUpdates(mLCb);
    }

    //地圖準備完成
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(23.037851,120.239122),16));
    }


    Runnable timerTask = new Runnable() {
        @Override
        public void run() {
            Log.i("myTimer", "啟動:"+mLat+","+mLon);
            if( isGPS_On ) { //只有在GPS開啟時才做地圖的定位
                LatLng pos = new LatLng(mLat, mLon);
                if( gMap!=null ) { //等待地圖載入完成有gMap物件時才處理
                    gMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                    gMap.clear();
                    gMap.addMarker(new MarkerOptions().position(pos).title("我在這兒"));
                }

                //嘗試發送座標給server
                //取回其他人的座標

            }
            timerHandler.postDelayed(this, 5000);  //5s後啟動計時器task
        }
    };

}
