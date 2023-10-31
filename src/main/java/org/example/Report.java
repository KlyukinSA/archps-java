package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Report {
    private int requestsCount;
    private int rejectsCount;
    private final BufferedWriter calendarWriter;
    private final List<ReportBufferElement> buffer;
    private final List<CalendarRow> calendarRows;
    private final List<Double> devices;
    private double endTime;

    public Report(int bufferSize, int devicesCount, List<CalendarRow> calendarRows) {
        this.requestsCount = 0;
        this.rejectsCount = 0;
        try {
            this.calendarWriter = new BufferedWriter(new FileWriter("calendar.csv"));
            calendarWriter.write("causer,time,tag,requestsCount,rejectsCount\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.buffer = new ArrayList<>(Collections.nCopies(bufferSize,
                new ReportBufferElement(0, 0, 0)));
        this.calendarRows = calendarRows;
        this.devices = new ArrayList<>(Collections.nCopies(devicesCount, .0));
    }

    public void register(Event event, boolean nextEventIsKnown) { // ОД1 — календарь событий, буфер и текущее состояние
        if (event.type() == EventType.SOURCE) {
            requestsCount++;
        }

        String causer = event.type().toString();
        if (event.type() != EventType.END_OF_MODELING) {
            causer += event.causer();
        }
        List<String> list = Arrays.asList(causer, String.format("%.1f", event.time()),
                String.valueOf(nextEventIsKnown ? 0 : 1), String.valueOf(requestsCount), String.valueOf(rejectsCount));
        calendarRows.add(new CalendarRow(causer, String.format("%.1f", event.time()), String.valueOf(nextEventIsKnown ? 0 : 1),
                String.valueOf(requestsCount), String.valueOf(rejectsCount)));

        try {
            calendarWriter.write(String.join(",", list) + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (event.type() == EventType.END_OF_MODELING) {
            endTime = event.time();
            close();
        }
    }

    public void markReject() {
        rejectsCount++;
    }

    private void close() {
        try {
            calendarWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        createBufferReport();
        createDevicesReport();
    }

    private void createDevicesReport() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("devices.csv"));
            writer.write("deviceNumber,usageRate\n");
            for (int i = 0; i < buffer.size(); i++) {
                List<String> list = Arrays.asList(String.valueOf(i + 1), String.valueOf(devices.get(i) / endTime));
                writer.write(String.join(",", list) + "\n");
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createBufferReport() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("buffer.csv"));
            writer.write("position,time,sourceNumber,requestNumber\n");
            for (int i = 0; i < buffer.size(); i++) {
                ReportBufferElement element = buffer.get(i);
                List<String> list = Arrays.asList(String.valueOf(i + 1), String.format("%.1f", element.time()), String.valueOf(element.sourceNumber()), String.valueOf(element.requestNumber()));
                writer.write(String.join(",", list) + "\n");
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateBuffer(int pos, Event event) {
        buffer.set(pos, new ReportBufferElement(event.time(), event.causer(), requestsCount));
    }

    public void addTimeToDevice(int dev, double delay) {
        Double time = devices.get(dev);
        devices.set(dev, time + delay);
    }
}
