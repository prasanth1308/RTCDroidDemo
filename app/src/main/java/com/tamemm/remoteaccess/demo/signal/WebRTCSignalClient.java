package com.tamemm.remoteaccess.demo.signal;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;


public class WebRTCSignalClient {
    private static final String TAG = "WebRTCSignalClient";

    public static final String MESSAGE_TYPE_OFFER = "OFFER";
    public static final String MESSAGE_TYPE_ANSWER = "ANSWER";
    public static final String MESSAGE_TYPE_CANDIDATE = "ICECANDIDATE";
    public static final String MESSAGE_TYPE_HANGUP = "LEAVE_CHANNEL";

    private static WebRTCSignalClient mInstance;
    private WebRTCSignalClient.OnSignalEventListener mOnSignalEventListener;

    private WebSocketClient mWebSocketClient;
    private String mUserId;
    private String mRoomName;

    public interface OnSignalEventListener {
        void onConnected();
        void onConnecting();
        void onDisconnected();
        void onRemoteUserJoined(String userId);
        void onRemoteUserLeft(String userId);
        void onBroadcastReceived(String message);
    }

    public static WebRTCSignalClient getInstance() {
        synchronized (WebRTCSignalClient.class) {
            if (mInstance == null) {
                mInstance = new WebRTCSignalClient();
            }
        }
        return mInstance;
    }

    public void setSignalEventListener(final OnSignalEventListener listener) {
        mOnSignalEventListener = listener;
    }

    public String getUserId() {
        return mUserId;
    }

    public void joinRoom(String url, String userId, String roomName) {
        Log.i(TAG, "joinRoom: " + url + ", " + userId + ", " + roomName);
        mUserId = userId;
        mRoomName = roomName;
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        mWebSocketClient = new WebSocketClient(uri, new Draft_6455()) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i(TAG, "Websocket Opened " + userId);
                mOnSignalEventListener.onConnected();

                try {
                    JSONObject args = new JSONObject();
                    args.put("type", "INIT");
                    args.put("data", new Object());
                    sendMessage(args);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onMessage(String s) {
                final String message = s;
                Log.i(TAG, "onMessage received" + message);
                mOnSignalEventListener.onBroadcastReceived(message);
            }
            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i(TAG, "Websocket Closed " + s);
            }
            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Websocket Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    public void leaveRoom() {
        Log.i(TAG, "leaveRoom: " + mRoomName);
        if (mWebSocketClient == null) {
            return;
        }
        try {
            JSONObject args = new JSONObject();
            args.put("userId", mUserId);
            args.put("roomName", mRoomName);

            mWebSocketClient.close();
            mWebSocketClient = null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(JSONObject message) {
        Log.i(TAG, "sendMessage: " + message);
        if (mWebSocketClient == null) {
            return;
        }
        mWebSocketClient.send(String.valueOf(message));
    }

}
