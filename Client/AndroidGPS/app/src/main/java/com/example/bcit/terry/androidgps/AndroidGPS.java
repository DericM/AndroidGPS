package com.example.bcit.terry.androidgps;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URISyntaxException;
import java.util.Enumeration;

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
    private String mUsername;
    private String mPassword;
    private String mServerUrl;
    private boolean isConnected;

    public void onCreate() {
        super.onCreate();
        isConnected = false;
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

    public void login(){
        JSONObject clientInfo = new JSONObject();
        try{
            clientInfo.put("username", mUsername);
            clientInfo.put("password", mPassword);
            clientInfo.put("deviceID", getDeviceID());
            clientInfo.put("ipAddress", getClientIP());
            mSocket.emit("login", clientInfo);
        }catch(JSONException ex){
            throw new RuntimeException(ex);
        }
    }

    public void sendLocationData(Location location){
        JSONObject clientInfo = new JSONObject();
        try{
            clientInfo.put("username", mUsername);
            clientInfo.put("deviceID", getDeviceID());
            clientInfo.put("deviceIP", getClientIP());
            clientInfo.put("latitude", location.getLatitude());
            clientInfo.put("longitude", location.getLongitude());
            clientInfo.put("time", System.currentTimeMillis() / 1000);
            mSocket.emit("location", clientInfo);
        }catch(JSONException ex){
            throw new RuntimeException(ex);
        }
    }

    public void setUsername(final String username){
        mUsername = username;
    }
    public void setPassword(final String password){
        mPassword = password;
    }
    public String getUsername(){return mUsername;}
    public String getPassword(){
        return mPassword;
    }


    private String getClientIP(){
        try {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            @SuppressWarnings("deprecation")
            NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            @SuppressWarnings("deprecation")
            NetworkInfo mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (wifi.isConnected()) {
                // If Wi-Fi connected
                return getWifiIP();
            }

            if (mobile.isConnected()) {
                // If Internet connected
                return getMobileIP();
            }

            return null;
        }catch(SecurityException e){
            return null;
        }
    }

    private String getMobileIP(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements();) {
                NetworkInterface networkinterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkinterface.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("Current IP", ex.toString());
        }
        return null;
    }

    private String getWifiIP()
    {
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        @SuppressWarnings("deprecation")
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }


    private String getDeviceID() {
        try {
            TelephonyManager telephonyManager;

            telephonyManager =
                    (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        /*
         * getDeviceId() function Returns the unique device ID.
         * for example,the IMEI for GSM and the MEID or ESN for CDMA phones.
         */
            return telephonyManager.getDeviceId();
        }catch(SecurityException e){
            return null;
        }
    }

    public String getServerUrl() {
        return mServerUrl;
    }

}
