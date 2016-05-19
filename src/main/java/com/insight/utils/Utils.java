package com.insight.utils;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ppearce on 2016-05-17.
 */
public class Utils {
    public static final String LINE_SEP = System.lineSeparator();

    /**
     * Build a representation of a log entry.
     *
     * @param source The source of the log data
     * @param data The log entry's data (may contain multiple lines)
     * @param timestampStartSentinal The character string denoting the start of a log entry.
     * @param timestampEndSentinal The character string denoting the end of the log timestamp.
     * @param sdf A Simple date formatter for the log entry's timestamp
     * @param startAt A String representation of the timestamp (matching the sdf) to start collecting log entries,
     *                null or empty implies no filtering
     * @param endAt A String representation of the timestamp (matching the sdf) to stop collecting log entries,
     *              null or empty implies no filtering.
     * @return
     * @throws ParseException
     */
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

    /**
     * Build a representation of a set of log entries from a single source.
     *
     * @param source The source of the log data
     * @param lines The lines of data that make up a log entry
     * @param timestampStartSentinal The character string denoting the start of a log entry.
     * @param timestampEndSentinal The character string denoting the end of the log timestamp.
     * @param sdf A Simple date formatter for the log entry's timestamp
     * @return
     * @throws ParseException
     */
    public static List<LogEntry> createLogEntries(
            final String source,
            final List<String> lines,
            final String timestampStartSentinal,
            final String timestampEndSentinal,
            final SimpleDateFormat sdf) throws ParseException {

        return createLogEntries(source, lines,timestampStartSentinal, timestampEndSentinal, sdf, null, null);
    }

    /**
     * Build a representation of a set of log entries from a single source.
     *
     * @param source The source of the log data
     * @param lines The lines of data that make up a log entry
     * @param timestampStartSentinal The character string denoting the start of a log entry.
     * @param timestampEndSentinal The character string denoting the end of the log timestamp.
     * @param sdf A Simple date formatter for the log entry's timestamp
     * @param startAt A String representation of the timestamp (matching the sdf) to start collecting log entries,
     *                null or empty implies no filtering
     * @param endAt A String representation of the timestamp (matching the sdf) to stop collecting log entries,
     *              null or empty implies no filtering.
     * @return
     * @throws ParseException
     */
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

    /**
     * Extract the filename from a path.
     *
     * @param path The full path to a file.
     * @return
     */
    public static String getFileNameFromFullPath(final String path) {
        File file = new File(path);

        return file.getName();
    }

    /**
     * Build a representation of a set of log entries from a single file.
     *
     * @param fileName The file containing log entries.
     * @param timestampStartSentinal The character string denoting the start of a log entry.
     * @param timestampEndSentinal The character string denoting the end of the log timestamp.
     * @param sdf A Simple date formatter for the log entry's timestamp
     * @return
     * @throws FileNotFoundException
     * @throws ParseException
     */
    public static List<LogEntry> createLogEntries(
            final String fileName,
            final String timestampStartSentinal,
            final String timestampEndSentinal,
            SimpleDateFormat sdf) throws FileNotFoundException, ParseException {

        return createLogEntries(fileName, timestampStartSentinal, timestampEndSentinal, sdf, null, null);
    }

    /**
     * Build a representation of a set of log entries from a single file.
     *
     * @param fileName The file containing log entries.
     * @param timestampStartSentinal The character string denoting the start of a log entry.
     * @param timestampEndSentinal The character string denoting the end of the log timestamp.
     * @param sdf A Simple date formatter for the log entry's timestamp
     * @param startAt A String representation of the timestamp (matching the sdf) to start collecting log entries,
     *                null or empty implies no filtering
     * @param endAt A String representation of the timestamp (matching the sdf) to stop collecting log entries,
     *              null or empty implies no filtering.
     * @return
     * @throws FileNotFoundException
     * @throws ParseException
     */
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

    /**
     * Time sort the list of lists.
     *
     * @param logs A list of log entry lists.
     * @return A list time sorted
     */
    public static List<LogEntry> timeSortLists(List<List<LogEntry>> logs) {
        List<LogEntry> timeSortedLogEntries   = new ArrayList<>();

        for(List<LogEntry> log : logs) {
            timeSortedLogEntries.addAll(log);
        }

        Collections.sort(timeSortedLogEntries);

        return timeSortedLogEntries;
    }

    /**
     * Display a LogEntry list on stdout.
     *
     * @param logEntries A list of time sorted log entries.
     * @param sources A list of the sources that made up the time sorted list.
     * @param label Text to display at the top of the outpit
     */
    public static void displayList(
            final List<LogEntry> logEntries,
            final List<String> sources,
            final String label) {

        PrintStream out = System.out;

        emitList(logEntries, sources, label, out);
    }

    /**
     * Emit a timesorted log entry List on the specified output.
     *
     * @param logEntries A list of time sorted log entries.
     * @param sources A list of the sources that made up the time sorted list.
     * @param label Text to display at the top of the outpit
     * @param out The stream to write to.
     */
    public static void emitList(
            final List<LogEntry> logEntries,
            final List<String> sources,
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

    /**
     * For usage from the command line
     *
     * @param args
     * @throws IOException
     * @throws ParseException
     */
    public static void main(final String[] args) throws IOException, ParseException {
        String timestampStartSentinal                   = "[";
        String timestampEndSentinal                     = "]";
        String timestampDateFormat                      = "yyyy-MM-dd HH:mm:ss,SSS";
        String startAt                                  = null;
        String endAt                                    = null;
        String propertyFileName                         = "logviewer.properties";
        final String timestampStartSentinalPropName     = "logentry.timestamp.start.sentinal";
        final String timestampEndSentinalPropName       = "logentry.timestamp.end.sentinal";
        final String timestampDateFormatPropName        = "logentry.timestamp.date.format";
        final String startAtPropName                    = "logentry.filter.start.date";
        final String endAtPropName                      = "logentry.filter.end.date";

        if(args.length < 2) {
            System.err.println("View multiple log files in a single time ascending order list.");
            System.err.println("");
            System.err.println("Usage: [@property_file] [@-] logfile logfile ...");
            System.err.println("");
            System.err.println("Notes:");
            System.err.println("The merged time ascending list is written to stdout.");
            System.err.println("");
            System.err.println("By default, property file [" + propertyFileName + "] in the current") ;
            System.err.println("directory will be read if it exists. To prevent this, either explicitly");
            System.err.println("specify an alternative file '@filename', else specify '@-' to prevent using a default.");
            System.err.println("");
            System.err.println("The property file can contain these keys, default values are used if");
            System.err.println("the property is not specified, or no property file specified.");
            System.err.println("");
            System.err.println(String.format("Key: [%-35s], default is '%s'", timestampStartSentinalPropName, timestampStartSentinal));
            System.err.println(String.format("Key: [%-35s], default is '%s'", timestampEndSentinalPropName, timestampEndSentinal));
            System.err.println(String.format("Key: [%-35s], default is '%s'", timestampDateFormatPropName, timestampDateFormat));
            System.err.println(String.format("Key: [%-35s], default is '%s'", startAtPropName, "empty or not set -> Earliest"));
            System.err.println(String.format("Key: [%-35s], default is '%s'", endAtPropName, "empty or not set -> Latest"));

            System.exit(1);
        }

        List<String>logFiles = new ArrayList<>();

        for(String filename : args) {
            if (filename.startsWith("@-")) {
                propertyFileName = null;
            } else {
                if (filename.startsWith("@")) {
                    propertyFileName = filename.substring(1);
                } else {
                    logFiles.add(filename);
                }
            }
        }

        if(null != propertyFileName) {
            Properties props = new Properties();
            props.load(new FileInputStream(propertyFileName)) ;

            timestampStartSentinal  = props.getProperty(timestampStartSentinalPropName, timestampStartSentinal);
            timestampEndSentinal    = props.getProperty(timestampEndSentinalPropName, timestampEndSentinal);
            timestampDateFormat     = props.getProperty(timestampDateFormatPropName, timestampDateFormat);
            startAt                 = props.getProperty(startAtPropName, startAt);
            endAt                   = props.getProperty(endAtPropName, endAt);
        }

        List<List<LogEntry>> logs   = new ArrayList<>();
        List<String> sources        = new ArrayList<>();
        SimpleDateFormat sdf        = new SimpleDateFormat(timestampDateFormat);

        for(String logFile : logFiles) {
            List<LogEntry> logEntries =
                    createLogEntries(
                            logFile,
                            timestampStartSentinal,
                            timestampEndSentinal,
                            sdf,
                            startAt,
                            endAt);

            logs.add(logEntries);
            sources.add(getFileNameFromFullPath(logFile));
        }

        List<LogEntry> timeSortedLogEntries = Utils.timeSortLists(logs);

        Utils.displayList(timeSortedLogEntries, sources, "");
    }
}
