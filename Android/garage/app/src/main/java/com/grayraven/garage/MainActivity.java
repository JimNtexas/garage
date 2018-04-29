package com.grayraven.garage;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MainActivity extends AppCompatActivity {

    final String TAG = "Garage_Main";
    private static Context mContext = GarageApp.getAppContext();
    private TextView main_text;
    View main_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        setContentView(R.layout.activity_main);
        main_text = (TextView)findViewById(R.id.main_text);
        main_text.setText("waiting for message");
        main_layout = (View)findViewById(R.id.main_layout);
        main_layout.setBackgroundColor(getResources().getColor(R.color.yellow));
    }

    final String broker = "tcp://10.211.1.127";
    String password = "monkey123";
    String username = "pi";
    final String topic = "door_distance";



    MqttClient client;
    IMqttToken token;





    String clientId = MqttAsyncClient.generateClientId();
    {
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setPassword(password.toCharArray());
        connectOptions.setUserName(username);
        try {


            MemoryPersistence persistence = new MemoryPersistence();
            client = new MqttClient(broker, clientId, persistence);
            client.setCallback(new MqttEventCallback());
            client.setTimeToWait(3000);
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
                    Log.d(TAG, "c" + e.getMessage());
                    e.printStackTrace();
                    break;
                case MqttException.REASON_CODE_FAILED_AUTHENTICATION:
                   Log.d(TAG, "REASON_CODE_FAILED_AUTHENTICATION");
                    Log.d(TAG, "b" + e.getMessage());
                    break;
                default:
                    Log.d(TAG, "default exception cause: " + e.getReasonCode() + " cause: " + e.getCause());
                    Log.d(TAG, "a" + e.getMessage());
            }
        }
    }




}



