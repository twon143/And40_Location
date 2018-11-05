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
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "edu.android.and39";
    private static final int REQ_FINE_LOCATION = 100;

    public static final String TABLE_NAME = "location";

    private FirebaseDatabase database;
    private DatabaseReference locationReference;
    private ValueEventListener valueEventListener;

    private GoogleMap mMap;
    private Marker currentMarker = null;
    private Marker currentAnimalMarker = null;

    private Polyline polyline = null;

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

        if(currentMarker != null && currentAnimalMarker != null){
            if(polyline != null){
                polyline.remove();
            }

            String URL = getDirectionURL(currentMarker.getPosition(), currentAnimalMarker.getPosition());
//            String URL = getDirectionURL(new LatLng(13.03, 77.6), new LatLng(13.0, 77.0));
            GetDirectionTask directionTask = new GetDirectionTask();
            directionTask.execute(URL);

        }

        mMap.setMinZoomPreference(15);
        mMap.setMaxZoomPreference(20);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));


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


//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//
//        stopService(intent);
//    }

//--------------------------- Google Direction API로 두 위치 사이의 경로를 표시하는 기능
    //-------------------------- but, 한국에서 경로를 표시하려면 권한(사업자 등록)을 해야한다고 한다. 개인 프로젝트에서 불가능함..
    public String getDirectionURL(LatLng origin, LatLng dest) {
        return "https://maps.googleapis.com/maps/api/directions/json?origin="
                + String.valueOf(origin.latitude) + "," + String.valueOf(origin.longitude)
                + "&destination="
                + String.valueOf(dest.latitude) + "," + String.valueOf(dest.longitude)
                + "&mode=walking&key=AIzaSyCB-00ZkTlkPyYFgaI_9N90M3KxYqU43jU";
    }

    class GetDirectionTask extends AsyncTask<String, Void, List<List<LatLng>>> {

        @Override
        protected List<List<LatLng>> doInBackground(String... strings) {
            Log.i(TAG, "GetDirectionTask doInBackground() 호출");
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(strings[0]).build();
            if(request != null){
                Log.i(TAG, request.toString());
            }
            List<List<LatLng>> result = new ArrayList<>();
            try {
                Response response = client.newCall(request).execute();
                String data = response.body().toString();

                GoogleMapDTO respObj = new Gson().fromJson(data, GoogleMapDTO.class);

                List<LatLng> path = new ArrayList<>();

                for (Steps steps :
                        respObj.getRoutes().get(0).getLegs().get(0).getSteps()) {

                    Log.i(TAG, "시작 위치: " + steps.getStart_location().getLat() + "," + steps.getStart_location().getLng());
                    Log.i(TAG, "마지막 위치: " + steps.getEnd_location().getLat() + "," + steps.getEnd_location().getLng());

                    LatLng startLatLng = new LatLng(Double.parseDouble(steps.getStart_location().getLat()),
                            Double.parseDouble(steps.getStart_location().getLng()));
                    path.add(startLatLng);
                    LatLng endLatLng = new LatLng(Double.parseDouble(steps.getEnd_location().getLat()),
                            Double.parseDouble(steps.getEnd_location().getLng()));
                    path.add(endLatLng);
                }
                result.add(path);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(List<List<LatLng>> lists) {
//            super.onPostExecute(lists);
            PolylineOptions lineOption = new PolylineOptions();
            for (List<LatLng> list : lists) {
                lineOption.addAll(list);
                lineOption.width(10f);
                lineOption.color(Color.BLUE);
                lineOption.geodesic(true);
            }
            polyline = mMap.addPolyline(lineOption);
        }
    }
}
