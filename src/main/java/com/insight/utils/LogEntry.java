package com.insight.utils;

/**
 * Created by ppearce on 2016-05-17.
 */
public class LogEntry implements Comparable<LogEntry> {
    private String source;
    private long rawTimeStamp;
    private String displayTimeStamp;
    private String payload;

    public LogEntry(
            final String source,
            final long rawTimeStamp,
            final String displayTimeStamp,
            final String payload)
    {
        this.source             = source;
        this.rawTimeStamp       = rawTimeStamp;
        this.displayTimeStamp   = displayTimeStamp;
        this.payload            = payload;
    }

    @Override
    public String toString() {
        return String.format("%d %s %s", rawTimeStamp, displayTimeStamp, payload);
    }

    public int compareTo(LogEntry o) {
        if (rawTimeStamp == o.rawTimeStamp) { return 0; }
        if(rawTimeStamp < o.rawTimeStamp) { return -1; }

        return 1;
    }

    public String getSource() {
        return source;
    }

    public String getDisplayTimeStamp() {
        return displayTimeStamp;
    }

    public String getPayload() {
        return payload;
    }
}
