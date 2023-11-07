package org.example;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalendarRow {
    private String causer;
    private String time;
    private String tag;
    private String requestsCount;
    private String rejectsCount;
    private boolean isMinimal;

    public CalendarRow(String causer, String time, String tag, String requestsCount, String rejectsCount, boolean isMinimal) {
        this.causer = causer;
        this.time = time;
        this.tag = tag;
        this.requestsCount = requestsCount;
        this.rejectsCount = rejectsCount;
        this.isMinimal = isMinimal;
    }

}
