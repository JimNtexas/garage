package com.grayraven.garage;

import android.annotation.SuppressLint;
import android.os.CountDownTimer;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;

public class MqttEventCallback implements MqttCallback {

    final String TAG = "Garage_MqttEventCb";
    private CountDownTimer timer = new CountDownTimer(3000, 1000) {

        public void onTick(long millisUntilFinished) {
           Log.d(TAG, "seconds remaining: " + millisUntilFinished / 1000);
        }

        public void onFinish() {
           Log.d(TAG, "countdown timeout!");
            GarageMqttMessage garageMsg = new GarageMqttMessage("door_distance", "-200");  //todo: define -200 with a symbol
            EventBus.getDefault().post(garageMsg);
        }
    };


    @Override
    public void connectionLost(Throwable arg0) {
        Log.d(TAG, "connection lost");
        GarageMqttMessage garageMsg = new GarageMqttMessage("door_distance", "-200");  //todo: define -200 with a symbol
        EventBus.getDefault().post(garageMsg);

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
        timer.cancel();
        timer.start();

    }
}