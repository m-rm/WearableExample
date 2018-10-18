package at.ac.ait.hbs.wearableproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Locale;

public class MainActivity extends WearableActivity implements SensorEventListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    private final static String[] permissions = new String[]{Manifest.permission.BODY_SENSORS, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private final long startTime = System.currentTimeMillis();

    private TextView heartRateTextView;
    private TextView stepCounterTextView;

    private SensorManager sensorManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the UI text elements
        heartRateTextView = findViewById(R.id.heart_rate);
        stepCounterTextView = findViewById(R.id.steps);

        sensorManager = getSystemService(SensorManager.class);

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If we already have all the permissions start immediately, otherwise request permissions
        if (permissionsGranted()) {
            startSensors();
        } else {
            Log.d(TAG, "Requesting permissions");
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }

    /**
     * Checks if all necessary permissions have been granted
     *
     * @return True if all necessary permissions have been granted, false otherwise
     */
    private boolean permissionsGranted() {
        Boolean result = true;

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                result = false;
            }
        }

        return result;
    }

    @Override
    protected void onPause() {
        // Unregister our listener to prevent leaks
        sensorManager.unregisterListener(this);

        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {
            if (permissions.length != MainActivity.permissions.length) {
                Log.e(TAG, "Number of permission results does not match expected number");
                return;
            }

            // Check if all permissions have been granted
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

            Log.d(TAG, "Permissions granted");

            // Now we can actually start
            startSensors();
        }
    }

    /**
     * Starts gathering sensor data
     */
    private void startSensors() {
        if (sensorManager != null) {
            // Start heart rate sensor
            final Sensor heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);

            // Start step counter
            final Sensor stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Sensors registered");
        } else {
            Log.e(TAG, "SensorManager is null");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Check if a value is attached, if not we can ignore it
        if (sensorEvent.values.length > 0) {
            final float value = sensorEvent.values[0];

            if (sensorEvent.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                Log.d(TAG, "Received new heart rate value: " + value);
                heartRateTextView.setText(getString(R.string.heart_rate, (int) value));

                // Add a new line to file where the heart rate is our value and the step count is empty
                writeSensorDataToFile(String.format(Locale.GERMAN, "%.0f", value), "");

            } else if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                Log.d(TAG, "Received new step counter value: " + value);
                stepCounterTextView.setText(getString(R.string.steps, (int) value));

                // Add a new line to file where the step count is our value and the heart rate is empty
                writeSensorDataToFile("", String.format(Locale.GERMAN, "%.0f", value));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not needed
    }

    /**
     * Writes the sensor data to file. Every call to this method appends a new line in the file.
     *
     * @param heartRate The heart rate, or null if not applicable
     * @param steps     The number of steps, or null if not applicable
     */
    private synchronized void writeSensorDataToFile(String heartRate, String steps) {
        final File directory = Environment.getExternalStorageDirectory();

        final File file = new File(directory, "sensordata.csv");

        // Calculate the time from app start until now in seconds
        final float time = (System.currentTimeMillis() - startTime) / 1000.0f;

        try {
            // Write to file
            final FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            final OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
            writer.append(String.format(Locale.GERMAN, "%.2f;%s;%s\n", time, heartRate, steps));
            writer.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
