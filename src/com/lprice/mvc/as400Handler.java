package com.lprice.mvc;
import static com.lprice.mvc.lputils.print;

import com.ibm.as400.access.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

public class as400Handler {
    /** method updates the JTable with list of current user's active jobs. Omits QPVA jobs.
            * @param as400 the AS400 object to get the spool data from
     * @throws PropertyVetoException
     * @throws AS400SecurityException
     * @throws ObjectDoesNotExistException
     * @throws IOException
     * @throws InterruptedException
     * @throws ErrorCompletingRequestException
     */
    public static void updateSpoolTable(AS400 as400, JTable tblSpool) throws PropertyVetoException, AS400SecurityException, ObjectDoesNotExistException, IOException, InterruptedException, ErrorCompletingRequestException {
        // Create list of all jobs on server
        JobList jList = new JobList(as400);
//        System.out.println("Created list of all jobs on system");

        // Filter job list to current user only
        jList.addJobSelectionCriteria(JobList.SELECTION_USER_NAME, JobList.SELECTION_USER_NAME_CURRENT); // Filter job list to active user
        jList.addJobSelectionCriteria(JobList.SELECTION_PRIMARY_JOB_STATUS_OUTQ, false); // Filter job list to active only
//        System.out.println("Job list filtered to current user");

        // Create enumeration of job list
        Enumeration jobs = jList.getJobs();

        // Iterate through job list enumeration to print the status of each job
//        System.out.println("Beginning iteration through user jobs");

        // Create array of values (will be used to populate the dialogue window)
        java.util.List<String> columns = new ArrayList<>();
        List<String[]> values = new ArrayList<>();

        // create column name list
        columns.add("Job Name"); columns.add("Job Number"); columns.add("Start Date");

        // add job data rows to array
        if (jobs.hasMoreElements()) {
            while (jobs.hasMoreElements()) {
                Job job = (Job) jobs.nextElement(); // Advance the enumeration forward
                String jName = job.getName(); // Get job name
                String jNum = job.getNumber(); // Get job number
                Date jDate = job.getDate(); // Get date
                values.add(new String[]{jName, jNum, jDate.toString()});
            }
        }

//        //print contents of horizon job array list to console
//        for (String[] arr : values) {System.out.println(Arrays.toString(arr));}
//
//        System.out.println("Iteration complete");

        // Create a dataset. Will be added to table in next step
        TableModel tableModel = new DefaultTableModel(values.toArray(new Object[][]{}), columns.toArray());

        // Populate swing table with dataset
        tblSpool.setModel(tableModel);

        print("spool file table updated");
    }

}
