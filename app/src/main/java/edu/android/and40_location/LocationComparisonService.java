package edu.android.and40_location;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LocationComparisonService extends IntentService {

    private static final int REQ_FINE_LOCATION = 100;

    public static final String TABLE_NAME = "location";
    private Location animalLocation;
    public static boolean isrunning = false;
    private FirebaseDatabase database;
    private DatabaseReference locationReference;
    private ValueEventListener valueEventListener;

    // 위치 정보(최근 위치, 주기적 업데이트 시작/취소)와 관련된 클래스
    private FusedLocationProviderClient locationClient;
    // 주기적 위치 업데이트를 요청할 때 설정 정보를 저장하는 클래스
    private LocationRequest locationRequest;
    // 주기적 위치 정보를 처리하는 콜백
    private LocationCallback locationCallback;

    public LocationComparisonService() {
        super("LocationComparisonService");
    }

    @Override
    public void onCreate() {
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        locationCallback = (LocationCallback) new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location personLocation = locationResult.getLastLocation();

                double latitude = personLocation.getLatitude();
                double longitude = personLocation.getLongitude();

                LatLng latLng = new LatLng(latitude,longitude);

                if(animalLocation !=null){




                }
            }
        };
        database = FirebaseDatabase.getInstance();
        locationReference = database.getReference(TABLE_NAME);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                LocationInfo info = dataSnapshot.getValue(LocationInfo.class);
                LatLng latLng = new LatLng(info.getLatitude(), info.getLongitude());



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        super.onCreate();
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isrunning = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {


    }

    @Override
    public boolean stopService(Intent name) {
        isrunning = false;
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
