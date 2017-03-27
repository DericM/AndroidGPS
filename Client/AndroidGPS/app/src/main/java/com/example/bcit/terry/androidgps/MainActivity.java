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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.content.ContentValues.TAG;


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

    private void startConnect() {
        Intent intent = new Intent(MainActivity.this, ConnectActivity.class);
        startActivityForResult(intent, REQUEST_CONNECT);
    }



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

    private class MyLocationListener implements LocationListener {
        private final Context mContext;

        private LocationManager locationManager = null;

        public Location currentLocation;
        // flag for GPS status
        boolean isGPSEnabled = false;

        // flag for network status
        boolean isNetworkEnabled = false;

        Location location; // location
        public MyLocationListener(Context context){
                this.mContext = context;
        }

        // The minimum distance to change Updates in meters
        private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 50; // 50 meters

        // The minimum time between updates in milliseconds
        private static final long MIN_TIME_BW_UPDATES = 5000; // 5 seconds

        @Override
        public void onLocationChanged(Location loc) {
            currentLocation = loc;
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
                        if (location == null) {
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
                }

            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }
}
