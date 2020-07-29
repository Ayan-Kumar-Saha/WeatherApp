package com.example.flexhale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flexhale.model.Report;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checking for permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        }
        else {
            enableLocation();
            getLocation();
        }

        setContentView(R.layout.activity_main);
    }

    // Requesting for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, Keystore.LOCATION_REQUEST_CODE);
    }

    // Result of requested permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Keystore.LOCATION_REQUEST_CODE && grantResults.length > 0) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocation();
                getLocation();
            }
            else Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(this)
                    .setMessage("Enable GPS?")
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("cancel", null)
                    .show();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {

        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.getFusedLocationProviderClient(MainActivity.this).requestLocationUpdates(locationRequest, new LocationCallback(){

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                LocationServices.getFusedLocationProviderClient(MainActivity.this).removeLocationUpdates(this);

                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    int index = locationResult.getLocations().size() - 1;

                    String latitude = Double.toString(locationResult.getLocations().get(index).getLatitude());
                    String longitude = Double.toString(locationResult.getLocations().get(index).getLongitude());

                    new WeatherReporter().execute(Keystore.API_KEY, latitude, longitude);
                }
            }
        }, Looper.getMainLooper());
    }


    public class WeatherReporter extends AsyncTask<String, Void, Report> {

        @Override
        protected void onPreExecute() {
            Toast.makeText(MainActivity.this, "Loading weather report", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Report doInBackground(String... values) {

            String apiKey = values[0];
            String latitude = values[1];
            String longitude = values[2];

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(String.format("http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s", latitude, longitude, apiKey))
                    .get()
                    .build();

            try {
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                String jsonString = responseBody.string();
                JSONObject jsonObject = new JSONObject(jsonString);

                Report report = new Report(jsonObject);

                return report;

            } catch (IOException | JSONException e) { return null; }
        }

        @Override
        protected void onPostExecute(Report report) {

            TextView location = findViewById(R.id.tv_location);
            location.setText(report.getLocation());

            ImageView icon = findViewById(R.id.iv_weather_icon);
            String imgResource = "icon"+report.getIcon();
            int imgResId = getResources().getIdentifier(imgResource, "drawable", getPackageName());
            icon.setImageResource(imgResId);

            TextView temperature = findViewById(R.id.tv_temp);
            temperature.setText(report.getTemperature());

            TextView temperatureFeelsLike = findViewById(R.id.tv_temp_feels_like);
            temperatureFeelsLike.setText("feels like " + report.getTemperatureFeelsLike());

            TextView temperatureMax = findViewById(R.id.tv_max_temp);
            temperatureMax.setText("H " + report.getTemperatureMax());

            TextView temperatureMin = findViewById(R.id.tv_min_temp);
            temperatureMin.setText("L " + report.getTemperatureMin());

            TextView weather = findViewById(R.id.tv_weather);
            weather.setText(report.getWeather());

            TextView humidity = findViewById(R.id.tv_humidity);
            humidity.setText("Humidity: " + report.getHumidity() + "%");

            TextView winds = findViewById(R.id.tv_winds);
            winds.setText("Winds: " + report.getWindDirection() + " " + report.getWindSpeed() + " KMH");
        }

    }
}