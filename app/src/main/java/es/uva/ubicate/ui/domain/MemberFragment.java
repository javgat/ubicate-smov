package es.uva.ubicate.ui.domain;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import es.uva.ubicate.R;

public class MemberFragment extends Fragment {

    private final String TAG = "MemberFragment";

    private void updateValuesView(View root){
        TextView text_nombre_empresa = root.findViewById(R.id.domain_member_title);
        String nombre;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("usuario").child(uid).child("empresa").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String idEmpresa = snapshot.getValue(String.class);
                if(idEmpresa==null){
                    Log.d(TAG, "Error al acceder a la empresa"); // No deberia pasar
                }else{
                    mDatabase.child("empresa").child(idEmpresa).child("nombre").addListenerForSingleValueEvent(new ValueEventListener() {
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