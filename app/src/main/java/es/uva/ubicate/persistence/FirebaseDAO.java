package es.uva.ubicate.persistence;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import es.uva.ubicate.R;
import es.uva.ubicate.data.model.Evento;

public class FirebaseDAO {
    public static void setLocation(String uid, LatLng locUser, Date fecha){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("usuario").child(uid).child("latitude").setValue(locUser.latitude);
        mDatabase.child("usuario").child(uid).child("longitude").setValue(locUser.longitude);
        mDatabase.child("usuario").child(uid).child("date").setValue(DateFormat.getDateTimeInstance().format(fecha));
    }

    public static void setName(String uid, String name) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("usuario").child(uid).child("public_name").setValue(name);
    }

    public static void creaEmpresa(String uid, String name){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String key = mDatabase.child("empresa").push().getKey();
        DatabaseReference mEmpresa = mDatabase.child("empresa").child(key);
        mEmpresa.child("miembros").child(uid).setValue(true);
        mEmpresa.child("nombre").setValue(name);

        DatabaseReference mUser = mDatabase.child("usuario").child(uid);
        mUser.child("empresa").setValue(key);
    }

    public static void joinEmpresa(String uid, String idEmpresa){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mEmpresa = mDatabase.child("empresa").child(idEmpresa);
        mEmpresa.child("miembros").child(uid).setValue(false);

        DatabaseReference mUsuario = mDatabase.child("usuario").child(uid);
        mUsuario.child("empresa").setValue(idEmpresa);
    }

    public static void exitDomainMember(String uid, String idEmpresa){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mEmpresa = mDatabase.child("empresa").child(idEmpresa);
        mEmpresa.child("miembros").child(uid).removeValue();

        DatabaseReference mUsuario = mDatabase.child("usuario").child(uid);
        mUsuario.child("empresa").removeValue();
    }

    public static void deleteDomain(String idEmpresa) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mEmpresa = mDatabase.child("empresa").child(idEmpresa);
        mEmpresa.removeValue();
    }

    public static void setAdmin(String idEmpresa, String id) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mMiembro = mDatabase.child("empresa").child(idEmpresa).child("miembros").child(id);
        mMiembro.setValue(true);

    }

    public static void crearJoinCode(String idEmpresa, String mensaje) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mEmpresa = mDatabase.child("empresa").child(idEmpresa);
        mEmpresa.child("join_empresa").setValue(mensaje);
        mDatabase.child("join_empresa").child(mensaje).setValue(idEmpresa);

    }

    public static void borrarCodigo(String idEmpresa, String mensaje) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mEmpresa = mDatabase.child("empresa").child(idEmpresa);
        mEmpresa.child("join_empresa").removeValue();
        mDatabase.child("join_empresa").child(mensaje).removeValue();
    }

    public static void changeDomainName(String idEmpresa, String nombre) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mEmpresa = mDatabase.child("empresa").child(idEmpresa);
        mEmpresa.child("nombre").setValue(nombre);
    }

    public static void borrarEvento(String idEmpresa, String idEvento) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mEmpresa = mDatabase.child("empresa").child(idEmpresa);
        mEmpresa.child("eventos").child(idEvento).removeValue();
    }

    public static void addEvento(String idEmpresa, Evento evento) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mEmpresa = mDatabase.child("empresa").child(idEmpresa);
        String key = mEmpresa.child("eventos").push().getKey();
        mEmpresa.child("eventos").child(key).setValue(evento);
    }

    public static void downloadImage(ImageView imageView, FirebaseStorage storage, String url, String TAG){
        StorageReference storageRef = storage.getReference();
        StorageReference pathReference = storageRef.child(url);
        final long ONE_MEGABYTE = 1024 * 1024;
        pathReference.getBytes(5*ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG, "Si tiene imagen de perfil");
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                if(width<height)
                    height=width;
                //int crop = (width - height) / 2;
                Bitmap cropImg = Bitmap.createBitmap(bitmap, 0, 0, height, height);
                imageView.setImageBitmap(cropImg);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (exception instanceof StorageException &&
                        ((StorageException) exception).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    Log.d(TAG, "No tiene imagen de perfil");
                }
            }
        });
    }

    public static Bitmap getBitmapFromView(View view) {
        view.setVisibility(View.VISIBLE);
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        /*if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);*/
        view.draw(canvas);
        return returnedBitmap;
    }

    public static void markerImage(Marker markPeer, View pinView, FirebaseStorage storage, String url, String TAG) {
        StorageReference storageRef = storage.getReference();
        StorageReference pathReference = storageRef.child(url);
        ImageView imageView = pinView.findViewById(R.id.center_icon);
        final long ONE_MEGABYTE = 1024 * 1024;
        pathReference.getBytes(5*ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                if(width<height)
                    height=width;
                //int crop = (width - height) / 2;
                Bitmap cropImg = Bitmap.createBitmap(bitmap, 0, 0, height, height);
                imageView.setImageBitmap(cropImg);

                Log.d(TAG, "Imagen del pin descargado con exito");
                pinView.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "RUN");
                        markPeer.setIcon(BitmapDescriptorFactory.fromBitmap(getBitmapFromView(pinView)));
                        pinView.setVisibility(View.GONE);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (exception instanceof StorageException &&
                        ((StorageException) exception).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                }
            }
        });
    }
}
