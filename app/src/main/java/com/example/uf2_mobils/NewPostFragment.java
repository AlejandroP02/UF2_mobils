package com.example.uf2_mobils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import firebase.com.protolitewrapper.BuildConfig;

/**
 * Fragmento para crear una nueva publicación.
 */
public class NewPostFragment extends Fragment {

    /**
     * Para publicar el post.
     */
    Button publishButton;
    /**
     * Para editar el contenido del post.
     */
    EditText postConentEditText;
    /**
     * Para poder navegar entre pantallas.
     */
    private NavController navController;
    /**
     * Para poder llamar a los metodos del
     * viewModel.
     */
    public AppViewModel appViewModel;

    /**
     * Contiene la información de el media.
     */
    Uri mediaUri;
    /**
     * Contiene el tipo de media.
     */
    String mediaTipo;

    /**
     * Constructor vacío requerido para instanciar el fragmento.
     */
    public NewPostFragment() {}

    /**
     * Método estático para crear una nueva instancia de NewPostFragment.
     * @return Una nueva instancia de NewPostFragment.
     */
    public static NewPostFragment newInstance() {
        NewPostFragment fragment = new NewPostFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el diseño para este fragmento
        return inflater.inflate(R.layout.fragment_new_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        publishButton = view.findViewById(R.id.publishButton);
        postConentEditText = view.findViewById(R.id.postContentEditText);
        navController = Navigation.findNavController(view);

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publicar();
            }
        });

        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);

        // multimedia
        view.findViewById(R.id.camara_fotos).setOnClickListener(v -> tomarFoto());
        view.findViewById(R.id.camara_video).setOnClickListener(v -> tomarVideo());
        view.findViewById(R.id.grabar_audio).setOnClickListener(v -> grabarAudio());
        view.findViewById(R.id.imagen_galeria).setOnClickListener(v ->
                seleccionarImagen());
        view.findViewById(R.id.video_galeria).setOnClickListener(v ->
                seleccionarVideo());
        view.findViewById(R.id.audio_galeria).setOnClickListener(v ->
                seleccionarAudio());
        appViewModel.mediaSeleccionado.observe(getViewLifecycleOwner(), media -> {
            this.mediaUri = media.uri;
            this.mediaTipo = media.tipo;
            Glide.with(this).load(media.uri).into((ImageView) view.findViewById(R.id.previsualizacion));
        });
    }

    /**
     * Método para publicar la nueva publicación.
     */
    private void publicar() {
        String postContent = postConentEditText.getText().toString();
        if(TextUtils.isEmpty(postContent)){
            postConentEditText.setError("Required");
            return;
        }
        publishButton.setEnabled(false);
        if (mediaTipo == null) {
            guardarEnFirestore(postContent, null);
        }
        else
        {
            pujaIguardarEnFirestore(postContent);
        }
    }

    /**
     * Método para guardar la publicación en Firestore.
     * @param postContent Contenido de la publicación.
     * @param mediaUrl URL del medio adjunto a la publicación.
     */
    private void guardarEnFirestore(String postContent, String mediaUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String photo="";
        //if (user.getPhotoUrl() != null){
        //    photo = user.getPhotoUrl().toString();
        //}
        String displayName = user.getDisplayName();

        if (user.getPhotoUrl() == null){
            photo = null;
        }else {
            photo = user.getPhotoUrl().toString();
        }

        if (user.getDisplayName() == null){
            displayName = user.getEmail().toString();
        }else {
            displayName = user.getDisplayName().toString();
        }
        Post post = new Post(user.getUid(), displayName, photo, postContent, mediaUrl, mediaTipo, System.currentTimeMillis());
        FirebaseFirestore.getInstance().collection("posts")
                .add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        navController.popBackStack();
                        appViewModel.setMediaSeleccionado( null, null);
                    }
                });
    }

    /**
     * Método para subir el archivo y guardar la
     * publicación en Firestore.
     * @param postText Contenido de la publicación.
     */
    private void pujaIguardarEnFirestore(final String postText) {
        FirebaseStorage.getInstance().getReference(mediaTipo + "/" +
                        UUID.randomUUID())
                .putFile(mediaUri)
                .continueWithTask(task ->
                        task.getResult().getStorage().getDownloadUrl())
                .addOnSuccessListener(url -> guardarEnFirestore(postText,
                        url.toString()));
    }

    // Métodos para la selección de medios y captura de contenido multimedia

    /**
     * Permite abrir la galeria del dispositivo.
     */
    private final ActivityResultLauncher<String> galeria =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                appViewModel.setMediaSeleccionado(uri, mediaTipo);
            });

    /**
     * Permite abrir la camara del dispositivo
     * para realizar una foto.
     */
    private final ActivityResultLauncher<Uri> camaraFotos =
            registerForActivityResult(new ActivityResultContracts.TakePicture(),
                    isSuccess -> {
                        appViewModel.setMediaSeleccionado(mediaUri, "image");
                    });

    /**
     * Permite abrir la camara del dispositivo
     * para realizar una video.
     */
    private final ActivityResultLauncher<Uri> camaraVideos =
            registerForActivityResult(new ActivityResultContracts.TakeVideo(), isSuccess
                    -> {
                appViewModel.setMediaSeleccionado(mediaUri, "video");
            });

    /**
     * Permite abrir la grabadora del dispositivo.
     */
    private final ActivityResultLauncher<Intent> grabadoraAudio =
            registerForActivityResult(new
                    ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    appViewModel.setMediaSeleccionado(result.getData().getData(),
                            "audio");
                }
            });

    /**
     * Permite seleccionar una imagen.
     */
    private void seleccionarImagen() {
        mediaTipo = "image";
        galeria.launch("image/*");
    }

    /**
     * Permite seleccionar un video.
     */
    private void seleccionarVideo() {
        mediaTipo = "video";
        galeria.launch("video/*");
    }

    /**
     * Permite seleccionar un audio.
     */
    private void seleccionarAudio() {
        mediaTipo = "audio";
        galeria.launch("audio/*");
    }

    /**
     * Permite realizar una foto.
     */
    private void tomarFoto() {
        try {
            mediaUri = FileProvider.getUriForFile(requireContext(),
                    BuildConfig.APPLICATION_ID + ".fileprovider", File.createTempFile("img",
                            ".jpg",
                            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)));
            camaraFotos.launch(mediaUri);
        } catch (IOException e) {}
    }

    /**
     * Permite realizar un video.
     */
    private void tomarVideo() {
        try {
            mediaUri = FileProvider.getUriForFile(requireContext(),
                    BuildConfig.APPLICATION_ID + ".fileprovider", File.createTempFile("vid",
                            ".mp4",
                            requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES)));
            camaraVideos.launch(mediaUri);
        } catch (IOException e) {}
    }

    /**
     * Permite grabar un audio.
     */
    private void grabarAudio() {
        grabadoraAudio.launch(new
                Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION));
    }

}