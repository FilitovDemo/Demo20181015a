package tw.edu.tut.mis.demo1015;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final String TAG = "TUTIM";
    private FusedLocationProviderClient mLPC;
    private LocationRequest mLReq;
    @BindView(R.id.mapView) MapView mapView;
    private GoogleMap gMap;

    boolean isGPS_On = true;
    @BindView(R.id.gpsswitch) ImageButton gpsSwitchButton;

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
//                    loc.getLatitude()
//                    loc.getLongitude()
                    LatLng pos = new LatLng( loc.getLatitude(), loc.getLongitude() );
                    gMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                    gMap.addMarker(new MarkerOptions().position(pos).title("我在這兒"));
                }

            }
        }
    };


    //
    @Override
    protected void onResume() {
        super.onResume();
        if(isGPS_On){
            startLocation();
        }
        mapView.onResume();
    }

    //
    @Override
    protected void onPause() {
        super.onPause();
        if(isGPS_On) {
            stopLocation();
        }
        mapView.onPause();
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
}
