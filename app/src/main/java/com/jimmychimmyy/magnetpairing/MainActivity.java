package com.jimmychimmyy.magnetpairing;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager mSensorManager;

    private TextView magXTextView;
    private TextView magYTextView;
    private TextView magZTextView;

    private ImageView mainAlertView;

    private Sensor mAccelerometer;
    private Sensor mGeomagnetic;
    private float[] accelerometerValues;
    private float[] geomagneticValues;

    // to prevent user shakes and rotatation from disrupting pairing, use the amplitude
    // change of the mangetometer readings as triggering indicator (ignore the magnetometer readings
    // at individual directions)
    // amplitudeChange that falls between lowAmplitude and highAmplitude will register as wanting to pair
    private int amplitudeChange; // = sqroot(B(x)^2 + B(y)^2 + B(z)^2)
    private int lowAmplitude;
    private int highAmplitude;

    /** Flags. */
    private boolean specDefined = false;
    private boolean kalmanFiltering = false;

    /** Rates. */
    private float nanoTtoGRate = 0.00001f;
    private final int gToCountRate = 1000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGeomagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        magXTextView = (TextView) findViewById(R.id.magXTextView);
        magYTextView = (TextView) findViewById(R.id.magYTextView);
        magZTextView = (TextView) findViewById(R.id.magZTextView);

        mainAlertView = (ImageView) findViewById(R.id.mainAlertView);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGeomagnetic, SensorManager.SENSOR_DELAY_FASTEST);

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String data = magXTextView.getText() + "&" + magYTextView.getText() + "&" + magZTextView.getText();
                        System.out.println(data);
                        //System.out.println("posting");
                        //new HttpPost().execute(data);
                    }
                });
            }
        };
        timer.schedule(task, 0, 100);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) return;

        synchronized (this) {
            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    accelerometerValues = sensorEvent.values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    geomagneticValues = sensorEvent.values.clone();
                    break;
            }

            if (accelerometerValues != null && geomagneticValues != null) {
                float[] Rs = new float[16];
                float[] I = new float[16];

                if (SensorManager.getRotationMatrix(Rs, I, accelerometerValues, geomagneticValues)) {
                    float[] RsInv = new float[16];
                    Matrix.invertM(RsInv, 0, Rs, 0);

                    float resultVec[] = new float[4];
                    float[] geomagneticValuesAdjusted = new float[4];
                    geomagneticValuesAdjusted[0] = geomagneticValues[0];
                    geomagneticValuesAdjusted[1] = geomagneticValues[1];
                    geomagneticValuesAdjusted[2] = geomagneticValues[2];
                    geomagneticValuesAdjusted[3] = 0;
                    Matrix.multiplyMV(resultVec, 0, RsInv, 0, geomagneticValuesAdjusted, 0);

                    for (int i = 0; i < resultVec.length; i++) {
                        resultVec[i] = resultVec[i] * nanoTtoGRate * gToCountRate;
                    }

                    if (kalmanFiltering) {

                    } else {
                        magXTextView.setText("x=" + resultVec[0]);
                        magYTextView.setText("y=" + resultVec[1]);
                        magZTextView.setText("z=" + resultVec[2]);

                        // the magnetometer readings at two (up to three) directions change abruptly which means time to pair
                        if (resultVec[1] > 500) {
                            mainAlertView.setVisibility(View.VISIBLE);
                        } else {
                            mainAlertView.setVisibility(View.INVISIBLE);
                        }
                        // TODO
                        // put each of the magVar readings into a list
                        // send over google nearby OR send to server to send to device
                        // compare the readings between this device and nearby device
                        // do we need to shift the readings?

                        // begin trying to pair when readings change abruptly (this means something is nearby)

                        // to prevent user shakes and rotatation from disrupting pairing, use the amplitude
                        // change of the mangetometer readings as triggering indicator (ignore the magnetometer readings
                        // at individual directions)

                        // set up timer based on the probablity that the nearby object is trying to pair
                        // if no reply, then stop trying to pair after set amount of seconds


                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class HttpPost extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... data) {

            String urlString = "http://acsweb.ucsd.edu/~kkhuong/data.php?" + data[0];
            //System.out.println(urlString);

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("HEAD");
                int responseCode = urlConnection.getResponseCode();
                //System.out.println(responseCode);
            } catch (Exception e) {
                System.out.print(e.getMessage());
                return e.getMessage();
            }

            return null;
        }
    }

}


















