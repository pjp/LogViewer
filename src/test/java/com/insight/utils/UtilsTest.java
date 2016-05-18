package com.insight.utils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class UtilsTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public UtilsTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( UtilsTest.class );
    }

    String timestampStartSentinal       = "[";
    String timestampEndSentinal         = "]";
    SimpleDateFormat SDF                = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    String TIMESTAMP1                   = timestampStartSentinal + "2016-05-16 03:34:56,789" + timestampEndSentinal;
    String TIMESTAMP2                   = timestampStartSentinal + "2016-05-16 06:34:56,789" + timestampEndSentinal;
    String TIMESTAMP3                   = timestampStartSentinal + "2016-05-16 09:34:56,789" + timestampEndSentinal;
    String TIMESTAMP4                   = timestampStartSentinal + "2016-05-16 12:34:56,789" + timestampEndSentinal;
    String TIMESTAMP5                   = timestampStartSentinal + "2016-05-16 15:34:56,789" + timestampEndSentinal;
    String TIMESTAMP6                   = timestampStartSentinal + "2016-05-16 18:34:56,789" + timestampEndSentinal;
    String SOURCE                       = "inline1";
    String SOURCE2                      = "inline2";
    String SOURCE3                      = "inline3";

    public void testCreateEmptyLogEntriesList() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;

        List<LogEntry> logEntries = Utils.createLogEntries(SOURCE, lines, timestampStartSentinal, timestampEndSentinal, SDF);

        assertNotNull(logEntries);
        assertEquals(0, logEntries.size());
    }

    public void testCreateSingleLogEntriesListWithOneLine() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;

        lines.add(TIMESTAMP1 + " WooHoo");
        List<LogEntry> logEntries = Utils.createLogEntries(SOURCE, lines, timestampStartSentinal, timestampEndSentinal, SDF);

        assertNotNull(logEntries);
        assertEquals(1, logEntries.size());
    }

    public void testCreateSingleLogEntriesListWithOneNonStartingLine() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;

        lines.add(" WooHoo");
        List<LogEntry> logEntries = Utils.createLogEntries(SOURCE, lines, timestampStartSentinal, timestampEndSentinal, SDF);

        assertNotNull(logEntries);
        assertEquals(0, logEntries.size());
    }

    public void testCreateSingleLogEntriesListWithTwoNonStartingLine() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;

        lines.add(" WooHoo1");
        lines.add(" WooHoo2");
        List<LogEntry> logEntries = Utils.createLogEntries(SOURCE, lines, timestampStartSentinal, timestampEndSentinal, SDF);

        assertNotNull(logEntries);
        assertEquals(0, logEntries.size());
    }

    public void testCreateSingleLogEntriesListWithTwoNonStartingLineThenStartingLine() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;

        lines.add(" WooHoo1");
        lines.add(" WooHoo2");
        lines.add(TIMESTAMP1 + " WooHoo");

        List<LogEntry> logEntries = Utils.createLogEntries(SOURCE, lines, timestampStartSentinal, timestampEndSentinal, SDF);

        assertNotNull(logEntries);
        assertEquals(1, logEntries.size());
    }

    public void testCreateTwoLogEntriesListWithOneLineEach() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;

        lines.add(TIMESTAMP1 + " WooHoo 1");
        lines.add(TIMESTAMP1 + " WooHoo 2");
        List<LogEntry> logEntries = Utils.createLogEntries(SOURCE, lines, timestampStartSentinal, timestampEndSentinal, SDF);

        assertNotNull(logEntries);
        assertEquals(2, logEntries.size());
    }

    public void testLogFileCompare() throws ParseException, FileNotFoundException {
        String file1                = "src/test/resources/gbsheusrchp08-EMEA-ES-PROD.log";
        String file2                = "src/test/resources/gbsheusrchp07-EMEA-ES-PROD.log" ;
        String file3                = "src/test/resources/gbsheusrchp01-EMEA-ES-PROD.log" ;
        String startAt              = "2016-05-16 00:00:00,000";
        String endAt                = "2016-05-16 05:00:00,000";
        List<LogEntry> logEntries   = null;
        List<List<LogEntry>> logs   = new ArrayList<>();

        logEntries = Utils.createLogEntries(file1, timestampStartSentinal, timestampEndSentinal, SDF, startAt, endAt);
        logs.add(logEntries);

        logEntries = Utils.createLogEntries(file2, timestampStartSentinal, timestampEndSentinal, SDF, startAt, endAt);
        logs.add(logEntries);

        logEntries = Utils.createLogEntries(file3, timestampStartSentinal, timestampEndSentinal, SDF, startAt, endAt);
        logs.add(logEntries);

        List<LogEntry> timeSortedLogEntries = Utils.timeSortLists(logs);

        List<String> sources = new ArrayList<>();
        sources.add(Utils.getFileNameFromFullPath(file1));
        sources.add(Utils.getFileNameFromFullPath(file2));
        sources.add(Utils.getFileNameFromFullPath(file3));

        PrintStream out = new PrintStream(new FileOutputStream(new File("result.log")));

        Utils.emitList(timeSortedLogEntries, sources, 10, "Filtered, start at " + startAt + ", end at " + endAt, out);
    }

    public void testCreateTwoLogEntriesListWithTwoLinesEach() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;

        lines.add(TIMESTAMP1 + " WooHoo 1a");
        lines.add("WooHoo 1b");

        lines.add(TIMESTAMP2 + " WooHoo 2a");
        lines.add("WooHoo 2b");

        List<List<LogEntry>> logs = new ArrayList<>();

        List<LogEntry> logEntries = Utils.createLogEntries(SOURCE, lines, timestampStartSentinal, timestampEndSentinal, SDF);
        logs.add(logEntries);

        assertNotNull(logEntries);
        assertEquals(2, logEntries.size());

        LogEntry logEntry = logEntries.get(0);
        assertEquals(
                " WooHoo 1a" + Utils.LINE_SEP + "WooHoo 1b" + Utils.LINE_SEP,
                logEntry.getPayload());
        assertEquals("2016-05-16 03:34:56,789", logEntry.getDisplayTimeStamp());

        logEntry = logEntries.get(1);
        assertEquals(
                " WooHoo 2a" + Utils.LINE_SEP + "WooHoo 2b" + Utils.LINE_SEP,
                logEntry.getPayload());
        assertEquals("2016-05-16 06:34:56,789", logEntry.getDisplayTimeStamp());

        List<LogEntry> timeSortedLogEntries = Utils.timeSortLists(logs);
        List<String> sources = new ArrayList<>();
        sources.add(SOURCE);
        sources.add(SOURCE2);

        Utils.displayList(timeSortedLogEntries, sources, 10, "");
    }

    public void testFilteredLists() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;
        String startAt              = null;
        String endAt                = null;
        List<LogEntry> logEntries   = null;

        lines.add(TIMESTAMP2 + " WooHoo 2");
        lines.add(TIMESTAMP3 + " WooHoo 3");
        lines.add(TIMESTAMP4 + " WooHoo 4");

        ///////////////////////////////////
        startAt              = TIMESTAMP1.substring(1, TIMESTAMP1.length());
        endAt                = TIMESTAMP5.substring(1, TIMESTAMP5.length());

        logEntries =
                Utils.createLogEntries(
                        SOURCE,
                        lines,
                        timestampStartSentinal,
                        timestampEndSentinal,
                        SDF,
                        startAt,
                        endAt);

        assertEquals(3, logEntries.size());
        assertEquals(" WooHoo 2" + Utils.LINE_SEP, logEntries.get(0).getPayload());
        assertEquals(" WooHoo 3" + Utils.LINE_SEP, logEntries.get(1).getPayload());
        assertEquals(" WooHoo 4" + Utils.LINE_SEP, logEntries.get(2).getPayload());

        ///////////////////////////////////
        startAt              = TIMESTAMP3.substring(1, TIMESTAMP3.length());
        endAt                = TIMESTAMP5.substring(1, TIMESTAMP5.length());

        logEntries =
                Utils.createLogEntries(
                        SOURCE,
                        lines,
                        timestampStartSentinal,
                        timestampEndSentinal,
                        SDF,
                        startAt,
                        endAt);

        assertEquals(2, logEntries.size());
        assertEquals(" WooHoo 3" + Utils.LINE_SEP, logEntries.get(0).getPayload());
        assertEquals(" WooHoo 4" + Utils.LINE_SEP, logEntries.get(1).getPayload());

        ///////////////////////////////////
        startAt              = TIMESTAMP3.substring(1, TIMESTAMP3.length());
        endAt                = TIMESTAMP3.substring(1, TIMESTAMP3.length());

        logEntries =
                Utils.createLogEntries(
                        SOURCE,
                        lines,
                        timestampStartSentinal,
                        timestampEndSentinal,
                        SDF,
                        startAt,
                        endAt);

        assertEquals(1, logEntries.size());
        assertEquals(" WooHoo 3" + Utils.LINE_SEP, logEntries.get(0).getPayload());

        ///////////////////////////////////
        startAt              = TIMESTAMP1.substring(1, TIMESTAMP1.length());
        endAt                = TIMESTAMP1.substring(1, TIMESTAMP1.length());

        logEntries =
                Utils.createLogEntries(
                        SOURCE,
                        lines,
                        timestampStartSentinal,
                        timestampEndSentinal,
                        SDF,
                        startAt,
                        endAt);

        assertEquals(0, logEntries.size());

        ///////////////////////////////////
        startAt              = TIMESTAMP5.substring(1, TIMESTAMP5.length());
        endAt                = TIMESTAMP5.substring(1, TIMESTAMP5.length());

        logEntries =
                Utils.createLogEntries(
                        SOURCE,
                        lines,
                        timestampStartSentinal,
                        timestampEndSentinal,
                        SDF,
                        startAt,
                        endAt);

        assertEquals(0, logEntries.size());
    }

    public void testCreateThreeListWithTwoLogEntriesEach() throws ParseException {
        List<List<LogEntry>> logs   = new ArrayList<>();
        List<String> linesFirst     = new ArrayList<String>() ;
        List<String> linesSecond    = new ArrayList<String>() ;
        List<String> linesThird     = new ArrayList<String>() ;

        linesFirst.add(TIMESTAMP1 + " WooHoo 1a");
        linesFirst.add("WooHoo 1a1");
        linesFirst.add(TIMESTAMP5 + " WooHoo 1b");

        linesSecond.add(TIMESTAMP2 + " WooHoo 2a");
        linesSecond.add(TIMESTAMP3 + " WooHoo 2b");
        linesSecond.add("WooHoo 2b1");
        linesSecond.add("WooHoo 2b2");

        linesThird.add(TIMESTAMP4 + " WooHoo 3a");
        linesThird.add("WooHoo 3a1");
        linesThird.add(TIMESTAMP6 + " WooHoo 3b");
        linesThird.add("WooHoo 3b1");

        List<LogEntry> logEntriesFirst =
                Utils.createLogEntries(SOURCE, linesFirst, timestampStartSentinal, timestampEndSentinal, SDF);

        List<LogEntry> logEntriesSecond =
                Utils.createLogEntries(SOURCE2, linesSecond, timestampStartSentinal, timestampEndSentinal, SDF);

        List<LogEntry> logEntriesThird =
                Utils.createLogEntries(SOURCE3, linesThird, timestampStartSentinal, timestampEndSentinal, SDF);

        ///////////////////////
        // Order doesn't matter
        logs.add(logEntriesSecond);
        logs.add(logEntriesThird);
        logs.add(logEntriesFirst);


        List<LogEntry> timeSortedLogEntries = Utils.timeSortLists(logs);
        List<String> sources = new ArrayList<>();
        sources.add(SOURCE);
        sources.add(SOURCE2);
        sources.add(SOURCE3);

        Utils.displayList(timeSortedLogEntries, sources, 10, "");
    }

    public void testCreateTwoLogEntriesListWithFirstHavingTwoLines() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;

        lines.add(TIMESTAMP1 + " WooHoo 1a");
        lines.add("WooHoo 1b");

        lines.add(TIMESTAMP1 + " WooHoo 2a");

        List<LogEntry> logEntries = Utils.createLogEntries(SOURCE, lines, timestampStartSentinal, timestampEndSentinal, SDF);

        assertNotNull(logEntries);
        assertEquals(2, logEntries.size());
    }

    public void testCreateTwoLogEntriesListWithSecondHavingTwoLines() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;

        lines.add(TIMESTAMP1 + " WooHoo 1a");

        lines.add(TIMESTAMP1 + " WooHoo 2a");
        lines.add("WooHoo 2b");

        List<LogEntry> logEntries = Utils.createLogEntries(SOURCE, lines, timestampStartSentinal, timestampEndSentinal, SDF);

        assertNotNull(logEntries);
        assertEquals(2, logEntries.size());
    }
}
