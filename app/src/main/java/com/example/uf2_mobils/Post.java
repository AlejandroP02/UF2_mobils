package com.example.uf2_mobils;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase que representa una publicación en la aplicación.
 */
public class Post {

    /**
     * Identificador único del usuario que
     * realizó la publicación.
     */
    public String uid;

    /**
     * Nombre del autor de la publicación.
     */
    public String author;

    /**
     * URL de la foto del autor de la publicación.
     */
    public String authorPhotoUrl;

    /**
     * Contenido de la publicación.
     */
    public String content;

    /**
     * URL del media adjunto a la publicación.
     */
    public String mediaUrl;

    /**
     * Tipo de media adjunto (imagen, video, audio).
     */
    public String mediaType;

    /**
     * Mapa que almacena los "me gusta" de la publicación.
     */
    public Map<String, Boolean> likes = new HashMap<>();

    /**
     * Marca de tiempo de la publicación.
     */
    public long timeStamp;

    /**
     * Constructor vacio requerido por Firestore.
     */
    public Post() {}

    /**
     * Constructor de la clase Post.
     * @param uid Identificador único del usuario que realizó la publicación.
     * @param author Nombre del autor de la publicación.
     * @param authorPhotoUrl URL de la foto del autor de la publicación.
     * @param content Contenido de la publicación.
     * @param mediaUrl URL del medio adjunto a la publicación.
     * @param mediaType Tipo de medio adjunto (imagen, video, audio).
     * @param timeStamp Marca de tiempo de la publicación.
     */
    public Post(String uid, String author, String authorPhotoUrl, String content, String mediaUrl, String mediaType, long timeStamp) {
        this.uid = uid;
        this.author = author;
        this.authorPhotoUrl = authorPhotoUrl;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.timeStamp = timeStamp;
    }
}