package com.example.marcy.neconect;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import android.content.*;


public class MainActivity extends Activity implements SensorEventListener {
    private final String TAG = MainActivity.class.getName();
    private final float GAIN = 0.9f;

    private TextView mTextView;
    private SensorManager mSensorManager;
    private GoogleApiClient mGoogleApiClient;
    private boolean RegisteredSensor = false;

    private String mNode;

    private double accx, accy, accz;
    private double magx, magy, magz;
    int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mTextView = (TextView) findViewById(R.id.text);
        mTextView.setTextSize(30.0f);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d(TAG, "onConnected");

//                        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                            @Override
                            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                                //Nodeは１個に限定
                                if (nodes.getNodes().size() > 0) {
                                    mNode = nodes.getNodes().get(0).getId();
                                }
                            }
                        });
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "onConnectionSuspended");

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed : " + connectionResult.toString());
                    }
                })
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(
                this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(
                this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
        mGoogleApiClient.connect();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // センサの取得値をそれぞれ保存しておく
        count++;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accx = event.values[0];
            accy = event.values[1];
            accz = event.values[2];
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magx = event.values[0];
            magy = event.values[1];
            magz = event.values[2];
        }


        if(count == 100) {
            mTextView.setText(String.format("%3f\n%3f\n%3f\n%d", accx, accy, accz, count));
            //転送セット
            final String SEND_DATA = accx + "," + accy + "," + accz + "," + magx + "," + magy + "," + magz;
            if (mNode != null) {
                Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode, SEND_DATA, null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.d(TAG, "ERROR : failed to send Message" + result.getStatus());
                        }
                    }
                });
            }
            count = 0;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}