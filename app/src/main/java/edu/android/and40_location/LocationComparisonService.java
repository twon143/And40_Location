package edu.android.and40_location;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LocationComparisonService extends IntentService {

    private static final int REQ_FINE_LOCATION = 100;

    public static final String TABLE_NAME = "location";

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
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
