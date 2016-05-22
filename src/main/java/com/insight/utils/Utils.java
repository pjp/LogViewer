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
     * @return
     * @throws ParseException
     */
    protected static LogEntry createLogEntry(
            final String source,
            final String data,
            final SimpleDateFormat sdf,
            final long rawTimeStamp,
            final String startAt,
            final String endAt) throws ParseException {
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
            logEntry = new LogEntry(source, rawTimeStamp, displayTimeStamp, payload);
        }

        return logEntry;
    }

    /**
     * Build a representation of a set of log entries from a single source.
     *
     * @param source The source of the log data
     * @param lines The lines of data that make up a log entry
     * @param timestampDateFormat A Simple date formatter String for the log entry's timestamp
     * @return
     * @throws ParseException
     */
    protected static List<LogEntry> createLogEntries(
            final String source,
            final List<String> lines,
            final String timestampDateFormat,
            final int timestampAdjustment) throws ParseException {

        return createLogEntries(source, lines, timestampDateFormat, null, null, timestampAdjustment);
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
     * @return
     * @throws ParseException
     */
    public static List<LogEntry> createLogEntries(
            final String source,
            final List<String> lines,
            final String timestampDateFormat,
            final String startAt,
            final String endAt,
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
                        endAt);

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
     * @param timestampDateFormat A Simple date formatter String for the log entry's timestamp
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
            final String timestampDateFormat,
            final String startAt,
            final String endAt,
            final int timestampAdjustment) throws FileNotFoundException, ParseException {
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
                            timestampDateFormat,
                            startAt,
                            endAt,
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
            out.println("# " + i + " " + theSource);
            i++;
        }
        out.println("#");

        out.println("# Time sorted log entries:");

        int lastIndex = -1;

        for(LogEntry logEntry : logEntries) {
            /////////////////////////////////////////////////////////////////////////////
            // Depending on the source position, where in the line we display the payload
            String dts  =   logEntry.getDisplayTimeStamp();
            source      =   logEntry.getSource();
            int index   =   sources.indexOf(source);
            String sol  =   " ";

            if(index != lastIndex) {
                sol = ".";
                lastIndex = index;
            }

            String pad1 =   String.format("%s%d %20s", sol, index + 1, dts);
            String pad2 =   String.format("%s%d %20s", " ", index + 1, "");

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
        System.err.println("Usage: [=t=TS] [=s=TS] [=e=TS] [=a=N,N...] logfile logfile ...");
        System.err.println("");
        System.err.println("   =t=TS   Set the log entry TimeStamp formatter to TS (default is '" + timestampDateFormat + "')");
        System.err.println("   =s=TS   Set the starting TimeStamp (TS) for filtering log entries.");
        System.err.println("   =e=TS   Set the ending TimeStamp (TS) for filtering log entries.");
        System.err.println("   =a=N,.. Set the mS timestamp offset adjustment for the 2nd log file entries onwards.");
        System.err.println("");
        System.err.println("Notes:");
        System.err.println("");
        System.err.println("The merged time ascending list is written to stdout.");
        System.err.println("");
        System.err.println("Command line values override everything else, and start/end timestamps (if specified)");
        System.err.println("HAVE to be in the same format as the log entry formatter.");
        System.err.println("");
        System.err.println("If =s= and =e= are set to an empty value, no filtering will be enabled for that value,");
        System.err.println("else they HAVE to match the TimeStamp format EXACTLY.");
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
     *
     * @param logFiles
     * @param timestampAdjustments
     * @return
     */
    protected static List<Integer> timestampAdjustments(List<String> logFiles, String timestampAdjustments) {
        List<Integer> adjustments   = new ArrayList<>();
        List<Integer> tsAdjustments = new ArrayList<>();

        if(null != logFiles && logFiles.size() > 0) {
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

            /////////////////////////////
            // First one is the reference
            tsAdjustments.add(0, 0);

            for(int i = 0 ; i < logFiles.size() ; i++) {
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

        for(String filename : args) {
            if (filename.startsWith("=s=")) {
                cmdLineStartAt = filename.substring(3);
            } else if (filename.startsWith("=e=")) {
                cmdLineEndAt = filename.substring(3);
            } else if(filename.startsWith("=t=")) {
                cmdLineDateFormat = filename.substring(3);
            } else if(filename.startsWith("=a=")) {
                cmdLineTimestampAdjustments = filename.substring(3);
            } else {
                logFiles.add(filename);
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
        List<Integer>adjustments    = timestampAdjustments(logFiles, timestampAdjustments);

        for(int i = 0 ; i < logFiles.size() ; i++) {
            String logFile      = logFiles.get(i);
            int tsAdjustment    = 0 ;

            if(i < adjustments.size()) {
                tsAdjustment = adjustments.get(i);
            }

            List<LogEntry> logEntries =
                    createLogEntries(
                            logFile,
                            timestampDateFormat,
                            startAt,
                            endAt,
                            tsAdjustment);

            logs.add(logEntries);
            sources.add(getFileNameFromFullPath(logFile));
        }

        List<LogEntry> timeSortedLogEntries = Utils.timeSortLists(logs);

        Utils.displayList(timeSortedLogEntries, sources, "");
    }

}
