package com.example.uf2_mobils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class RegisterFragment extends Fragment {
    private EditText emailEditText, passwordEditText;
    private Button registerButton;
    private TextView foto;
    private FirebaseAuth mAuth;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImage;

    NavController navController;

    String url;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);

        registerButton = view.findViewById(R.id.registerButton);
        foto = view.findViewById(R.id.foto);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crearCuenta();
            }
        });
        foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galeriPhoto();
            }
        });

        mAuth = FirebaseAuth.getInstance();

    }

    private void galeriPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImage = data.getData();
            // Handle the selected image URI here
        }
    }

    private void crearCuenta() {
        if (!validarFormulario()) {
            return;
        }

        registerButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            actualizarUI(mAuth.getCurrentUser());
                            if (selectedImage !=null) {
                                subirFoto(selectedImage, mAuth.getCurrentUser().getEmail().toString());
                                String imageFileName = mAuth.getCurrentUser().getEmail().toString().replace("@", "_").replace(".", "_");
                                url = "https://firebasestorage.googleapis.com/v0/b/uf2-mobils.appspot.com/o/profiles%2F" + imageFileName + "?alt=media&token=";
                            } else guardarUrl("https://i.imgur.com/H0N68ue.jpeg".toString());

                            String imageFileName = emailEditText.getText().toString().replace("@", "_").replace(".", "_");
                            User user = new User(mAuth.getCurrentUser().getEmail(),
                                    passwordEditText.getText().toString(),
                                    mAuth.getCurrentUser().getUid(),
                                    url);
                            FirebaseFirestore.getInstance().collection("users").document(user.getUID()).set(user);
                        } else {
                            // Manejar el error
                            Snackbar.make(requireView(), "Error: " + task.getException(), Snackbar.LENGTH_LONG).show();
                        }
                        registerButton.setEnabled(true);
                    }
                });
    }

    private void subirFoto(Uri imageUri, String userEmail) {
        String imageFileName = userEmail.replace("@", "_").replace(".", "_");
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("profiles/" + imageFileName);
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String photoUrl = uri.toString();
                        guardarUrl(photoUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(requireView(), "Error al cargar la foto: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }


    private void guardarUrl(String photoUrl) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        databaseRef.child("photoUrl").setValue(photoUrl)
                .addOnSuccessListener(aVoid -> {
                    registerButton.setEnabled(true);
                    navController.navigate(R.id.homeFragment);
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(requireView(), "Error al guardar la URL de la foto en la base de datos: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    registerButton.setEnabled(true);
                });
        url=photoUrl;
    }

    private void actualizarUI(FirebaseUser currentUser) {
        if(currentUser != null){
            navController.navigate(R.id.homeFragment);
        }
    }
    

    private boolean validarFormulario() {
        boolean valid = true;

        if (TextUtils.isEmpty(emailEditText.getText().toString())) {
            emailEditText.setError("Required.");
            valid = false;
        } else {
            emailEditText.setError(null);
        }

        if (TextUtils.isEmpty(passwordEditText.getText().toString())) {
            passwordEditText.setError("Required.");
            valid = false;
        } else {
            passwordEditText.setError(null);
        }

        return valid;
    }
}