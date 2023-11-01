package org.example;

import java.util.ArrayList;
import java.util.List;

public class SourceStats {
    public int requestsCount;
    public int rejectsCount;
    public double time;
    public final List<Double> bufferTimes;
    public final List<Double> servicingTimes;

    public SourceStats() {
        this.requestsCount = 0;
        this.rejectsCount = 0;
        this.time = 0;
        this.bufferTimes = new ArrayList<>();
        this.servicingTimes = new ArrayList<>();
    }

}
