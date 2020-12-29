package es.uva.ubicate;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.PointerIcon;
import android.view.View;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;
import java.util.Date;

import es.uva.ubicate.persistence.FirebaseDAO;

public class DrawerActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private LocationRequest locationRequest;
    private Intent updateLocationService;

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int REQ_LOCATION = 201;

    private String TAG = "DrawerActivity";

    public void updateUserDataDrawer(){
        TextView title = (TextView) findViewById(R.id.textTitleView);
        TextView textEmail = (TextView) findViewById(R.id.textView);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        title.setText(currentUser.getDisplayName());
        textEmail.setText(currentUser.getEmail());

        FirebaseStorage storage = FirebaseStorage.getInstance();
        String path = "images/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+".jpg";
        Log.d(TAG, path);
        FirebaseDAO.downloadImage(imageView, storage, path, TAG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "Back pulsado, no deberia hacer nada");
                // Handle the back button event
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Accion de añadir un evento
                Snackbar.make(view, "Añadir evento aún no disponible", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_maps, R.id.nav_chat, R.id.nav_domain, R.id.nav_profile, R.id.nav_logout)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        tryActivateLocation();

    }

    private void noLocation(){
        Log.d(TAG, "Pues nada no hay ubicacion :(");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);//???
        switch (requestCode) {
            case REQ_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    tryActivateLocation();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.location_no_allowed, Toast.LENGTH_LONG).show();
                    noLocation();
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }

    private void showRationaleLocation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Activity activity = this;
        builder.setMessage(R.string.dialog_rationale_location)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQ_LOCATION);//??
                    }
                });
        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void tryActivateLocation(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_DENIED){
            if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
                showRationaleLocation();
            else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQ_LOCATION);//??
        }else
            tryActivateLocationAllowed();
    }

    private void tryActivateLocationAllowed(){
        if(locationRequest==null)
            locationRequest = createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        Task<LocationSettingsResponse> task =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    launchUpdateLocationService();
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        DrawerActivity.this,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            Log.d(TAG, "Settings change unavailable");
                            noLocation();
                            break;
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
                        // All required changes were successfully made
                        launchUpdateLocationService();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        showStopLocation();
                        noLocation();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    private void launchUpdateLocationService(){
        updateLocationService = new Intent(DrawerActivity.this, UpdateLocationService.class);
        updateLocationService.putExtra("inputExtra", "Foreground Service Example in Android");
        updateLocationService.putExtra("locationRequest", locationRequest);
        ContextCompat.startForegroundService(DrawerActivity.this, updateLocationService);
        showStartLocation();
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(15000);
        // Si la ubicacion fuera inexacta cambiar a HIGH ACCURACY
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        return locationRequest;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);

        //Lo de update este deberia ir en otra parte pero bueno funca si esta aqui
        updateUserDataDrawer();

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_ubi_serv:
                Log.d(TAG, "Pulsado switch");
                if(updateLocationService==null){
                    tryActivateLocation();
                }else{
                    stopLocationService();
                }
                return true;
            case R.id.action_settings:
                //startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                Log.d(TAG, "Otra cosa");
                return false;
        }
    }

    // Para que se llame desde una clase interior con un boton
    public void stopLocationService(){
        if(updateLocationService!=null) {
            stopService(updateLocationService);
            updateLocationService=null;
        }
        showStopLocation();
    }

    private void showStopLocation(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        MenuItem ubi_serv = toolbar.getMenu().findItem(R.id.change_ubi_serv);
        if(ubi_serv!=null)
            ubi_serv.setIcon(getDrawable(R.drawable.baseline_location_off_black_18dp));
        Toast.makeText(getApplicationContext(), "Has dejado de compartir tu ubicacion", Toast.LENGTH_LONG).show();
    }

    private void showStartLocation(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        MenuItem ubi_serv = toolbar.getMenu().findItem(R.id.change_ubi_serv);
        if(ubi_serv!=null)
            ubi_serv.setIcon(getDrawable(R.drawable.baseline_location_on_black_18dp));
        Toast.makeText(getApplicationContext(), "Has empezado a compartir tu ubicacion", Toast.LENGTH_LONG).show();
    }

    public void exit(){
        stopLocationService();
        setResult(Activity.RESULT_OK);
        finish();
    }
}