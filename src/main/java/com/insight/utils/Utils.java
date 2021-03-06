package com.insight.utils;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ppearce on 2016-05-17.
 *
 * When comparing multiple log files from servers, it can be very useful to see the timeline of events from the logs.
 *
 * This utility take multiple log files (up to 10), then time ascending sorts them into a single list so that
 * log events on the different servers can be visualized in a single place.
 *
 */
public class Utils {
    public static final String LINE_SEP = System.lineSeparator();

    /**
     * Build a representation of a log entry.
     *
     * @param source The source of the log data
     * @param data The log entry's data (may contain multiple lines)
     * @param sdf A Simple date formatter for the log entry's timestamp
     * @param rawTimeStamp mS timestamp of this entry.
     * @param startAt A String representation of the timestamp (matching the sdf) to start collecting log entries,
     *                null or empty implies no filtering
     * @param endAt A String representation of the timestamp (matching the sdf) to stop collecting log entries,
     *              null or empty implies no filtering.
     * @param searchText A list of text string to match a lig entry against.
     *
     * @return
     * @throws ParseException
     */
    protected static LogEntry createLogEntry(
            final String source,
            final String data,
            final SimpleDateFormat sdf,
            final long rawTimeStamp,
            final String startAt,
            final String endAt,
            final List<String> searchText) throws ParseException {
        String displayTimeStamp = "0";
        String payload = null;
        LogEntry logEntry = null;
        long startTs = 0;
        long endTs = Long.MAX_VALUE;

        ////////////////////////////////
        // Extract the display timestamp
        int patternLength   = sdf.toPattern().length();

        displayTimeStamp    = sdf.format(new Date(rawTimeStamp));
        payload             = data.substring(patternLength);

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
            if(null != searchText && searchText.size() > 0) {
                for(String text : searchText) {
                    if(payload.contains(text))  {
                        logEntry = new LogEntry(source, rawTimeStamp, displayTimeStamp, payload);
                        break;
                    }
                }
            } else {
                logEntry = new LogEntry(source, rawTimeStamp, displayTimeStamp, payload);
            }
        }

        return logEntry;
    }

    /**
     * Build a representation of a set of log entries from a single source.
     *
     * @param source The source of the log data
     * @param lines The lines of data that make up a log entry
     * @param timestampDateFormat A Simple date formatter String for the log entry's timestamp
     * @param searchText A list of text string to match a lig entry against.
     * @param timestampAdjustment A mS adjustment to the log entries timestamp.
     *
     * @return
     * @throws ParseException
     */
    protected static List<LogEntry> createLogEntries(
            final String source,
            final List<String> lines,
            final String timestampDateFormat,
            final List<String> searchText,
            final int timestampAdjustment) throws ParseException {

        return createLogEntries(source, lines, timestampDateFormat, null, null, searchText, timestampAdjustment);
    }

    /**
     * Determine if the line starts with a matching timestamp
     *
     * @param line The line to match against.
     * @param sdf A date formatter to match the line's timestamp against.
     * @return extracted (matched) timestamp as mS; else 0
     */
    protected static long mSecTimeStampFromStartOfLine(final String line, final SimpleDateFormat sdf) {
        long ts = 0 ;

        if(null == sdf) {
            return ts;
        }

        String pattern      = sdf.toPattern();
        int patternLength   = pattern.length();

        if(null == line || line.length() < patternLength) {
            return ts;
        }

        String lineSegment  = line.substring(0, patternLength);

        try {
            Date d  = sdf.parse(lineSegment);
            ts      = d.getTime();
        } catch (ParseException e) {}

        return ts;
    }

    /**
     * Build a representation of a set of log entries from a single source.
     *
     * @param source The source of the log data
     * @param lines The lines of data that make up a log entry
     * @param timestampDateFormat A Simple date formatter String for the log entry's timestamp
     * @param startAt A String representation of the timestamp (matching the sdf) to start collecting log entries,
     *                null or empty implies no filtering
     * @param endAt A String representation of the timestamp (matching the sdf) to stop collecting log entries,
     *              null or empty implies no filtering.
     * @param searchText A list of text string to match a lig entry against.
     * @param timestampAdjustment A mS adjustment to the log entries timestamp.
     *
     * @return
     * @throws ParseException
     */
    public static List<LogEntry> createLogEntries(
            final String source,
            final List<String> lines,
            final String timestampDateFormat,
            final String startAt,
            final String endAt,
            final List<String> searchText,
            final int timestampAdjustment) throws ParseException {
        List<LogEntry> logEntries   = new ArrayList<LogEntry>();
        StringBuilder currentEntry  = new StringBuilder();
        boolean atStart             = true;
        SimpleDateFormat sdf        = new SimpleDateFormat(timestampDateFormat);
        long ts                     = 0;
        List<Long>timestampIndexes  = new ArrayList<>(lines.size());

        sdf.setLenient(false);

        int startingLogEntryIndex   = 0;
        int maxIndex                = lines.size() - 1;

        //////////////////////////////////////////////////////
        // Build a list of indexes where timestamps were found
        for(int index = 0 ; index <= maxIndex ; index++) {
            String line = lines.get(index);

            ts = mSecTimeStampFromStartOfLine(line, sdf);

            timestampIndexes.add(ts);

            /////////////////////////////////////////////////////
            // Initially skip all lines until we find a timestamp
            if (atStart) {
                if (0 == ts) {
                    continue;
                }

                startingLogEntryIndex = index;
                atStart = false;
            }
        }

        ///////////////////////////////////////////////
        // Examine the original list of log entry lines
        for(int index = startingLogEntryIndex ; index <= maxIndex ; index++) {
            ts = timestampIndexes.get(index);

            if(ts > 0) {
                //////////////////////
                // Find next timestamp
                int nextIndex = index + 1;

                while(nextIndex <= maxIndex) {
                    if(timestampIndexes.get(nextIndex) > 0) {
                        // Found the next log entry timestamp
                        break;
                    }
                    nextIndex++;
                }

                ////////////////////////////////////////////////////////////////////////
                // Extract all the lines up to but not including the next timestamp line
                currentEntry.setLength(0);

                for(int i = index ; i < nextIndex; i++) {
                    String nextLine = lines.get(i);

                    currentEntry.append(nextLine).append(LINE_SEP);
                }

                ////////////////////////////
                // Try and create a LogEntry
                LogEntry logEntry = createLogEntry(
                        source,
                        currentEntry.toString(),
                        sdf,
                        ts + timestampAdjustment,
                        startAt,
                        endAt,
                        searchText);

                if(null != logEntry) {
                    //////////////////////////////////////
                    // Did not get filtered out, so add it
                    logEntries.add(logEntry);
                }
            }
        }

        return logEntries;
    }

    /**
     * Build a representation of a set of log entries from a single file.
     *
     * @param logFilePath The file containing log entries.
     * @param timestampDateFormat A Simple date formatter String for the log entry's timestamp
     * @param startAt A String representation of the timestamp (matching the sdf) to start collecting log entries,
     *                null or empty implies no filtering
     * @param endAt A String representation of the timestamp (matching the sdf) to stop collecting log entries,
     *              null or empty implies no filtering.
     * @param searchText A list of text string to match a lig entry against.
     * @param timestampAdjustment A mS adjustment to the log entries timestamp.
     *
     * @return
     * @throws FileNotFoundException
     * @throws ParseException
     */
    public static List<LogEntry> createLogEntries(
            final String logFilePath,
            final String timestampDateFormat,
            final String startAt,
            final String endAt,
            final List<String> searchText,
            final int timestampAdjustment) throws FileNotFoundException, ParseException {
        File file = new File(logFilePath);
        List<LogEntry> logEntries   = null;
        List<String> lines          = new ArrayList<String>();

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()){
                lines.add(scanner.nextLine());
            }

            logEntries =
                    createLogEntries(
                            logFilePath,
                            lines,
                            timestampDateFormat,
                            startAt,
                            endAt,
                            searchText,
                            timestampAdjustment);
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
            out.println(String.format("# %2d %s", i, theSource));
            i++;
        }
        out.println("#");

        out.println("# Time sorted log entries:");

        int lastIndex   = -1;
        long lastTs     = -1;

        for(LogEntry logEntry : logEntries) {
            /////////////////////////////////////////////////////////////////////////////
            // Depending on the source position, where in the line we display the payload
            String dts  =   logEntry.getDisplayTimeStamp();
            source      =   logEntry.getSource();
            long rawTs  =   logEntry.getRawTimeStamp();
            int index   =   sources.indexOf(source);
            long diffTs =   -1;
            String pad1 =   "";
            String pad2 =   "";

            if(-1 == lastTs) {
                diffTs = 0;
            } else {
                diffTs  = rawTs - lastTs;
            }
            lastTs = rawTs;

            pad1 = String.format("%s%2d%9d %20s",
                    (index != lastIndex ? "*" : "."),
                    (index + 1),
                    diffTs,
                    dts);

            pad2 = String.format("%s%2d%9s %-20s",
                    ".",
                    (index + 1),
                    ".",
                    ".");

            lastIndex = index;

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

    static void usage(final String timestampDateFormat) {
        System.err.println("");
        System.err.println("LogViewer: View multiple log files in a single time ascending order list.");
        System.err.println("");
        System.err.println("Usage: [=t=TS] [=s=TS] [=e=TS] [=f=T [=f=T] ...] [=a=N,N...] logfile logfile ...");
        System.err.println("");
        System.err.println("   =t=TS   Set the log entry TimeStamp formatter to TS (default is '" + timestampDateFormat + "')");
        System.err.println("   =s=TS   Set the starting TimeStamp (TS) for filtering log entries.");
        System.err.println("   =e=TS   Set the ending TimeStamp (TS) for filtering log entries.");
        System.err.println("   =f=T    Set the text to find (case sensitive) for filtering log entries, can be multiple.");
        System.err.println("   =a=N,.. Set the mS timestamp offset adjustment for the relevant log file's entries.");
        System.err.println("");
        System.err.println("Notes:");
        System.err.println("");
        System.err.println("The merged time ascending list is written to stdout.");
        System.err.println("");
        System.err.println("Command line values override everything else, and start/end timestamps (if specified)");
        System.err.println("HAVE to be in the same format as the log entry formatter.");
        System.err.println("");
        System.err.println("If =s= and =e= are set to an empty value (or not specified), no filtering will be enabled for");
        System.err.println("that value, else they HAVE to match the TimeStamp format EXACTLY.");
        System.err.println("");

        System.exit(1);
    }

    /**
     * Validate that start end timestamps are valid and in the right order
     *
     * @param timestampDateFormat
     * @param startAt
     * @param endAt
     * @throws ParseException
     */
    protected static void validateFilterRanges (
            final String timestampDateFormat,
            final String startAt,
            final String endAt)  {

        long startTs                = -1;
        long endTs                  = -1;
        SimpleDateFormat sdf        = null;

        try {
            sdf = new SimpleDateFormat(timestampDateFormat);
        } catch(Exception e) {
            throw new RuntimeException("Problems with timestampDateFormat [" + timestampDateFormat + "]", e) ;
        }

        ///////////////////////////////
        // Build any time range filters
        if (null != startAt && startAt.trim().length() > 0) {
            Date dS = null;
            try {
                dS = sdf.parse(startAt);
            } catch (ParseException e) {
                throw new RuntimeException(
                        "Problems with startAt timestampDateFormat ["
                                + startAt
                                + "], it doesn't match ["
                                + timestampDateFormat
                                + "]");
            }
            startTs = dS.getTime();
        }

        if (null != endAt && endAt.trim().length() > 0) {
            Date dS = null;
            try {
                dS = sdf.parse(endAt);
            } catch (ParseException e) {
                throw new RuntimeException(
                        "Problems with endAt timestampDateFormat ["
                                + endAt
                                + "], it doesn't match ["
                                + timestampDateFormat
                                + "]");
            }
            endTs = dS.getTime();
        }

        /////////////////////////////////////////////
        // Check if both start and end were specified
        if(startTs > -1 && endTs > -1) {
            if(startTs > endTs) {
                throw new RuntimeException(
                                "Invalid range, start filter timestamp [" + startAt +
                                "] > snd filter timestamp [" + endAt+
                                "]") ;
            }
        }
    }

    /**
     * Build an Integer list of ms offsets for the log files speciffied on the command line.
     *
     * @param logfileCount How many log files were specified on the command line
     * @param timestampAdjustments The comma separated list of mS adjustments for each files specified.
     *
     * @return An Integer list representation of the command line parameter.
     */
    protected static List<Integer> timestampAdjustments(final int logfileCount, String timestampAdjustments) {
        List<Integer> adjustments   = new ArrayList<>();
        List<Integer> tsAdjustments = new ArrayList<>();

        if(logfileCount > 0) {
            if(null != timestampAdjustments && timestampAdjustments.trim().length() > 0) {
                StringTokenizer st = new StringTokenizer(timestampAdjustments, ",");

                try {
                    while (st.hasMoreTokens()) {
                        tsAdjustments.add(new Integer(st.nextToken()));
                    }
                } catch(NumberFormatException e) {
                    throw new RuntimeException("Invalid timestampAdjustments [" + timestampAdjustments + "]", e);
                }
            }

            for(int i = 0 ; i < logfileCount ; i++) {
                if(i < tsAdjustments.size()) {
                    adjustments.add(tsAdjustments.get(i));
                } else {
                    adjustments.add(0);
                }
            }
        }

        return adjustments;
    }

    /**
     * For usage from the command line
     *
     * @param args
     * @throws IOException
     * @throws ParseException
     */
    public static void main(final String[] args) throws FileNotFoundException, ParseException {
        String timestampDateFormat                      = "yyyy-MM-dd HH:mm:ss,SSS";
        String startAt                                  = null;
        String endAt                                    = null;
        String timestampAdjustments                     = null;
        List<String> searchText                         = new ArrayList<>();
        final String NAME                               = "LogViewer";
        final String VERSION                            = "1.1";

        System.out.println(String.format("# %s - v%s", NAME, VERSION));

        System.out.print("# Cmd line: ");

        for(String arg : args) {
            System.out.print(arg + " ");
        }
        System.out.println();

        if(args.length < 1) {
            usage(timestampDateFormat);
        }

        List<String>logFiles                = new ArrayList<>();
        String cmdLineStartAt               = null;
        String cmdLineEndAt                 = null;
        String cmdLineDateFormat            = null;
        String cmdLineTimestampAdjustments  = null ;

        for(String filePath : args) {
            if (filePath.startsWith("=s=")) {
                cmdLineStartAt = filePath.substring(3);
            } else if (filePath.startsWith("=e=")) {
                cmdLineEndAt = filePath.substring(3);
            } else if(filePath.startsWith("=t=")) {
                cmdLineDateFormat = filePath.substring(3);
            } else if(filePath.startsWith("=a=")) {
                cmdLineTimestampAdjustments = filePath.substring(3);
            } else if(filePath.startsWith("=f=")) {
                searchText.add(filePath.substring(3));
            } else {
                if(! logFiles.contains(filePath)) {
                    logFiles.add(filePath);
                } else {
                    System.out.println("# Not processing duplicate file [" + filePath + "]");
                }
            }
        }

        /////////////////////////////////////////////
        // Check that specified files can be accessed
        boolean allFilesFound = true;

        for(String logFile : logFiles) {
            if(! new File(logFile).exists()) {
                System.err.println("File [" + logFile + "] cannot be accessed.");
                allFilesFound = false;
            }
        }

        if(!allFilesFound || logFiles.size() < 1) {
            usage(timestampDateFormat);
        }

        // Override any value from those on the command line
        if(null != cmdLineStartAt) {
            startAt = cmdLineStartAt;
        }

        if(null != cmdLineEndAt) {
            endAt = cmdLineEndAt;
        }

        if(null != cmdLineDateFormat) {
            timestampDateFormat = cmdLineDateFormat;
        }

        if(null != cmdLineTimestampAdjustments) {
            timestampAdjustments = cmdLineTimestampAdjustments;
        }

        validateFilterRanges(timestampDateFormat, startAt, endAt);

        List<List<LogEntry>> logs   = new ArrayList<>();
        List<String> sources        = new ArrayList<>();
        List<Integer>adjustments    = timestampAdjustments(logFiles.size(), timestampAdjustments);

        for(int i = 0 ; i < logFiles.size() ; i++) {
            String logFilePath      = logFiles.get(i);
            int tsAdjustment    = 0 ;

            if(i < adjustments.size()) {
                tsAdjustment = adjustments.get(i);
            }

            List<LogEntry> logEntries =
                    createLogEntries(
                            logFilePath,
                            timestampDateFormat,
                            startAt,
                            endAt,
                            searchText,
                            tsAdjustment);

            logs.add(logEntries);
            sources.add(logFilePath);
        }

        List<LogEntry> timeSortedLogEntries = Utils.timeSortLists(logs);

        Utils.displayList(timeSortedLogEntries, sources, "");
    }
}
