package com.routeiq.controllers;

/**
 * Clase singleton que actúa como controlador de sesión.
 * Permite guardar y recuperar información del usuario durante la ejecución de la aplicación.
 */
public class SessionController {

    // Instancia única de la sesión (singleton)
    private static SessionController instance;

    // Rol del usuario autenticado (ADM, EMP, etc.)
    private String rolUsuario;

    /**
     * Constructor privado para evitar la creación directa de instancias.
     * Solo se accede mediante el método estático `getInstance()`.
     */
    private SessionController() {}

    /**
     * Devuelve la instancia única del controlador de sesión.
     * Si no existe, la crea.
     */
    public static SessionController getInstance() {
        if (instance == null) {
            instance = new SessionController();
        }
        return instance;
    }

    /**
     * Obtiene el rol del usuario actualmente en sesión.
     */
    public String getRolUsuario() {
        return rolUsuario;
    }

    /**
     * Establece el rol del usuario para esta sesión.
     */
    public void setRolUsuario(String rolUsuario) {
        this.rolUsuario = rolUsuario;
    }

    /**
     * Limpia la sesión actual, eliminando la instancia singleton.
     * Se llama, por ejemplo, al cerrar sesión o salir de la aplicación.
     */
    public void clearSession() {
        instance = null;
    }
}
