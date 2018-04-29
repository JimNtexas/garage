package com.grayraven.garage;

import android.annotation.SuppressLint;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;


public class MqttEventCallback implements MqttCallback {

    final String TAG = "MqttEventCallback";

    @Override
    public void connectionLost(Throwable arg0) {
        Log.d(TAG, "connection lost");

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken arg0) {
        Log.i(TAG, "deliveryComplete" );
    }

    @Override
    @SuppressLint("NewApi")
    public void messageArrived(String topic, final MqttMessage msg) throws Exception {
      //  Log.i(TAG, "Message arrived from topic" + topic);

        GarageMqttMessage garageMsg = new GarageMqttMessage(topic, msg.toString());
        EventBus.getDefault().post(garageMsg);

    }
}