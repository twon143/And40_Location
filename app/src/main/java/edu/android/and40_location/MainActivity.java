package edu.android.and40_location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "edu.android.and40";
    private static final int REQ_CODE_LOCATION = 100;

    public static final String TABLE_NAME = "location";

    private FirebaseDatabase database;
    private DatabaseReference locationReference;


    private TextView textView;
    // Google Play Service의 위치 정보를 사용하는 클래스
    private FusedLocationProviderClient locationClient;
    // 주기적인 위치 정보 업데이트를 위한 설정을 하는 클래스
    private LocationRequest locationRequest;
    // 주기적인 위치 정보를 전달받는 콜백
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();
        locationReference = database.getReference(TABLE_NAME);



        textView = findViewById(R.id.textView);

        // Google Play Service에 포함된
        // FusedLocationProviderClient 객체를 가져옴
        locationClient = LocationServices.
                getFusedLocationProviderClient(this);

        // 주기적인 위치 정보 업데이트 설정
        createLocationRequest();

        // LocationCallback 객체 생성
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // 주기적인 위치 정보를 요청했을 때
                // ART이 위치 정보를 주기적으로 전달할 때 호출되는 콜백 메소드
                // GPS에서 전달받은 위치 정보
                Location location = locationResult.getLastLocation();

                // 위치를 정보를 전달받은 시간
                Date currentDate = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String current = sdf.format(currentDate);

                String text = current + "\n"
                        + "위도: " + location.getLatitude() + "\n"
                        + "경도: " + location.getLongitude();
                textView.setText(text);

                locationReference.setValue(new LocationInfo(location.getLatitude(), location.getLongitude()));

            }
        };

        // 위치 정보 권한(permission)이 허용됐는 지 체크
        checkLocationPermission();

    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        // 위치 정보를 전달받는 주기(간격), 단위: ms
        locationRequest.setInterval(5 * 1000);
        // 주기적인 위치 정보를 처리할 수 있는 가장 짧은 주기, 단위: ms
        locationRequest.setFastestInterval(2500);
        // GPS와 배터리 사용량 우선 순위를 결정
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void checkLocationPermission() {
        int check = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (check == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "위치 정보 허용됨");
            // 가장 최근 위치 정보 확인
            getLastLocation();

        } else {
            Log.i(TAG, "위치 정보 거부됨");
            // 사용자에게 위치 정보 허용을 요청
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        String[] permissions =
                { Manifest.permission.ACCESS_FINE_LOCATION };

        // 사용자에게 permission 요청 다이얼로그를 띄워줌
        // -> 사용자의 허용/거부 선택은 콜백 메소드로 전달됨
        ActivityCompat.requestPermissions(this,
                permissions,
                REQ_CODE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == REQ_CODE_LOCATION) {
            if (grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: 허용");
                // 가장 최근 위치를 검색함
                getLastLocation();

            } else {
                Log.i(TAG, "onRequestPermissionsResult: 거부");
                textView.setText("위치 정보를 허용하지 않으면 앱을 사용할 수 없습니다.");
            }
        }

    }


    private void getLastLocation() {
        Task<Location> task = locationClient.getLastLocation();
        task.addOnSuccessListener(this,
                new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double lat = location.getLatitude(); // 위도
                    double lon = location.getLongitude(); // 경도
                    textView.setText("위도: " + lat + ", 경도: " + lon);
                    locationReference.setValue(new LocationInfo(location.getLatitude(), location.getLongitude()));

                } else {
                    textView.setText("최근 위치 정보 확인 불가");
                }
            }
        });

    }

    public void startLocationUpdate(View view) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,
                    "위치 권한을 허용해 주세요.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 주기적인 GPS 정보 요청
        locationClient.requestLocationUpdates(locationRequest,
                locationCallback, null);
    }

    public void stopLocationUpdate(View view) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // 주기적인 위치 정보 업데이트를 취소
        locationClient.removeLocationUpdates(locationCallback);
    }

}
