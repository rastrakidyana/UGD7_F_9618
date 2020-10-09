package com.maderastra.ugd7_f_9618;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private long lastUpdate=0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD=600;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mProximity;
    private Camera mCamera = null;
    private CameraView mCameraView = null;
    TextView tvShake;
    ImageButton imageClose;
    private int CAMERA_PERMISSION_CODE = 1;
    int cameraId = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        tvShake = (TextView) findViewById(R.id.shake_tv);
        imageClose = (ImageButton) findViewById(R.id.imgClose);
        imageClose.setVisibility(View.GONE);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        Sensor mySensor2 = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[0];
            float z = sensorEvent.values[0];

            long curTime = System.currentTimeMillis();

            if ((curTime-lastUpdate)>100){
                long diffTime = (curTime-lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x+y+z-last_x-last_y-last_z)/diffTime*10000;

                if (speed>SHAKE_THRESHOLD){

                    tvShake.setVisibility(View.GONE);
                    imageClose.setVisibility(View.VISIBLE);

                    try {
                        mCamera = Camera.open(cameraId);
                    } catch (Exception e) {
                        Log.d("Error", "Failed to get Camera" + e.getMessage());
                    }
                    if (mCamera != null){
                        mCameraView = new CameraView(this, mCamera);
                        FrameLayout camera_view = (FrameLayout) findViewById(R.id.FLCamera);
                        camera_view.addView(mCameraView);
                    }

                    Log.d("key","asd");
                }
                last_x=x;
                last_y=y;
                last_z=z;
            }
        }

        if (mCamera != null){

            imageClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.exit(0);
                }
            });
            FrameLayout camera_view = (FrameLayout) findViewById(R.id.FLCamera);
            if (mySensor2.getType() == Sensor.TYPE_PROXIMITY){
                if (sensorEvent.values[0]==0) {
                    mCamera.stopPreview();
                    mCamera.release();
                    cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                    mCamera = Camera.open(cameraId);
                    mCameraView = new CameraView(this, mCamera);
                    camera_view.addView(mCameraView);
                    mCamera.startPreview();
                    Toast.makeText(getApplicationContext(), "Kamera Belakang", Toast.LENGTH_SHORT).show();
                }
                else {
                    mCamera.stopPreview();
                    mCamera.release();
                    cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    mCamera = Camera.open(cameraId);
                    mCameraView = new CameraView(this, mCamera);
                    camera_view.addView(mCameraView);
                    mCamera.startPreview();
                    Toast.makeText(getApplicationContext(), "Kamera Depan", Toast.LENGTH_SHORT).show();
                }
            }
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);

    }

    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}