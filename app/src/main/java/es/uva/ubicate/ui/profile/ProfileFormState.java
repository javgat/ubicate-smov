package es.uva.ubicate.ui.profile;

import androidx.annotation.Nullable;

/**
 * Data validation state of the profile form.
 */
class ProfileFormState {
    @Nullable
    private Integer nameError;
    @Nullable
    private Integer emailError;
    @Nullable
    private Integer passwordError;
    private boolean isDataValid;

    ProfileFormState(@Nullable Integer nameError, @Nullable Integer emailError, @Nullable Integer passwordError) {
        this.nameError = nameError;
        this.emailError = emailError;
        this.passwordError = passwordError;
        this.isDataValid = false;
    }

    ProfileFormState(boolean isDataValid) {
        this.nameError = null;
        this.emailError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    Integer getNameError() {
        return nameError;
    }

    @Nullable
    Integer getEmailError() {
        return emailError;
    }

    @Nullable
    Integer getPasswordError() { return passwordError; }

    boolean isDataValid() {
        return isDataValid;
    }
}