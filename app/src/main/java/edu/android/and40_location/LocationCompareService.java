package edu.android.and40_location;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.util.concurrent.Executor;

public class LocationCompareService extends Service {

    private static final String TAG = "edu.android.and40";
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

    private LatLng animalLatLng, personLatLng;

    Thread t = null;

    public LocationCompareService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Service onCreate() 호출");

        database = FirebaseDatabase.getInstance();
        locationReference = database.getReference(TABLE_NAME);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LocationInfo info = dataSnapshot.getValue(LocationInfo.class);
                LatLng latLng = new LatLng(info.getLatitude(), info.getLongitude());

//                Log.i(TAG, "animalLatlng: " + info.getLatitude() + ", " + info.getLongitude());

                animalLatLng = latLng;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        locationReference.addValueEventListener(valueEventListener);

        locationClient = LocationServices
                .getFusedLocationProviderClient(LocationCompareService.this);
        createLocationRequest();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);

//                Log.i(TAG, "personLatlng: " + latitude + ", " + longitude);

                personLatLng = latLng;

            }
        };
//        getLastLocation();
        requestLocationUpdate();


    }

    private void getLastLocation() {

        Task<Location> task = locationClient.getLastLocation();
        task.addOnSuccessListener((Executor) this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location == null) {
                    return;
                }
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                personLatLng = new LatLng(lat, lng);
            }
        });
    }

    private void requestLocationUpdate() {
        locationClient.requestLocationUpdates(
                locationRequest, locationCallback, null);
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand() 호출");
        Runnable r = new Runnable() {
            @Override
            public void run() {

                while (true) {
                    if (personLatLng != null && animalLatLng != null) {

                        try {
                            Log.i(TAG, "personLocation(" + personLatLng.latitude + ", " + personLatLng.longitude + ") animalLocation(" + animalLatLng.latitude + ", " + animalLatLng.longitude + ")");
                            Log.i(TAG, "사람과 동물 사이의 거리: " + SphericalUtil.computeDistanceBetween(personLatLng, animalLatLng) + "m");

                            double radius = 5;
                            double distance = SphericalUtil.computeDistanceBetween(personLatLng, animalLatLng);
                            if (distance > radius) {
                                Log.i(TAG, "거리 초과");

                                stopSelf();
                                throw new InterruptedException();
                            }

                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Log.i(TAG, "Thread Interrupt 되어 스레드 종료함");


                            break;
                        }
                    } else {
                        Log.i(TAG, "위도/경도 값을 받지 못함");
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Log.i(TAG, "Thread Interrupt 되어 스레드 종료함");
                            break;
                        }
                    }
                }

            }
        };
        t = new Thread(r);
        t.start();
        Log.i(TAG, "Service 내에서 작업스레드 생성 및 실행");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        locationClient.removeLocationUpdates(locationCallback);
        t.interrupt();

        //TODO: 알림음, 알림진동과 단말기 상태표시줄에 알림을 띄우고, 그 알림을 터치했을 때 진행하는 프로젝트 어플리케이션을 실행하는 Notification builder 를 생성하고 실행

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "channel";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelId,channelName,importance);
            notificationManager.createNotificationChannel(channel);
        }

        Bitmap mLargeIconForNoti =
                BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_large_);

        Intent intent = new Intent(LocationCompareService.this, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Context context = LocationCompareService.this;
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context,channelId)
                        .setSmallIcon(R.drawable.notification_icon_)
                        .setContentTitle("동물과 거리가 멀어짐")
                        .setContentText("알림을 클릭하여 반려동물의 위치를 확인하세요.")
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setLargeIcon(mLargeIconForNoti)
//                        .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(mLargeIconForNoti).bigLargeIcon(null))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setAutoCancel(true)
                        .setWhen(System.currentTimeMillis())
                        .setContentIntent(pendingIntent)
                        .setVibrate(new long[]{500,500,500,500});



        notificationManager.notify(0, builder.build());

        Log.i(TAG, "Service onDestroy() 호출");
        Log.i(TAG, "Service 종료됨...");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
