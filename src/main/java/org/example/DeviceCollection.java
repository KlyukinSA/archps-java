package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeviceCollection {
//    private final int size;
    private int pointer;
    private final List<EventHolder> devices;

    public DeviceCollection(int size) {
//        this.size = size;
        this.pointer = 0;
        this.devices = new ArrayList<>(Collections.nCopies(size, new EventHolder(false, null)));
    }

    public boolean hasFreeDevice() {
        return devices.stream().filter(EventHolder::isOccupied).count() < devices.size();
    }

    public int occupyOneWith(Event request) { // Д2П2 — выбор прибора по кольцу
        int start = pointer;
        while (true) {
            if (devices.get(pointer).isOccupied()) {
                pointer = (1 + pointer) % devices.size();
                if (pointer == start) {
                    throw new RuntimeException("no free device" + start + " " + pointer + " " + request);
                }
            } else {
                devices.set(pointer, new EventHolder(true, request));
                return pointer;
            }
        }
    }

    public Event freeAt(int i) {
        Event request = devices.get(i).request();
        devices.set(i, new EventHolder(false, request));
        return request;
    }
}
