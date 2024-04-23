package edu.sdsmt.project3johnsonna;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.provider.ProviderProperties;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.location.LocationListenerCompat;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager = null;
    private final ActiveListener activeListener = new ActiveListener();
    private DrawingView drawingView;
    private static final int NEED_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        drawingView = findViewById(R.id.drawingView);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Called when this application becomes foreground again.
     */
    @Override
    protected void onStart() {
        super.onStart();
        registerListeners();
    }

    /**
     * Called when this application is no longer the foreground application.
     */
    @Override
    protected void onStop() {
        super.onStop();
        unregisterListeners();
    }

    private void registerListeners() {
        unregisterListeners();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            List<String> providers = locationManager.getProviders(true);
            String bestAvailable = providers.get(0);
            for (int i = providers.size() - 1; i > 0; i--) {

                //use LocationProvider is under android S, and provider properties if over to check accuracy
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
                    LocationProvider loc = locationManager.getProvider(providers.get(i));
                    if (Objects.requireNonNull(loc).getAccuracy() < Objects.requireNonNull(locationManager.getProvider(bestAvailable)).getAccuracy()) {
                        bestAvailable = providers.get(i);
                    }
                } else {
                    ProviderProperties loc = locationManager.getProviderProperties(providers.get(i));
                    if (bestAvailable.equals(LocationManager.PASSIVE_PROVIDER) || Objects.requireNonNull(loc).getAccuracy() < Objects.requireNonNull(locationManager.getProviderProperties(bestAvailable)).getAccuracy()) {
                        bestAvailable = providers.get(i);
                    }
                }
            }
            if (!bestAvailable.equals(LocationManager.PASSIVE_PROVIDER)) {
                locationManager.requestLocationUpdates(bestAvailable, 500, 1, activeListener);
                Location location = locationManager.getLastKnownLocation(bestAvailable);
                drawingView.updateLocation(location);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, NEED_PERMISSIONS);
        }
    }

    private void unregisterListeners() {
        locationManager.removeUpdates(activeListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NEED_PERMISSIONS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                registerListeners();

            } else {
                Toast.makeText(getApplicationContext(), R.string.permissions_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ActiveListener implements LocationListenerCompat {

        @Override
        public void onLocationChanged(@NonNull Location location) {
            drawingView.updateLocation(location);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            registerListeners();
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            registerListeners();
        }
    }
}