package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Report {
    private int requestsCount;
    private int rejectsCount;
    private final BufferedWriter writer;

    public Report() {
        this.requestsCount = 0;
        this.rejectsCount = 0;
        try {
            this.writer = new BufferedWriter(new FileWriter("calendar.csv"));
            writer.write("causer,time,tag,requestsCount,rejectsCount\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void register(Event event, boolean nextEventIsKnown) { // ОД1 — календарь событий, буфер и текущее состояние
        if (event.type() == EventType.SOURCE) {
            requestsCount++;
        }
        String causer = event.type().toString();
        if (event.type() != EventType.END_OF_MODELING) {
            causer += event.causer();
        }
        List<String> list = Arrays.asList(causer, String.format("%.1f", event.time()), String.valueOf(nextEventIsKnown ? 0 : 1), String.valueOf(requestsCount), String.valueOf(rejectsCount));
        try {
            writer.write(String.join(",", list) + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void markReject() {
        rejectsCount++;
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
