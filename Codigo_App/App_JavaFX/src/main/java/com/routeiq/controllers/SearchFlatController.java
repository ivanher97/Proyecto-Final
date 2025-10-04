package com.routeiq.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

/**
 * Controlador de la vista intermedia para seleccionar el tipo de búsqueda o acción relacionada con los planos.
 */
public class SearchFlatController {

    /**
     * Acción al presionar el botón de "Búsqueda Manual".
     * Carga la vista `manual_search.fxml`.
     */
    @FXML
    private void busquedaManual(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.routeiq/view/manual_search.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene); // Cambia a la escena de búsqueda manual
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Ir a Búsqueda Manual (pendiente)");
    }

    /**
     * Acción al presionar el botón de "Búsqueda Automática".
     * Actualmente no implementada (solo imprime por consola).
     */
    @FXML
    private void busquedaAutomatica(ActionEvent event) {
        System.out.println("Ir a Búsqueda Automática (pendiente)");
    }

    /**
     * Acción al presionar el botón de "Crear Registro".
     * Carga la vista `create_record.fxml`.
     */
    @FXML
    private void crearRegistro(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.routeiq/view/create_record.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene); // Cambia a la escena de creación de registro
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Ir a Crear Registro (pendiente)");
    }

    /**
     * Acción al presionar el botón de "Ver Historial".
     * Funcionalidad aún no implementada (mensaje por consola).
     */
    @FXML
    private void verHistorial(ActionEvent event) {
        System.out.println("Ir a Historial de creaciones (pendiente)");
    }

    /**
     * Acción al presionar el botón de "Volver".
     * Regresa al menú principal cargando `menu.fxml`.
     */
    @FXML
    private void volver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.routeiq/view/menu.fxml"));
            Scene scene = new Scene(loader.load());

            // El controlador del menú recupera el rol directamente desde SessionController
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
