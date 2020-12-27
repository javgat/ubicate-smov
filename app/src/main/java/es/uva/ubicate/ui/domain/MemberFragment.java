package es.uva.ubicate.ui.domain;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import es.uva.ubicate.R;
import es.uva.ubicate.persistence.FirebaseDAO;

public class MemberFragment extends Fragment {

    private final String TAG = "MemberFragment";

    private void reloadDomain(){
        final FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        int contId = ((ViewGroup)getView().getParent()).getId();
        ft.replace(contId, new DomainFragment());
        ft.commit();
    }

    private View createMemberCard(String nombre, String url){
        CardView card = new CardView(getContext());
        TextView texto = new TextView(getContext());
        texto.setText(nombre);
        texto.setTextSize(18);
        card.addView(texto);
        card.setContentPadding(20, 10, 20, 10);

        ImageView imageView = new ImageView(getContext());
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        Log.d(TAG, url);
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
                Log.d(TAG, "No tiene imagen de perfil");
            }
        });
        card.addView(imageView);
        return card;
    }

    private void exitDomain(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("usuario").child(uid).child("empresa").addListenerForSingleValueEvent(new ValueEventListener() {//Lee la empresa del usuario
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String idEmpresa = snapshot.getValue(String.class);
                if (idEmpresa == null) {
                    Log.d(TAG, "Error al acceder a la empresa"); // No deberia pasar
                } else {
                    FirebaseDAO.exitDomainMember(uid, idEmpresa);
                    reloadDomain();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void tryExitDomain(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.dialog_exit_domain)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        exitDomain();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void updateValuesView(View root){
        TextView text_nombre_empresa = root.findViewById(R.id.domain_member_title);
        Button button_exit = root.findViewById(R.id.button_exit_member);
        button_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryExitDomain();
            }
        });
        String nombre;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        LinearLayout linearScroll =(LinearLayout) root.findViewById(R.id.linear_scroll_member_domain);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("usuario").child(uid).child("empresa").addListenerForSingleValueEvent(new ValueEventListener() {//Lee la empresa del usuario
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String idEmpresa = snapshot.getValue(String.class);
                if(idEmpresa==null){
                    Log.d(TAG, "Error al acceder a la empresa"); // No deberia pasar
                }else{
                    mDatabase.child("empresa").child(idEmpresa).child("nombre").addListenerForSingleValueEvent(new ValueEventListener() {//Lee el nombre de la empresa
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String nombre = snapshot.getValue(String.class);
                            if(nombre==null){
                                Log.d(TAG, "Error al acceder al nombre de la empresa"); // No deberia pasar
                            }else {
                                text_nombre_empresa.setText(nombre);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    mDatabase.child("empresa").child(idEmpresa).child("miembros").addListenerForSingleValueEvent(new ValueEventListener() {//Lee los miembros de la empresa
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot childSnap : snapshot.getChildren()) {
                                String memberId = childSnap.getKey();
                                mDatabase.child("usuario").child(memberId).child("public_name").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String nombre = snapshot.getValue(String.class);
                                        String url = "images/"+memberId+".jpg";
                                        View memberCard = createMemberCard(nombre, url);
                                        linearScroll.addView(memberCard);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_member, container, false);
        updateValuesView(root);

        return root;
    }
}