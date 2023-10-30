package org.example;

import java.util.Comparator;
import java.util.PriorityQueue;

public class QueuingSystem {
    private SystemConfiguration configuration;
    private DeviceCollection devices;
    private Buffer buffer;
    private PriorityQueue<Event> queue;
    private Report report;

    public QueuingSystem(SystemConfiguration configuration, Report report) {
        this.configuration = configuration;
        this.report = report;
        this.devices = new DeviceCollection(configuration.devicesCount);
        this.buffer = new Buffer(configuration.bufferSize);
        this.queue = initQueue(configuration.sourcesCount, configuration.sourceDelay);
    }

    public boolean makeEvent() {
        Event event = queue.poll();
        double t = event.time();
        System.out.println("event " + event);
        if (t > 1000) {
            System.out.println("stop");
            report.register(new Event(t, EventType.END_OF_MODELING, 0), false);
            return false;
        } else if (event.type() == EventType.SOURCE) {
            int sourceNumber = event.causer();
            System.out.println("accept request from " + sourceNumber);
            report.register(event, true);
            queue.add(new Event(t + configuration.sourceDelay, EventType.SOURCE, sourceNumber)); // ИЗ2 — равномерный закон распределения
            if (devices.hasFreeDevice()) {
                System.out.println("immediately occupy the device");
                occupyDevice(devices, event, queue, t);
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
                occupyDevice(devices, request, queue, t);
                nextDeviceReleaseTimeIsKnown = true;
            }
            report.register(event, nextDeviceReleaseTimeIsKnown);
        }
        return true;
    }

    private PriorityQueue<Event> initQueue(int sourcesCount, double sourceDelay) {
        PriorityQueue<Event> queue = new PriorityQueue<>(Comparator.comparingDouble(Event::time));
        for (int i = 0; i < sourcesCount; i++) {
            queue.add(new Event(getNextDelay(sourceDelay), EventType.SOURCE, i));
        }
        return queue;
    }

    private void occupyDevice(DeviceCollection devices, Event request, PriorityQueue<Event> queue, double t) {
        int dev = devices.occupyOneWith(request);
        double delay = getNextDelay(configuration.deviceDelay);
        report.addTimeToDevice(dev, delay);
        queue.add(new Event(t + delay, EventType.DEVICE, dev)); // ПЗ1 — экспоненциальный закон распределения времени обслуживания
    }

    private double getNextDelay(double average) {
        return -1 * average * Math.log(Math.random());
    }
}