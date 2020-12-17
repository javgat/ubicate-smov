package es.uva.ubicate.persistence;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

public class FirebaseDAO {
    public static void setLocation(String uid, LatLng locUser, Date fecha){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("usuario").child(uid).child("latitude").setValue(locUser.latitude);
        mDatabase.child("usuario").child(uid).child("longitude").setValue(locUser.longitude);
        mDatabase.child("usuario").child(uid).child("date").setValue(fecha.toString());
    }

}