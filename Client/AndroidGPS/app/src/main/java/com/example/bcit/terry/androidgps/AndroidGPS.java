package com.example.bcit.terry.androidgps;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

import static android.content.ContentValues.TAG;

/**
 * Created by Terry on 2017-03-12.
 */
public class AndroidGPS extends Application {
    private Socket mSocket;
    private String mServerIP;
    private String mServerPort;
    private String mServerUrl;
    private boolean isConnected;

    private LocationManager locationManager = null;
    private LocationListener locationListener = null;
    private Location currentLocation;


    public void onCreate() {
        super.onCreate();
        isConnected = false;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        currentLocation = getLastBestLocation();

        locationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
    }

    public Socket getSocket() {
        return mSocket;
    }

    public Socket setupSocket(final String serverIP, final String serverPort) {
        mServerIP = serverIP;
        mServerPort = serverPort;
        mServerUrl = "http://" + mServerIP + ":" + mServerPort;
        try {
            mSocket = IO.socket(mServerUrl);
            return mSocket;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void sendClientInfo() {
        JSONObject clientInfo = new JSONObject();
        try {
            clientInfo.put("id", getDeviceID());
            clientInfo.put("ipAddress", getClientIP());
            clientInfo.put("latitude", currentLocation.getLatitude());
            clientInfo.put("longitude", currentLocation.getLongitude());
            mSocket.emit("connected", clientInfo);
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String getClientIP() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        return ipAddress;
    }

    private String getDeviceID() {
        TelephonyManager telephonyManager;

        telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        /*
         * getDeviceId() function Returns the unique device ID.
         * for example,the IMEI for GSM and the MEID or ESN for CDMA phones.
         */
        return telephonyManager.getDeviceId();
    }

    public String getServerUrl() {
        return mServerUrl;
    }


    private Location getLastBestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Location defaultLoc = new Location("default");
            defaultLoc.setLatitude(0);
            defaultLoc.setLongitude(0);
            return defaultLoc;
        }
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) {
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }


    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            currentLocation = loc;
            Log.v(TAG, "" + currentLocation.getLatitude());
            Log.v(TAG, "" + currentLocation.getLongitude());
            String locationMsg= "Location changed : Lat: " + currentLocation.getLatitude()
                    + " Lng: " + currentLocation.getLongitude();
            Toast.makeText(getBaseContext(), locationMsg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }
}
