package org.example;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemConfiguration {
    int sourcesCount = 6;
    double sourceDelay = 350;

    int devicesCount = 3;
    double deviceDelay = 250;

    int bufferSize = 3;

    int requestsCount = 20;
}
