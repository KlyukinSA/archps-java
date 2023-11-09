package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        ObservableList<CalendarRow> calendarViewList = FXCollections.observableArrayList();

        SystemConfiguration configuration = initConfigurationWithFile();

        ObservableList<ReportBufferElement> bufferViewList = initBufferView(configuration);

        Report report = new Report(configuration, calendarViewList, bufferViewList);
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
            SystemConfiguration conf = initConfigurationWithFile();
            conf.requestsCount = 100;
            Report report1 = runNewSystem(conf);
            double p = report1.getRejectProbability();
            double p0 = p;
            double N = 1.643 * 1.643 * (1 - p) / (p * 0.1 * 0.1);
            while (true) {
                conf.requestsCount = (int) N;
                report1 = runNewSystem(conf);
                double p2 = report1.getRejectProbability();
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
            label.setText("N = " + String.format("%.0f", N) +
                    "\n RejectProbability = " + String.format("%1.2f", p) +
                    "\n AverageTimeInSystem = " + String.format("%.2f", report1.getAverageTimeInSystem()) +
                    "\n DeviceUsageRate = " + String.format("%1.2f", report1.getDeviceUsageRate()) +
                    "\n fits: " + (p < 0.3 && report1.getAverageTimeInSystem() < 50 && report1.getDeviceUsageRate() > 0.6));
        });

        Button graphButton = new Button("change sourceDelay and see how rejectProbability changes");
        graphButton.setOnAction((e) -> {
            SystemConfiguration conf = initConfigurationWithFile();
            conf.requestsCount = 800;
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter("graph.csv"));
                writer.write("sourceDelay,rejectProbability\n");
                for (conf.sourceDelay = 10; conf.sourceDelay < 30; conf.sourceDelay += 0.5) {
                    double p = runNewSystem(conf).getRejectProbability();
                    List<String> list = Arrays.asList(String.format("%,.2f", conf.sourceDelay), String.format("%,.2f", p));
                    writer.write(String.join(";", list) + "\n");
                }
                writer.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            label.setText("result in graph.csv");
        });

        TableView<CalendarRow> calendarTable = new TableView<>(calendarViewList);

        TableColumn<CalendarRow, String> causerCol = new TableColumn<>("causer");
        causerCol.setCellValueFactory(itemData -> new ReadOnlyStringWrapper(itemData.getValue().getCauser()));
        calendarTable.getColumns().add(causerCol);
        TableColumn<CalendarRow, String> timeCol = new TableColumn<>("time");
        timeCol.setCellValueFactory(itemData -> new ReadOnlyStringWrapper(itemData.getValue().getTime()));
        calendarTable.getColumns().add(timeCol);
        TableColumn<CalendarRow, String> min = new TableColumn<>("is min of tag 0s");
        min.setCellValueFactory(itemData -> new ReadOnlyStringWrapper(itemData.getValue().isMinimal() ? "true" : ""));
        calendarTable.getColumns().add(min);
        TableColumn<CalendarRow, String> tagCol = new TableColumn<>("tag");
        tagCol.setCellValueFactory(itemData -> new ReadOnlyStringWrapper(itemData.getValue().getTag()));
        calendarTable.getColumns().add(tagCol);
        TableColumn<CalendarRow, String> requestsCountCol = new TableColumn<>("requestsCount");
        requestsCountCol.setCellValueFactory(itemData -> new ReadOnlyStringWrapper(itemData.getValue().getRequestsCount()));
        calendarTable.getColumns().add(requestsCountCol);
        TableColumn<CalendarRow, String> rejectsCountCol = new TableColumn<>("rejectsCount");
        rejectsCountCol.setCellValueFactory(itemData -> new ReadOnlyStringWrapper(itemData.getValue().getRejectsCount()));
        calendarTable.getColumns().add(rejectsCountCol);

        TableView<ReportBufferElement> bufferTable = new TableView<>(bufferViewList);

        TableColumn<ReportBufferElement, String> timeCol2 = new TableColumn<>("time");
        timeCol2.setCellValueFactory(itemData -> new ReadOnlyStringWrapper(String.valueOf(itemData.getValue().time())));
        bufferTable.getColumns().add(timeCol2);
        TableColumn<ReportBufferElement, String> sourceNumberCol2 = new TableColumn<>("sourceNumber");
        sourceNumberCol2.setCellValueFactory(itemData -> new ReadOnlyStringWrapper(String.valueOf(itemData.getValue().sourceNumber())));
        bufferTable.getColumns().add(sourceNumberCol2);
        TableColumn<ReportBufferElement, String> requestNumberCol2 = new TableColumn<>("requestNumber");
        requestNumberCol2.setCellValueFactory(itemData -> new ReadOnlyStringWrapper(String.valueOf(itemData.getValue().requestNumber())));
        bufferTable.getColumns().add(requestNumberCol2);

        calendarTable.setPrefSize(550, 400);
        bufferTable.setPrefSize(550, 400);

        FlowPane root = new FlowPane(label, calendarTable, bufferTable, stepButton, runButton, nButton, graphButton);

        Scene scene = new Scene(root, 1500, 700);

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

    private static ObservableList<ReportBufferElement> initBufferView(SystemConfiguration configuration) {
        ObservableList<ReportBufferElement> objects = FXCollections.observableArrayList();// Collections.nCopies(configuration.bufferSize,
        for (int i = 0; i < configuration.bufferSize; i++) {
            objects.add(new ReportBufferElement(0, 0, 0));
        }
        return objects;
    }

    private static SystemConfiguration initConfigurationWithFile() {
        SystemConfiguration configuration = new SystemConfiguration();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String name = "system-conf.yaml";
        try {
            File f = new File(name);
            if (!f.exists()) {
                mapper.writeValue(f, configuration);
            } else {
                configuration = mapper.readValue(f, SystemConfiguration.class);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return configuration;
    }

    private Report runNewSystem(SystemConfiguration configuration) {
        Report report = new Report(configuration, new ArrayList<>(), initBufferView(configuration));
        QueuingSystem system = new QueuingSystem(configuration, report);
        while (system.makeEvent()) {}
        return report;
    }

}
