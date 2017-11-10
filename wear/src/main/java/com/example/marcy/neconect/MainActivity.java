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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity implements SensorEventListener {
    private final String TAG = MainActivity.class.getName();
    private final float GAIN = 0.9f;

    private TextView mTextView;
    private SensorManager mSensorManager;
    private GoogleApiClient mGoogleApiClient;
    private String mNode;
    private double x,y,z;

    //////////////////////////////////////////////////////////////////
    private double v=0;
    private ArrayList<Double> acc_x = new ArrayList<Double>();
    private ArrayList<Double> acc_y = new ArrayList<Double>();
    private ArrayList<Double> acc_z = new ArrayList<Double>();
    private double acx,acy,acz;
    private double lacx=0,lacy=0,lacz=0;
    int arraycount=0;
    double angle=0;

//////////////////////////////////////////////////////////////////////


    int count = 0;
    //final DateFormat df = new SimpleDateFormat("HH:mm:ssSSS");
    private Date date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mTextView = (TextView) findViewById(R.id.text);
        mTextView.setTextSize(30.0f);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

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

        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
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
        if(count>= 2) {
            count = 0;
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                //x = (x * GAIN + event.values[0] * (1 - GAIN));
                //y = (y * GAIN + event.values[1] * (1 - GAIN));
                //z = (z * GAIN + event.values[2] * (1 - GAIN));

                //////////////////////////////////////////////////////////////////////////角度，速度

                acc_x.add((double)event.values[0]);
                acc_y.add((double)event.values[1]);
                acc_z.add((double)event.values[2]);

                arraycount++;

                if(arraycount==10){
                    for(int i=0;i<10;i++){
                        acx+=acc_x.get(i);
                        acy+=acc_y.get(i);
                        acz+=acc_z.get(i);
                    }
                    acx/=10;
                    acy/=10;
                    acz/=10;
                    arraycount=0;

                    x=lacx-acx;
                    y=lacy-acy;
                    z=lacz-acz;

                    v=Math.sqrt(x*x+y*y+z*z);

                    angle=Math.atan2(acy,acx)*57.2958 + 180;
                    if (mTextView != null)

                        mTextView.setText(String.format("%f度 \n V : %f",angle,v));

                    acc_x.clear();
                    acc_y.clear();
                    acc_z.clear();

                    lacx=acx;
                    lacy=acy;
                    lacz=acz;

//                    v=0;

                    acx=0;
                    acy=0;
                    acz=0;
                }

/////////////////////////////////////////////////////////////////////////

                int intAngle = (int)angle;
                //転送セット
                final String SEND_DATA = intAngle + "," + v ;
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
            }
        }else count++;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}