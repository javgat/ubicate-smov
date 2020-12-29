package es.uva.ubicate.ui.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import es.uva.ubicate.DrawerActivity;
import es.uva.ubicate.R;

public class RegisterActivity extends AppCompatActivity {


    private LoginViewModel loginViewModel;
    private String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        final EditText editName = findViewById(R.id.register_name);
        final EditText editEmail = findViewById(R.id.register_email);
        final EditText editPass = findViewById(R.id.register_password);
        final EditText editPass2 = findViewById(R.id.register_password2);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading_reg);
        final Button registerButton = findViewById(R.id.register_button);

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);


        loginViewModel.getRegisterFormState().observe(this, new Observer<RegisterFormState>() {
            @Override
            public void onChanged(@Nullable RegisterFormState registerFormState) {
                if (registerFormState == null) {
                    return;
                }
                registerButton.setEnabled(registerFormState.isDataValid());
                if (registerFormState.getUsernameError() != null) {
                    editName.setError(getString(registerFormState.getUsernameError()));
                }
                if (registerFormState.getEmailError() != null) {
                    editEmail.setError(getString(registerFormState.getEmailError()));
                }
                if (registerFormState.getPasswordError() != null) {
                    editPass.setError(getString(registerFormState.getPasswordError()));
                }
                if (registerFormState.getPassword2ErrorError() != null) {
                    editPass2.setError(getString(registerFormState.getPassword2ErrorError()));
                }
            }
        });

        // Hay que mirarla
        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showRegisterFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                    //}
                    //finActivity();
                }
                // Creo que finish solo si lo logra
            }
        });

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
                loginViewModel.registerDataChanged(editName.getText().toString(),
                        editEmail.getText().toString(),
                        editPass.getText().toString(),
                        editPass2.getText().toString());
            }
        };
        editName.addTextChangedListener(afterTextChangedListener);
        editEmail.addTextChangedListener(afterTextChangedListener);
        editPass.addTextChangedListener(afterTextChangedListener);
        editPass2.addTextChangedListener(afterTextChangedListener);
        editPass2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            // Para captar el enter en el password como un "hey quiero darle a login"
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.register(editName.getText().toString(),
                            editEmail.getText().toString(),
                            editPass.getText().toString());
                }
                return false;
            }
        });

        // pulsa register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.register(editName.getText().toString(),
                        editEmail.getText().toString(),
                        editPass.getText().toString());
            }
        });

    }

    // cuanto tiene exito
    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();

        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        Intent mainAct = new Intent(this, DrawerActivity.class);
        startActivity(mainAct);
    }

    // cuando falla el register
    private void showRegisterFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        setResult(Activity.RESULT_OK);
        finish();
        return super.onOptionsItemSelected(item);
    }
}