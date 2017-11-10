package com.example.marcy.neconect;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.widget.Button;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        MessageApi.MessageListener, RobotChangedStateListener {
    private static final String TAG = MainActivity.class.getName();

    private double velocity;
    private double magnetic;

    private float[] fAccell = new float[3];
    private float[] fMagnetic = new float[3];

    private ArrayList<Double> acc_x = new ArrayList<Double>();
    private ArrayList<Double> acc_y = new ArrayList<Double>();
    private ArrayList<Double> acc_z = new ArrayList<Double>();
    private double acx, acy, acz;
    private double lacx = 0, lacy = 0, lacz = 0;
    private double x, y, z;
    int arraycount = 0;

    int count = 0;

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 42;
    private ConvenienceRobot mRobot;
    private GoogleApiClient mGoogleApiClient;

    TextView xTextView;
    TextView yTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DualStackDiscoveryAgent.getInstance().addRobotStateListener(this);

        xTextView = (TextView) findViewById(R.id.xValue);
        yTextView = (TextView) findViewById(R.id.yValue);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed:" + connectionResult.toString());
                    }
                })
                .addApi(Wearable.API)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                Log.e("Sphero", "Location permission has not already been granted");
                List<String> permissions = new ArrayList<String>();
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CODE_LOCATION_PERMISSION);
            } else {
                Log.d("Sphero", "Location permission already granted");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION_PERMISSION: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        startDiscovery();
                        Log.d("Permissions", "Permission Granted: " + permissions[i]);
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d("Permissions", "Permission Denied: " + permissions[i]);
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startDiscovery();
        }
    }

    private void startDiscovery() {
        //If the DiscoveryAgent is not already looking for robots, start discovery.
        if (!DualStackDiscoveryAgent.getInstance().isDiscovering()) {
            try {
                DualStackDiscoveryAgent.getInstance().startDiscovery(this);
            } catch (DiscoveryException e) {
                Log.e("Sphero", "DiscoveryException: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onStop() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        //If the DiscoveryAgent is in discovery mode, stop it.
        if (DualStackDiscoveryAgent.getInstance().isDiscovering()) {
            DualStackDiscoveryAgent.getInstance().stopDiscovery();
        }

        //If a robot is connected to the device, disconnect it
        if (mRobot != null) {
            mRobot.disconnect();
            mRobot = null;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DualStackDiscoveryAgent.getInstance().addRobotStateListener(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //if (id == R.id.action_settings) {
        //    return true;
        //}
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnecteds");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        // xTextView.setText(messageEvent.getPath());
        String msg = messageEvent.getPath();
        String[] value = msg.split(",", 0);

        fAccell[0] = Float.parseFloat(value[0]);
        fAccell[1] = Float.parseFloat(value[1]);
        fAccell[2] = Float.parseFloat(value[2]);
        fMagnetic[0] = Float.parseFloat(value[3]);
        fMagnetic[1] = Float.parseFloat(value[4]);
        fMagnetic[2] = Float.parseFloat(value[5]);

        Log.v("accx", String.valueOf(fAccell[0]));
        Log.v("accy", String.valueOf(fAccell[1]));
        Log.v("accz", String.valueOf(fAccell[2]));


        acc_x.add((double) fAccell[0]);
        acc_y.add((double) fAccell[1]);
        acc_z.add((double) fAccell[2]);
        arraycount++;

        if (arraycount == 10) {
            for (int i = 0; i < 10; i++) {
                acx += acc_x.get(i);
                acy += acc_y.get(i);
                acz += acc_z.get(i);
            }
            acx /= 10;
            acy /= 10;
            acz /= 10;
            arraycount = 0;

            x = lacx - acx;
            y = lacy - acy;
            z = lacz - acz;


            velocity = Math.sqrt(x * x + y * y + z * z);

            acc_x.clear();
            acc_y.clear();
            acc_z.clear();

            lacx = acx;
            lacy = acy;
            lacz = acz;

            acx = 0;
            acy = 0;
            acz = 0;
        }

        // fAccell と fMagnetic から傾きと方位角を計算する
        if (fAccell != null && fMagnetic != null) {
            // 回転行列を得る
            float[] inR = new float[9];
            SensorManager.getRotationMatrix(
                    inR,
                    null,
                    fAccell,
                    fMagnetic);
            // ワールド座標とデバイス座標のマッピングを変換する
            float[] outR = new float[9];
            SensorManager.remapCoordinateSystem(
                    inR,
                    SensorManager.AXIS_X, SensorManager.AXIS_Y,
                    outR);
            // 姿勢を得る
            float[] fAttitude = new float[3];
            SensorManager.getOrientation(
                    outR,
                    fAttitude);

            if (fAttitude[0] < 0) {
                magnetic = (double) rad2deg(fAttitude[0]) + 360;
            } else {
                magnetic = (double) rad2deg(fAttitude[0]);
            }

            xTextView.setText(String.valueOf(magnetic));
            xTextView.setText(String.valueOf(velocity));

//                Log.v("velocity", String.valueOf(velocity));
//                Log.v("magnetic", String.valueOf(magnetic));

            if (mRobot != null) {
                if (velocity > 1) {
                    mRobot.drive((float) magnetic, (float) 0.5f);
                } else {
                    mRobot.drive((float) magnetic, (float) velocity);
                }
            }
        }
    }

    private float rad2deg(float rad) {
        return rad * (float) 180.0 / (float) Math.PI;
    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType type) {
        switch (type) {
            case Online: {
                //Save the robot as a ConvenienceRobot for additional utility methods
                Log.v("online", "online");
                mRobot = new ConvenienceRobot(robot);
                break;
            }
        }
    }
}
