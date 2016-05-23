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

    String TS1_START_SENTINAL           = "[";
    String TS1_END_SENTINAL             = "]";
    String TS1_FORMAT                   = "yyyy-MM-dd HH:mm:ss,SSS";
    String TS1_FORMAT_WITH_SENTINALS    = TS1_START_SENTINAL + TS1_FORMAT + TS1_END_SENTINAL;
    String TS1                          = "2016-05-16 03:34:56,789";
    String TS2                          = "2016-05-16 06:34:56,789";
    String TS3                          = "2016-05-16 09:34:56,789";
    String TS4                          = "2016-05-16 12:34:56,789";
    String TS5                          = "2016-05-16 15:34:56,789";
    String TS6                          = "2016-05-16 18:34:56,789";
    String TS1_WITH_SENTINALS           = TS1_START_SENTINAL + TS1 + TS1_END_SENTINAL;
    String TS2_WITH_SENTINALS           = TS1_START_SENTINAL + TS2 + TS1_END_SENTINAL;
    String TS3_WITH_SENTINALS           = TS1_START_SENTINAL + TS3 + TS1_END_SENTINAL;
    String TS4_WITH_SENTINALS           = TS1_START_SENTINAL + TS4 + TS1_END_SENTINAL;
    String TS5_WITH_SENTINALS           = TS1_START_SENTINAL + TS5 + TS1_END_SENTINAL;
    String TS6_WITH_SENTINALS           = TS1_START_SENTINAL + TS6 + TS1_END_SENTINAL;
    String SOURCE                       = "inline1";
    String SOURCE2                      = "inline2";
    String SOURCE3                      = "inline3";
    int tsAdjustment                    = 0;
    String searchText                   = null;
    
    public void testCreateEmptyLogEntriesList() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;

        List<LogEntry> logEntries = Utils.createLogEntries(SOURCE, lines, TS1_FORMAT_WITH_SENTINALS, searchText, tsAdjustment);

        assertNotNull(logEntries);
        assertEquals(0, logEntries.size());
    }

    public void testCreateSingleLogEntriesListWithOneLine() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;
        List<LogEntry> logEntries   = null;

        lines.add(TS1_WITH_SENTINALS + " WooHoo");
        logEntries = Utils.createLogEntries(SOURCE, lines, TS1_FORMAT_WITH_SENTINALS, searchText, tsAdjustment);

        assertNotNull(logEntries);
        assertEquals(1, logEntries.size());
    }

    public void testCreateSingleLogEntriesListWithOneNonStartingLine() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;

        lines.add(" WooHoo");
        List<LogEntry> logEntries = Utils.createLogEntries(SOURCE, lines, TS1_FORMAT_WITH_SENTINALS, searchText, tsAdjustment);

        assertNotNull(logEntries);
        assertEquals(0, logEntries.size());
    }

    public void testCreateSingleLogEntriesListWithTwoNonStartingLine() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;

        lines.add(" WooHoo1");
        lines.add(" WooHoo2");
        List<LogEntry> logEntries = Utils.createLogEntries(SOURCE, lines, TS1_FORMAT_WITH_SENTINALS, searchText, tsAdjustment);

        assertNotNull(logEntries);
        assertEquals(0, logEntries.size());
    }

    public void testCreateSingleLogEntriesListWithTwoNonStartingLineThenStartingLine() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;

        lines.add(" WooHoo1");
        lines.add(" WooHoo2");
        lines.add(TS1_WITH_SENTINALS + " WooHoo");

        List<LogEntry> logEntries = Utils.createLogEntries(SOURCE, lines, TS1_FORMAT_WITH_SENTINALS, searchText, tsAdjustment);

        assertNotNull(logEntries);
        assertEquals(1, logEntries.size());
    }

    public void testCreateTwoLogEntriesListWithOneLineEach() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;

        lines.add(TS1_WITH_SENTINALS + " WooHoo 1");
        lines.add(TS1_WITH_SENTINALS + " WooHoo 2");
        List<LogEntry> logEntries = Utils.createLogEntries(SOURCE, lines, TS1_FORMAT_WITH_SENTINALS, searchText, tsAdjustment);

        assertNotNull(logEntries);
        assertEquals(2, logEntries.size());
    }

    public void testCreateTwoLogEntriesListWithFirstWithOneLineSecondWithTwo() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;
        LogEntry logEntry           = null;

        lines.add(TS1_WITH_SENTINALS + " WooHoo 1");
        lines.add(TS1_WITH_SENTINALS + " WooHoo 2");
        lines.add("WooHoo 2a");

        List<LogEntry> logEntries = Utils.createLogEntries(SOURCE, lines, TS1_FORMAT_WITH_SENTINALS, searchText, tsAdjustment);

        assertNotNull(logEntries);
        assertEquals(2, logEntries.size());

        logEntry = logEntries.get(0);
        assertEquals(" WooHoo 1" + Utils.LINE_SEP, logEntry.getPayload());

        logEntry = logEntries.get(1);
        assertEquals(" WooHoo 2" + Utils.LINE_SEP + "WooHoo 2a" + Utils.LINE_SEP, logEntry.getPayload());

    }

    public void testLogFileCompareOne() throws ParseException, FileNotFoundException {
        String file1                = "src/test/resources/gbsheusrchp08-EMEA-ES-PROD.log";
        String file2                = "src/test/resources/gbsheusrchp07-EMEA-ES-PROD.log" ;
        String file3                = "src/test/resources/gbsheusrchp01-EMEA-ES-PROD.log" ;
        String startAt              = "[2016-05-16 00:00:00,000]";
        String endAt                = "[2016-05-16 05:00:00,000]";
        List<LogEntry> logEntries   = null;
        List<List<LogEntry>> logs   = new ArrayList<>();

        logEntries = Utils.createLogEntries(file1, TS1_FORMAT_WITH_SENTINALS, startAt, endAt, searchText, tsAdjustment);
        logs.add(logEntries);

        logEntries = Utils.createLogEntries(file2, TS1_FORMAT_WITH_SENTINALS, startAt, endAt, searchText, tsAdjustment);
        logs.add(logEntries);

        logEntries = Utils.createLogEntries(file3, TS1_FORMAT_WITH_SENTINALS, startAt, endAt, searchText, tsAdjustment);
        logs.add(logEntries);

        List<LogEntry> timeSortedLogEntries = Utils.timeSortLists(logs);

        List<String> sources = new ArrayList<>();
        sources.add(file1);
        sources.add(file2);
        sources.add(file3);

        PrintStream out = new PrintStream(new FileOutputStream(new File("result.log")));

        Utils.emitList(timeSortedLogEntries, sources, "Filtered, start at " + startAt + ", end at " + endAt, out);
    }

    public void testLogFileCompareTwo() throws ParseException, FileNotFoundException {
        String file1                = "src/test/resources/jb-server.log";
        String file2                = "src/test/resources/jb-error.log";
        String startAt              = "2016-05-17 17:45:57,849";
        String endAt                = "2016-05-19 18:46:01,793";
        List<LogEntry> logEntries   = null;
        List<List<LogEntry>> logs   = new ArrayList<>();

        logEntries = Utils.createLogEntries(file1, TS1_FORMAT, startAt, endAt, searchText, tsAdjustment);
        logs.add(logEntries);

        logEntries = Utils.createLogEntries(file2, TS1_FORMAT, startAt, endAt, searchText, tsAdjustment);
        logs.add(logEntries);

        List<LogEntry> timeSortedLogEntries = Utils.timeSortLists(logs);

        List<String> sources = new ArrayList<>();
        sources.add(file1);
        sources.add(file2);

        PrintStream out = new PrintStream(new FileOutputStream(new File("result2.log")));

        Utils.emitList(timeSortedLogEntries, sources, "Filtered, start at " + startAt + ", end at " + endAt, out);
    }

    public void testCreateTwoLogEntriesListWithTwoLinesEach() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;

        lines.add(TS1_WITH_SENTINALS + " WooHoo 1a");
        lines.add("WooHoo 1b");

        lines.add(TS2_WITH_SENTINALS + " WooHoo 2a");
        lines.add("WooHoo 2b");

        List<List<LogEntry>> logs = new ArrayList<>();

        List<LogEntry> logEntries = Utils.createLogEntries(SOURCE, lines, TS1_FORMAT_WITH_SENTINALS, searchText, tsAdjustment);
        logs.add(logEntries);

        assertNotNull(logEntries);
        assertEquals(2, logEntries.size());

        LogEntry logEntry = logEntries.get(0);
        assertEquals(
                " WooHoo 1a" + Utils.LINE_SEP + "WooHoo 1b" + Utils.LINE_SEP,
                logEntry.getPayload());
        assertEquals("[2016-05-16 03:34:56,789]", logEntry.getDisplayTimeStamp());

        logEntry = logEntries.get(1);
        assertEquals(
                " WooHoo 2a" + Utils.LINE_SEP + "WooHoo 2b" + Utils.LINE_SEP,
                logEntry.getPayload());
        assertEquals("[2016-05-16 06:34:56,789]", logEntry.getDisplayTimeStamp());

        List<LogEntry> timeSortedLogEntries = Utils.timeSortLists(logs);
        List<String> sources = new ArrayList<>();
        sources.add(SOURCE);
        sources.add(SOURCE2);

        Utils.displayList(timeSortedLogEntries, sources, "");
    }

    public void testFilteredLists() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;
        String startAt              = null;
        String endAt                = null;
        List<LogEntry> logEntries   = null;

        lines.add(TS2_WITH_SENTINALS + " WooHoo 2");
        lines.add(TS3_WITH_SENTINALS + " WooHoo 3" + Utils.LINE_SEP + "WooHoo 3a");
        lines.add(TS4_WITH_SENTINALS + " WooHoo 4");

        ///////////////////////////////////
        startAt              = TS1_WITH_SENTINALS;
        endAt                = TS5_WITH_SENTINALS;

        logEntries =
                Utils.createLogEntries(
                        SOURCE,
                        lines,
                        TS1_FORMAT_WITH_SENTINALS,
                        startAt,
                        endAt, searchText, tsAdjustment);

        assertEquals(3, logEntries.size());
        assertEquals(" WooHoo 2" + Utils.LINE_SEP, logEntries.get(0).getPayload());
        assertEquals(" WooHoo 3" + Utils.LINE_SEP + "WooHoo 3a" + Utils.LINE_SEP, logEntries.get(1).getPayload());
        assertEquals(" WooHoo 4" + Utils.LINE_SEP, logEntries.get(2).getPayload());

        ///////////////////////////////////
        startAt              = TS1_WITH_SENTINALS;
        endAt                = TS5_WITH_SENTINALS;
        searchText          = "WooHoo 3a";

        logEntries =
                Utils.createLogEntries(
                        SOURCE,
                        lines,
                        TS1_FORMAT_WITH_SENTINALS,
                        startAt,
                        endAt, searchText, tsAdjustment);

        assertEquals(1, logEntries.size());
        assertEquals(" WooHoo 3" + Utils.LINE_SEP + "WooHoo 3a" + Utils.LINE_SEP, logEntries.get(0).getPayload());

        ///////////////////////////////////
        startAt              = TS3_WITH_SENTINALS;
        endAt                = TS5_WITH_SENTINALS;
        searchText           = null ;

        logEntries =
                Utils.createLogEntries(
                        SOURCE,
                        lines,
                        TS1_FORMAT_WITH_SENTINALS,
                        startAt,
                        endAt, searchText, tsAdjustment);

        assertEquals(2, logEntries.size());
        assertEquals(" WooHoo 3" + Utils.LINE_SEP + "WooHoo 3a" + Utils.LINE_SEP, logEntries.get(0).getPayload());
        assertEquals(" WooHoo 4" + Utils.LINE_SEP, logEntries.get(1).getPayload());

        ///////////////////////////////////
        startAt              = TS3_WITH_SENTINALS;
        endAt                = TS3_WITH_SENTINALS;

        logEntries =
                Utils.createLogEntries(
                        SOURCE,
                        lines,
                        TS1_FORMAT_WITH_SENTINALS,
                        startAt,
                        endAt, searchText, tsAdjustment);

        assertEquals(1, logEntries.size());
        assertEquals(" WooHoo 3" + Utils.LINE_SEP + "WooHoo 3a" + Utils.LINE_SEP, logEntries.get(0).getPayload());

        ///////////////////////////////////
        startAt              = TS1_WITH_SENTINALS;
        endAt                = TS1_WITH_SENTINALS;

        logEntries =
                Utils.createLogEntries(
                        SOURCE,
                        lines,
                        TS1_FORMAT_WITH_SENTINALS,
                        startAt,
                        endAt, searchText, tsAdjustment);

        assertEquals(0, logEntries.size());

        ///////////////////////////////////
        startAt              = TS5_WITH_SENTINALS;
        endAt                = TS5_WITH_SENTINALS;

        logEntries =
                Utils.createLogEntries(
                        SOURCE,
                        lines,
                        TS1_FORMAT_WITH_SENTINALS,
                        startAt,
                        endAt, searchText, tsAdjustment);

        assertEquals(0, logEntries.size());
    }

    public void testCreateThreeListWithTwoLogEntriesEach() throws ParseException {
        List<List<LogEntry>> logs   = new ArrayList<>();
        List<String> linesFirst     = new ArrayList<String>() ;
        List<String> linesSecond    = new ArrayList<String>() ;
        List<String> linesThird     = new ArrayList<String>() ;

        linesFirst.add(TS1_WITH_SENTINALS + " WooHoo 1a");
        linesFirst.add("WooHoo 1a1");
        linesFirst.add(TS5_WITH_SENTINALS + " WooHoo 1b");

        linesSecond.add(TS2_WITH_SENTINALS + " WooHoo 2a");
        linesSecond.add(TS3_WITH_SENTINALS + " WooHoo 2b");
        linesSecond.add("WooHoo 2b1");
        linesSecond.add("WooHoo 2b2");

        linesThird.add(TS4_WITH_SENTINALS + " WooHoo 3a");
        linesThird.add("WooHoo 3a1");
        linesThird.add(TS6_WITH_SENTINALS + " WooHoo 3b");
        linesThird.add("WooHoo 3b1");

        List<LogEntry> logEntriesFirst =
                Utils.createLogEntries(SOURCE, linesFirst, TS1_WITH_SENTINALS, searchText, tsAdjustment);

        List<LogEntry> logEntriesSecond =
                Utils.createLogEntries(SOURCE2, linesSecond, TS1_WITH_SENTINALS, searchText, tsAdjustment);

        List<LogEntry> logEntriesThird =
                Utils.createLogEntries(SOURCE3, linesThird, TS1_WITH_SENTINALS, searchText, tsAdjustment);

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

        Utils.displayList(timeSortedLogEntries, sources, "");
    }

    public void testCreateTwoLogEntriesListWithFirstHavingTwoLines() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;

        lines.add(TS1_WITH_SENTINALS + " WooHoo 1a");
        lines.add("WooHoo 1b");

        lines.add(TS1_WITH_SENTINALS + " WooHoo 2a");

        List<LogEntry> logEntries = Utils.createLogEntries(SOURCE, lines, TS1_FORMAT_WITH_SENTINALS, searchText, tsAdjustment);

        assertNotNull(logEntries);
        assertEquals(2, logEntries.size());
    }

    public void testCreateTwoLogEntriesListWithSecondHavingTwoLines() throws ParseException {
        List<String> lines          = new ArrayList<String>() ;

        lines.add(TS1_WITH_SENTINALS + " WooHoo 1a");

        lines.add(TS1_WITH_SENTINALS + " WooHoo 2a");
        lines.add("WooHoo 2b");

        List<LogEntry> logEntries = Utils.createLogEntries(SOURCE, lines, TS1_FORMAT_WITH_SENTINALS, searchText, tsAdjustment);

        assertNotNull(logEntries);
        assertEquals(2, logEntries.size());
    }


    public void testExtractingTimeStampFromLine() {
        String line            = null;
        SimpleDateFormat sdf   = null;
        long mS                 = 0;

        assertEquals(0, Utils.mSecTimeStampFromStartOfLine(line, sdf));

        ///////////////////////////////////////////
        line = "";
        assertEquals(0, Utils.mSecTimeStampFromStartOfLine(line, sdf));

        ///////////////////////////////////////////
        line = " ";
        assertEquals(0, Utils.mSecTimeStampFromStartOfLine(line, sdf));

        ///////////////////////////////////////////
        line = "abc";
        assertEquals(0, Utils.mSecTimeStampFromStartOfLine(line, sdf));

        ///////////////////////////////////////////
        ///////////////////////////////////////////
        sdf = new SimpleDateFormat("[" + TS1_FORMAT + "]");
        sdf.setLenient(false);
        line = null;
        assertEquals(0, Utils.mSecTimeStampFromStartOfLine(line, sdf));

        ///////////////////////////////////////////
        line = "";
        assertEquals(0, Utils.mSecTimeStampFromStartOfLine(line, sdf));

        ///////////////////////////////////////////
        line = " ";
        assertEquals(0, Utils.mSecTimeStampFromStartOfLine(line, sdf));

        ///////////////////////////////////////////
        line = TS1;
        mS = Utils.mSecTimeStampFromStartOfLine(line, sdf);
        assertEquals(0, mS);

        ///////////////////////////////////////////
        line = TS1 + "]";
        mS = Utils.mSecTimeStampFromStartOfLine(line, sdf);
        assertEquals(0, mS);

        ///////////////////////////////////////////
        line = " " + TS1;
        mS = Utils.mSecTimeStampFromStartOfLine(line, sdf);
        assertEquals(0, mS);

        ///////////////////////////////////////////
        line = " " + TS1 + " ";
        mS = Utils.mSecTimeStampFromStartOfLine(line, sdf);
        assertEquals(0, mS);

        ///////////////////////////////////////////
        line = "[" + TS1 + " ";
        mS = Utils.mSecTimeStampFromStartOfLine(line, sdf);
        assertEquals(0, mS);

        ///////////////////////////////////////////
        line = " " + TS1 + "]";
        mS = Utils.mSecTimeStampFromStartOfLine(line, sdf);
        assertEquals(0, mS);

        ///////////////////////////////////////////
        line = "[ " + TS1 + "]";
        mS = Utils.mSecTimeStampFromStartOfLine(line, sdf);
        assertEquals(0, mS);

        ///////////////////////////////////////////
        line = " [" + TS1 + "]";
        mS = Utils.mSecTimeStampFromStartOfLine(line, sdf);
        assertEquals(0, mS);

        ///////////////////////////////////////////
        line = "[" + TS1 + "]";
        mS = Utils.mSecTimeStampFromStartOfLine(line, sdf);
        assertTrue(mS > 0);

        ///////////////////////////////////////////
        line = "[" + TS1 + "] ";
        mS = Utils.mSecTimeStampFromStartOfLine(line, sdf);
        assertTrue(mS > 0);

        ///////////////////////////////////////////
        ///////////////////////////////////////////
        sdf = new SimpleDateFormat(TS1_FORMAT + " ");
        sdf.setLenient(false);

        ///////////////////////////////////////////
        line = "[" + TS1 + "]";
        mS = Utils.mSecTimeStampFromStartOfLine(line, sdf);
        assertEquals(0, mS);

        ///////////////////////////////////////////
        line = " " + TS1 + "]";
        mS = Utils.mSecTimeStampFromStartOfLine(line, sdf);
        assertEquals(0, mS);

        ///////////////////////////////////////////
        line = TS1 + "]";
        mS = Utils.mSecTimeStampFromStartOfLine(line, sdf);
        assertEquals(0, mS);

        ///////////////////////////////////////////
        line = TS1 + "] ";
        mS = Utils.mSecTimeStampFromStartOfLine(line, sdf);
        assertEquals(0, mS);

        ///////////////////////////////////////////
        line = " " + TS1;
        mS = Utils.mSecTimeStampFromStartOfLine(line, sdf);
        assertEquals(0, mS);

        ///////////////////////////////////////////
        line = " " + TS1 + " ";
        mS = Utils.mSecTimeStampFromStartOfLine(line, sdf);
        assertEquals(0, mS);

        ///////////////////////////////////////////
        line = TS1 + " ";
        mS = Utils.mSecTimeStampFromStartOfLine(line, sdf);
        assertTrue(mS > 0);

        ///////////////////////////////////////////
        line = TS1 + " ]";
        mS = Utils.mSecTimeStampFromStartOfLine(line, sdf);
        assertTrue(mS > 0);
    }
    
    public void testTimestampAdjustments() {
        List<String> files          = null;
        String cmdLine              = null ;
        List<Integer> adjustments   = null ;

        ////////////////////////////////////////////////////////
        adjustments = Utils.timestampAdjustments(files, cmdLine);
        assertNotNull(adjustments);
        assertEquals(0, adjustments.size());

        ////////////////////////////////////////////////////////
        files          = new ArrayList<>();

        adjustments = Utils.timestampAdjustments(files, cmdLine);
        assertNotNull(adjustments);
        assertEquals(0, adjustments.size());

        ////////////////////////////////////////////////////////
        cmdLine     = "";

        adjustments = Utils.timestampAdjustments(files, cmdLine);
        assertNotNull(adjustments);
        assertEquals(0, adjustments.size());

        ////////////////////////////////////////////////////////
        files.add("A");

        adjustments = Utils.timestampAdjustments(files, cmdLine);
        assertNotNull(adjustments);
        assertEquals(1, adjustments.size());
        assertEquals(new Integer(0), adjustments.get(0));

        ////////////////////////////////////////////////////////
        cmdLine     = "1";

        adjustments = Utils.timestampAdjustments(files, cmdLine);
        assertNotNull(adjustments);
        assertEquals(1, adjustments.size());
        assertEquals(new Integer(1), adjustments.get(0));

        ////////////////////////////////////////////////////////
        files.add("B");

        adjustments = Utils.timestampAdjustments(files, cmdLine);
        assertNotNull(adjustments);
        assertEquals(2, adjustments.size());
        assertEquals(new Integer(1), adjustments.get(0));
        assertEquals(new Integer(0), adjustments.get(1));

        ////////////////////////////////////////////////////////
        files.add("C");

        adjustments = Utils.timestampAdjustments(files, cmdLine);
        assertNotNull(adjustments);
        assertEquals(3, adjustments.size());
        assertEquals(new Integer(1), adjustments.get(0));
        assertEquals(new Integer(0), adjustments.get(1));
        assertEquals(new Integer(0), adjustments.get(2));

        ////////////////////////////////////////////////////////
        cmdLine     = "1,2";

        adjustments = Utils.timestampAdjustments(files, cmdLine);
        assertNotNull(adjustments);
        assertEquals(3, adjustments.size());
        assertEquals(new Integer(1), adjustments.get(0));
        assertEquals(new Integer(2), adjustments.get(1));
        assertEquals(new Integer(0), adjustments.get(2));

        ////////////////////////////////////////////////////////
        cmdLine     = "1,-2,3";

        adjustments = Utils.timestampAdjustments(files, cmdLine);
        assertNotNull(adjustments);
        assertEquals(3, adjustments.size());
        assertEquals(new Integer(1), adjustments.get(0));
        assertEquals(new Integer(-2), adjustments.get(1));
        assertEquals(new Integer(3), adjustments.get(2));

        ////////////////////////////////////////////////////////
        cmdLine     = ",-2,3";

        adjustments = Utils.timestampAdjustments(files, cmdLine);
        assertNotNull(adjustments);
        assertEquals(3, adjustments.size());
        assertEquals(new Integer(-2), adjustments.get(0));
        assertEquals(new Integer(3), adjustments.get(1));
        assertEquals(new Integer(0), adjustments.get(2));

        ////////////////////////////////////////////////////////
        cmdLine     = "1,-Two,3";

        try {
            adjustments = Utils.timestampAdjustments(files, cmdLine);
            fail("Should have thrown an exception");
        } catch(RuntimeException e) {}

    }
}
