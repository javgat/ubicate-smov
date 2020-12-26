package es.uva.ubicate.ui.domain;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import es.uva.ubicate.R;

public class DomainFragment extends Fragment {

    private void createNoEmpresa(View root){

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        int fragment = R.layout.fragment_domain;
        View root = inflater.inflate(fragment, container, false);
        final TextView textView = root.findViewById(R.id.text_create_domain);
        createNoEmpresa(root);
        //textView.setText("Organizacion no disponible");
        return root;
    }
}
