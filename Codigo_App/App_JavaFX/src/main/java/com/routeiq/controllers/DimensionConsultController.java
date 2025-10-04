package com.routeiq.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador encargado de consultar, crear y actualizar dimensiones de registros en la tabla Enlace.
 */
public class DimensionConsultController {

    // Campos de entrada de usuario (formulario)
    @FXML private TextField carreteraPrincipalField;
    @FXML private TextField carreteraDestinoField;
    @FXML private TextField puntoKilometricoField;
    @FXML private ComboBox<String> direccionField;
    @FXML private TextField largoField;
    @FXML private TextField anchoField;
    @FXML private ComboBox<String> tipoCombo;

    // Elementos de la tabla de resultados
    @FXML private TableView<Dimension> tablaResultados;
    @FXML private TableColumn<Dimension, String> colLargo;
    @FXML private TableColumn<Dimension, String> colAncho;
    @FXML private TableColumn<Dimension, String> colNombrePlano;
    @FXML private TableColumn<Dimension, String> colFecha;

    // Lista observable que contiene los datos mostrados en la tabla
    private final ObservableList<Dimension> datos = FXCollections.observableArrayList();

    /**
     * Inicializa el controlador, configurando la tabla y los ComboBox.
     */
    @FXML
    public void initialize() {
        // Asignación de propiedades a las columnas de la tabla
        colLargo.setCellValueFactory(new PropertyValueFactory<>("largo"));
        colAncho.setCellValueFactory(new PropertyValueFactory<>("ancho"));
        colNombrePlano.setCellValueFactory(new PropertyValueFactory<>("nombrePlano"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        // Enlaza los datos con la tabla
        tablaResultados.setItems(datos);

        // Carga de opciones para tipo y dirección
        tipoCombo.getItems().addAll("enlace", "glorieta", "galibo", "curva", "salida");
        tipoCombo.setOnAction(event -> ajustarCamposPorTipo());

        direccionField.getItems().addAll(
                "NORTE -> NORTE", "NORTE -> SUR", "NORTE -> ESTE", "NORTE -> OESTE",
                "SUR -> NORTE", "SUR -> SUR", "SUR -> ESTE", "SUR -> OESTE",
                "ESTE -> NORTE", "ESTE -> SUR", "ESTE -> ESTE", "ESTE -> OESTE",
                "OESTE -> NORTE", "OESTE -> SUR", "OESTE -> ESTE", "OESTE -> OESTE"
        );
    }

    /**
     * Habilita o deshabilita campos en función del tipo seleccionado.
     */
    private void ajustarCamposPorTipo() {
        String tipo = tipoCombo.getValue();
        if (tipo == null) return;

        boolean esEnlace = tipo.equals("enlace");
        boolean esOtro = tipo.equals("glorieta") || tipo.equals("galibo") || tipo.equals("curva") || tipo.equals("salida");

        carreteraDestinoField.setDisable(esOtro);         // Destino sólo es útil si es enlace
        puntoKilometricoField.setDisable(esEnlace);       // PK no se usa para enlaces
    }

    /**
     * Realiza una búsqueda de dimensiones según los criterios ingresados.
     */
    @FXML
    private void buscarDimensiones() {
        datos.clear(); // Limpiar tabla antes de buscar

        // Obtener valores de los campos
        String principal = carreteraPrincipalField.getText().trim();
        String destino = carreteraDestinoField.getText().trim();
        String pk = puntoKilometricoField.getText().trim();
        String direccion = direccionField.getValue();
        String tipo = tipoCombo.getValue();

        // Verificación: al menos un criterio debe estar presente
        if (principal.isEmpty() && destino.isEmpty() && pk.isEmpty() &&
                (direccion == null || direccion.isEmpty()) &&
                (tipo == null || tipo.isEmpty())) {
            mostrarAlerta("Introduce al menos un criterio para buscar.");
            return;
        }

        try {
            Connection conn = com.routeiq.connection.DatabaseConnectionCONS.getConnection();

            // Construcción dinámica de consulta SQL
            StringBuilder sql = new StringBuilder("""
                SELECT longitud, anchura, fecha, idEnlace
                FROM Enlace
                WHERE 1=1
            """);

            List<Object> parametros = new ArrayList<>();

            if (!principal.isEmpty()) {
                sql.append(" AND codCarreteraOrigen = ?");
                parametros.add(principal);
            }
            if (direccion != null && !direccion.isEmpty()) {
                sql.append(" AND direccion = ?");
                parametros.add(direccion.toUpperCase());
            }
            if (tipo != null && !tipo.isEmpty()) {
                sql.append(" AND tipo = ?");
                parametros.add(tipo.toUpperCase().substring(0, 3));
            }
            if (tipo != null && tipo.equals("enlace") && !destino.isEmpty()) {
                sql.append(" AND codCarreteraDestino = ?");
                parametros.add(destino);
            } else if (!pk.isEmpty()) {
                sql.append(" AND km = ?");
                parametros.add(new java.math.BigDecimal(pk.replace(",", ".")));
            }

            PreparedStatement stmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String largo = rs.getBigDecimal("longitud") + " m";
                String ancho = rs.getBigDecimal("anchura") + " m";
                String plano = rs.getString("idEnlace");
                String fecha = rs.getDate("fecha").toString();

                datos.add(new Dimension(largo, ancho, plano, fecha));
            }

            rs.close();
            stmt.close();
            conn.close();

            if (datos.isEmpty()) {
                mostrarAlerta("No se encontraron resultados con los criterios indicados.");
            }

        } catch (Exception e) {
            mostrarAlerta("❌ Error al buscar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Permite crear un nuevo registro de dimensiones.
     */
    @FXML
    private void crearRegistro() {
        // Recolectar datos del formulario
        String principal = carreteraPrincipalField.getText().trim();
        String destino = carreteraDestinoField.getText().trim();
        String pk = puntoKilometricoField.getText().trim();
        String direccion = direccionField.getValue();
        String largo = largoField.getText().trim();
        String ancho = anchoField.getText().trim();
        String tipo = tipoCombo.getValue();

        // Validaciones de campos requeridos
        if (principal.isEmpty() || direccion == null || largo.isEmpty() || ancho.isEmpty() || tipo == null) {
            mostrarAlerta("Todos los campos son obligatorios.");
            return;
        }

        if (tipo.equals("enlace") && destino.isEmpty()) {
            mostrarAlerta("La carretera destino es obligatoria para tipo 'enlace'.");
            return;
        }

        if (!tipo.equals("enlace") && pk.isEmpty()) {
            mostrarAlerta("El punto kilométrico es obligatorio para tipos distintos a 'enlace'.");
            return;
        }

        try {
            Connection conn = com.routeiq.connection.DatabaseConnectionCONS.getConnection();

            String sql = """
                INSERT INTO Enlace (km, tipo, direccion, longitud, anchura, codCarreteraOrigen, codCarreteraDestino, fecha)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setBigDecimal(1, !tipo.equals("enlace") && !pk.isEmpty()
                    ? new java.math.BigDecimal(pk.replace(",", ".")) : null);

            stmt.setString(2, tipo.toUpperCase().substring(0, 3));
            stmt.setString(3, direccion.toUpperCase());
            stmt.setBigDecimal(4, new java.math.BigDecimal(largo.replace(",", ".")));
            stmt.setBigDecimal(5, new java.math.BigDecimal(ancho.replace(",", ".")));
            stmt.setString(6, principal);
            stmt.setString(7, tipo.equals("enlace") ? destino : null);
            stmt.setDate(8, java.sql.Date.valueOf(LocalDate.now()));

            stmt.executeUpdate();
            conn.close();

            mostrarAlerta("✅ Registro guardado con éxito.");
        } catch (Exception e) {
            mostrarAlerta("❌ Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Permite actualizar las medidas (largo/ancho) de un registro existente.
     */
    @FXML
    private void actualizarMedidas() {
        Dimension seleccion = tablaResultados.getSelectionModel().getSelectedItem();
        if (seleccion == null) {
            mostrarAlerta("Selecciona una fila en la tabla para actualizar.");
            return;
        }

        String largo = largoField.getText().trim();
        String ancho = anchoField.getText().trim();

        if (largo.isEmpty() || ancho.isEmpty()) {
            mostrarAlerta("Indica los nuevos valores de largo y ancho.");
            return;
        }

        try {
            Connection conn = com.routeiq.connection.DatabaseConnectionCONS.getConnection();

            String sql = """
                UPDATE Enlace
                SET longitud = ?, anchura = ?, fecha = ?
                WHERE idEnlace = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setBigDecimal(1, new java.math.BigDecimal(largo.replace(",", ".")));
            stmt.setBigDecimal(2, new java.math.BigDecimal(ancho.replace(",", ".")));
            stmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setString(4, seleccion.getNombrePlano());

            int filasAfectadas = stmt.executeUpdate();
            conn.close();

            if (filasAfectadas > 0) {
                mostrarAlerta("Registro actualizado correctamente.");
                buscarDimensiones(); // Refresca tabla tras actualización
            } else {
                mostrarAlerta("No se encontró un registro para actualizar.");
            }

        } catch (Exception e) {
            mostrarAlerta("Error al actualizar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Vuelve al menú principal.
     */
    @FXML
    private void volver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.routeiq/view/menu.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Muestra una alerta emergente con un mensaje al usuario.
     */
    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Clase interna que representa los datos mostrados en la tabla.
     */
    public static class Dimension {
        private final String largo;
        private final String ancho;
        private final String nombrePlano;
        private final String fecha;

        public Dimension(String largo, String ancho, String nombrePlano, String fecha) {
            this.largo = largo;
            this.ancho = ancho;
            this.nombrePlano = nombrePlano;
            this.fecha = fecha;
        }

        public String getLargo() { return largo; }
        public String getAncho() { return ancho; }
        public String getNombrePlano() { return nombrePlano; }
        public String getFecha() { return fecha; }
    }
}
