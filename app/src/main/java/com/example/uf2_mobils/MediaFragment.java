package com.example.uf2_mobils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

/**
 * Fragmento que muestra los media (imagen, video o audio)
 * asociados a una publicación.
 */
public class MediaFragment extends Fragment {
    /**
     * Para poder visualizar videos.
     */
    VideoView videoView;
    /**
     * Para poder visualizar imagenes.
     */
    ImageView imageView;
    /**
     * Para poder llamar a los metodos del
     * viewModel.
     */
    public AppViewModel appViewModel;

    /**
     * Constructor vacío requerido para instanciar el fragmento.
     */
    public MediaFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el diseño para este fragmento
        return inflater.inflate(R.layout.fragment_media, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Inicialización del ViewModel
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);

        // Inicialización de vistas
        imageView = view.findViewById(R.id.imageView);
        videoView = view.findViewById(R.id.videoView);

        // Observador para actualizar la vista según el medio seleccionado
        appViewModel.postSeleccionado.observe(getViewLifecycleOwner(), post ->
        {
            if ("video".equals(post.mediaType) || "audio".equals(post.mediaType)) {
                // Configuración para video o audio
                MediaController mc = new MediaController(requireContext());
                mc.setAnchorView(videoView);
                videoView.setMediaController(mc);
                videoView.setVideoPath(post.mediaUrl);
                videoView.start();
            } else if ("image".equals(post.mediaType)) {
                // Cargar imagen
                Glide.with(requireView()).load(post.mediaUrl).into(imageView);
            }
        });
    }
}