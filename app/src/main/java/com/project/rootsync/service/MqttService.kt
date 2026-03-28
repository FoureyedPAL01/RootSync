package com.project.rootsync.service

import android.util.Log
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttService @Inject constructor() {

    private var client: MqttClient? = null
    private var isConnected = false

    fun connect(host: String, port: Int, username: String, password: String) {
        try {
            client = MqttClient(
                "ssl://$host:$port",
                MqttClient.generateClientId(),
                MemoryPersistence()
            )

            val options = MqttConnectOptions().apply {
                this.userName = username
                this.password = password.toCharArray()
                isCleanSession = true
                connectionTimeout = 30
                keepAliveInterval = 60
                isAutomaticReconnect = true
            }

            client?.setCallback(object : MqttCallback {
                override fun messageArrived(topic: String, message: MqttMessage) {
                    Log.d("MqttService", "Message arrived on $topic: ${message.toString()}")
                }

                override fun connectionLost(cause: Throwable?) {
                    Log.e("MqttService", "Connection lost: ${cause?.message}")
                    isConnected = false
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d("MqttService", "Delivery complete")
                }
            })

            client?.connect(options)
            isConnected = true
            Log.d("MqttService", "Connected to MQTT broker")
        } catch (e: Exception) {
            Log.e("MqttService", "Failed to connect: ${e.message}")
            isConnected = false
        }
    }

    fun publishPumpCommand(deviceId: String, on: Boolean) {
        try {
            val payload = if (on) "pump_on" else "pump_off"
            val message = MqttMessage(payload.toByteArray())
            message.qos = 1
            client?.publish("rootsync/$deviceId/pump", message)
            Log.d("MqttService", "Published pump command: $payload to rootsync/$deviceId/pump")
        } catch (e: Exception) {
            Log.e("MqttService", "Failed to publish: ${e.message}")
        }
    }

    fun subscribe(topic: String) {
        try {
            client?.subscribe(topic)
            Log.d("MqttService", "Subscribed to $topic")
        } catch (e: Exception) {
            Log.e("MqttService", "Failed to subscribe: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            client?.disconnect()
            isConnected = false
            Log.d("MqttService", "Disconnected from MQTT broker")
        } catch (e: Exception) {
            Log.e("MqttService", "Failed to disconnect: ${e.message}")
        }
    }

    fun isConnected(): Boolean = isConnected
}
