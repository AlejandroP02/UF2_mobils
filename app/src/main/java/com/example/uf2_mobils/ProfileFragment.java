package com.example.uf2_mobils;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.UUID;

public class ProfileFragment extends Fragment {

    ImageView photoImageView;
    TextView displayNameTextView, emailTextView;
    Button editProfile, saveData, editPhoto;
    private View rootView;
    private String userName, userEmail, userPhoto, mediaTipo;
    public AppViewModel appViewModel;
    private Uri mediaUri;
    private FirebaseAuth mAuth;



    public ProfileFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rootView = view; // Asigna la vista actual a rootView

        photoImageView = view.findViewById(R.id.photoImageView);
        displayNameTextView = view.findViewById(R.id.displayNameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        editProfile = view.findViewById(R.id.editProfile);
        saveData = view.findViewById(R.id.enviar);
        editPhoto = view.findViewById(R.id.editPhoto);
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        mAuth = FirebaseAuth.getInstance();
        NavController navController = Navigation.findNavController(view);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            String name = user.getEmail().split("@")[0].toString();
            if (user.getDisplayName() != null){
                displayNameTextView.setText(user.getDisplayName());
            }
            else {
                displayNameTextView.setText(name);
            }
            emailTextView.setText(user.getEmail());
            if (user.getPhotoUrl() == null){
                Glide.with(requireView()).load(R.drawable.user).into(photoImageView);
            } else {
                Glide.with(requireView()).load(user.getPhotoUrl()).into(photoImageView);
            }
            DocumentReference userFromFirebase = FirebaseFirestore.getInstance().collection("users").document(user.getUid());
            userFromFirebase.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User usuario = documentSnapshot.toObject(User.class);

                    displayNameTextView.setText(usuario.getName());
                    emailTextView.setText(usuario.getEmail());
                    Glide.with(requireView()).load(usuario.getMediaUri()).circleCrop().into(photoImageView);
                }
            });
        }
        else{

        }

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cambiar TextView 1 a EditText
                convertToEditText(displayNameTextView);

                saveData.setVisibility(View.VISIBLE);
                editPhoto.setVisibility(View.VISIBLE);
                editProfile.setVisibility(View.GONE);
            }
        });

        saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener referencias a los EditText desde el rootView
                EditText displayNameEditText = rootView.findViewById(R.id.displayNameTextView);

                // Guardar los cambios en las variables locales
                userName = displayNameEditText.getText().toString();
                userEmail = emailTextView.getText().toString();
                pujaIguardarEnFirestore(userName);

                navController.navigate(R.id.profileFragment);

            }
        });
        view.findViewById(R.id.editPhoto).setOnClickListener(v -> seleccionarImagen());

        appViewModel.mediaSeleccionado.observe(getViewLifecycleOwner(), media -> {
            this.mediaTipo = media.tipo;
            this.mediaUri = media.uri;
            Glide.with(this).load(media.uri).circleCrop().into((ImageView) view.findViewById(R.id.photoImageView));
        });


    }


    private void convertToEditText(View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            // Obtener el texto actual
            String text = textView.getText().toString();

            // Crear un nuevo EditText con el mismo texto
            EditText editText = new EditText(getContext());
            editText.setText(text);

            // Copiar propiedades del TextView al EditText
            editText.setLayoutParams(textView.getLayoutParams());
            editText.setId(textView.getId());
            editText.setPadding(textView.getPaddingLeft(), textView.getPaddingTop(),
                    textView.getPaddingRight(), textView.getPaddingBottom());

            // Obtener el contenedor principal y reemplazar la vista
            ViewGroup parentLayout = (ViewGroup) textView.getParent();
            int index = parentLayout.indexOfChild(textView);
            parentLayout.removeView(textView);
            parentLayout.addView(editText, index);
        }
    }

    private final ActivityResultLauncher<String> galeria =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                appViewModel.setMediaSeleccionado(uri, mediaTipo);
            });

    private void seleccionarImagen() {
        mediaTipo = "image";
        galeria.launch("image/*");
    }

    private void pujaIguardarEnFirestore(final String postText) {
        FirebaseStorage.getInstance().getReference(mediaTipo + "/" +
                        UUID.randomUUID())
                .putFile(mediaUri) .continueWithTask(task ->
                        task.getResult().getStorage().getDownloadUrl())
                .addOnSuccessListener(url -> selectPhoto(url.toString()));
    }

    private void selectPhoto(String uri){
        userPhoto = uri;
        User user = new User(userEmail,null,mAuth.getCurrentUser().getUid(),userPhoto,userName);
        FirebaseFirestore.getInstance().collection("users").document(user.getUID()).set(user);

    }
}