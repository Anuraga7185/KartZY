package com.ordering.blinkit_clone.service;

import android.util.Log;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.json.JSONObject;

import java.net.URISyntaxException;
public class SocketService {
    private static SocketService instance;
    private Socket mSocket;

    private SocketService() {
        try {
            mSocket = IO.socket("http://192.168.1.36.com"); // Replace with your server URL
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SocketService getInstance() {
        if (instance == null) {
            instance = new SocketService();
        }
        return instance;
    }

    public void connect() {
        mSocket.connect();
    }

    public void disconnect() {
        mSocket.disconnect();
    }

    public void sendMessage(String event, JSONObject message) {
        mSocket.emit(event, message);
    }

    public void listenForEvent(String event, Emitter.Listener listener) {
        mSocket.on(event, listener);
    }
}