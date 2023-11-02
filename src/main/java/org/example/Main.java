package org.example;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.File;
import java.net.MalformedURLException;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        ObservableList<CalendarRow> people = FXCollections.observableArrayList();
        SystemConfiguration configuration = new SystemConfiguration();
        Report report = new Report(configuration, people);
        QueuingSystem system = new QueuingSystem(configuration, report);

        Button stepButton = new Button("make event step");
        stepButton.setOnAction((e) -> system.makeEvent());

        Button runButton = new Button("run the simulation to completion");
        runButton.setOnAction((e) -> {
            stepButton.setVisible(false);
            while (system.makeEvent()) {}
        });

        Label label = new Label("reports will be saved in scv in current dir");

        Button nButton = new Button("run the simulation n times, find n");
        nButton.setOnAction((e) -> {
            configuration.requestsCount = 100;
            double p = runNewSystem(configuration);
            double p0 = p;
            double N = 1.643 * 1.643 * (1 - p) / (p * 0.1 * 0.1);
            while (true) {
                configuration.requestsCount = (int) N;
                double p2 = runNewSystem(configuration);
                if (p2 < 0.00001) {
                    label.setText("p < 0.00001");
                    break;
                }
                N = 1.643 * 1.643 * (1 - p2) / (p2 * 0.1 * 0.1);
                if (Math.abs(p2 - p) < 0.1 * p0) {
                    break;
                }
                p = p2;
            }
            label.setText(String.format("%1.2f", p) + "\t" + String.format("%5.0f", N));
        });

        TableView<CalendarRow> table = new TableView<>(people);
        table.setPrefSize(700, 500);

        TableColumn<CalendarRow, String> causerCol = new TableColumn<>("causer");
        causerCol.setCellValueFactory(itemData -> new ReadOnlyStringWrapper(itemData.getValue().getCauser()));
        table.getColumns().add(causerCol);
        TableColumn<CalendarRow, String> timeCol = new TableColumn<>("time");
        timeCol.setCellValueFactory(itemData -> new ReadOnlyStringWrapper(itemData.getValue().getTime()));
        table.getColumns().add(timeCol);
        TableColumn<CalendarRow, String> tagCol = new TableColumn<>("tag");
        tagCol.setCellValueFactory(itemData -> new ReadOnlyStringWrapper(itemData.getValue().getTag()));
        table.getColumns().add(tagCol);
        TableColumn<CalendarRow, String> requestsCountCol = new TableColumn<>("requestsCount");
        requestsCountCol.setCellValueFactory(itemData -> new ReadOnlyStringWrapper(itemData.getValue().getRequestsCount()));
        table.getColumns().add(requestsCountCol);
        TableColumn<CalendarRow, String> rejectsCountCol = new TableColumn<>("rejectsCount");
        rejectsCountCol.setCellValueFactory(itemData -> new ReadOnlyStringWrapper(itemData.getValue().getRejectsCount()));
        table.getColumns().add(rejectsCountCol);

        FlowPane root = new FlowPane(label, table, stepButton, runButton, nButton);

        Scene scene = new Scene(root, 3000, 3000);

//        scene.getStylesheets().add("https://raw.githubusercontent.com/antoniopelusi/JavaFX-Dark-Theme/main/style.css"); // "https://github.com/antoniopelusi/JavaFX-Dark-Theme/blob/main/style.css"
        File style = new File("src/main/resources/style.css");
        try {
            scene.getStylesheets().add(style.toURI().toURL().toExternalForm());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        stage.setScene(scene);
        stage.setTitle("TableView in JavaFX");
        stage.show();
    }

    private double runNewSystem(SystemConfiguration configuration) {
        Report report = new Report(configuration, null);
        QueuingSystem system = new QueuingSystem(configuration, report);
        while (system.makeEvent()) {}
        return report.getRejectProbability();
    }

}
