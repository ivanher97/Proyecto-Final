package com.routeiq.controllers;

import com.routeiq.connection.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador encargado de realizar búsquedas manuales de estudios en base a múltiples criterios.
 */
public class ManualSearchController {

    // Campos de entrada de la interfaz
    @FXML private TextField carreteraPrincipalField;
    @FXML private TextField carreteraDestinoField;
    @FXML private TextField pkField;
    @FXML private ComboBox<String> transporteCombo;
    @FXML private ComboBox<String> direccionCombo;
    @FXML private ComboBox<String> tipoCombo;

    // Tabla y columnas para mostrar resultados
    @FXML private TableView<Estudio> tablaEstudios;
    @FXML private TableColumn<Estudio, String> colNumeroEstudio;
    @FXML private TableColumn<Estudio, String> colPlano;

    // Lista de datos para alimentar la tabla
    private final ObservableList<Estudio> datos = FXCollections.observableArrayList();

    /**
     * Inicializa el controlador al cargar la interfaz.
     * Configura combos, columnas de la tabla y carga los transportes desde la base de datos.
     */
    @FXML
    public void initialize() {
        // Dirección y tipo posibles
        direccionCombo.setItems(FXCollections.observableArrayList(
                "NORTE -> NORTE", "NORTE -> SUR", "NORTE -> ESTE", "NORTE -> OESTE",
                "SUR -> NORTE", "SUR -> SUR", "SUR -> ESTE", "SUR -> OESTE",
                "ESTE -> NORTE", "ESTE -> SUR", "ESTE -> ESTE", "ESTE -> OESTE",
                "OESTE -> OESTE", "OESTE -> ESTE"));

        tipoCombo.setItems(FXCollections.observableArrayList("enlace", "glorieta", "galibo", "curva", "salida"));

        // Asociar las columnas a las propiedades de la clase Estudio
        colNumeroEstudio.setCellValueFactory(new PropertyValueFactory<>("numeroEstudio"));
        colPlano.setCellValueFactory(new PropertyValueFactory<>("plano"));
        tablaEstudios.setItems(datos);

        // Añadir tooltip a la columna del plano para mostrar el texto completo
        colPlano.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    setTooltip(new Tooltip(item));
                }
            }
        });

        cargarTransportes();
    }

    /**
     * Carga todos los códigos de transporte desde la base de datos y los coloca en el combo.
     */
    private void cargarTransportes() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT CodTransporte FROM Transporte");
            ResultSet rs = stmt.executeQuery();

            ObservableList<String> transportes = FXCollections.observableArrayList();
            while (rs.next()) {
                transportes.add(rs.getString("CodTransporte").toUpperCase());
            }

            transporteCombo.setItems(transportes);
            conn.close();
        } catch (Exception e) {
            mostrarAlerta("Error al cargar transportes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cambia la activación de los campos dependiendo del tipo seleccionado.
     */
    @FXML
    private void actualizarCamposTipo() {
        String tipo = tipoCombo.getValue();
        if (tipo == null) return;

        boolean esEnlace = tipo.equals("enlace");
        boolean esOtro = tipo.equals("glorieta") || tipo.equals("galibo") || tipo.equals("curva") || tipo.equals("salida");

        carreteraDestinoField.setDisable(esOtro);     // Se habilita si es enlace
        pkField.setDisable(esEnlace);                 // El PK no se usa para enlaces
    }

    /**
     * Realiza una búsqueda de estudios aplicando los filtros ingresados.
     */
    @FXML
    private void buscarEstudios() {
        datos.clear(); // Limpiar tabla

        // Obtener criterios de búsqueda
        String principal = carreteraPrincipalField.getText().trim().toUpperCase();
        String destino = carreteraDestinoField.getText().trim().toUpperCase();
        String pk = pkField.getText().trim();
        String transporte = transporteCombo.getValue();
        String direccion = direccionCombo.getValue();
        String tipo = tipoCombo.getValue();

        // Validar que al menos un filtro esté presente
        if (principal.isEmpty() && destino.isEmpty() && pk.isEmpty() &&
                (transporte == null || transporte.isEmpty()) &&
                (direccion == null || direccion.isEmpty()) &&
                (tipo == null || tipo.isEmpty())) {
            mostrarAlerta("Introduce al menos un criterio de búsqueda.");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();

            // Construcción dinámica de la consulta con LIKE para buscar coincidencias en codPlano
            StringBuilder sql = new StringBuilder("SELECT CodEstudio, codPlano FROM Estudio_Plano WHERE 1=1 ");
            List<String> params = new ArrayList<>();

            if (!principal.isEmpty()) {
                sql.append("AND codPlano LIKE ? ");
                params.add("%" + principal + "%");
            }
            if (!destino.isEmpty()) {
                sql.append("AND codPlano LIKE ? ");
                params.add("%" + destino + "%");
            }
            if (!pk.isEmpty()) {
                sql.append("AND codPlano LIKE ? ");
                params.add("%" + pk.replace(",", ".") + "%");
            }
            if (direccion != null && !direccion.isEmpty()) {
                sql.append("AND codPlano LIKE ? ");
                params.add("%" + direccion.toUpperCase() + "%");
            }
            if (tipo != null && !tipo.isEmpty()) {
                sql.append("AND codPlano LIKE ? ");
                params.add("%" + tipo.toUpperCase().substring(0, 3) + "%");
            }
            if (transporte != null && !transporte.isEmpty()) {
                sql.append("AND codPlano LIKE ? ");
                params.add("%" + transporte + "%");
            }

            // Preparar y ejecutar la consulta
            PreparedStatement stmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                datos.add(new Estudio(
                        rs.getString("CodEstudio"),
                        rs.getString("codPlano")
                ));
            }

            if (datos.isEmpty()) {
                mostrarAlerta("No se encontraron estudios con esos criterios.");
            }

            conn.close();

        } catch (Exception e) {
            mostrarAlerta("Error al buscar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Permite volver a la pantalla anterior (búsqueda avanzada o principal).
     */
    @FXML
    private void volver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.routeiq/view/search_flat.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Muestra una alerta informativa en pantalla.
     */
    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Clase interna que representa los resultados mostrados en la tabla.
     */
    public static class Estudio {
        private final String numeroEstudio;
        private final String plano;

        public Estudio(String numeroEstudio, String plano) {
            this.numeroEstudio = numeroEstudio;
            this.plano = plano;
        }
    }
}
