package es.uva.ubicate.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Patterns;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import java.util.HashMap;

import es.uva.ubicate.data.LoginRepository;
import es.uva.ubicate.data.Result;
import es.uva.ubicate.data.model.LoggedInUser;
import es.uva.ubicate.R;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<RegisterFormState> registerFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<RegisterFormState> getRegisterFormState() {
        return registerFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    private class LoginTask extends AsyncTask<String, Void,  Result<LoggedInUser>> {

        @Override
        protected Result<LoggedInUser> doInBackground(String... datos) {
            Result<LoggedInUser> result = loginRepository.login(datos[0], datos[1]);
            return result;
        }

        @Override
        protected void onPostExecute(Result<LoggedInUser> result){
            if (result instanceof Result.Success) {
                LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
            } else {
                loginResult.setValue(new LoginResult(R.string.login_failed));
            }
        }
    }

    // Intenta el logeo y guarda resultado en loginResult, y loginRepository ahora tiene al usuario
    public void login(String username, String password) {
        new LoginTask().execute(username, password);
        // can be launched in a separate asynchronous job
/*
        Result<LoggedInUser> result = loginRepository.login(username, password);

        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
        } else {
            loginResult.setValue(new LoginResult(R.string.login_failed));
        }*/
    }

    private class LoginGoogleTask extends AsyncTask<GoogleSignInAccount, Void,  Result<LoggedInUser>> {

        @Override
        protected Result<LoggedInUser> doInBackground(GoogleSignInAccount... accounts) {
            Result<LoggedInUser> result = loginRepository.loginGoogle(accounts[0]);
            return result;
        }

        @Override
        protected void onPostExecute(Result<LoggedInUser> result){
            if (result instanceof Result.Success) {
                LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
            } else {
                loginResult.setValue(new LoginResult(R.string.login_failed));
            }
        }
    }


    public void loginGoogle(GoogleSignInAccount account){
        new LoginGoogleTask().execute(account);
    }

    private class RegisterTask extends AsyncTask<String, Void,  Result<LoggedInUser>> {

        @Override
        protected Result<LoggedInUser> doInBackground(String... datos) {
            Result<LoggedInUser> result = loginRepository.register(datos[0], datos[1], datos[2]);
            return result;
        }

        @Override
        protected void onPostExecute(Result<LoggedInUser> result){
            if (result instanceof Result.Success) {
                LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
            } else {
                loginResult.setValue(new LoginResult(R.string.register_failed));
            }
        }
    }

    public void register(String name, String email, String password){
        new RegisterTask().execute(name, email, password);
    }

    // Modifica el LoginFormState para saber si los datos son validos
    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    public void registerDataChanged(String username, String email, String password, String password2){
        if (!isUserNameValid(username)) {
            registerFormState.setValue(new RegisterFormState(R.string.invalid_username, null, null, null));
        }else if(!isEmailValid(email)){
            registerFormState.setValue(new RegisterFormState(null, R.string.invalid_email, null, null));
        } else if (!isPasswordValid(password)) {
            registerFormState.setValue(new RegisterFormState(null, null, R.string.invalid_password, null));
        } else if(!isPassword2Valid(password, password2)) {
            registerFormState.setValue(new RegisterFormState(null, null, null, R.string.invalid_password2));
        }else{
            registerFormState.setValue(new RegisterFormState(true));
        }
    }

    // A placeholder username validation check
    // Requisitos nombre de usuario
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    private boolean isEmailValid(String email){
        if (email == null) {
            return false;
        }
        if(email.contains("@")){
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }else
            return false;
    }

    // A placeholder password validation check
    // Requisitos password
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    private boolean isPassword2Valid(String pass, String pass2){
        if(pass==null || pass2==null)
            return false;
        return pass.equals(pass2);
    }
}