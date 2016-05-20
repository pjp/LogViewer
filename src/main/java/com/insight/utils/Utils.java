package com.insight.utils;

import javax.swing.text.Position;
import java.io.*;
import java.text.ParseException;
import java.text.ParsePosition;
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

        displayTimeStamp    = data.substring(0, patternLength);
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
            final String timestampDateFormat) throws ParseException {

        return createLogEntries(source, lines, timestampDateFormat, null, null);
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
            final String endAt) throws ParseException {
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
                        ts,
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
                            timestampDateFormat,
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
            String pad1 =   String.format("%d  %20s", index + 1, dts);
            String pad2 =   String.format("%d  %20s", index + 1, "");

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

    static void usage(
            final String propertyFileName,
            final String timestampDateFormatPropName,
            final String timestampDateFormat,
            final String startAtPropName,
            final String endAtPropName) {

        System.err.println("View multiple log files in a single time ascending order list.");
        System.err.println("");
        System.err.println("Usage: [@FILE] [@-] [=s=TS] [=e=TS] [#t#TS] logfile logfile ...");
        System.err.println("");
        System.err.println("   @-      Do not load any properties file.");
        System.err.println("   @FILE   A properties file to load configuration values from.");
        System.err.println("   =s=TS   Set the starting TimeStamp (TS) for filtering log entries.");
        System.err.println("   =e=TS   Set the ending TimeStamp (TS) for filtering log entries.");
        System.err.println("   #t#TS   Set the log entry TimeStamp formatter to TS");
        System.err.println("");
        System.err.println("Notes:");
        System.err.println("");
        System.err.println("The merged time ascending list is written to stdout.");
        System.err.println("");
        System.err.println("Command line values override everything else, and HAVE to be in");
        System.err.println("the same format as the default values.");
        System.err.println("");
        System.err.println("If #s# and #e# are set to an empty value, no filtering will be enabled for that value.");
        System.err.println("");
        System.err.println("By default, property file [" + propertyFileName + "] in the current") ;
        System.err.println("directory will be read if it exists. To prevent this, either explicitly");
        System.err.println("specify an alternative file '@filename', else specify '@-' to prevent using a default.");
        System.err.println("");
        System.err.println("The property file can contain these keys, default values are used if");
        System.err.println("the property is not specified, or no property file specified.");
        System.err.println("");
        System.err.println(String.format("Key: [%-35s], default is '%s'", timestampDateFormatPropName, timestampDateFormat));
        System.err.println(String.format("Key: [%-35s], default is '%s'", startAtPropName, "empty or not set -> Earliest"));
        System.err.println(String.format("Key: [%-35s], default is '%s'", endAtPropName, "empty or not set -> Latest"));

        System.exit(1);
    }


    /**
     * For usage from the command line
     *
     * @param args
     * @throws IOException
     * @throws ParseException
     */
    public static void main(final String[] args) throws IOException, ParseException, FileNotFoundException {
        String timestampDateFormat                      = "yyyy-MM-dd HH:mm:ss,SSS ";
        String startAt                                  = null;
        String endAt                                    = null;
        String propertyFileName                         = "logviewer.properties";
        final String timestampDateFormatPropName        = "logentry.timestamp.date.format";
        final String startAtPropName                    = "logentry.filter.start.date";
        final String endAtPropName                      = "logentry.filter.end.date";
        final String NAME                               = "LogViewer";
        final String VERSION                            = "1.0";

        System.out.println(String.format("# %s - v%s", NAME, VERSION));

        System.out.print("# Cmd line: ");

        for(String arg : args) {
            System.out.print(arg + " ");
        }
        System.out.println();

        if(args.length < 1) {
            usage(  propertyFileName,
                    timestampDateFormatPropName,
                    timestampDateFormat,
                    startAtPropName,
                    endAtPropName);
        }

        List<String>logFiles                = new ArrayList<>();
        boolean usingDefaultPropertyFile    = true;
        String cmdLineStartAt               = null;
        String cmdLineEndAt                 = null;
        String cmdLineDateFormat            = null;
        String cmdLinePropertyFileName      = null;

        for(String filename : args) {
            if (filename.startsWith("@-")) {
                usingDefaultPropertyFile = false;
                cmdLinePropertyFileName = filename;
            } else if (filename.startsWith("@")) {
                cmdLinePropertyFileName = filename.substring(1);
                usingDefaultPropertyFile = false;
            } else if (filename.startsWith("=s=")) {
                cmdLineStartAt = filename.substring(3);
            } else if (filename.startsWith("=e=")) {
                cmdLineEndAt = filename.substring(3);
            } else if(filename.startsWith("=t=")) {
                cmdLineDateFormat = filename.substring(3);
            } else {
                logFiles.add(filename);
            }
        }

        if(logFiles.size() < 1) {
            usage(  propertyFileName,
                    timestampDateFormatPropName,
                    timestampDateFormat,
                    startAtPropName,
                    endAtPropName);
        }

        // Pain, need to do some dancing around for the property file name.
        if(null != cmdLinePropertyFileName) {
            if(cmdLinePropertyFileName.startsWith("@-")) {
                propertyFileName = null;
            } else {
                propertyFileName = cmdLinePropertyFileName;
            }
        }

        if(null != propertyFileName) {
            try {
                Properties props = new Properties();
                props.load(new FileInputStream(propertyFileName));

                timestampDateFormat     = props.getProperty(timestampDateFormatPropName, timestampDateFormat);
                startAt                 = props.getProperty(startAtPropName, startAt);
                endAt                   = props.getProperty(endAtPropName, endAt);
                System.out.println("# Loaded property file [" + propertyFileName + "]");
            } catch(IOException e) {
                if(! usingDefaultPropertyFile) {
                    throw e;
                }
            }
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

        List<List<LogEntry>> logs   = new ArrayList<>();
        List<String> sources        = new ArrayList<>();

        for(String logFile : logFiles) {
            List<LogEntry> logEntries =
                    createLogEntries(
                            logFile,
                            timestampDateFormat,
                            startAt,
                            endAt);

            logs.add(logEntries);
            sources.add(getFileNameFromFullPath(logFile));
        }

        List<LogEntry> timeSortedLogEntries = Utils.timeSortLists(logs);

        Utils.displayList(timeSortedLogEntries, sources, "");
    }
}
