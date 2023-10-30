package org.example;

public class CalendarRow {
    private String causer;
    private String time;
    private String tag;
    private String requestsCount;
    private String rejectsCount;

    public CalendarRow(String causer, String time, String tag, String requestsCount, String rejectsCount) {
        this.causer = causer;
        this.time = time;
        this.tag = tag;
        this.requestsCount = requestsCount;
        this.rejectsCount = rejectsCount;
    }

    public String getCauser() {
        return causer;
    }

    public void setCauser(String causer) {
        this.causer = causer;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRequestsCount() {
        return requestsCount;
    }

    public void setRequestsCount(String requestsCount) {
        this.requestsCount = requestsCount;
    }

    public String getRejectsCount() {
        return rejectsCount;
    }

    public void setRejectsCount(String rejectsCount) {
        this.rejectsCount = rejectsCount;
    }
}
