package es.uva.ubicate.ui.domain;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import es.uva.ubicate.R;
import es.uva.ubicate.persistence.FirebaseDAO;

public class NoDomainFragment extends Fragment {

    private void reloadDomain(){
        final FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        int contId = ((ViewGroup)getView().getParent()).getId();
        ft.replace(contId, new DomainFragment());
        ft.commit();
    }

    private void crearEmpresa(String name){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDAO.creaEmpresa(uid, name);
        reloadDomain();
    }

    private void joinEmpresa(String code){

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mJoinEmpresa = mDatabase.child("join_empresa").child(code);
        mJoinEmpresa.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String idEmpresa = snapshot.getValue(String.class);
                if(idEmpresa==null){
                    Toast.makeText(getContext(), "Codigo de empresa no existente", Toast.LENGTH_LONG).show();
                }else{
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    FirebaseDAO.joinEmpresa(uid, idEmpresa);
                    reloadDomain();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createNoEmpresa(View root){
        final EditText edit_name_empresa = root.findViewById(R.id.edit_create);
        final Button boton_crear = root.findViewById(R.id.button_create);
        final EditText edit_join = root.findViewById(R.id.edit_join);
        final Button boton_join = root.findViewById(R.id.button_join);

        boton_crear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearEmpresa(edit_name_empresa.getText().toString());
            }
        });

        boton_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinEmpresa(edit_join.getText().toString());
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_no_domain, container, false);
        createNoEmpresa(root);
        return root;
    }   
}