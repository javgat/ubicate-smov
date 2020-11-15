package es.uva.ubicate.data;

import android.util.Log;

import androidx.annotation.NonNull;

import es.uva.ubicate.data.model.LoggedInUser;

import java.io.IOException;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private String TAG = "LoginDataSource";

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    public Result<LoggedInUser> login(String username, String password) {

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        try {
            // TODO: handle loggedInUser authentication
            // AUTENTIFICACION SE HACE AQUI
            // Pongo esto para poder observar que pasa cuando no funciona
            Task<AuthResult> task = mAuth.createUserWithEmailAndPassword(username, password);
            //Task<AuthResult> task = mAuth.signInWithEmailAndPassword(username, password);
            /*.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d(TAG, "signIn:onComplete:" + task.isSuccessful());
                    //hideProgressBar();

                    if (task.isSuccessful()) {
                        onAuthSuccess(task.getResult().getUser());
                    } else {
                        //Toast.makeText(SignInActivity.this, "Sign In Failed",
                        //        Toast.LENGTH_SHORT).show();
                    }
                }
            });*/
            while(!task.isComplete()){}
            if(task.isSuccessful()) {
                FirebaseUser fUser = task.getResult().getUser();
                LoggedInUser usuario = new LoggedInUser(fUser.getUid(),fUser.getDisplayName());
                //mDatabase.child("usuario").child(fUser.getUid()).child("username").setValue(usuario.getDisplayName());
                        //setValue(usuario);
                return new Result.Success<>(usuario);
            }else{
                return new Result.Error(new IllegalArgumentException("Error de username"));
            }
        } catch (Exception e) {
            Log.d(TAG, "Error en registro:" + e);
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}