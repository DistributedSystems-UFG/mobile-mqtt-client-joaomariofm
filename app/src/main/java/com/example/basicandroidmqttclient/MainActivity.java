package com.example.basicandroidmqttclient;

import androidx.appcompat.app.AppCompatActivity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import android.widget.EditText;
import android.widget.ListView;


import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final String brokerURI = "23.21.17.173";

    Activity thisActivity;

    ListView simpleList;
    List<String> messagesList = new ArrayList<>();
    ArrayAdapter <String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        thisActivity = this;

        simpleList = (ListView) findViewById(R.id.simpleListView);
        arrayAdapter = new ArrayAdapter <String>(this, R.layout.list_item, R.id.textView, messagesList);
        simpleList.setAdapter(arrayAdapter);

        this.topicSubscription();
    }

    public void publishMaxValues(View view) {
        String maxTemperatureTopic = "/set-max-temperature";
        String maxLuminosityTopic = "/set-max-luminosity";

        EditText maxTemp = (EditText) findViewById(R.id.editMaxTemp);
        EditText maxLum = (EditText) findViewById(R.id.editMaxLum);

        Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(brokerURI)
                .buildBlocking();

        client.connect();

        client.publishWith()
                .topic(maxTemperatureTopic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(maxTemp.getText().toString().getBytes())
                .send();

        client.publishWith()
                .topic(maxLuminosityTopic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(maxLum.getText().toString().getBytes())
                .send();

        client.disconnect();
    }

    public void topicSubscription() {
        String temperatureTopic = "/temperature-android";
        String luminosityTopic = "/luminosity-android";

        Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(brokerURI)
                .buildBlocking();

        client.connect();

        client.toAsync().subscribeWith()
                .topicFilter(temperatureTopic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(msg -> {
                    thisActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            arrayAdapter.insert(new String(msg.getPayloadAsBytes(), StandardCharsets.UTF_8), arrayAdapter.getCount());
                            arrayAdapter.notifyDataSetChanged();
                        }
                    });
                })
                .send();

        client.toAsync().subscribeWith()
                .topicFilter(luminosityTopic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(msg -> {
                    thisActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            String message = new String(msg.getPayloadAsBytes(), StandardCharsets.UTF_8);

                            arrayAdapter.insert(message, arrayAdapter.getCount());
                            arrayAdapter.notifyDataSetChanged();
                        }
                    });
                })
                .send();
    }
}