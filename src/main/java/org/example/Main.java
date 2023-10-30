package org.example;

public class Main {
    public static void main(String[] args) {
        SystemConfiguration configuration = new SystemConfiguration();
        Report report = new Report(configuration.bufferSize);
        QueuingSystem system = new QueuingSystem(configuration, report);
        while (system.makeEvent()) { }
        report.close();
    }
}
