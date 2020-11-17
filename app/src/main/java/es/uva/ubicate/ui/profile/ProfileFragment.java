package es.uva.ubicate.ui.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import es.uva.ubicate.R;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private final String TAG = "ProfileFragment";

    private void updateValuesUI(View root){
        final EditText editName = root.findViewById(R.id.editTextPersonName);
        final EditText editEmail = root.findViewById(R.id.editTextEmailAddress);
        final FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        editName.setText(cUser.getDisplayName());
        editEmail.setText(cUser.getEmail());
    }

    private void updateValuesDB(View root){

        final EditText editName = root.findViewById(R.id.editTextPersonName);
        final EditText editEmail = root.findViewById(R.id.editTextEmailAddress);
        final EditText editPassword = root.findViewById(R.id.editTextPassword);
        final FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();

        cUser.updateEmail(editEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User email address updated.");
                }
            }
        });
        cUser.updatePassword(editPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User password updated.");
                }
            }
        });
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(editName.getText().toString()).build();

        cUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            String updated = getString(R.string.updated_success);
                            Log.d(TAG, "User profile updated.");
                            Toast.makeText(getContext(), updated, Toast.LENGTH_LONG).show();
                        }else{
                            String updated = getString(R.string.updated_failure);
                            Log.d(TAG, "User profile failed to update.");
                            Toast.makeText(getContext(), updated, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        final EditText editName = root.findViewById(R.id.editTextPersonName);
        final EditText editEmail = root.findViewById(R.id.editTextEmailAddress);
        final EditText editPassword = root.findViewById(R.id.editTextPassword);
        final Button button_update = root.findViewById(R.id.button_update);

        profileViewModel.getProfileFormState().observe(getViewLifecycleOwner(), new Observer<ProfileFormState>() {
            @Override
            public void onChanged(@Nullable ProfileFormState profileFormState) {

                if (profileFormState == null) {
                    return;
                }
                button_update.setEnabled(profileFormState.isDataValid());
                if (profileFormState.getNameError() != null) {
                    editName.setError(getString(profileFormState.getNameError()));
                }
                if (profileFormState.getEmailError() != null) {
                    editEmail.setError(getString(profileFormState.getEmailError()));
                }
                if (profileFormState.getPasswordError() != null) {
                    editPassword.setError(getString(profileFormState.getPasswordError()));
                }
            }
        });
        updateValuesUI(root);

        // Actualiza el loginViewModel
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                profileViewModel.profileDataChanged(editName.getText().toString(),
                        editEmail.getText().toString(), editPassword.getText().toString());
            }
        };
        editName.addTextChangedListener(afterTextChangedListener);
        editEmail.addTextChangedListener(afterTextChangedListener);
        editPassword.addTextChangedListener(afterTextChangedListener);

        button_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateValuesDB(root);
            }
        });

        return root;
    }
}