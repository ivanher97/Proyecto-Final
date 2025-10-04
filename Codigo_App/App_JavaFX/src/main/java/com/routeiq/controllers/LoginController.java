package com.routeiq.controllers;

import com.routeiq.connection.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Controlador encargado de manejar la lógica de inicio de sesión del usuario.
 */
public class LoginController {

    // Campos de entrada para el nombre de usuario y la contraseña
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    /**
     * Método ejecutado al hacer clic en el botón de "Iniciar sesión".
     * Verifica credenciales y redirige al menú si son correctas.
     */
    @FXML
    private void onLoginClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validación de campos vacíos
        if (username.isEmpty() || password.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos", "Debes ingresar usuario y contraseña.");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();

            // Consulta para obtener la contraseña cifrada y el rol del usuario
            String sql = "SELECT Contrasena, Rol FROM Usuario WHERE NombreUsuario = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("Contrasena");
                String rol = rs.getString("Rol");

                // Validación de la contraseña con BCrypt
                if (BCrypt.checkpw(password, hashedPassword)) {
                    System.out.println("Login exitoso");

                    // Guardar el rol del usuario en la sesión
                    SessionController.getInstance().setRolUsuario(rol);

                    // Cargar la pantalla principal
                    URL url = getClass().getResource("/com.routeiq/view/menu.fxml");
                    FXMLLoader loader = new FXMLLoader(url);
                    Scene scene = new Scene(loader.load());

                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.setScene(scene);

                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error de inicio", "Contraseña incorrecta.");
                }

            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Usuario no encontrado", "El usuario no existe en la base de datos.");
            }

            rs.close();
            stmt.close();

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de conexión", "No se pudo acceder a la base de datos.");
            e.printStackTrace();
        }
    }

    /**
     * Método asociado al botón "Cambiar contraseña".
     * Actualmente no implementado (funcionalidad simulada).
     */
    @FXML
    private void onChangePassword() {
        mostrarAlerta(Alert.AlertType.INFORMATION, "Función no implementada", "Aquí iría la pantalla para cambiar contraseña.");
    }

    /**
     * Método de utilidad para mostrar una alerta al usuario.
     * @param tipo Tipo de alerta (ERROR, WARNING, etc.)
     * @param titulo Título de la ventana de alerta
     * @param mensaje Contenido del mensaje
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Método de acceso rápido para desarrolladores.
     * Simula un inicio de sesión directo sin credenciales.
     */
    @FXML
    private void loginDesarrollador() {
        try {
            String rol = "ADM"; // Rol predefinido. Puede cambiarse por "EMP" para simular otro perfil

            // Guardar rol en la sesión sin autenticación
            SessionController.getInstance().setRolUsuario(rol);

            // Cargar directamente la pantalla principal
            URL url = getClass().getResource("/com.routeiq/view/menu.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo cargar la pantalla principal.");
            e.printStackTrace();
        }
    }
}
