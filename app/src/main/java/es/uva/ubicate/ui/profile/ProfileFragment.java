package es.uva.ubicate.ui.profile;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import es.uva.ubicate.DrawerActivity;
import es.uva.ubicate.R;
import es.uva.ubicate.persistence.FirebaseDAO;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private final String TAG = "ProfileFragment";

    private static final int PICK_IMAGE = 100;
    private static final int REQ_STORAGE = 200;
    private View raiz;

    private void tryBorrarImage(StorageReference imageRef){

        final ProgressBar loadingProgressBar =  raiz.findViewById(R.id.loading_profile);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.dialog_delete_image)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadingProgressBar.setVisibility(View.VISIBLE);
                        FirebaseDAO.borrarImagen(imageRef).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(getContext(), R.string.image_uploaded, Toast.LENGTH_LONG).show();
                                    updateUserDataDrawer();
                                    updateValuesUI(raiz);
                                }
                                loadingProgressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void updateValuesUI(View root){
        final TextView textName = root.findViewById(R.id.text_name);
        final TextView textEmail = root.findViewById(R.id.text_email);
        final FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        textName.setText(cUser.getDisplayName());
        textEmail.setText(cUser.getEmail());

        final Button borrar_image = root.findViewById(R.id.boton_borrar_imagen);
        borrar_image.setVisibility(View.GONE);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imagesRef = storageRef.child("images");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference profilePicture = imagesRef.child(uid+".jpg");
        final long ONE_MEGABYTE = 1024 * 1024;
        profilePicture.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                borrar_image.setVisibility(View.VISIBLE);
                borrar_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tryBorrarImage(profilePicture);
                    }
                });
            }
        });
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

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){

            final ProgressBar loadingProgressBar =  raiz.findViewById(R.id.loading_profile);
            loadingProgressBar.setVisibility(View.VISIBLE);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference imagesRef = storageRef.child("images");
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference profilePicture = imagesRef.child(uid+".jpg");


            Uri imageUri = data.getData();
            ImageView imageView = new ImageView(getContext());
            imageView.setImageURI(imageUri);

            //imageView.setDrawingCacheEnabled(true);
            //imageView.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] datas = baos.toByteArray();

            UploadTask uploadTask = profilePicture.putBytes(datas);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d(TAG, "Fallo en actualizar imagen" + exception.toString());
                    Toast.makeText(getContext(), R.string.image_fail, Toast.LENGTH_LONG).show();
                    loadingProgressBar.setVisibility(View.GONE);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "Exito 8) en actualizar imagen");
                    Toast.makeText(getContext(), R.string.image_uploaded, Toast.LENGTH_LONG).show();
                    updateUserDataDrawer();
                    updateValuesUI(raiz);
                    loadingProgressBar.setVisibility(View.GONE);
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                }
            });

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case REQ_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    tryEditarImage();
                }  else {
                    Toast.makeText(getContext(), R.string.almacenamiento_no_activa, Toast.LENGTH_LONG).show();
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }

    private void tryEditarImage(){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQ_STORAGE);//??
        }else
            openGallery();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        raiz = root;
        final TextView textName = root.findViewById(R.id.text_name);
        final TextView textEmail = root.findViewById(R.id.text_email);
        final ImageView imageName = root.findViewById(R.id.image_edit_name);
        final ImageView imageEmail = root.findViewById(R.id.image_edit_email);
        final Button button_password = root.findViewById(R.id.button_update);
        final Button button_image = root.findViewById(R.id.button_image_profile);

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

        button_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryEditarImage();
            }
        });

        return root;
    }
}