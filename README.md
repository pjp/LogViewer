# LogViewer

A utility to read multiple log files and display the log entries in time ascending order

For this to work, the utility has to know the format of the log entries timestamp (SimpleDateFormat)

The default is 'yyyy-MM-dd HH:mm:ss,SSS', this can be changed as per the example below.

java -cp logviewer-1.0-SNAPSHOT.jar com.insight.utils.Utils 
    '=t=[yyyy/MM/dd HH:mm:ss.SSS]' 
    server20160520.log server20160521.log

Also, start and end timestamps can be specified to cut down the time range needed, e.g.

java -cp logviewer-1.0-SNAPSHOT.jar com.insight.utils.Utils 
    '=t=yyyy-MM-dd HH:mm:ss.SSS' 
    '=s=2016-05-20 22:00:00.000' 
    '=e=2016-05-21 02:00:00.000' 
    server20160520.log server20160521.log

Note that the start and end timestamps HAVE to match the defined timestamp format.