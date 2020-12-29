package es.uva.ubicate.ui.domain;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.ContactsContract;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import es.uva.ubicate.R;
import es.uva.ubicate.persistence.FirebaseDAO;

public class AdminDomainFragment extends Fragment {

    private final String TAG = "AdminDomainFragment";

    private void reloadDomain(){
        final FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        int contId = ((ViewGroup)getView().getParent()).getId();
        ft.replace(contId, new DomainFragment());
        ft.commit();
    }

    private View createMemberCard(String nombre, String url, boolean esAdmin, String id, String idEmpresa){
        View memberView = getLayoutInflater().inflate(R.layout.domain_member, null);
        TextView texto = memberView.findViewById(R.id.nombre_member);
        texto.setText(nombre);

        ImageView imageView = memberView.findViewById(R.id.image_member);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseDAO.downloadImage(imageView, storage, url, TAG);

        Button button_admin = memberView.findViewById(R.id.button_admin_member);
        Button button_expulsar = memberView.findViewById(R.id.button_delete_member);

        if(!esAdmin){
            ImageView adminCheck = memberView.findViewById(R.id.admin_member);
            adminCheck.setVisibility(View.INVISIBLE);
            button_admin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDAO.setAdmin(idEmpresa, id);
                    reloadDomain();
                }
            });

            button_expulsar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDAO.exitDomainMember(id, idEmpresa);
                    reloadDomain();
                }
            });
        }else{
            button_admin.setVisibility(View.INVISIBLE);
            button_expulsar.setVisibility(View.INVISIBLE);
        }


        return memberView;
    }

    private void exitDomainAdmin(){
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
                    mDatabase.child("empresa").child(idEmpresa).child("miembros").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            long cantMember = snapshot.getChildrenCount();
                            if(cantMember==1){
                                FirebaseDAO.deleteDomain(idEmpresa);
                            }else {
                                Iterable<DataSnapshot> datas = snapshot.getChildren();
                                boolean hayAdmin = false;
                                String id = "";
                                for(DataSnapshot miembroSnap : datas){
                                    hayAdmin = hayAdmin || miembroSnap.getValue(Boolean.class);
                                    id = miembroSnap.getKey();
                                }
                                if(!hayAdmin){
                                    FirebaseDAO.setAdmin(idEmpresa, id);
                                }
                            }
                            reloadDomain();
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

    private void tryExitDomain(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.dialog_exit_domain_admin)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        exitDomainAdmin();
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

    private void crearJoinCode(String idEmpresa, String mensaje){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("join_empresa").child(mensaje).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Toast.makeText(getContext(), R.string.join_code_exists, Toast.LENGTH_LONG).show();
                }else{
                    FirebaseDAO.crearJoinCode(idEmpresa, mensaje);
                    reloadDomain();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void tryCrearCode(String idEmpresa){
        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.dialog_input_code_domain)
                .setView(input)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        crearJoinCode(idEmpresa, input.getText().toString());
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

    private void tryChangeDomainName(String idEmpresa){
        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.dialog_change_domain_name)
                .setView(input)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseDAO.changeDomainName(idEmpresa, input.getText().toString());
                        reloadDomain();
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

        TextView code_empresa = root.findViewById(R.id.text_code_admin);
        Button button_code = root.findViewById(R.id.button_code_admin);

        final ImageView image_name = root.findViewById(R.id.image_edit_name_domain);

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
                                Boolean es_admin = childSnap.getValue(Boolean.class);
                                mDatabase.child("usuario").child(memberId).child("public_name").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String nombre = snapshot.getValue(String.class);
                                        String url = "images/"+memberId+".jpg";
                                        View memberCard = createMemberCard(nombre, url, es_admin.booleanValue(), memberId, idEmpresa);
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

                    mDatabase.child("empresa").child(idEmpresa).child("join_empresa").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){//Con codigo
                                String codigo = snapshot.getValue(String.class);
                                code_empresa.setText("Codigo: "+codigo);
                                code_empresa.setVisibility(View.VISIBLE);
                                button_code.setText(R.string.delete_code_domain);
                                button_code.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        FirebaseDAO.borrarCodigo(idEmpresa, codigo);
                                        reloadDomain();
                                    }
                                });
                            }else{//Sin codigo
                                button_code.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        tryCrearCode(idEmpresa);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                image_name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tryChangeDomainName(idEmpresa);
                    }
                });
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
        View root = inflater.inflate(R.layout.fragment_admin_domain, container, false);
        updateValuesView(root);
        return root;
    }
}