package edu.android.and40_location;

public class LocationInfo {
    private double Latitude;
    private double Longitude;

    public LocationInfo() {
    }

    public LocationInfo(double latitude, double longitude) {
        Latitude = latitude;
        Longitude = longitude;
    }

    @Override
    public String toString() {
        return "LocationInfo{" +
                "Latitude=" + Latitude +
                ", Longitude=" + Longitude +
                '}';
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }
}
