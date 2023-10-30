package org.example;

import java.util.Comparator;
import java.util.PriorityQueue;

public class Main {
    public static void main(String[] args) {
        int sourcesCount = 6;
        double sourceDelay = 250;

        int devicesCount = 3;
        double deviceDelay = 250;

        int bufferSize = 3;

        DeviceCollection devices = new DeviceCollection(devicesCount);
        Buffer buffer = new Buffer(bufferSize);
        PriorityQueue<Event> queue = initQueue(sourcesCount, sourceDelay);

        Report report = new Report(bufferSize);

        while (true) {
            Event event = queue.poll();
            double t = event.time();
            System.out.println("event " + event);
            if (t > 1000) {
                System.out.println("stop");
                report.register(new Event(t, EventType.END_OF_MODELING, 0), false);
                break;
            } else if (event.type() == EventType.SOURCE) {
                int sourceNumber = event.causer();
                System.out.println("accept request from " + sourceNumber);
                report.register(event, true);
                queue.add(new Event(t + sourceDelay, EventType.SOURCE, sourceNumber)); // ИЗ2 — равномерный закон распределения
                if (devices.hasFreeDevice()) {
                    System.out.println("immediately occupy the device");
                    occupyDevice(devices, event, queue, t, deviceDelay);
                } else if (buffer.hasPlace()) {
                    System.out.println("occupy buffer");
                    int pos = buffer.put(event);
                    report.updateBuffer(pos, event);
                } else {
                    System.out.println("reject!");
                    report.markReject();
                    Event rejected = buffer.reject(event);
                    System.out.println(rejected);
                }
            } else {
                assert event.type() == EventType.DEVICE;
                int deviceNumber = event.causer();
                System.out.println("free device " + deviceNumber);
                devices.freeAt(deviceNumber);
                boolean nextDeviceReleaseTimeIsKnown = false;
                if (buffer.hasRequest()) {
                    System.out.println("take new request from buffer");
                    Event request = buffer.takeRequest();
                    System.out.println("occupy this device with this request");
                    occupyDevice(devices, request, queue, t, deviceDelay);
                    nextDeviceReleaseTimeIsKnown = true;
                }
                report.register(event, nextDeviceReleaseTimeIsKnown);
            }
        }
        report.close();
    }

    private static PriorityQueue<Event> initQueue(int sourcesCount, double sourceDelay) {
        PriorityQueue<Event> queue = new PriorityQueue<>(Comparator.comparingDouble(Event::time));
        for (int i = 0; i < sourcesCount; i++) {
            queue.add(new Event(getNextDelay(sourceDelay), EventType.SOURCE, i));
        }
        return queue;
    }

    private static void occupyDevice(DeviceCollection devices, Event request, PriorityQueue<Event> queue, double t, double deviceDelay) {
        int dev = devices.occupyOneWith(request);
        queue.add(new Event(t + getNextDelay(deviceDelay), EventType.DEVICE, dev)); // ПЗ1 — экспоненциальный закон распределения времени обслуживания
    }

    private static double getNextDelay(double average) {
        return -1 * average * Math.log(Math.random());
    }
}