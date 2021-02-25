package com.demo.thirdhometask;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.snackbar.Snackbar;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;


public class ScreenActivity extends AppCompatActivity {
    private ImageView imgViewSun;
    private Animation sunTurnAnim;
    private View mLayout;
    private FusedLocationProviderClient fusedLocationClient;
    private Location mCurrentLocation;
    private CancellationTokenSource cancellationTokenSource;

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.i("zzz", "registerForActivityResult");
                    requestCurrentLocation();
                } else {
                    accessDenyed();
                }
            });

    private void accessDenyed() {
        Snackbar.make(mLayout, R.string.location_access_denyed, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, view -> {
                    Intent intent = new Intent(ScreenActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    ScreenActivity.this.finish();
                }).show();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        imgViewSun = findViewById(R.id.sun);
        sunTurnAnim = AnimationUtils.loadAnimation(this, R.anim.sun_turn);
        imgViewSun.startAnimation(sunTurnAnim);
        mLayout = findViewById(R.id.start_activity);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        cancellationTokenSource = new CancellationTokenSource();

        // После включения GPS и нажатия кнопки назад идет возврат на ScreenActivity и после нажатия на OK requestCurrentLocation() не вызывается, почему?
//        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
//        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        if(!statusOfGPS) onGPS();

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(mLayout, R.string.location_permission_granted, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, view -> {
                        requestCurrentLocation();
                    }).show();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Snackbar.make(mLayout, R.string.location_access_required, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, view -> {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                    }).show();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void onGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", (dialog, which) ->
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("No", (dialog, which) -> dialog.cancel());
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }



    @Override
    protected void onStop() {
        super.onStop();
        cancellationTokenSource.cancel();
    }

    @SuppressLint("MissingPermission")
    private void requestCurrentLocation() {

        fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        mCurrentLocation = task.getResult();
                        Intent intent = new Intent(ScreenActivity.this, MainActivity.class);
                        intent.putExtra("latitude", mCurrentLocation.getLatitude());
                        intent.putExtra("longitude", mCurrentLocation.getLongitude());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        ScreenActivity.this.finish();
                    }
                });
    }
}