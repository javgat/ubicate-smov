package es.uva.ubicate.ui.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import es.uva.ubicate.R;
import es.uva.ubicate.data.model.Evento;
import es.uva.ubicate.data.model.Mensaje;
import es.uva.ubicate.persistence.FirebaseDAO;

public class ChatsFragment extends Fragment {

    private final String TAG = "ChatsFragment";

    private void fillUINoMember(View root){
        LinearLayout linearLayout = root.findViewById(R.id.scroll_chat).findViewById(R.id.layout_mensajes);
        TextView textView = new TextView(getContext());
        textView.setText(R.string.chat_no_org);
        linearLayout.addView(textView);

        EditText editText = root.findViewById(R.id.area_mensaje).findViewById(R.id.input_mensaje);
        editText.setEnabled(false);
    }

    private View createMensajeView(Mensaje mensaje, String uid){
        View mensajeView;
        if(!uid.equals(mensaje.getIdAutor())) {
            mensajeView = getLayoutInflater().inflate(R.layout.mensaje, null);
            TextView autor = mensajeView.findViewById(R.id.autor_mensaje);
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("usuario").child(mensaje.getIdAutor()).child("public_name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    autor.setText(snapshot.getValue(String.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            mensajeView = getLayoutInflater().inflate(R.layout.mensaje_propio, null);
            TextView autor = mensajeView.findViewById(R.id.autor_mensaje);
            autor.setText("Tú");
        }
        TextView cuerpo = mensajeView.findViewById(R.id.cuerpo_mensaje);
        TextView date = mensajeView.findViewById(R.id.date_mensaje);

        cuerpo.setText(mensaje.getMensaje());
        date.setText(mensaje.getFecha());
        return mensajeView;
    }

    private void fillUIMember(View root, String uid, String idEmpresa){
        ScrollView scroll = root.findViewById(R.id.scroll_chat);
        LinearLayout linearLayout = scroll.findViewById(R.id.layout_mensajes);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("empresa").child(idEmpresa).child("mensajes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                linearLayout.removeAllViews();
                Iterable<DataSnapshot> datas = snapshot.getChildren();
                List<Mensaje> mensajeList = new ArrayList<>();
                for(DataSnapshot dato : datas){
                    Mensaje mensaje = dato.getValue(Mensaje.class);;
                    mensajeList.add(mensaje);
                }
                Collections.sort(mensajeList);
                for(Mensaje mensaje : mensajeList){
                    View mensajeView = createMensajeView(mensaje, uid);
                    linearLayout.addView(mensajeView);
                }
                scroll.post(new Runnable() {
                    @Override
                    public void run() {
                        scroll.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        EditText input = root.findViewById(R.id.area_mensaje).findViewById(R.id.input_mensaje);
        ImageView enviar_mensaje = root.findViewById(R.id.area_mensaje).findViewById(R.id.sendButton);
        enviar_mensaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date now = Calendar.getInstance().getTime();
                String fecha = DateFormat.getDateTimeInstance().format(now);
                Mensaje mensaje = new Mensaje(input.getText().toString(), uid, fecha);
                FirebaseDAO.sendMensaje(idEmpresa, mensaje, mDatabase);
                input.setText("");
            }
        });

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chats, container, false);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase.child("usuario").child(uid).child("empresa").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String idEmpresa = snapshot.getValue(String.class);
                    fillUIMember(root, uid, idEmpresa);
                }else{
                    fillUINoMember(root);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return root;
    }
}
