package com.example.marcy.neconect;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;


public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        MessageApi.MessageListener, RobotChangedStateListener{
    private static final String TAG = MainActivity.class.getName();

    private GoogleApiClient mGoogleApiClient;
    TextView xTextView;
    TextView yTextView;
    TextView zTextView;
    LineChart mChart;
    int x,y,z;

    String[] names = new String[]{"x-value", "y-value", "z-value"};
    int[] colors = new int[]{Color.RED, Color.GREEN, Color.BLUE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xTextView = (TextView)findViewById(R.id.xValue);
        yTextView = (TextView)findViewById(R.id.yValue);
        zTextView = (TextView)findViewById(R.id.zValue);
        ActionBar ab = getActionBar();
        //ab.hide();

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
        mChart = (LineChart) findViewById(R.id.lineChart);

//        mChart.setDescription(null); // 表のタイトルを空にする
//        mChart.setData(new LineData()); // 空のLineData型インスタンスを追加

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
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

        xTextView.setText(String.valueOf(value[0]));
        yTextView.setText(String.valueOf(value[1]));
        zTextView.setText(String.valueOf(value[2]));

//        x =  Integer.parseInt(value[0]);
//        y =  Integer.parseInt(value[1]);
//        z =  Integer.parseInt(value[2]);


/*        LineData data = mChart.getLineData();
        if (data != null) {
            for (int i = 0; i < 3; i++) {
                ILineDataSet set = data.getDataSetByIndex(i);
                if (set == null) {
                    set = createSet(names[i], colors[i]);
                    data.addDataSet(set);
                }

                data.addEntry(new Entry(set.getEntryCount(),Float.parseFloat(value[i])), i);
                data.notifyDataChanged();
            }

            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(50);
            mChart.moveViewToX(data.getEntryCount());
        }*/

    }
    /*
    private LineDataSet createSet(String label, int color) {

        LineDataSet set = new LineDataSet(null, label);
        set.setLineWidth(2.5f);
        set.setColor(color);
        set.setDrawCircles(false);
        set.setDrawValues(false);

        return set;

    }*/
}
