package com.grayraven.garage;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class MainActivity extends AppCompatActivity {

    final String TAG = "Garage_Main";
    private static Context mContext = GarageApp.getAppContext();
    private static final int STATUS_UNKNOWN = -1;
    private static final int STATUS_ERROR = 0;
    private static final int STATUS_DOOR_CLOSED = 1;
    private static final int STATUS_DOOR_OPEN = 2;
    private static final int STATUS_TIMEOUT = 3;
    private final int STATUS = STATUS_UNKNOWN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        int brightness = 30;
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
        Log.d(TAG, "brightness: " + brightness);
        RestartMQTT(3000);
    }

    final String broker = "tcp://10.211.1.127";
    String password = "monkey123";
    String username = "pi";
    final String topic = "door_distance";

    MqttClient client;
    IMqttToken token;

    void RestartMQTT (int delay) {  //connection delay in milliseconds

        Log.d(TAG, "RestartMQTT");

        String clientId = MqttAsyncClient.generateClientId();
        {
            /* not yet required since we don't change the state of anything in the real world

            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setPassword(password.toCharArray());
            connectOptions.setUserName(username); */

            try {
                MemoryPersistence persistence = new MemoryPersistence();
                client = new MqttClient(broker, clientId, persistence);
                client.setCallback(new MqttEventCallback());
                client.setTimeToWait(delay);
                client.connect(/*connectOptions*/);
                client.subscribe(topic);
            } catch (MqttSecurityException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                e.printStackTrace();
                switch (e.getReasonCode()) {
                    case MqttException.REASON_CODE_BROKER_UNAVAILABLE:
                    case MqttException.REASON_CODE_CLIENT_TIMEOUT:
                    case MqttException.REASON_CODE_CONNECTION_LOST:
                    case MqttException.REASON_CODE_SERVER_CONNECT_ERROR:
                        Log.d(TAG, "exception: " + e.getMessage());
                        e.printStackTrace();
                        break;
                    case MqttException.REASON_CODE_FAILED_AUTHENTICATION:
                        Log.d(TAG, "REASON_CODE_FAILED_AUTHENTICATION");
                        Log.d(TAG, "exception: " + e.getMessage());
                        break;
                    default:
                        Log.d(TAG, "default exception cause: " + e.getReasonCode() + " cause: " + e.getCause());
                        Log.d(TAG, "a" + e.getMessage());
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GarageMqttMessage garageMqttMessage) {
        Log.d(TAG, "GarageMqttMessage msg: " + garageMqttMessage.msg);
        String msg = garageMqttMessage.msg.toString();
        float distance = Float.parseFloat(msg);
        if (distance > 13.5) {
                SetDisplayMode(STATUS_DOOR_CLOSED, "");
        } else if (distance < 13.5 && distance >= 0 ) {
                SetDisplayMode(STATUS_DOOR_OPEN, "");
        } else if (distance == -200) {
            SetDisplayMode(STATUS_TIMEOUT, "timeout"); //todo: define -200 with a symbol
            RestartMQTT(6000);
        } else if (distance < 8) {
            SetDisplayMode(STATUS_ERROR, garageMqttMessage.msg.toString());
        }
    }

    private void SetDisplayMode(int status, String reason){
        TextView main_text;
        View main_layout;
        setContentView(R.layout.activity_main);
        main_text = (TextView)findViewById(R.id.main_text);
        main_layout = (View)findViewById(R.id.main_layout);
        main_text.setTextColor(Color.WHITE);
        switch(status) {
            case STATUS_TIMEOUT:
                Log.d(TAG, "status timeout");
                main_layout.setBackgroundColor(getResources().getColor(R.color.red));
                main_text.setText("LOST CONTACT WITH DOOR");
                break;

            case STATUS_UNKNOWN:
                Log.d(TAG, "STATUS_UNKNOWN");
                main_layout.setBackgroundColor(getResources().getColor(R.color.yellow));
                main_text.setTextColor(getResources().getColor(R.color.darkText));
                main_text.setText("DOOR STATUS UNKNOWN");
                break;

            case STATUS_DOOR_OPEN:
                Log.d(TAG, "STATUS_DOOR_OPEN");
                main_layout.setBackgroundColor(getResources().getColor(R.color.blue));
                main_text.setText("DOOR OPEN");
                break;

            case STATUS_DOOR_CLOSED:
                Log.d(TAG, "STATUS_DOOR_CLOSED");
                main_layout.setBackgroundColor(getResources().getColor(R.color.green));
                main_text.setText("DOOR CLOSED");
                break;

            case STATUS_ERROR:
                main_layout.setBackgroundColor((getResources().getColor(R.color.red)));
                main_text.setText("SYSTEM ERROR");
        }

    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        if(client.isConnected()) {
            try {
                client.disconnect();
                client.close();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

}



