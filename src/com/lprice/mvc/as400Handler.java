package com.lprice.mvc;
import static com.lprice.mvc.lputils.print;

import com.ibm.as400.access.*;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

public class as400Handler {

//    public static int active = JobList.SELECTION_PRIMARY_JOB_STATUS_ACTIVE;
//    public static int outq = JobList.SELECTION_PRIMARY_JOB_STATUS_OUTQ;
//    public static int jobq = JobList.SELECTION_PRIMARY_JOB_STATUS_JOBQ;
//    public static int jstat = JobList.SELECTION_ACTIVE_JOB_STATUS;

    /** method updates the JTable with list of current user's active jobs. Omits QPVA jobs.
     * @param as400 the AS400 object to get the spool data from
     * @param tblSpool the JTable object to be updated with as400 jobs
     * @param inactive true to pull inactive jobs; false to pull active
     * @param verbose true if you want activity to print to console
     * @throws PropertyVetoException
     * @throws AS400SecurityException
     * @throws ObjectDoesNotExistException
     * @throws IOException
     * @throws InterruptedException
     * @throws ErrorCompletingRequestException
     */
    public static void updateSpoolTable(AS400 as400, JTable tblSpool, boolean inactive, boolean verbose) throws PropertyVetoException, AS400SecurityException, ObjectDoesNotExistException, IOException, InterruptedException, ErrorCompletingRequestException, ParseException {
        // Create list of all jobs on server
        JobList jList = new JobList(as400);
        if (verbose){print("Created list of all jobs on system");}

        // Filter job list
        jList.addJobSelectionCriteria(JobList.SELECTION_USER_NAME, JobList.SELECTION_USER_NAME_CURRENT); // Filter job list to active user
        jList.addJobSelectionCriteria(JobList.SELECTION_PRIMARY_JOB_STATUS_OUTQ, inactive); // Filter job list to active only

        if (verbose){print("Job list filtered to current user");}

        // Create enumeration of job list
        Enumeration jobs = jList.getJobs();

        // Iterate through job list enumeration to print the status of each job
        if (verbose){print("Beginning iteration through user jobs");}

        // Create array of values (will be used to populate the dialogue window)
        java.util.List<String> columns = new ArrayList<>();
        List<String[]> values = new ArrayList<>();

        // create column name list
        columns.add("Job Name"); columns.add("Job Number"); columns.add("Start Date");

        // date formatting
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // add job data rows to array
        if (jobs.hasMoreElements()) {
            while (jobs.hasMoreElements()) {
                Job job = (Job) jobs.nextElement(); // Advance the enumeration forward
                String jName = job.getName(); // Get job name
                String jNum = job.getNumber(); // Get job number
                Date jDate = job.getDate(); // Get date
                String jDateString = outputDateFormat.format(jDate); // Format date to something more usable
                values.add(new String[]{jName, jNum, jDateString});
            }
        }

        values.sort(Comparator.comparing(row -> row[2],Comparator.reverseOrder()));

        //print contents of horizon job array list to console
        if (verbose){for (String[] arr : values) {
            print(Arrays.toString(arr));
        }}

        if (verbose){print("Iteration complete");}

        // Create a dataset. Will be added to table in next step
        TableModel tableModel = new DefaultTableModel(values.toArray(new Object[][]{}), columns.toArray());

        // Populate swing table with dataset
        tblSpool.setModel(tableModel);

        print("spool file table updated");
    }
    
    public static void monitorJob(AS400 as400, String jobName, String userName, String jobNumber) throws AS400SecurityException, ObjectDoesNotExistException, IOException, InterruptedException, ErrorCompletingRequestException, LineUnavailableException {

        // print system details
        print(as400.getUserId());
        print(as400.getSystemName());
        print(String.valueOf(new JobList(as400)));

        // Get job
        Job job = new Job(as400, jobName, userName, jobNumber);

        // Get job status as string
        String jbStatus = job.getStatus().toString();
        print("Job status is: " + jbStatus.trim());
        print("Beginning job monitor...");

        while (!job.getStatus().equals(Job.JOB_STATUS_OUTQ)) {
            // Wait a while.
            Thread.sleep(1000);
            // Refresh the attribute values.
            job.loadInformation();
        }

        LocalDateTime finishTime = LocalDateTime.now();

        // Alert user when job has finished
        ///////////////////////////////////////////////////////////
        // Open an input stream  to the audio file.
        InputStream in = new FileInputStream("src/com/lprice/assets/alert1.wav");
        // Create an AudioStream object from the input stream.
        AudioStream as = new AudioStream(in);
        // Use the static class member "player" from class AudioPlayer to play clip.
        AudioPlayer.player.start(as);
        Thread.sleep(1500);
        ///////////////////////////////////////////////////////////
        //java.awt.Toolkit.getDefaultToolkit().beep();
        String infoMessage = finishTime + "; Job status is: " + job.getStatus();
        print(infoMessage);
        messageBox(infoMessage);
        System.exit(1);
    }
    public static void messageBox (String message){
        String html = "<html><body style='width: %1spx'>%1s";

        JOptionPane.showMessageDialog(
                null, String.format(html, 300, message));

        print("User clicked \"OK\"");


    }
}
