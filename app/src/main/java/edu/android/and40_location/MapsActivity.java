package edu.android.and40_location;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cs.googlemaproute.DrawRoute;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DrawRoute.onDrawRoute {

    private static final String TAG = "edu.android.and39";
    private static final int REQ_FINE_LOCATION = 100;

    public static final String TABLE_NAME = "location";

    private FirebaseDatabase database;
    private DatabaseReference locationReference;
    private ValueEventListener valueEventListener;

    private GoogleMap mMap;
    private Marker currentMarker = null;
    private Marker currentAnimalMarker = null;


    private Intent intent = null;

    // 위치 정보(최근 위치, 주기적 업데이트 시작/취소)와 관련된 클래스
    private FusedLocationProviderClient locationClient;
    // 주기적 위치 업데이트를 요청할 때 설정 정보를 저장하는 클래스
    private LocationRequest locationRequest;
    // 주기적 위치 정보를 처리하는 콜백
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        database = FirebaseDatabase.getInstance();
        locationReference = database.getReference(TABLE_NAME);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mMap == null) {
                    // GoogleMap 객체가 생성되어 있지 않을 때 화면 업데이트를 하면 안됨
                    return;
                }

                LocationInfo info = dataSnapshot.getValue(LocationInfo.class);
                LatLng latLng = new LatLng(info.getLatitude(), info.getLongitude());

                if (currentAnimalMarker != null) {
                    currentAnimalMarker.remove();
                }
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

                currentAnimalMarker = mMap.addMarker(markerOptions);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        locationReference.addValueEventListener(valueEventListener);

        locationClient = LocationServices
                .getFusedLocationProviderClient(this);
        createLocationRequest();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);
                updateGoogleMap(latLng);
            }
        };

        checkLocationPermission();

        SupportMapFragment fragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        fragment.getMapAsync(this);
    }

    private void checkLocationPermission() {
        int check = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (check == PackageManager.PERMISSION_GRANTED) {
            // 최근 위치 -> 지도 업데이트
            getLastLocation();
            // 위치 정보 업데이트 요청
            requestLocationUpdate();

        } else {
            requestLocationPermission();
        }
    }

    private void requestLocationUpdate() {
        locationClient.requestLocationUpdates(
                locationRequest, locationCallback, null);
    }

    private void requestLocationPermission() {
        String[] permissions =
                {Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this,
                permissions, REQ_FINE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == REQ_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 가장 최근 위치 정보를 확인 -> 그 위치로 지도 이동
                getLastLocation();
                // 주기적 위치 업데이트 요청()
                requestLocationUpdate();
            } else {
                Toast.makeText(this,
                        "위치 권한이 있어야 앱을 사용할 수 있습니다.",
                        Toast.LENGTH_LONG).show();
                finish(); // Activity 종료
            }
        }
    }

    private void getLastLocation() {

        Task<Location> task = locationClient.getLastLocation();
        task.addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location == null) {
                    return;
                }
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                LatLng latLng = new LatLng(lat, lng);
                updateGoogleMap(latLng);
            }
        });
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);

        // TODO: Google 지도 화면 업데이트
    }

    private void updateGoogleMap(LatLng latLng) {
        if (mMap == null) {
            // GoogleMap 객체가 생성되어 있지 않을 때 화면 업데이트를 하면 안됨
            return;
        }

        if (currentMarker != null) currentMarker.remove();

        currentMarker = mMap.addMarker(new MarkerOptions().position(latLng));
        mMap.setMinZoomPreference(15);
        mMap.setMaxZoomPreference(20);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

//        if (currentMarker != null && currentAnimalMarker != null) {
//
//            DrawRoute.getInstance(this, MapsActivity.this)
//                    .setFromLatLong(currentMarker.getPosition().latitude, currentMarker.getPosition().longitude)
//                    .setToLatLong(currentAnimalMarker.getPosition().latitude, currentAnimalMarker.getPosition().longitude)
//                    .setGmapAndKey("AIzaSyDY5a6wDy34nyL_bQswV-1q9dPpKGMMnHU", mMap).run();
//        }


    }


    @Override
    protected void onPause() {
        super.onPause();

        // 주기적 위치 업데이트를 취소
        locationClient.removeLocationUpdates(locationCallback);
    }

    public void startLocationService(View view) {
        intent = new Intent(MapsActivity.this, LocationCompareService.class);
        startService(intent);
    }

    public void stopLocationService(View view) {
        stopService(intent);
    }

    @Override
    public void afterDraw(String result) {
        Log.d(TAG, result);
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//
//        stopService(intent);
//    }
}
