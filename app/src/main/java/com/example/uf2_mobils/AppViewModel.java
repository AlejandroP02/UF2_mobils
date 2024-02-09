package com.example.uf2_mobils;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.*;

/**
 * Clase que representa el ViewModel de la aplicación.
 * Extiende de AndroidViewModel para mantener los datos
 * relacionados con la UI a lo largo de los ciclos de vida de la aplicación.
 * Esta clase define un objeto Media que contiene una URI y un tipo de media.
 */
public class AppViewModel extends AndroidViewModel {

    /**
     * Clase interna que representa un objeto Media.
     */
    public static class Media {
        /**
         * La URI del archivo recibido.
         */
        public Uri uri;
        /**
         * El tipo de media (por ejemplo, "imagen", "video", etc.).
         */
        public String tipo;

        /**
         * Constructor de la clase Media.
         * @param uri  La URI del media.
         * @param tipo El tipo de media.
         */
        public Media(Uri uri, String tipo) {
            this.uri = uri;
            this.tipo = tipo;
        }
    }

    /**
     * LiveData que contiene el post seleccionado.
     */
    public MutableLiveData<Post> postSeleccionado = new MutableLiveData<>();

    /**
     * LiveData que contiene el media seleccionado.
     */
    public MutableLiveData<Media> mediaSeleccionado = new MutableLiveData<>();

    /**
     * Constructor de la clase AppViewModel.
     * @param application La aplicación Android.
     */
    public AppViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Método para establecer el media seleccionado.
     * @param uri  La URI del media.
     * @param type El tipo de media.
     */
    public void setMediaSeleccionado(Uri uri, String type) {
        mediaSeleccionado.setValue(new Media(uri, type));
    }
}