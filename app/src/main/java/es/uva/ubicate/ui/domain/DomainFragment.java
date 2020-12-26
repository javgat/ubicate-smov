package es.uva.ubicate.ui.domain;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import es.uva.ubicate.R;
import es.uva.ubicate.persistence.FirebaseDAO;

public class DomainFragment extends Fragment {

    private final String TAG = "DomainFragment";

    private void changeFragment(Fragment F){
        final FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        int contId = ((ViewGroup)getView().getParent()).getId();
        ft.replace(contId, F);
        ft.commit();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        int fragment = R.layout.fragment_domain;
        View root = inflater.inflate(fragment, container, false);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mEmpresa = mDatabase.child("usuario").child(uid).child("empresa");
        mEmpresa.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String empresa = snapshot.getValue(String.class);
                if(empresa==null){
                    changeFragment(new NoDomainFragment());
                }else{
                    mDatabase.child("empresa").child(empresa).child("miembros").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Boolean admin = snapshot.getValue(Boolean.class);
                            if(admin==null){
                                Log.d(TAG, "Error al obtener el tipo de usuario en la empresa");
                            }else if(!admin){
                                changeFragment(new MemberFragment());
                            }else{
                                //Aqui a adminFragment
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, "Error al obtener el tipo de usuario en la empresa");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "Error al obtener la empresa del usuario");
            }
        });
        return root;
    }
}
