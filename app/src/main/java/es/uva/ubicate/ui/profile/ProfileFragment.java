package es.uva.ubicate.ui.profile;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import es.uva.ubicate.DrawerActivity;
import es.uva.ubicate.R;
import es.uva.ubicate.persistence.FirebaseDAO;

public class ProfileFragment extends Fragment {

    private final String TAG = "ProfileFragment";

    private void updateValuesUI(View root){
        final TextView textName = root.findViewById(R.id.text_name);
        final TextView textEmail = root.findViewById(R.id.text_email);
        final FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        textName.setText(cUser.getDisplayName());
        textEmail.setText(cUser.getEmail());
    }

    private void updateUserDataDrawer(){
        ((DrawerActivity)getActivity()).updateUserDataDrawer();
    }

    private void tryEditarName(View root){
        final ProgressBar loadingProgressBar =  root.findViewById(R.id.loading_profile);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT );//| InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setMessage(R.string.dialog_edit_name)
                .setView(input)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadingProgressBar.setVisibility(View.VISIBLE);
                        String name = input.getText().toString();
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        FirebaseDAO.setName(mAuth.getCurrentUser().getUid(), name);
                        UserProfileChangeRequest change = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();
                        Task<Void> task = mAuth.getCurrentUser().updateProfile(change);
                        task.addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                loadingProgressBar.setVisibility(View.GONE);
                                if(task.isSuccessful()){
                                    Toast.makeText(getContext(), R.string.nombre_cambiado, Toast.LENGTH_LONG).show();
                                    updateValuesUI(root);
                                    updateUserDataDrawer();
                                }else{
                                    Toast.makeText(getContext(), R.string.updated_failure, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
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

    private void tryEditarEmail(View root){ //Como el de name pero comprobando que el tipo de dato es correct
        final ProgressBar loadingProgressBar =  root.findViewById(R.id.loading_profile);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        builder.setMessage(R.string.dialog_edit_email)
                .setView(input)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadingProgressBar.setVisibility(View.VISIBLE);
                        String email = input.getText().toString();
                        if (email.contains("@") && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                            Task<Void> task = mAuth.getCurrentUser().updateEmail(email);
                            task.addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    loadingProgressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), R.string.email_cambiado, Toast.LENGTH_LONG).show();
                                        updateValuesUI(root);
                                        updateUserDataDrawer();
                                    } else {
                                        Toast.makeText(getContext(), R.string.updated_failure, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }else{
                            loadingProgressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), R.string.invalid_email, Toast.LENGTH_LONG).show();
                        }
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

    private void tryEditarPassword(View root){
        final ProgressBar loadingProgressBar =  root.findViewById(R.id.loading_profile);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final EditText oldPass = new EditText(getContext());
        oldPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        oldPass.setHint("Contraseña actual");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Nueva contraseña");

        final EditText input2 = new EditText(getContext());
        input2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input2.setHint("Repite la nueva contraseña");

        Context context = getContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(oldPass);

        layout.addView(input); // Notice this is an add method
        layout.addView(input2); // Another add method

        builder.setMessage(R.string.dialog_edit_password)
                .setView(layout)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadingProgressBar.setVisibility(View.VISIBLE);
                        String passOld = oldPass.getText().toString();
                        String pass = input.getText().toString();
                        String pass2 = input2.getText().toString();
                        if(passOld!=null && pass!=null && pass2!=null && pass.equals(pass2)) {
                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                            FirebaseUser user = mAuth.getCurrentUser();
                            final String email = user.getEmail();
                            AuthCredential credential = EmailAuthProvider.getCredential(email,passOld);
                            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                  @Override
                                  public void onComplete(@NonNull Task<Void> task1) {
                                      if (task1.isSuccessful()) {
                                          Task<Void> task = user.updatePassword(pass);

                                          task.addOnCompleteListener(new OnCompleteListener<Void>() {
                                              @Override
                                              public void onComplete(@NonNull Task<Void> task) {
                                                  loadingProgressBar.setVisibility(View.GONE);
                                                  if (task.isSuccessful()) {
                                                      Toast.makeText(getContext(), R.string.pass_cambiado, Toast.LENGTH_LONG).show();
                                                      updateValuesUI(root);
                                                      updateUserDataDrawer();
                                                  } else {
                                                      Toast.makeText(getContext(), R.string.updated_failure, Toast.LENGTH_LONG).show();
                                                  }
                                              }
                                          });
                                      }else{
                                          loadingProgressBar.setVisibility(View.GONE);
                                          Toast.makeText(getContext(), R.string.auth_failed, Toast.LENGTH_LONG).show();
                                      }
                                  }
                            });
                        }else{
                            loadingProgressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), R.string.invalid_password2, Toast.LENGTH_LONG).show();
                        }
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

/*
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
    }*/

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        final TextView textName = root.findViewById(R.id.text_name);
        final TextView textEmail = root.findViewById(R.id.text_email);
        final ImageView imageName = root.findViewById(R.id.image_edit_name);
        final ImageView imageEmail = root.findViewById(R.id.image_edit_email);
        final Button button_password = root.findViewById(R.id.button_update);

        updateValuesUI(root);

        imageName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryEditarName(root);
            }
        });

        imageEmail.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                tryEditarEmail(root);
            }
        });

        button_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryEditarPassword(root);
            }
        });
/*
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
        });*/

        return root;
    }
}