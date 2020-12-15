package es.uva.ubicate.ui.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.uva.ubicate.DrawerActivity;
import es.uva.ubicate.R;

public class MapsFragment extends Fragment {

    private final String TAG = "MapsFragment";

    private final int ZOOM_CAMERA = 16; //Entre 2 y 21 tiene que ser, 21 full zoom

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            /*LatLng vall = new LatLng(41.6524076, -4.7246467);
            googleMap.addMarker(new MarkerOptions().position(vall).title("Marcador en Valladolid"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vall, 17));*/
            updateMap(googleMap);

        }

        public void updateMap(GoogleMap googleMap) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            LatLng vall = new LatLng(41.6524076, -4.7246467); //LatLng de default para inicial

            DatabaseReference mMesRef = mDatabase.child("usuario").child(mAuth.getUid()).child("empresa");
            Log.d(TAG, "Pidiendo datos  del server");
            mMesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String empresaId = dataSnapshot.getValue(String.class);
                    Log.d(TAG, "Recibido datos de usuario, es de la empresa " + empresaId);
                    mDatabase.child("empresa").child(empresaId).child("miembros").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            String peer = snapshot.getKey();
                            Log.d(TAG, "Recibido datos de empresa, miembro: " + peer);
                            Marker markPeer = googleMap.addMarker(new MarkerOptions().position(vall).visible(false).title(peer));

                            mDatabase.child("usuario").child(peer).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Log.d(TAG, "Recibido datos de compa");
                                    double latitude = snapshot.child("latitude").getValue(Double.class);
                                    double longitude = snapshot.child("longitude").getValue(Double.class);
                                    String date = snapshot.child("date").getValue(String.class);
                                    Log.d(TAG, "El pibe está en " + latitude + " " + longitude);
                                    Log.d(TAG, "Eso a las " + date);
                                    LatLng peerLL = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
                                    markPeer.setPosition(peerLL);

                                    if(peer.equals(mAuth.getUid())){
                                        markPeer.setTitle("Tu ubicacion");
                                        markPeer.setSnippet("¡Este eres tú!");
                                        if(!markPeer.isVisible())//la primera vez no es visible, asi que no se mueve el zoom every time
                                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(peerLL, ZOOM_CAMERA));
                                    }else{
                                        markPeer.setTitle(snapshot.child("public_name").getValue(String.class));
                                        markPeer.setSnippet(date);
                                    }
                                    markPeer.setVisible(true);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d(TAG, "Error al acceder a los datos de " + peer + ", cual es su ubicacion " + error);
                                }
                            });

                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, "Error al acceder a los datos de la empresa, cuales son sus usuarios " + error);

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.d(TAG, "Error al acceder a los datos del usuario, cual es su empresa " + error);
                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

}