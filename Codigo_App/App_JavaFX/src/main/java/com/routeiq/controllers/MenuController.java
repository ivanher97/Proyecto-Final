package com.routeiq.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

/**
 * Controlador para la pantalla de menú principal.
 * Gestiona la navegación hacia otras vistas y controla el acceso según el rol del usuario.
 */
public class MenuController {

    // Botón de creación de usuarios, solo visible para administradores
    @FXML
    private Button crearUsuarioBtn;

    /**
     * Método llamado automáticamente al cargar la vista.
     * Verifica el rol del usuario en sesión y ajusta la visibilidad de los componentes.
     */
    @FXML
    public void initialize() {
        String rol = SessionController.getInstance().getRolUsuario(); // Obtener rol desde sesión

        // Si el usuario no es administrador, ocultar el botón de crear usuario
        if (!"ADM".equals(rol)) {
            crearUsuarioBtn.setDisable(true);
            crearUsuarioBtn.setVisible(false);
        }
    }

    /**
     * Navega a la vista de búsqueda de planos.
     */
    @FXML
    private void buscarPlanos(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.routeiq/view/search_flat.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Obtener ventana actual
            stage.setScene(scene); // Cargar nueva escena
        } catch (Exception e) {
            e.printStackTrace(); // Mostrar error por consola si ocurre
        }
    }

    /**
     * Navega a la vista de consulta de dimensiones.
     */
    @FXML
    private void consultarDimensiones(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.routeiq/view/dimension_consult.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Navega a la vista para crear un nuevo usuario.
     * Solo accesible si el botón no está desactivado (rol ADM).
     */
    @FXML
    private void irACrearUsuario(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.routeiq/view/create_user.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cierra la aplicación y limpia los datos de sesión.
     */
    @FXML
    private void salir(ActionEvent event) {
        SessionController.getInstance().clearSession(); // Limpia el rol del usuario
        Platform.exit(); // Cierra completamente la aplicación JavaFX
    }
}
