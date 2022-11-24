package com.example.seyoung.happytoytv.config;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;

/**
 * 사용자의 위도 경도를 저장하고 가져올 수 있는 클래스입니다.
 * 또한 사용자가 위치 권한을 설정했는 지 알아내는 클래스이기도 합니다.
 */
public class GeoLib {
    private LocationManager locationManager;                    // 사용자의 기기에서 위치기반을 켰는 지 확인해주는 위치 매니저입니다
    public final String TAG = GeoLib.class.getSimpleName();
    private volatile static GeoLib instance;
    Location location = null;

    public static GeoLib getInstance() {
        if (instance == null) {
            synchronized (GeoLib.class) {
                if (instance == null) {
                    instance = new GeoLib();
                }
            }
        }
        return instance;
    }

    /**
     * 사용자의 현재 위도, 경도를 반환한다.
     * 실제로는 최근 측정된 위치 정보이다.
     * @param context 컨텍스트 객체
     */
    public void setLastKnownLocation(Context context) {
        locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        int result = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
        }

    }
}