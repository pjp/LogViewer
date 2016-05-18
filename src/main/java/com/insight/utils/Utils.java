package com.insight.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ppearce on 2016-05-17.
 */
public class Utils {
    public static final String LINE_SEP = System.lineSeparator();

    public static LogEntry createLogEntry(
            final String source,
            final String data,
            final String timestampStartSentinal,
            final String timestampEndSentinal,
            final SimpleDateFormat sdf,
            final String startAt,
            final String endAt) throws ParseException {
        long rawTimeStamp = 0;
        String displayTimeStamp = "0";
        String payload = null;
        LogEntry logEntry = null;
        long startTs = 0;
        long endTs = Long.MAX_VALUE;

        ////////////////////////////////
        // Extract the display timestamp
        int s = data.indexOf(timestampStartSentinal);
        int e = data.indexOf(timestampEndSentinal);

        displayTimeStamp = data.substring(s + 1, e);
        payload = data.substring(e + 1);

        //////////////////////////////////////////////////////
        // Create the raw timestamp from the display timestamp
        Date d = sdf.parse(displayTimeStamp);
        rawTimeStamp = d.getTime();

        ///////////////////////////////
        // Build any time range filters
        if (null != startAt && startAt.trim().length() > 0) {
            Date dS = sdf.parse(startAt);
            startTs = dS.getTime();
        }

        if (null != endAt && endAt.trim().length() > 0) {
            Date dS = sdf.parse(endAt);
            endTs = dS.getTime();
        }

        if (rawTimeStamp >= startTs && rawTimeStamp <= endTs) {
            logEntry = new LogEntry(source, rawTimeStamp, displayTimeStamp, payload);
        }

        return logEntry;
    }

    public static List<LogEntry> createLogEntries(
            final String source,
            final List<String> lines,
            final String timestampStartSentinal,
            final String timestampEndSentinal,
            final SimpleDateFormat sdf) throws ParseException {

        return createLogEntries(source, lines,timestampStartSentinal, timestampEndSentinal, sdf, null, null);
    }

    public static List<LogEntry> createLogEntries(
            final String source,
            final List<String> lines,
            final String timestampStartSentinal,
            final String timestampEndSentinal,
            final SimpleDateFormat sdf,
            final String startAt,
            final String endAt) throws ParseException {
        List<LogEntry> logEntries   = new ArrayList<LogEntry>();
        StringBuilder currentEntry  = new StringBuilder();
        boolean atStart             = true;

        for (String line : lines) {
            if(atStart) {
                if(!line.startsWith(timestampStartSentinal)) {
                    continue;
                }
                atStart = false;
            }

            if(line.startsWith(timestampStartSentinal)) {
                if(currentEntry.length() > 0) {
                    LogEntry logEntry = createLogEntry(
                            source,
                            currentEntry.toString(),
                            timestampStartSentinal,
                            timestampEndSentinal,
                            sdf,
                            startAt,
                            endAt);

                    if(null != logEntry) {
                        logEntries.add(logEntry);
                    }

                    currentEntry = new StringBuilder();
                }
            }
            currentEntry.append(line).append(LINE_SEP);
        }

        if(currentEntry.length() > 0) {
            LogEntry logEntry = createLogEntry(
                    source,
                    currentEntry.toString(),
                    timestampStartSentinal,
                    timestampEndSentinal,
                    sdf,
                    startAt,
                    endAt);

            if(null != logEntry) {
                logEntries.add(logEntry);
            }
        }

        return logEntries;
    }

    public static List<LogEntry> createLogEntries(
            final String fileName,
            final String timestampStartSentinal,
            final String timestampEndSentinal,
            SimpleDateFormat sdf) throws FileNotFoundException, ParseException {

        return createLogEntries(fileName, timestampStartSentinal, timestampEndSentinal, sdf, null, null);
    }

    public static String getFileNameFromFullPath(final String path) {
        File file = new File(path);

        return file.getName();
    }

    public static List<LogEntry> createLogEntries(
            final String fileName,
            final String timestampStartSentinal,
            final String timestampEndSentinal,
            SimpleDateFormat sdf,
            final String startAt,
            final String endAt) throws FileNotFoundException, ParseException {
        File file = new File(fileName);
        List<LogEntry> logEntries   = null;
        List<String> lines          = new ArrayList<String>();

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()){
                lines.add(scanner.nextLine());
            }
            logEntries =
                    createLogEntries(
                            getFileNameFromFullPath(fileName),
                            lines,
                            timestampStartSentinal,
                            timestampEndSentinal,
                            sdf,
                            startAt,
                            endAt);
        }

        return logEntries;
    }

    public static List<LogEntry> timeSortLists(List<List<LogEntry>> logs) {
        List<LogEntry> timeSortedLogEntries   = new ArrayList<>();

        for(List<LogEntry> log : logs) {
            timeSortedLogEntries.addAll(log);
        }

        Collections.sort(timeSortedLogEntries);

        return timeSortedLogEntries;
    }

    public static void displayList(
            final List<LogEntry> logEntries,
            final List<String> sources,
            final int offset,
            final String label) {

        PrintStream out = System.out;

        emitList(logEntries, sources, offset, label, out);
    }

    public static void emitList(
            final List<LogEntry> logEntries,
            final List<String> sources,
            final int offset,
            final String label,
            PrintStream out) {
        String source   = null;

        if(null != label && label.trim().length() > 0) {
            out.println("# Label: " + label);
        }

        out.println("# Sources:");

        int i = 1;
        for(String theSource : sources) {
            out.println("# " + i + " " + theSource);
            i++;
        }
        out.println("#");

        out.println("# Time sorted log entries:");

        for(LogEntry logEntry : logEntries) {
            /////////////////////////////////////////////////////////////////////////////
            // Depending on the source position, where in the line we display the payload
            String dts  =   logEntry.getDisplayTimeStamp();
            source      =   logEntry.getSource();
            int index   =   sources.indexOf(source);
            String pad1 =   String.format("%d  %20s ", index + 1, dts);
            String pad2 =   String.format("%d  %20s ", index + 1, "");

            /////////////////////////////////////////////////////////////////
            // Extract each line from the payload and display it with padding
            boolean firstLine   = true;

            StringTokenizer st = new StringTokenizer(logEntry.getPayload(), Utils.LINE_SEP);
            while(st.hasMoreElements()) {
                if(firstLine) {
                    out.println(pad1 + st.nextToken());
                    firstLine   = false;
                } else {
                    out.println(pad2 + st.nextToken());
                }
            }
        }
    }
}
