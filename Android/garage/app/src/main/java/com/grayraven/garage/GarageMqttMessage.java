package com.grayraven.garage;
// used by EventBu

public class GarageMqttMessage {
    public final String topic;
    public final String msg;

    public GarageMqttMessage(String topic, String msg) {
        this.topic = topic;
        this.msg = msg;
    }
}
