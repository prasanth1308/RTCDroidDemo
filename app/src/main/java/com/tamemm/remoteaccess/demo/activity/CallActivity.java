package com.tamemm.remoteaccess.demo.activity;

import static org.slf4j.helpers.Util.report;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.tamemm.remoteaccess.demo.R;
import com.tamemm.remoteaccess.demo.signal.WebRTCSignalClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.ScreenCapturerAndroid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CallActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private static final int VIDEO_RESOLUTION_WIDTH = 1280;
    private static final int VIDEO_RESOLUTION_HEIGHT = 720;
    private static final int VIDEO_FPS = 30;

    private TextView mLogcatView;
    private Button mStartCallBtn;
    private Button mEndCallBtn;

    private static final String TAG = "CallActivity";

    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";

    private EglBase mRootEglBase;

    private PeerConnection mPeerConnection;
    private PeerConnectionFactory mPeerConnectionFactory;

    private SurfaceTextureHelper mSurfaceTextureHelper;

//    private SurfaceViewRenderer mLocalSurfaceView;
//    private SurfaceViewRenderer mRemoteSurfaceView;

    private VideoSource mVideoSource;

    private AudioTrack mAudioTrack;
    private VideoTrack mVideoTrack;

    private VideoCapturer mVideoCapturer;
    private MediaStream mLocalMediaStream;

    private ScreenCapturerAndroid mScreenCapturerAndroid;

    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;
    private static Intent mMediaProjectionPermissionResultData;
    private static int mMediaProjectionPermissionResultCode;

    public static String STREAM_NAME_PREFIX = "android_device_stream";

    public static int sDeviceWidth;
    public static int sDeviceHeight;
    public static final int SCREEN_RESOLUTION_SCALE = 2;

    String serverAddr;
    String roomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        sDeviceWidth = metrics.widthPixels;
        sDeviceHeight = metrics.heightPixels;

        mLogcatView = findViewById(R.id.LogcatView);
        mStartCallBtn = findViewById(R.id.StartCallButton);
        mEndCallBtn = findViewById(R.id.EndCallButton);

        // RTCSignalClient.getInstance().setSignalEventListener(mOnSignalEventListener);

        WebRTCSignalClient.getInstance().setSignalEventListener(mOnSignalEventListener);

        serverAddr = getIntent().getStringExtra("ServerAddr");
        roomName = getIntent().getStringExtra("RoomName");
        // RTCSignalClient.getInstance().joinRoom(serverAddr, UUID.randomUUID().toString(), roomName);

        WebRTCSignalClient.getInstance().joinRoom(serverAddr, UUID.randomUUID().toString(), roomName);

        mRootEglBase = EglBase.create();

//        mLocalSurfaceView = findViewById(R.id.LocalSurfaceView);
//        mRemoteSurfaceView = findViewById(R.id.RemoteSurfaceView);
//
//        mLocalSurfaceView.init(mRootEglBase.getEglBaseContext(), null);
//        mLocalSurfaceView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
//        mLocalSurfaceView.setMirror(true);
//        mLocalSurfaceView.setEnableHardwareScaler(false /* enabled */);
//
//        mRemoteSurfaceView.init(mRootEglBase.getEglBaseContext(), null);
//        mRemoteSurfaceView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
//        mRemoteSurfaceView.setMirror(true);
//        mRemoteSurfaceView.setEnableHardwareScaler(true /* enabled */);
//        mRemoteSurfaceView.setZOrderMediaOverlay(true);
//
//        ProxyVideoSink videoSink = new ProxyVideoSink();
//        videoSink.setTarget(mLocalSurfaceView);

        mPeerConnectionFactory = createPeerConnectionFactory(this);

        // NOTE: this _must_ happen while PeerConnectionFactory is alive!
        Logging.enableLogToDebugOutput(Logging.Severity.LS_VERBOSE);

//        mVideoCapturer = createVideoCapturer();
//
//        mSurfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", mRootEglBase.getEglBaseContext());
//        VideoSource videoSource = mPeerConnectionFactory.createVideoSource(true);
//        mVideoCapturer.initialize(mSurfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
//
//        mVideoTrack = mPeerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
//        mVideoTrack.setEnabled(true);
//        mVideoTrack.addSink(videoSink);
//
//        AudioSource audioSource = mPeerConnectionFactory.createAudioSource(new MediaConstraints());
//        mAudioTrack = mPeerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
//        mAudioTrack.setEnabled(false);

        mPeerConnectionFactory.setVideoHwAccelerationOptions(mRootEglBase.getEglBaseContext(), mRootEglBase.getEglBaseContext());
        startScreenCapture();
    }

    public void initPeerConnection() {
        // create notification
//        Pair<Integer, Notification> notification = NotificationUtils.getNotification(this);
//        startForeground(notification.first, notification.second);
//        mVideoCapturer = createScreenCapturer();
    }

    @TargetApi(21)
    private void startScreenCapture() {
        Log.i(TAG, "startScreenCapture");
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getApplication().getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
    }

    @TargetApi(21)
    private VideoCapturer createScreenCapturer() {
        Log.i(TAG, "createScreenCapturer");
        if (mMediaProjectionPermissionResultCode != Activity.RESULT_OK) {
            report("User didn't give permission to capture the screen.");
            return null;
        }
        return new ScreenCapturerAndroid(
                mMediaProjectionPermissionResultData, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                report("User revoked permission to capture the screen.");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE)
            return;

        mMediaProjectionPermissionResultCode = resultCode;
        mMediaProjectionPermissionResultData = data;
        // init();
        // initPeerConnection();\
        Log.i(TAG, "onActivityResult" + requestCode);

        Log.i(TAG, "onActivityResult" + resultCode);

        Log.i(TAG, "onActivityResult" + data);

        if (resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "startService");
            startService(ScreenCaptureService.getStartIntent(getApplicationContext(), resultCode, data));
        }

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        mVideoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, VIDEO_FPS);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        try {
//            mVideoCapturer.stopCapture();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doEndCall();
//        mLocalSurfaceView.release();
//        mRemoteSurfaceView.release();
        mVideoCapturer.dispose();
        mSurfaceTextureHelper.dispose();
        PeerConnectionFactory.stopInternalTracingCapture();
        PeerConnectionFactory.shutdownInternalTracer();
//        RTCSignalClient.getInstance().leaveRoom();
        WebRTCSignalClient.getInstance().leaveRoom();
    }

    public static class ProxyVideoSink implements VideoSink {
        private VideoSink mTarget;
        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (mTarget == null) {
                Log.d(TAG, "Dropping frame in proxy because target is null.");
                return;
            }
            mTarget.onFrame(frame);
        }
        synchronized void setTarget(VideoSink target) {
            this.mTarget = target;
        }
    }

    public static class SimpleSdpObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Log.i(TAG, "SdpObserver: onCreateSuccess !");
        }

        @Override
        public void onSetSuccess() {
            Log.i(TAG, "SdpObserver: onSetSuccess");
        }

        @Override
        public void onCreateFailure(String msg) {
            Log.e(TAG, "SdpObserver onCreateFailure: " + msg);
        }

        @Override
        public void onSetFailure(String msg) {
            Log.e(TAG, "SdpObserver onSetFailure: " + msg);
        }
    }

    public void onClickStartCallButton(View v) {
        doStartCall();
    }

    public void onClickEndCallButton(View v) {
        doEndCall();
    }

    private void updateCallState(boolean idle) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (idle) {
                    mStartCallBtn.setVisibility(View.VISIBLE);
                    mEndCallBtn.setVisibility(View.GONE);
                    // mRemoteSurfaceView.setVisibility(View.GONE);
                } else {
                    mStartCallBtn.setVisibility(View.GONE);
                    mEndCallBtn.setVisibility(View.VISIBLE);
                    // mRemoteSurfaceView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void doStartCall() {
        logcatOnUI("Start Call, Wait ...");
        // mVideoCapturer = createScreenCapturer();
        mVideoCapturer = createScreenCapturer();
        mLocalMediaStream = mPeerConnectionFactory.createLocalMediaStream("ARDAMS");
        MediaConstraints mediaConstraints = new MediaConstraints();
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        mediaConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));

        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer.toString(sDeviceHeight / SCREEN_RESOLUTION_SCALE)));
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer.toString(sDeviceWidth / SCREEN_RESOLUTION_SCALE)));
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(45)));
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(30)));

        mSurfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", mRootEglBase.getEglBaseContext());
        Log.i(TAG, "mVideoCapturer " + mVideoCapturer);
        mVideoSource = mPeerConnectionFactory.createVideoSource(mVideoCapturer);
        mVideoCapturer.isScreencast();
        mVideoCapturer.startCapture(sDeviceWidth, sDeviceHeight, 45);
        VideoTrack localVideoTrack = mPeerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, mVideoSource);
        localVideoTrack.setEnabled(true);
        mLocalMediaStream.addTrack(mPeerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, mVideoSource));
        AudioSource audioSource = mPeerConnectionFactory.createAudioSource(new MediaConstraints());
        mLocalMediaStream.addTrack(mPeerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource));

        mVideoTrack = mPeerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, mVideoSource);


        if (mPeerConnection == null) {
            mPeerConnection = createPeerConnection();
        }

        mPeerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.i(TAG, "Create local offer success: \n" + sessionDescription.description);
                mPeerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);

                JSONObject message = new JSONObject();
                JSONObject data = new JSONObject();
                JSONObject offer = new JSONObject();

                try {
                    offer.put("type", "offer");
                    offer.put("sdp", sessionDescription.description);

                    data.put("deviceId", roomName);
                    data.put("offer", offer);

                    message.put("type", "OFFER");
                    message.put("data", data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                WebRTCSignalClient.getInstance().sendMessage(message);
            }
        }, mediaConstraints);
    }

    public void doEndCall() {
        logcatOnUI("End Call, Wait ...");
        hanup();
        JSONObject message = new JSONObject();
        try {
            message.put("userId", WebRTCSignalClient.getInstance().getUserId());
            message.put("msgType", WebRTCSignalClient.MESSAGE_TYPE_HANGUP);
            WebRTCSignalClient.getInstance().sendMessage(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void doAnswerCall() {
        logcatOnUI("Answer Call, Wait ...");
        if (mPeerConnection == null) {
            mPeerConnection = createPeerConnection();
        }
        MediaConstraints sdpMediaConstraints = new MediaConstraints();
        Log.i(TAG, "Create answer ...");
        mPeerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.i(TAG, "Create answer success !");
                mPeerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                JSONObject message = new JSONObject();
                try {
//                    message.put("userId", RTCSignalClient.getInstance().getUserId());
//                    message.put("msgType", RTCSignalClient.MESSAGE_TYPE_ANSWER);
//                    message.put("sdp", sessionDescription.description);
//                    RTCSignalClient.getInstance().sendMessage(message);
                    message.put("userId", WebRTCSignalClient.getInstance().getUserId());
                    message.put("msgType", WebRTCSignalClient.MESSAGE_TYPE_ANSWER);
                    message.put("sdp", sessionDescription.description);
                    WebRTCSignalClient.getInstance().sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, sdpMediaConstraints);
        //updateCallState(false);
    }

    private void hanup() {
        logcatOnUI("Hanup Call, Wait ...");
        startService(com.tamemm.remoteaccess.demo.activity.ScreenCaptureService.getStopIntent(this));
        if (mPeerConnection == null) {
            return;
        }
        mPeerConnection.close();
        mPeerConnection = null;
        logcatOnUI("Hanup Done.");
        //updateCallState(true);
    }

    public PeerConnection createPeerConnection() {
        Log.i(TAG, "Create PeerConnection ...");
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:relay.metered.ca:80").createIceServer());

        iceServers.add(PeerConnection.IceServer.builder("turn:relay.metered.ca:80").setUsername("bf4d3e74b01669d87f18b11c").setPassword("NNZiqapqcRttpZKx").createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("turn:relay.metered.ca:443").setUsername("bf4d3e74b01669d87f18b11c").setPassword("NNZiqapqcRttpZKx").createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("turn:relay.metered.ca:443?transport=tcp").setUsername("bf4d3e74b01669d87f18b11c").setPassword("NNZiqapqcRttpZKx").createIceServer());


        PeerConnection.RTCConfiguration configuration = new PeerConnection.RTCConfiguration(iceServers);
        PeerConnection connection = mPeerConnectionFactory.createPeerConnection(configuration, mPeerConnectionObserver);
        if (connection == null) {
            Log.e(TAG, "Failed to createPeerConnection !");
            return null;
        }
//         connection.addTrack(mVideoTrack);
//         connection.addTrack(mAudioTrack);
        Log.i(TAG, "connection.addStream(mLocalMediaStream);" + connection);
        connection.addStream(mLocalMediaStream);
        // connection.addTrack(mVideoTrack);
        return connection;
    }

    public PeerConnectionFactory createPeerConnectionFactory(Context context) {
        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;

        encoderFactory = new DefaultVideoEncoderFactory(
                mRootEglBase.getEglBaseContext(), true /* enableIntelVp8Encoder */, true);
        decoderFactory = new DefaultVideoDecoderFactory(mRootEglBase.getEglBaseContext());

        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions());

        PeerConnectionFactory.Builder builder = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory);
        builder.setOptions(null);

        return builder.createPeerConnectionFactory();
    }

    /*
     * Read more about Camera2 here
     * https://developer.android.com/reference/android/hardware/camera2/package-summary.html
     **/
    private VideoCapturer createVideoCapturer() {
        if (Camera2Enumerator.isSupported(this)) {
            return createCameraCapturer(new Camera2Enumerator(this));
        } else {
            return createCameraCapturer(new Camera1Enumerator(true));
        }
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Log.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Log.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }

    private PeerConnection.Observer mPeerConnectionObserver = new PeerConnection.Observer() {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.i(TAG, "onSignalingChange: " + signalingState);
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Log.i(TAG, "onIceConnectionChange: " + iceConnectionState);
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            Log.i(TAG, "onIceConnectionChange: " + b);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            Log.i(TAG, "onIceGatheringChange: " + iceGatheringState);
        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            Log.i(TAG, "onIceCandidate: " + iceCandidate);

            try {

                JSONObject message = new JSONObject();
                JSONObject data = new JSONObject();
                JSONObject candidate = new JSONObject();

                candidate.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                candidate.put("sdpMid", iceCandidate.sdpMid);
                candidate.put("candidate", iceCandidate.sdp);
                data.put("deviceId", roomName);
                data.put("candidate", candidate);
                message.put("data", data);
                message.put("type", WebRTCSignalClient.MESSAGE_TYPE_CANDIDATE);

                WebRTCSignalClient.getInstance().sendMessage(message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
            for (int i = 0; i < iceCandidates.length; i++) {
                Log.i(TAG, "onIceCandidatesRemoved: " + iceCandidates[i]);
            }
            mPeerConnection.removeIceCandidates(iceCandidates);
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            Log.i(TAG, "onAddStream: " + mediaStream.videoTracks.size());
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            Log.i(TAG, "onRemoveStream");
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            Log.i(TAG, "onDataChannel");
        }

        @Override
        public void onRenegotiationNeeded() {
            Log.i(TAG, "onRenegotiationNeeded");
        }

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
            MediaStreamTrack track = rtpReceiver.track();
            if (track instanceof VideoTrack) {
                Log.i(TAG, "onAddVideoTrack");
                VideoTrack remoteVideoTrack = (VideoTrack) track;
                remoteVideoTrack.setEnabled(true);
                ProxyVideoSink videoSink = new ProxyVideoSink();
                //videoSink.setTarget(mRemoteSurfaceView);
                remoteVideoTrack.addSink(videoSink);
            }
        }
    };

    private void logcatOnUI(String msg) {
        Log.i(TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String output = mLogcatView.getText() + "\n" + msg;
                mLogcatView.setText(output);
            }
        });
    }

    private WebRTCSignalClient.OnSignalEventListener mOnSignalEventListener = new WebRTCSignalClient.OnSignalEventListener() {
        @Override
        public void onConnected() {
            logcatOnUI("Signal Server Connected !");
        }

        @Override
        public void onConnecting() {
            logcatOnUI("Signal Server Connecting !");
        }

        @Override
        public void onDisconnected() {
            logcatOnUI("Signal Server Connecting !");
        }

        @Override
        public void onRemoteUserJoined(String userId) {
            logcatOnUI("Remote User Joined: " + userId);
        }

        @Override
        public void onRemoteUserLeft(String userId) {
            logcatOnUI("Remote User Leaved: " + userId);
        }

        @Override
        public void onBroadcastReceived(String message) {
            Log.i(TAG, "onBroadcastReceived: " + message);
            try {
                JSONObject jsonObject = new JSONObject(message);
                // String userId = jsonObject.getString("userId");
                String type = jsonObject.getString("type");
                switch (type) {
                    case "INIT_SUCCESS":
                        JSONObject requestMessage = new JSONObject();
                        JSONObject data = new JSONObject();

                        try {
                            data.put("deviceId", roomName);

                            requestMessage.put("type", "JOIN_CHANNEL");
                            requestMessage.put("data", data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        WebRTCSignalClient.getInstance().sendMessage(requestMessage);
                        break;
                    case WebRTCSignalClient.MESSAGE_TYPE_OFFER:
                        onRemoteOfferReceived(roomName, jsonObject);
                        break;
                    case WebRTCSignalClient.MESSAGE_TYPE_ANSWER:
                        onRemoteAnswerReceived(roomName, jsonObject);
                        break;
                    case WebRTCSignalClient.MESSAGE_TYPE_CANDIDATE:
                        onRemoteCandidateReceived(roomName, jsonObject);
                        break;
                    case WebRTCSignalClient.MESSAGE_TYPE_HANGUP:
                        onRemoteHangup(roomName);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void onRemoteOfferReceived(String userId, JSONObject message) {
            logcatOnUI("Receive Remote Call ...");
            if (mPeerConnection == null) {
                mPeerConnection = createPeerConnection();
            }
            try {
                String description = message.getString("sdp");
                mPeerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(SessionDescription.Type.OFFER, description));
                doAnswerCall();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void onRemoteAnswerReceived(String userId, JSONObject message) {
            logcatOnUI("Receive Remote Answer ...");
            try {
                JSONObject dataJSONObject = message.getJSONObject("data");
                JSONObject answerJSONObject = dataJSONObject.getJSONObject("answer");
                String description = answerJSONObject.getString("sdp");
                mPeerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(SessionDescription.Type.ANSWER, description));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //updateCallState(false);
        }

        private void onRemoteCandidateReceived(String userId, JSONObject message) {
            logcatOnUI("Receive Remote Candidate ...");
            try {
                JSONObject dataJSONObject = message.getJSONObject("data");
                JSONObject candidateJSONObject = dataJSONObject.getJSONObject("candidate");
                IceCandidate remoteIceCandidate = new IceCandidate(candidateJSONObject.getString("sdpMid"), candidateJSONObject.getInt("sdpMLineIndex"), candidateJSONObject.getString("candidate"));
                mPeerConnection.addIceCandidate(remoteIceCandidate);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void onRemoteHangup(String userId) {
            logcatOnUI("Receive Remote Hanup Event ...");
            hanup();
        }
    };
}
