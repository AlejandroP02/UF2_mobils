package com.example.uf2_mobils;

import com.google.firebase.firestore.PropertyName;

/**
 * Clase modelo para representar un usuario.
 */
public class User {

    /**
     * Correo del usuario.
     */
    private String email;

    /**
     * Nombre del usuario-
     */
    private String name;

    /**
     * Contraseña del usuario.
     */
    private String password;

    /**
     * Imagen del usuario.
     */
    private String mediaUri;

    /**
     * Id del usuario.
     */
    private String UID;

    /**
     * Constructor vacío requerido para
     * la deserialización desde Firestore.
     */
    public User() {
        // Necesario para la deserialización desde Firestore
    }

    /**
     * Constructor de la clase User.
     * @param email Correo electrónico del usuario.
     * @param password Contraseña del usuario.
     * @param UID Identificador único del usuario.
     * @param mediaUri URI de los medios asociados al usuario.
     */
    public User(String email, String password, String UID, String mediaUri) {
        this.email = email;
        this.name = getName(email);
        this.password = password;
        this.mediaUri = mediaUri;
        this.UID = UID;
    }

    /**
     * Constructor de la clase User.
     * @param email Correo electrónico del usuario.
     * @param password Contraseña del usuario.
     * @param UID Identificador único del usuario.
     * @param mediaUri URI de los medios asociados al usuario.
     * @param name Nombre del usuario.
     */
    public User(String email,String password,String UID, String mediaUri, String name) {
        this.email = email;
        this.name = name;
        this.mediaUri = mediaUri;
        this.UID = UID;
    }

    // Getters y setters de la clase usuario.
    @PropertyName("email")
    public String getEmail() {
        return email;
    }

    @PropertyName("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @PropertyName("name")
    public String getName() {
        return name;
    }

    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("password")
    public String getPassword() {
        return password;
    }

    @PropertyName("password")
    public void setPassword(String password) {
        this.password = password;
    }

    @PropertyName("mediaUri")
    public String getMediaUri() {
        return mediaUri;
    }

    @PropertyName("mediaUri")
    public void setMediaUri(String mediaUri) {
        this.mediaUri = mediaUri;
    }

    @PropertyName("UID")
    public String getUID() {
        return UID;
    }

    @PropertyName("UID")
    public void setUID(String UID) {
        this.UID = UID;
    }

    /**
     * Método privado para obtener el nombre
     * de usuario a partir del correo electrónico.
     * @param email Correo electrónico del usuario.
     * @return Nombre de usuario.
     */
    private String getName(String email) {
        String temp[] = email.split("@");
        String userName = temp[0];
        return userName;
    }
}
