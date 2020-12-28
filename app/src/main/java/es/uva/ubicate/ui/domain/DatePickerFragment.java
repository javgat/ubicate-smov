package es.uva.ubicate.ui.domain;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.widget.DatePicker;

import java.text.DateFormat;
import java.util.Calendar;

import es.uva.ubicate.data.model.Evento;
import es.uva.ubicate.persistence.FirebaseDAO;
import es.uva.ubicate.ui.home.HomeFragment;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private String title, description, idEmpresa;
    private int contId;

    private void changeFragment(Fragment F, int contId){
        final FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        ft.replace(contId, F);
        ft.commit();
    }

    private void reloadHome(){
        changeFragment(new HomeFragment(), contId);
    }

    public DatePickerFragment(String title, String description, String idEmpresa, int contId){
        this.title = title;
        this.description = description;
        this.idEmpresa = idEmpresa;
        this.contId = contId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar mCalender = Calendar.getInstance();
        mCalender.set(Calendar.YEAR, year);
        mCalender.set(Calendar.MONTH, month);
        mCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String selectedDate = DateFormat.getDateInstance(DateFormat.FULL).format(mCalender.getTime());
        Evento evento = new Evento(title, description, selectedDate);
        FirebaseDAO.addEvento(idEmpresa, evento);
        reloadHome();
    }
}