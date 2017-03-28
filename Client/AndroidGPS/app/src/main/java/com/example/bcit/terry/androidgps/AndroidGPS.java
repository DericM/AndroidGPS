package com.example.bcit.terry.androidgps;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URISyntaxException;
import java.util.Enumeration;

import io.socket.client.IO;
import io.socket.client.Socket;

/*------------------------------------------------------------------------------------------------------------------
* CLASS: AndroidGPS
*   Contains the main application class.
*
* PROGRAM: AndroidGPS
*
* FUNCTIONS:
*   public void onCreate()
*   public Socket setupSocket(final String serverIP, final String serverPort)
*   public void login()
*   public void sendLocationData(Location location)
*   private String getClientIP()
*   private String getMobileIP()
*   private String getDeviceID()
*
* SETTERS/GETTERS
*   public void setUsername(final String username)
*   public void setPassword(final String password)
*   public void setConnected(boolean connected)
*   public String getUsername()
*   public String getPassword()
*   public Socket getSocket()
*   public boolean isConnected()
*
* DATE: March 27, 2017
* REVISIONS: (Date and Description)
*
* DESIGNER:   Jackob Frank / Mark Tattrie
* PROGRAMMER: Terry Kang / Deric Mccadden
*
* NOTES:
*   This file Contains the main application class.
----------------------------------------------------------------------------------------------------------------------*/
public class AndroidGPS extends Application {
    private Socket mSocket;
    private String mServerIP;
    private String mServerPort;
    private String mUsername;
    private String mPassword;
    private String mServerUrl;
    private boolean isConnected;

    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: onCreate
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: public void onCreate()
    *
    * RETURN: void
    *
    * NOTES:
    *   Create a new Application.
    ----------------------------------------------------------------------------------------------------------------------*/
    public void onCreate() {
        super.onCreate();
        isConnected = false;
    }

    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: getSocket
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: Socket getSocket()
    *
    * RETURN: Socket
    *
    * NOTES:
    *   Socket getter.
    ----------------------------------------------------------------------------------------------------------------------*/
    public Socket getSocket() {
        return mSocket;
    }

    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: setupSocket
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: public Socket setupSocket(final String serverIP, final String serverPort)
    *
    * RETURN: Socket
    *
    * NOTES:
    *   Sets up a new socket
    ----------------------------------------------------------------------------------------------------------------------*/
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

    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: setConnected
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: public void setConnected(boolean connected)
    *
    * RETURN: void
    *
    * NOTES:
    *   Sets connected state.
    ----------------------------------------------------------------------------------------------------------------------*/
    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: isConnected
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: public boolean isConnected()
    *
    * RETURN: boolean
    *
    * NOTES:
    *   Checks connected state.
    ----------------------------------------------------------------------------------------------------------------------*/
    public boolean isConnected() {
        return isConnected;
    }

    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: login
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: public void login()
    *
    * RETURN: void
    *
    * NOTES:
    *   Logs in the user
    ----------------------------------------------------------------------------------------------------------------------*/
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

    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: sendLocationData
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Deric Mccadden
    *
    * INTERFACE: public void sendLocationData(Location location)
    *
    * RETURN: void
    *
    * NOTES:
    *   Send location data to the server.
    ----------------------------------------------------------------------------------------------------------------------*/
    public void sendLocationData(Location location){
        JSONObject clientInfo = new JSONObject();
        try{
            clientInfo.put("username", mUsername);
            clientInfo.put("deviceID", getDeviceID());
            clientInfo.put("deviceIP", getClientIP());
            clientInfo.put("latitude", location.getLatitude());
            clientInfo.put("longitude", location.getLongitude());
            clientInfo.put("time", System.currentTimeMillis() / 1000);
            clientInfo.put("accuracy", location.getAccuracy());
            mSocket.emit("location", clientInfo);
        }catch(JSONException ex){
            throw new RuntimeException(ex);
        }
    }

    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: setUsername
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: public void setUsername(final String username)
    *
    * RETURN: void
    *
    * NOTES:
    *   Sets the user name.
    ----------------------------------------------------------------------------------------------------------------------*/
    public void setUsername(final String username){
        mUsername = username;
    }

    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: setPassword
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: public void setPassword(final String password)
    *
    * RETURN: void
    *
    * NOTES:
    *   Sets the password.
    ----------------------------------------------------------------------------------------------------------------------*/
    public void setPassword(final String password){
        mPassword = password;
    }

    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: getUsername
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: public String getUsername()
    *
    * RETURN: String
    *
    * NOTES:
    *   gets the username.
    ----------------------------------------------------------------------------------------------------------------------*/
    public String getUsername(){return mUsername;}

    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: getPassword
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: public String getPassword()
    *
    * RETURN: String
    *
    * NOTES:
    *   gets the password.
    ----------------------------------------------------------------------------------------------------------------------*/
    public String getPassword(){
        return mPassword;
    }

    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: getClientIP
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: public String getClientIP()
    *
    * RETURN: String
    *
    * NOTES:
    *   gets the clientIP.
    ----------------------------------------------------------------------------------------------------------------------*/
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

    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: getMobileIP
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: public String getMobileIP()
    *
    * RETURN: String
    *
    * NOTES:
    *   gets the mobileIP.
    ----------------------------------------------------------------------------------------------------------------------*/
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

    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: getWifiIP
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: public String getWifiIP()
    *
    * RETURN: String
    *
    * NOTES:
    *   gets the getWifiIP.
    ----------------------------------------------------------------------------------------------------------------------*/
    private String getWifiIP()
    {
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        @SuppressWarnings("deprecation")
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }

    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: getDeviceID
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: public String getDeviceID()
    *
    * RETURN: String
    *
    * NOTES:
    *   gets the getDeviceID.
    ----------------------------------------------------------------------------------------------------------------------*/
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

    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: getServerUrl
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: public String getServerUrl()
    *
    * RETURN: String
    *
    * NOTES:
    *   gets the getServerUrl.
    ----------------------------------------------------------------------------------------------------------------------*/
    public String getServerUrl() {
        return mServerUrl;
    }

}
