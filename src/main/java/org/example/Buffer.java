package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Buffer {
    private final List<Device> elements;
    private int batchPriority;

    public Buffer(int size) {
        this.elements = new ArrayList<>(Collections.nCopies(size, new Device(false, null)));
        this.batchPriority = 0;
    }

    public boolean hasPlace() {
        return elements.stream().filter(Device::isOccupied).count() < elements.size();
    }

    public void put(Event request) { // Д10З3 — на свободное место
        for (int i = 0; i < elements.size(); i++) {
            if (!elements.get(i).isOccupied()) {
                elements.set(i, new Device(true, request));
                break;
            }
        }
    }

    public boolean hasRequest() {
        return elements.stream().anyMatch(Device::isOccupied);
    }

    public Event takeRequest() { // Д2Б5 — приоритет по номеру источника, заявки в пакете
        System.out.println("\t\t\t\t\t\t\t" + elements.stream().filter(Device::isOccupied).map(d -> d.request().causer()).toList());
        if (elements.stream().filter(Device::isOccupied).noneMatch(d -> d.request().causer() == batchPriority)) {
            batchPriority = elements.stream().filter(Device::isOccupied).min(Comparator.comparingInt(d -> d.request().causer())).get().request().causer();
        }
//        Device device = elements.stream().filter(Device::isOccupied).filter(d -> d.request().causer() == batchPriority).findFirst().get();
        for (int i = 0; i < elements.size(); i++) {
            Device device1 = elements.get(i);
            if (device1.isOccupied() && device1.request().causer() == batchPriority) {
                elements.set(i, new Device(false, device1.request()));
                System.out.println("\t\t\t\t\t\t\t" + device1.request());
                return device1.request();
            }
        }
        return null;
    }
}
