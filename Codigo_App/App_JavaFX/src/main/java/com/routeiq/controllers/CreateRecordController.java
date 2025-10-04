package com.routeiq.controllers;

import com.routeiq.connection.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Controlador para manejar la creación de registros en la base de datos
 */
public class CreateRecordController {

    // Campos del formulario vinculados con FXML
    @FXML private TextField carreteraPrincipalField;
    @FXML private TextField carreteraDestinoField;
    @FXML private TextField pkField;
    @FXML private ComboBox<String> direccionCombo;
    @FXML private ComboBox<String> tipoCombo;
    @FXML private TextField transporteField;
    @FXML private TextField estudioField;
    @FXML private TextField coordenadasField;

    /**
     * Método de inicialización de la interfaz.
     * Se cargan los valores de los ComboBox y se configura el evento para tipoCombo.
     */
    @FXML
    public void initialize() {
        direccionCombo.getItems().addAll("NORTE -> NORTE", "NORTE -> SUR", "NORTE -> ESTE", "NORTE -> OESTE",
                "SUR -> NORTE", "SUR -> SUR", "SUR -> ESTE", "SUR -> OESTE",
                "ESTE -> NORTE", "ESTE -> SUR", "ESTE -> ESTE", "ESTE -> OESTE",
                "OESTE -> NORTE", "OESTE -> SUR", "OESTE -> ESTE", "OESTE -> OESTE");
        tipoCombo.getItems().addAll("enlace", "glorieta", "galibo", "curva", "salida");

        tipoCombo.setOnAction(event -> ajustarCamposPorTipo()); // Lógica condicional de campos al seleccionar tipo
    }

    /**
     * Habilita o deshabilita campos en función del tipo seleccionado.
     */
    private void ajustarCamposPorTipo() {
        String tipo = tipoCombo.getValue();
        if (tipo == null) return;

        boolean esEnlace = tipo.equals("enlace");
        pkField.setDisable(esEnlace);                  // Desactiva PK si es enlace
        carreteraDestinoField.setDisable(!esEnlace);   // Activa carretera destino si es enlace
    }

    /**
     * Método principal para guardar un nuevo registro en la base de datos.
     */
    @FXML
    private void guardarRegistro(ActionEvent event) {
        // Obtener valores del formulario
        String principal = carreteraPrincipalField.getText().trim().toUpperCase();
        String destino = carreteraDestinoField.getText().trim().toUpperCase();
        String pk = pkField.getText().trim();
        String direccion = direccionCombo.getValue();
        String tipo = tipoCombo.getValue();
        String transporte = transporteField.getText().trim().toUpperCase();
        String estudio = estudioField.getText().trim().toUpperCase();
        String coordenadas = coordenadasField.getText();

        // Validaciones básicas
        if (principal.isEmpty() || direccion == null || tipo == null || transporte.isEmpty() || estudio.isEmpty() || coordenadas.isEmpty()) {
            mostrarAlerta("Todos los campos visibles son obligatorios.");
            return;
        }

        if (tipo.equals("enlace") && destino.isEmpty()) {
            mostrarAlerta("La carretera destino es obligatoria para tipo 'enlace'.");
            return;
        }

        if (!tipo.equals("enlace") && pk.isEmpty()) {
            mostrarAlerta("El punto kilométrico es obligatorio para tipos distintos de 'enlace'.");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();

            // Insertar el estudio si no existe
            PreparedStatement estudioStmt = conn.prepareStatement("INSERT IGNORE INTO Estudio (CodEstudio) VALUES (?)");
            estudioStmt.setString(1, estudio);
            estudioStmt.executeUpdate();

            // Insertar el transporte si no existe
            PreparedStatement transporteStmt = conn.prepareStatement("INSERT IGNORE INTO Transporte (CodTransporte) VALUES (?)");
            transporteStmt.setString(1, transporte);
            transporteStmt.executeUpdate();

            // Tipo de enlace a formato corto (ej. "GLOR" → "GLO")
            String tipoBD = tipo.toUpperCase().substring(0, 3);

            // Comprobar si ya existe un enlace con los datos dados
            PreparedStatement checkStmt = conn.prepareStatement("""
                SELECT idEnlace FROM Enlace
                WHERE codCarreteraOrigen = ? AND tipo = ? AND direccion = ?
                AND (codCarreteraDestino = ? OR (codCarreteraDestino IS NULL AND km = ?))
            """);

            checkStmt.setString(1, principal);
            checkStmt.setString(2, tipoBD);
            checkStmt.setString(3, direccion.toUpperCase());
            checkStmt.setString(4, tipo.equals("enlace") ? destino : null);
            if (!tipo.equals("enlace")) {
                checkStmt.setBigDecimal(5, new java.math.BigDecimal(pk.replace(",", ".")));
            } else {
                checkStmt.setNull(5, java.sql.Types.DECIMAL);
            }

            ResultSet rsCheck = checkStmt.executeQuery();
            String idEnlace = null;

            if (rsCheck.next()) {
                idEnlace = rsCheck.getString("idEnlace"); // Ya existe
            } else {
                // Insertar nuevo enlace si no existe
                PreparedStatement insertEnlace = conn.prepareStatement("""
                    INSERT INTO Enlace (km, tipo, direccion, codCarreteraOrigen, codCarreteraDestino, coordenadas)
                    VALUES (?, ?, ?, ?, ?, ?)
                """);

                if (!tipo.equals("enlace")) {
                    insertEnlace.setBigDecimal(1, new java.math.BigDecimal(pk.replace(",", ".")));
                } else {
                    insertEnlace.setNull(1, java.sql.Types.DECIMAL);
                }

                insertEnlace.setString(2, tipoBD);
                insertEnlace.setString(3, direccion.toUpperCase());
                insertEnlace.setString(4, principal);
                insertEnlace.setString(5, tipo.equals("enlace") ? destino : null);
                insertEnlace.setString(6, coordenadas);
                insertEnlace.executeUpdate();
                insertEnlace.close();

                // Recuperar el idEnlace recién insertado
                PreparedStatement getId = conn.prepareStatement("""
                    SELECT idEnlace FROM Enlace
                    WHERE codCarreteraOrigen = ? AND tipo = ? AND direccion = ?
                    AND (codCarreteraDestino = ? OR (codCarreteraDestino IS NULL AND km = ?))
                    ORDER BY idEnlace DESC LIMIT 1
                """);

                getId.setString(1, principal);
                getId.setString(2, tipoBD);
                getId.setString(3, direccion.toUpperCase());
                getId.setString(4, tipo.equals("enlace") ? destino : null);
                if (!tipo.equals("enlace")) {
                    getId.setBigDecimal(5, new java.math.BigDecimal(pk.replace(",", ".")));
                } else {
                    getId.setNull(5, java.sql.Types.DECIMAL);
                }

                ResultSet rsId = getId.executeQuery();
                if (rsId.next()) {
                    idEnlace = rsId.getString("idEnlace");
                } else {
                    mostrarAlerta("No se pudo recuperar el idEnlace generado.");
                    conn.close();
                    return;
                }

                rsId.close();
                getId.close();
            }

            rsCheck.close();
            checkStmt.close();

            // Comprobar si el plano ya existe
            PreparedStatement checkPlano = conn.prepareStatement("""
                SELECT codPlano FROM Plano WHERE codTransporte = ? AND idEnlace = ?
            """);
            checkPlano.setString(1, transporte);
            checkPlano.setString(2, idEnlace);
            ResultSet rsPlano = checkPlano.executeQuery();

            String codPlano = null;

            if (!rsPlano.next()) {
                // Insertar nuevo plano
                PreparedStatement planoStmt = conn.prepareStatement("""
                    INSERT INTO Plano (codTransporte, idEnlace, coordenadas)
                    VALUES (?, ?, ?)
                """);
                planoStmt.setString(1, transporte);
                planoStmt.setString(2, idEnlace);
                planoStmt.setString(3, coordenadas);
                planoStmt.executeUpdate();
                planoStmt.close();

                // Recuperar el código del plano insertado
                PreparedStatement selectCodPlano = conn.prepareStatement("""
                    SELECT codPlano FROM Plano WHERE codTransporte = ? AND idEnlace = ?
                """);
                selectCodPlano.setString(1, transporte);
                selectCodPlano.setString(2, idEnlace);
                ResultSet rsCodPlano = selectCodPlano.executeQuery();
                if (rsCodPlano.next()) {
                    codPlano = rsCodPlano.getString("codPlano");
                }
                rsCodPlano.close();
                selectCodPlano.close();
            } else {
                codPlano = rsPlano.getString("codPlano"); // Ya existe
            }

            rsPlano.close();
            checkPlano.close();

            // Asociar el plano al estudio correspondiente
            if (codPlano != null) {
                // Verificar si ya existe una relación
                PreparedStatement checkRelation = conn.prepareStatement("""
                    SELECT CodEstudio FROM Estudio_Plano WHERE codPlano = ?
                """);
                checkRelation.setString(1, codPlano);
                ResultSet rsRelation = checkRelation.executeQuery();

                if (rsRelation.next()) {
                    String estudioExistente = rsRelation.getString("CodEstudio");

                    if (estudioExistente.equals(estudio)) {
                        mostrarAlerta("Este plano ya está registrado con el estudio indicado.");
                        rsRelation.close();
                        checkRelation.close();
                        conn.close();
                        return;
                    } else {
                        // Si hay un estudio anterior distinto, se elimina
                        PreparedStatement deleteRelation = conn.prepareStatement("""
                            DELETE FROM Estudio_Plano WHERE codPlano = ?
                        """);
                        deleteRelation.setString(1, codPlano);
                        deleteRelation.executeUpdate();
                        deleteRelation.close();
                    }
                }

                rsRelation.close();
                checkRelation.close();

                // Insertar nueva relación estudio-plano
                PreparedStatement epStmt = conn.prepareStatement("""
                    INSERT INTO Estudio_Plano (codPlano, CodEstudio)
                    VALUES (?, ?)
                """);
                epStmt.setString(1, codPlano);
                epStmt.setString(2, estudio);
                epStmt.executeUpdate();
                epStmt.close();
            }

            mostrarAlerta("Registro guardado correctamente.");

        } catch (Exception e) {
            mostrarAlerta("Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método para volver a la vista anterior
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
     * Muestra una alerta con el mensaje proporcionado.
     */
    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
