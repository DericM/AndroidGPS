package com.example.bcit.terry.androidgps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.content.ContentValues.TAG;

/*------------------------------------------------------------------------------------------------------------------
* CLASS: MainActivity
*   Contains the main Apps entry point.
*
* PROGRAM: AndroidGPS
*
* FUNCTIONS:
*   protected void onCreate(Bundle savedInstanceState)
*   private void startConnect()
*   public void onActivityResult(int requestCode, int resultCode, Intent data)
*
* DATE: March 27, 2017
* REVISIONS: (Date and Description)
*
* DESIGNER:   Jackob Frank / Mark Tattrie
* PROGRAMMER: Terry Kang / Deric Mccadden
*
* NOTES:
*   This file Contains the main Apps entry point.
----------------------------------------------------------------------------------------------------------------------*/
public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CONNECT = 0;
    private Socket mSocket;
    private AndroidGPS app;
    private MyLocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();
        app = (AndroidGPS) getApplicationContext();

        if(!app.isConnected())
            startConnect();

        locationListener = new MyLocationListener(this);
    }

    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: startConnect
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: private void startConnect()
    *
    * RETURN: void
    *
    * NOTES:
    *   Start the connect activity
    ----------------------------------------------------------------------------------------------------------------------*/
    private void startConnect() {
        Intent intent = new Intent(MainActivity.this, ConnectActivity.class);
        startActivityForResult(intent, REQUEST_CONNECT);
    }


    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: onActivityResult
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: private void onActivityResult()
    *
    * RETURN: void
    *
    * NOTES:
    *   Handle returns results from the connection activity
    ----------------------------------------------------------------------------------------------------------------------*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)  {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("OnActivityResult", "Main");
        if (requestCode == REQUEST_CONNECT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                mSocket = app.getSocket();
                locationListener.init();
                mSocket.on(Socket.EVENT_CONNECT, onConnect);
                mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
                mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
                mSocket.on("login_error", onLoginError);

                WebView webView = (WebView) findViewById(R.id.webView);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setAllowContentAccess(true);
                webView.getSettings().setAppCacheEnabled(true);
                webView.getSettings().setDatabaseEnabled(true);
                webView.getSettings().setDomStorageEnabled(true);
                String url = app.getServerUrl() + "/doLogin";
                try{
                    String postData = "username=" + URLEncoder.encode(app.getUsername(), "UTF-8") + "&password=" + URLEncoder.encode(app.getPassword(), "UTF-8");
                    webView.postUrl(url, postData.getBytes());
                }catch(UnsupportedEncodingException e){
                    webView.loadUrl(app.getServerUrl());

                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    WebView.setWebContentsDebuggingEnabled(true);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE))
                    { WebView.setWebContentsDebuggingEnabled(true); }
                }
            }
        }
    }


    /*------------------------------------------------------------------------------------------------------------------
    * LISTENER: onConnect
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: private Emitter.Listener onConnect
    *
    * NOTES:
    *   Listens for onConnect
    ----------------------------------------------------------------------------------------------------------------------*/
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!app.isConnected()) {
                        app.login();
                        Toast.makeText(MainActivity.this.getApplicationContext(),
                        R.string.connect, Toast.LENGTH_LONG).show();
                        app.setConnected(true);
                    }
                }
            });
        }
    };

    /*------------------------------------------------------------------------------------------------------------------
    * LISTENER: onDisconnect
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: private Emitter.Listener onDisconnect
    *
    * NOTES:
    *   Listens for onDisconnect
    ----------------------------------------------------------------------------------------------------------------------*/
    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    app.setConnected(false);
                    Toast.makeText(MainActivity.this.getApplicationContext(),
                            R.string.disconnect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    /*------------------------------------------------------------------------------------------------------------------
    * LISTENER: onConnectError
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: private Emitter.Listener onConnectError
    *
    * NOTES:
    *   Listens for onConnectError
    ----------------------------------------------------------------------------------------------------------------------*/
    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    app.setConnected(false);
                    Toast.makeText(MainActivity.this.getApplicationContext(),
                            R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    /*------------------------------------------------------------------------------------------------------------------
    * LISTENER: onLoginError
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: private Emitter.Listener onLoginError
    *
    * NOTES:
    *   Listens for onLoginError
    ----------------------------------------------------------------------------------------------------------------------*/
    private Emitter.Listener onLoginError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Log.d("onLoginError", "MainActivity");
                    //Toast.makeText(getApplicationContext(), R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
            mSocket.close();
            startConnect();
        }
    };

    /*------------------------------------------------------------------------------------------------------------------
    * FUNCTION: checkPermissions
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Terry Kang
    *
    * INTERFACE: private void checkPermissions()
    *
    * NOTES:
    *   Checks and prompts for permissions
    ----------------------------------------------------------------------------------------------------------------------*/
    private void checkPermissions(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new
                    String[]{Manifest.permission.READ_PHONE_STATE},1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new
                    String[]{Manifest.permission.ACCESS_NETWORK_STATE},1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new
                    String[]{Manifest.permission.INTERNET},1);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new
                    String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new
                    String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }
    }

    /*------------------------------------------------------------------------------------------------------------------
    * CLASS: MyLocationListener
    *   Contains a custom LocationListener
    *
    * PROGRAM: AndroidGPS
    *
    * FUNCTIONS:
    *   public MyLocationListener(Context context)
    *   public void onLocationChanged(Location loc)
    *   public void init()
    *
    * DATE: March 27, 2017
    * REVISIONS: (Date and Description)
    *
    * DESIGNER:   Jackob Frank / Mark Tattrie
    * PROGRAMMER: Deric Mccadden / Terry Kang
    *
    * NOTES:
    *   This file Contains the main Apps entry point.
    ----------------------------------------------------------------------------------------------------------------------*/
    private class MyLocationListener implements LocationListener {
        private final Context mContext;
        private LocationManager locationManager = null;
        public Location currentLocation;
        Location location; // location
        boolean isGPSEnabled = false;// flag for GPS status
        boolean isNetworkEnabled = false;// flag for network status

        // The minimum distance to change Updates in meters
        private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 50; // 50 meters
        // The minimum time between updates in milliseconds
        private static final long MIN_TIME_BW_UPDATES = 2000; // 2 seconds

        public MyLocationListener(Context context){
                this.mContext = context;
        }

        /*------------------------------------------------------------------------------------------------------------------
        * FUNCTION: onLocationChanged
        *
        * DATE: March 27, 2017
        * REVISIONS: (Date and Description)
        *
        * DESIGNER:   Jackob Frank / Mark Tattrie
        * PROGRAMMER: Deric Mccadden
        *
        * INTERFACE: public void onLocationChanged(Location loc)
        *
        * NOTES:
        *   Called on location change events.
        ----------------------------------------------------------------------------------------------------------------------*/
        @Override
        public void onLocationChanged(Location loc) {
            if(loc.getAccuracy() > 35)
                return;

            Log.v(TAG, "" + currentLocation.getLatitude());
            Log.v(TAG, "" + currentLocation.getLongitude());
            String locationMsg = "Location changed:"
                    + "\nLat: " + currentLocation.getLatitude()
                    + "\nLng: " + currentLocation.getLongitude();
            Toast.makeText(getBaseContext(), locationMsg, Toast.LENGTH_SHORT).show();
            app.sendLocationData(loc);
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

        /*------------------------------------------------------------------------------------------------------------------
        * FUNCTION: init
        *
        * DATE: March 27, 2017
        * REVISIONS: (Date and Description)
        *
        * DESIGNER:   Jackob Frank / Mark Tattrie
        * PROGRAMMER: Deric Mccadden
        *
        * INTERFACE: public void init()
        *
        * NOTES:
        *   Initialize the location listener.
        ----------------------------------------------------------------------------------------------------------------------*/
        public void init(){
            Toast.makeText(getBaseContext(), "Init GPS", Toast.LENGTH_SHORT).show();
            try {

                locationManager = (LocationManager) mContext
                        .getSystemService(LOCATION_SERVICE);

                // getting GPS status
                isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                // getting network status
                isNetworkEnabled = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    // no network provider is enabled
                } else {
                    // First get location from Network Provider
                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");

                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                currentLocation = location;
                            }
                        }
                    }
                    // if GPS Enabled get lat/long using GPS Services
                    if (isGPSEnabled) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                currentLocation = location;

                            }
                        }
                    }
                }

            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }
}
