package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Buffer {
    private final List<Device> elements;
    public Buffer(int size) {
        this.elements = new ArrayList<>(Collections.nCopies(size, new Device(false, null)));
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

    public Event takeRequest() { // TODO Д2Б5 — приоритет по номеру источника, заявки в пакете
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i).isOccupied()) {
                elements.set(i, new Device(false, elements.get(i).request()));
                return elements.get(i).request();
            }
        }
        return null;
    }
}
