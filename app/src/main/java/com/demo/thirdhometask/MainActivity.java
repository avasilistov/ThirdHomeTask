package com.demo.thirdhometask;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import com.demo.thirdhometask.fragments.FragmentAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawer;
    private SwipeRefreshLayout swipe;
    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource cancellationTokenSource;
    private Toolbar toolbar;


    private ActivityResultLauncher<String> requestPermissionLauncher =
            getActivityResultLauncher();

    @NotNull
    private ActivityResultLauncher<String> getActivityResultLauncher() {
        return registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                requestCurrentLocation();
            } else {
                accessDenyed();
            }
        });
    }

    private void accessDenyed() {
        Snackbar.make(drawer, R.string.location_access_denyed, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, view -> {
                }).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.draw_layout);
        swipe = findViewById(R.id.swipe_refresh_layout);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        cancellationTokenSource = new CancellationTokenSource();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        ViewPager2 pager = findViewById(R.id.pager);
        pager.setAdapter(new FragmentAdapter(this));

        TabLayout tab_layout = findViewById(R.id.tab_layout);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
                tab_layout, pager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.main);
                    break;
                case 1:
                    tab.setText(R.string.settings);
                    break;
            }
        });
        tabLayoutMediator.attach();


    }


    @Override
    protected void onStart() {
        super.onStart();
        Bundle arguments = getIntent().getExtras();
        if (arguments != null) {
            Double lat = arguments.getDouble(getString(R.string.latitude));
            Double lng = arguments.getDouble(getString(R.string.longitude));
            getCityName(lat, lng);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        swipe.setOnRefreshListener(() -> {
            swipe.setRefreshing(false);
            if (ContextCompat.checkSelfPermission(
                    MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                            requestCurrentLocation();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }

        });
    }



    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @SuppressLint("MissingPermission")
    private void requestCurrentLocation() {

        fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location location = task.getResult();
                        Double lat = location.getLatitude();
                        Double lng = location.getLongitude();
                        getCityName(lat, lng);
                    }
                });
    }

    private void getCityName(Double lat, Double lng) {
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() > 0) {
            String city = addresses.get(0).getLocality();
            toolbar.setTitle("Location: " + city);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancellationTokenSource.cancel();
    }
}