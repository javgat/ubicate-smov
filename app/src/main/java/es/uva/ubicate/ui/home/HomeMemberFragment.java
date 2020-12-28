package es.uva.ubicate.ui.home;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import es.uva.ubicate.R;
import es.uva.ubicate.data.model.Evento;
import es.uva.ubicate.persistence.FirebaseDAO;
import es.uva.ubicate.ui.domain.DatePickerFragment;
import es.uva.ubicate.ui.domain.DomainFragment;

public class HomeMemberFragment extends Fragment{

    private final String TAG = "HomeMemberFragment";

    private void changeFragment(Fragment F){
        final FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        int contId = ((ViewGroup)getView().getParent()).getId();
        ft.replace(contId, F);
        ft.commit();
    }

    private void reloadEvent(){
        changeFragment(new HomeFragment());
    }

    private View createEventoView(Evento evento, boolean esAdmin, String idEmpresa, String idEvento){
        View eventoView = getLayoutInflater().inflate(R.layout.evento, null);
        TextView title = eventoView.findViewById(R.id.title_event);
        TextView description = eventoView.findViewById(R.id.description_event);
        TextView date = eventoView.findViewById(R.id.date_event);
        Button borrar = eventoView.findViewById(R.id.boton_borrar_evento);
        title.setText(evento.getTitulo());
        description.setText(evento.getDescripcion());
        date.setText(evento.getFecha());
        if(esAdmin) {
            borrar.setVisibility(View.VISIBLE);
            borrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDAO.borrarEvento(idEmpresa, idEvento);
                    reloadEvent();
                }
            });
        }
        return eventoView;
    }

    private void tryAddEvento(View root, String idEmpresa){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final EditText input = new EditText(getContext());
        final EditText input2 = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT );//| InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Título");
        input2.setInputType(InputType.TYPE_CLASS_TEXT );//| InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input2.setHint("Descripción");

        Context context = getContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(input); // Notice this is an add method
        layout.addView(input2); // Another add method


        builder.setMessage(R.string.dialog_new_event)
                .setView(layout)
                .setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String title = input.getText().toString();
                        String description = input2.getText().toString();
                        int contId = ((ViewGroup)getView().getParent()).getId();
                        DatePickerFragment datePickerFragment = new DatePickerFragment(title, description, idEmpresa, contId);
                        datePickerFragment.show(getParentFragmentManager(), "Selecciona la fecha");

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });//.show
        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home_member, container, false);
        LinearLayout linearLayout = root.findViewById(R.id.layout_member_home);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("usuario").child(uid).child("empresa").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String idEmpresa = snapshot.getValue(String.class);

                mDatabase.child("empresa").child(idEmpresa).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean esAdmin = snapshot.child("miembros").child(uid).getValue(Boolean.class);
                        if(esAdmin){
                            Button boton_add = root.findViewById(R.id.boton_add_evento);
                            boton_add.setVisibility(View.VISIBLE);
                            boton_add.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    tryAddEvento(root, idEmpresa);
                                }
                            });
                        }
                        Iterable<DataSnapshot> eventosSnap = snapshot.child("eventos").getChildren();
                        List<Evento> eventos = new ArrayList<>();
                        for(DataSnapshot eventoSnap : eventosSnap){
                            Evento evento = eventoSnap.getValue(Evento.class);
                            String idEvento = eventoSnap.getKey();
                            evento.setIdEvento(idEvento);
                            eventos.add(evento);
                        }
                        Collections.sort(eventos);
                        for(Evento event : eventos){
                            String idEvento = event.getIdEvento();
                            View eventoView = createEventoView(event, esAdmin, idEmpresa, idEvento);
                            linearLayout.addView(eventoView);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return root;
    }
}