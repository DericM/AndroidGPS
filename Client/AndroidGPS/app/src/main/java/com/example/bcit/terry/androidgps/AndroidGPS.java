package com.example.bcit.terry.androidgps;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

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
    public void setConnected(boolean connected){
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
            mSocket.emit("connected", clientInfo);
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

    private String getClientIP(){
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        return ipAddress;
    }

    private String getDeviceID(){
        TelephonyManager    telephonyManager;

        telephonyManager  =
                (TelephonyManager)getSystemService( Context.TELEPHONY_SERVICE );

        /*
         * getDeviceId() function Returns the unique device ID.
         * for example,the IMEI for GSM and the MEID or ESN for CDMA phones.
         */
        return telephonyManager.getDeviceId();
    }

    public String getServerUrl(){
        return mServerUrl;
    }
}
