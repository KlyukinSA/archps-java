package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Report {
    private int requestsCount;
    private int rejectsCount;
    private final BufferedWriter calendarWriter;
    private final List<ReportBufferElement> buffer;
    private final List<CalendarRow> calendarRows;
    private final List<Double> devices;
    private double endTime;
    private final List<SourceStats> sources;
    private SystemConfiguration systemConfiguration;

    public Report(SystemConfiguration configuration, List<CalendarRow> calendarRows, List<ReportBufferElement> buffer) {
        this.requestsCount = 0;
        this.rejectsCount = 0;
        try {
            this.calendarWriter = new BufferedWriter(new FileWriter("calendar.csv"));
            calendarWriter.write("causer,time,tag,requestsCount,rejectsCount\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.buffer = buffer;
        this.calendarRows = initCalendarRows(configuration, calendarRows);
        updateMinimal(0);
        this.devices = new ArrayList<>(Collections.nCopies(configuration.devicesCount, .0));

        this.sources = new ArrayList<>(Collections.nCopies(configuration.sourcesCount, null));
        for (int i = 0; i < configuration.sourcesCount; i++) {
            sources.set(i, new SourceStats());
        }
        this.systemConfiguration = configuration;
    }

    private List<CalendarRow> initCalendarRows(SystemConfiguration configuration, List<CalendarRow> calendarRows) {
        for (int i = 0; i < configuration.sourcesCount; i++) {
            calendarRows.add(new CalendarRow("source" + (i + 1), "0", "0", "", "", false));
        }
        for (int i = 0; i < configuration.devicesCount; i++) {
            calendarRows.add(new CalendarRow("device" + (i + 1), "0", "1", "", "", false));
        }
        return calendarRows;
    }

    public void register(Event event, boolean nextEventIsKnown) { // ОД1 — календарь событий, буфер и текущее состояние
        if (event.type() == EventType.SOURCE) {
            requestsCount++;
            SourceStats sourceStats = sources.get(event.causer());
            sourceStats.requestsCount++;
            updateSourceView(event, sourceStats);
        } else if (event.type() == EventType.DEVICE) {
            int index = event.causer() + systemConfiguration.sourcesCount;
            CalendarRow calendarRow = calendarRows.get(index);
            calendarRow.setTag(String.valueOf(nextEventIsKnown ? 0 : 1));
            updateMinimal(index);
            calendarRows.set(index, calendarRow);
        }

        String causer = event.type().toString();
        if (event.type() != EventType.END_OF_MODELING) {
            causer += event.causer();
        }
        List<String> list = Arrays.asList(causer, String.format("%.1f", event.time()),
                String.valueOf(nextEventIsKnown ? 0 : 1), String.valueOf(requestsCount), String.valueOf(rejectsCount));
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

    private void updateMinimal(int index) {
        calendarRows.get(index).setMinimal(false);
        double min = calendarRows.stream().filter(r -> r.getTag().equals("0")).mapToDouble(r -> Double.parseDouble(r.getTime())).min().getAsDouble();
        for (int i = 0; i < calendarRows.size(); i++) {
            double current = Double.parseDouble(calendarRows.get(i).getTime());
            if (Math.abs(min - current) < 0.0001 && calendarRows.get(i).getTag().equals("0")) {
                calendarRows.get(i).setMinimal(true);
                calendarRows.set(i, calendarRows.get(i));
            }
        }
    }

    private void updateSourceView(Event event, SourceStats sourceStats) {
        int index = event.causer();
        CalendarRow calendarRow = calendarRows.get(index);
        calendarRow.setRequestsCount(String.valueOf(sourceStats.requestsCount));
        calendarRow.setRejectsCount(String.valueOf(sourceStats.rejectsCount));
        calendarRow.setTime(String.valueOf(event.time()));
        updateMinimal(index);
        calendarRows.set(index, calendarRow);
    }

    public void markReject(Event rejected, double t) {
        rejectsCount++;
        SourceStats sourceStats = sources.get(rejected.causer());
        sourceStats.rejectsCount++;
        sourceStats.time += t - rejected.time();
        sourceStats.bufferTimes.add(t - rejected.time());
    }

    private void close() {
        try {
            calendarWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        createBufferReport();
        createDevicesReport();
        createSourcesReport();
    }

    private void createSourcesReport() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("sources.csv"));
            writer.write("sourceNumber,requestsCount,rejectProbability,time,bufferTime,servicingTime,bufferDispersion,servicingDispersion\n");
            for (int i = 0; i < sources.size(); i++) {
                SourceStats stats = sources.get(i);
                double averageTimeInSystem = stats.time / stats.requestsCount;
                double averageBufferTime = stats.bufferTimes.stream().mapToDouble(Double::doubleValue).sum() / stats.requestsCount;
                double bufferDispersion = stats.bufferTimes.stream().mapToDouble(t -> t - averageBufferTime).map(t -> t * t).sum() + (stats.requestsCount - stats.bufferTimes.size()) * averageBufferTime * averageBufferTime;
                double averageServicingTime = stats.servicingTimes.stream().mapToDouble(Double::doubleValue).sum() / stats.requestsCount;
                double servicingDispersion = stats.servicingTimes.stream().mapToDouble(t -> t - averageServicingTime).map(t -> t * t).sum() + (stats.requestsCount - stats.servicingTimes.size()) * averageServicingTime * averageServicingTime;
                List<String> list = Arrays.asList(
                        String.valueOf(i + 1),
                        String.valueOf(stats.requestsCount),
                        String.valueOf(stats.rejectsCount / (double) stats.requestsCount),
                        String.valueOf(averageTimeInSystem),
                        String.valueOf(averageBufferTime),
                        String.valueOf(averageServicingTime),
                        String.valueOf(bufferDispersion),
                        String.valueOf(servicingDispersion)
                );
                writer.write(String.join(",", list) + "\n");
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDevicesReport() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("devices.csv"));
            writer.write("deviceNumber,usageRate\n");
            for (int i = 0; i < devices.size(); i++) {
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

    public void markPutInBuffer(int pos, Event event) {
        buffer.set(pos, new ReportBufferElement(event.time(), event.causer(), requestsCount));
    }

    public void markLeaveBuffer(Event request, double t) {
        SourceStats sourceStats = sources.get(request.causer());
        sourceStats.bufferTimes.add(t - request.time());
    }

    public void markOccupyDevice(int dev, double delay, Event request, double t) {
        Double totalThisDeviceUsageTime = devices.get(dev);
        devices.set(dev, totalThisDeviceUsageTime + delay);

        int pos = request.causer();
        SourceStats sourceStats = sources.get(pos);
        sourceStats.servicingTimes.add(delay);
        sourceStats.time += t + delay - request.time();

        int index = dev + systemConfiguration.sourcesCount;
        CalendarRow calendarRow = calendarRows.get(index);
        calendarRow.setTime(String.valueOf(t + delay));
        calendarRow.setTag(String.valueOf(0));
        updateMinimal(index);
        calendarRows.set(index, calendarRow);
    }

    public Integer getRequestsCount() {
        return requestsCount;
    }

    public double getRejectProbability() {
        return sources.stream().mapToDouble(s -> s.rejectsCount / (double) s.requestsCount).sum() / sources.size();
    }
}
