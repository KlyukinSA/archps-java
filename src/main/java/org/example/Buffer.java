package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Buffer {
    private final List<EventHolder> elements;
    private int batchPriority;

    public Buffer(int size) {
        this.elements = new ArrayList<>(Collections.nCopies(size, new EventHolder(false, null)));
        this.batchPriority = 0;
    }

    public boolean hasPlace() {
        return elements.stream().filter(EventHolder::isOccupied).count() < elements.size();
    }

    public void put(Event request) { // Д10З3 — на свободное место
        for (int i = 0; i < elements.size(); i++) {
            if (!elements.get(i).isOccupied()) {
                elements.set(i, new EventHolder(true, request));
                break;
            }
        }
    }

    public boolean hasRequest() {
        return elements.stream().anyMatch(EventHolder::isOccupied);
    }

    public Event takeRequest() { // Д2Б5 — приоритет по номеру источника, заявки в пакете
        System.out.println("\t\t\t\t\t\t\t" + elements.stream().filter(EventHolder::isOccupied).map(d -> d.request().causer()).toList());
        if (elements.stream().filter(EventHolder::isOccupied).noneMatch(d -> d.request().causer() == batchPriority)) {
            batchPriority = elements.stream().filter(EventHolder::isOccupied).min(Comparator.comparingInt(d -> d.request().causer())).get().request().causer();
        }
//        Device device = elements.stream().filter(Device::isOccupied).filter(d -> d.request().causer() == batchPriority).findFirst().get();
        for (int i = 0; i < elements.size(); i++) {
            EventHolder eventHolder1 = elements.get(i);
            if (eventHolder1.isOccupied() && eventHolder1.request().causer() == batchPriority) {
                elements.set(i, new EventHolder(false, eventHolder1.request()));
                System.out.println("\t\t\t\t\t\t\t" + eventHolder1.request());
                return eventHolder1.request();
            }
        }
        return null;
    }
}
