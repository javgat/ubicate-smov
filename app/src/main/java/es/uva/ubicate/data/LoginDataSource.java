package es.uva.ubicate.data;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import es.uva.ubicate.data.model.LoggedInUser;
import es.uva.ubicate.persistence.FirebaseDAO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
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
            //Task<AuthResult> task = mAuth.createUserWithEmailAndPassword(username, password);

            Task<AuthResult> task = mAuth.signInWithEmailAndPassword(username, password);
            while(!task.isComplete()){} // Espera activa, no pasa nada pero ha de ser lanzado en background (si no falla si no hay conexion)
            if(task.isSuccessful()) {
                FirebaseUser fUser = task.getResult().getUser();
                LoggedInUser usuario = new LoggedInUser(fUser.getUid(),fUser.getDisplayName());
                /*Map<String, String> userData = new HashMap<String, String>();
                userData.put("Nombre", "fUser.getDisplayName()");
                userData.put("Email", fUser.getEmail());
                Map<String, Map> user = new HashMap<String, Map>();
                user.put(fUser.getUid(), userData);

                mDatabase.child("usuario").child(fUser.getUid()).setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "signIn:onComplete:" + task.isSuccessful());
                    }
                });*/

                //mDatabase.child("usuario").child(fUser.getUid()).push()
                        //setValue(usuario);
                return new Result.Success<>(usuario);
            }else{
                return new Result.Error(new IllegalArgumentException("Error de username"));
            }
        } catch (Exception e) {
            Log.d(TAG, "Error en login:" + e);
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public Result<LoggedInUser> loginGoogle(GoogleSignInAccount account){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        try {
            String idToken = account.getIdToken();
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            Task<AuthResult> task = mAuth.signInWithCredential(credential);
            while(!task.isComplete()){}// Espera activa, no pasa nada pero ha de ser lanzado en background (si no falla si no hay conexion)
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success");
                FirebaseUser fUser = mAuth.getCurrentUser();
                LoggedInUser usuario = new LoggedInUser(fUser.getUid(),fUser.getDisplayName());
                return new Result.Success<>(usuario);
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithCredential:failure", task.getException());
                return new Result.Error(new IllegalArgumentException("Error de login Google"));
            }
        }catch (Exception e) {
            Log.d(TAG, "Error en login con Google:" + e);
            return new Result.Error(new IOException("Error logging in con Google ", e));
        }
    }

    public Result<LoggedInUser> register(String name, String email, String password) {

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        try {
            //Task<AuthResult> task = mAuth.signInWithEmailAndPassword(username, password);
            Task<AuthResult> task = mAuth.createUserWithEmailAndPassword(email, password);
            while(!task.isComplete()){} // Espera activa, no pasa nada pero ha de ser lanzado en background (si no falla si no hay conexion)
            if(task.isSuccessful()) {
                FirebaseUser fUser = task.getResult().getUser();
                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                fUser.updateProfile(userProfileChangeRequest);
                LoggedInUser usuario = new LoggedInUser(fUser.getUid(),name);
                FirebaseDAO.setName(fUser.getUid(),name);
                /*Map<String, String> userData = new HashMap<String, String>();
                userData.put("Nombre", "fUser.getDisplayName()");
                userData.put("Email", fUser.getEmail());
                Map<String, Map> user = new HashMap<String, Map>();
                user.put(fUser.getUid(), userData);

                mDatabase.child("usuario").child(fUser.getUid()).setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "signIn:onComplete:" + task.isSuccessful());
                    }
                });*/

                //mDatabase.child("usuario").child(fUser.getUid()).push()
                //setValue(usuario);
                return new Result.Success<>(usuario);
            }else{
                return new Result.Error(new IllegalArgumentException("Error de username"));
            }
        } catch (Exception e) {
            Log.d(TAG, "Error en login:" + e);
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}