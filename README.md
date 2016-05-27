# LogViewer

## A utility to read multiple log files and display the log entries in time ascending order

For this to work, the utility has to know the format of the log entries timestamp (SimpleDateFormat)

The default is 'yyyy-MM-dd HH:mm:ss,SSS', this can be changed as per the example below.

java -cp logviewer-1.0-SNAPSHOT.jar com.insight.utils.Utils 
    '=t=[yyyy/MM/dd HH:mm:ss.SSS]' 
    server20160520.log server20160521.log
    
## Filtering

### Time Range

Start and end timestamps can be specified to cut down the time range needed, e.g.

java -cp logviewer-1.0-SNAPSHOT.jar com.insight.utils.Utils 
    '=t=yyyy-MM-dd HH:mm:ss.SSS' 
    '=s=2016-05-20 22:00:00.000' 
    '=e=2016-05-21 02:00:00.000' 
    server20160520.log server20160521.log

Note that the start and end timestamps HAVE to match the defined timestamp format.

### Text 

Text can be used for filtering as well e.g. 

java -cp logviewer-1.0-SNAPSHOT.jar com.insight.utils.Utils 
    '=t=yyyy-MM-dd HH:mm:ss.SSS' 
    '=s=2016-05-20 22:00:00.000' 
    '=e=2016-05-21 02:00:00.000'
    '=f= ERROR' '=f= WARN '
    server20160520.log server20160521.log
    
Here two text filters are specified (read as ERROR or WARN must appear within the log record


## Timestamp Adjustments

If there is a difference between the clock on the computers that created the log files that are to be compared, LogViewer
can add a timestamp adjustment (either positive or negative) e.g.

java -cp logviewer-1.0-SNAPSHOT.jar com.insight.utils.Utils 
    '=t=yyyy-MM-dd HH:mm:ss.SSS' 
    '=s=2016-05-20 22:00:00.000' 
    '=e=2016-05-21 02:00:00.000' 
    '=a=10,0,-2'
    server20160520.log server20160521.log server20160522.log

This will add 10mS to every log entry's timestamp for the 1st log file 'server20160520.log', no adjustment for the 
2nd log file 'server20160521.log' and subtract 2ms from the 3rd log entry's timestamp in file 'server20160522.log' 
before sorting all the log entries. Note that the log entry timestamp written to the output will reflect this change, 
and therefore be different to the actual log entry timestamp from the source file.

## Output

Each log entry line of the output is preceeded by a number indicating which file produced that line, there is a list at
the beginning of the output, mapping a filename to this number. If a number is preceeded with a period, it indicates that the
line is from a different file to the previous line.

Each log entry line (see sample below) of the output starts with 4 values:-

1st: \* or .         \*= the file this log entry is from, is different from the last log entry's file; else .= same file

2nd: file number    The index (1 relative) of the filename from the command line this entry is from.

3rd: delta mS       The difference (in mS) between this log entry's timestamp and the last log entry.

4th: . or timestamp The timestamp of this log's entry; else .= the line is from the same single log entry.

