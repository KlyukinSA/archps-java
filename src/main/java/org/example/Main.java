package org.example;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
        Report report = new Report(configuration.bufferSize, people);
        QueuingSystem system = new QueuingSystem(configuration, report);
//        while (system.makeEvent()) { }
//        report.close();

        Button stepButton = new Button("step");
        stepButton.setOnAction((e) -> system.makeEvent());

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

        FlowPane root = new FlowPane(table, stepButton);

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
}
