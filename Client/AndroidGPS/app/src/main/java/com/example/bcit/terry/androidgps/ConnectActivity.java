package com.example.bcit.terry.androidgps;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ConnectActivity extends AppCompatActivity {
    private Socket mSocket;
    private AndroidGPS app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        app = (AndroidGPS) getApplicationContext();

        EditText serverIpView = (EditText)findViewById(R.id.input_server_ip);
        EditText serverPortView = (EditText)findViewById(R.id.input_server_port);

        serverIpView.setText("96.49.228.48");
        serverPortView.setText("4200");


        Button connectButton = (Button) findViewById(R.id.connect_button);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptConnect();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Destroy", "ConnectActivity");
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("login_success", onLogin);
        mSocket.off("login_error", onLoginError);
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptConnect() {
        //mSocket.emit("disconnect");
        // Reset errors.
//        mUsernameView.setError(null);

        EditText serverIpView = (EditText)findViewById(R.id.input_server_ip);
        EditText serverPortView = (EditText)findViewById(R.id.input_server_port);
        EditText usernameView = (EditText)findViewById(R.id.input_username);
        EditText passwordView = (EditText)findViewById(R.id.input_password);


        // Store values at the time of the connect attempt.
        String serverIP = serverIpView.getText().toString().trim();
        String serverPort = serverPortView.getText().toString().trim();
        String username = usernameView.getText().toString().trim();
        String password = passwordView.getText().toString().trim();

        // Check for a valid ip address.
        if (TextUtils.isEmpty(serverIP)) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            serverIpView.setError(getString(R.string.error_field_required));
            serverIpView.requestFocus();
            return;
        }

        // Check for a valid port.
        if (TextUtils.isEmpty(serverPort)) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            serverPortView.setError(getString(R.string.error_field_required));
            serverPortView.requestFocus();
            return;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            usernameView.setError(getString(R.string.error_field_required));
            usernameView.requestFocus();
            return;
        }

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            passwordView.setError(getString(R.string.error_field_required));
            passwordView.requestFocus();
            return;
        }

        mSocket = app.setupSocket(serverIP, serverPort);
        mSocket.connect();

        app.setUsername(username);
        app.setPassword(password);


        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("login_success", onLogin);
        mSocket.on("login_error", onLoginError);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            app.login();
            app.setConnected(true);
            ConnectActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Log.d("onConnect", "ConnectActivity");
                    //Toast.makeText(getApplicationContext(), R.string.connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            ConnectActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Log.d("onConnectError", "ConnectActivity");
                    Toast.makeText(getApplicationContext(), R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
            mSocket.close();
        }
    };

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            ConnectActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Log.d("onLogin", "ConnectActivity");
                    Toast.makeText(getApplicationContext(), R.string.connect, Toast.LENGTH_LONG).show();
                }
            });
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private Emitter.Listener onLoginError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            app.login();
            app.setConnected(true);
            ConnectActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Log.d("onLoginError", "ConnectActivity");
                    Toast.makeText(getApplicationContext(), R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
            mSocket.close();
        }
    };
}
