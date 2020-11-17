package es.uva.ubicate.ui.profile;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import es.uva.ubicate.R;

public class ProfileViewModel extends ViewModel {

    private MutableLiveData<ProfileFormState> mPFS;

    public ProfileViewModel() {
        mPFS = new MutableLiveData<>();
        mPFS.setValue(new ProfileFormState(false));
    }

    public LiveData<ProfileFormState> getProfileFormState() {
        return mPFS;
    }

    private boolean isNameValid(String name){
        return !name.isEmpty();
    }

    private boolean isEmailValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    public void profileDataChanged(String name, String email, String password) {

        if (!isNameValid(name)) {
            mPFS.setValue(new ProfileFormState(R.string.invalid_name, null, null));
        } else if (!isEmailValid(email)) {
            mPFS.setValue(new ProfileFormState(null, R.string.invalid_username, null));
        } else if (!isPasswordValid(password)){
            mPFS.setValue(new ProfileFormState(null, null, R.string.invalid_password));
        }else {
            mPFS.setValue(new ProfileFormState(true));
        }
    }
}