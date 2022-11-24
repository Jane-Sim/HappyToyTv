package com.example.seyoung.happytoytv.config;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;


/**
 * 사용자가 앱을 시작할 때, 위도와 경도를 저장해주는 클래스입니다.
 * 계속 위치를 받아오는 것이 아닌 마지막에 업데이트한 위도 경도를 계속 사용합니다.
 */

public class GeoItem {
    public static double knownLatitude;
    public static double knownLongitude;
    public static Location knownLocation;
    /**
     * 사용자의 위도, 경도 객체를 반환한다. 만약 사용자의 위치를 알 수 없다면 서울 위치를 반환한다.
     * @return LatLng 위도,경도 객체
     */

    public static LatLng getKnownLocation() {

if (knownLatitude == 0 || knownLongitude == 0) {
            return new LatLng(37.484876, 126.970673);
        } else {
   return new LatLng(knownLatitude, knownLongitude);
       }
    }


    public static Location getKnownLocation2() {
        if (knownLatitude == 0 || knownLongitude == 0) {
            return new Location(knownLocation);
        } else {
            return new Location(knownLocation);
        }
    }

}