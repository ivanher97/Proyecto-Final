package com.routeiq.controllers;

import com.routeiq.connection.DatabaseConnection;
import javafx.scene.control.Alert;
import org.mindrot.jbcrypt.BCrypt;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ComboBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controlador encargado de manejar la creación de nuevos usuarios en la aplicación.
 */
public class CreateUserController {

    // Elementos de la interfaz vinculados mediante FXML
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> rolCombo;

    /**
     * Método de inicialización del controlador.
     * Se configura el ComboBox con los roles disponibles.
     */
    @FXML
    public void initialize() {
        rolCombo.getItems().addAll("EMP", "ADM"); // EMP = Empleado, ADM = Administrador
    }

    /**
     * Método principal llamado al presionar el botón de crear usuario.
     * Realiza validaciones, comprueba duplicados y registra el usuario en la base de datos.
     */
    @FXML
    private void crearUsuario() {
        // Obtener valores ingresados por el usuario
        String email = emailField.getText().trim();
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        String rol = rolCombo.getValue();

        // Validación de campos obligatorios
        if (email.isEmpty() || user.isEmpty() || pass.isEmpty() || confirm.isEmpty() || rol == null) {
            mostrarAlerta("Todos los campos son obligatorios.");
            return;
        }

        // Validación del formato del correo electrónico
        if (!correoValido(email)) {
            mostrarAlerta("El correo no tiene un formato válido.");
            return;
        }

        // Validación de coincidencia de contraseñas
        if (!pass.equals(confirm)) {
            mostrarAlerta("Las contraseñas no coinciden.");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();

            // Comprobación de si el correo o nombre de usuario ya existen
            String checkSql = "SELECT * FROM Usuario WHERE Email = ? OR NombreUsuario = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, email);
            checkStmt.setString(2, user);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Se encontró un registro duplicado
                if (email.equalsIgnoreCase(rs.getString("Email"))) {
                    mostrarAlerta("El correo electrónico ya está registrado.");
                } else {
                    mostrarAlerta("El nombre de usuario ya está en uso.");
                }
                rs.close();
                checkStmt.close();
                conn.close();
                return;
            }

            rs.close();
            checkStmt.close();

            // Cifrado seguro de la contraseña utilizando BCrypt
            String hashedPassword = BCrypt.hashpw(pass, BCrypt.gensalt());

            // Inserción del nuevo usuario
            String insertSql = "INSERT INTO Usuario (NombreUsuario, Email, Contrasena, Rol) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, user);
            insertStmt.setString(2, email);
            insertStmt.setString(3, hashedPassword);
            insertStmt.setString(4, rol);

            int rowsInserted = insertStmt.executeUpdate();
            insertStmt.close();
            conn.close();

            if (rowsInserted > 0) {
                mostrarAlerta("Usuario creado exitosamente.");
                limpiarCampos(); // Limpiar el formulario tras el registro
            }

        } catch (SQLException e) {
            mostrarAlerta("Error al insertar en la base de datos:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Limpia los campos del formulario después de una acción exitosa.
     */
    private void limpiarCampos() {
        emailField.clear();
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        rolCombo.setValue(null);
    }

    /**
     * Valida si un correo tiene un formato válido utilizando expresión regular.
     */
    private boolean correoValido(String email) {
        return email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    /**
     * Método llamado al presionar el botón "Volver".
     * Redirige al menú principal de la aplicación.
     */
    @FXML
    private void volverAlMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.routeiq/view/menu.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Muestra una alerta informativa con el mensaje dado.
     */
    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
