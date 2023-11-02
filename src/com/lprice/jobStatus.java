package com.lprice;

import com.ibm.as400.access.*;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import javax.sound.sampled.*;

//http://www.java2s.com/Tutorials/Java/Swing_How_to/JTable/Populate_JTable_Using_List.htm

public class jobStatus extends JFrame {
    private JTable tblSpoolFiles;
    private JPanel panel1;
    private JLabel lblUser;
    private JButton btnOK;
    private JButton btnRfsh;

    public List<String[]> getJobList (AS400 as400, String userName) throws PropertyVetoException, AS400SecurityException, ObjectDoesNotExistException, IOException, InterruptedException, ErrorCompletingRequestException {
        // Create list of all jobs on server
        JobList jList = new JobList(as400);
        System.out.println("Created list of all jobs on system");

        // Filter job list to current user only
        jList.addJobSelectionCriteria(JobList.SELECTION_USER_NAME, JobList.SELECTION_USER_NAME_CURRENT); // Filter job list to active user
        jList.addJobSelectionCriteria(JobList.SELECTION_PRIMARY_JOB_STATUS_OUTQ, false); // Filter job list to active only
        System.out.println("Job list filtered to current user");

        // Create enumeration of job list
        Enumeration jobs = jList.getJobs();

        // Iterate through job list enumeration to print the status of each job
        System.out.println("Beginning iteration through user jobs");

        // Populate array with active user jobs
        List<String[]> values = new ArrayList<String[]>();
        if (jobs.hasMoreElements()) {
            while (jobs.hasMoreElements()) {
                Job job = (Job) jobs.nextElement(); // Advance the enumeration forward
                if (!job.getName().contains("QPADEV")) {
                    String jName = job.getName(); // Get job name
                    String jNum = job.getNumber(); // Get job number
                    Date jDate = job.getDate(); // Get date
                    values.add(new String[]{jName, jNum, jDate.toString()});
                }
            }
        } else {
            messageBox("User \"" + userName + "\" does not have any active jobs.");
            System.out.println("class \"messageBox\" is complete");
            System.exit(1);
        }

        System.out.println("Iteration complete");

        return values;
    }

    public jobStatus(AS400 as400) throws PropertyVetoException, AS400SecurityException, ObjectDoesNotExistException, IOException, InterruptedException, ErrorCompletingRequestException {
        // Get username
        String userName = as400.getUserId();

        // Get array of active jobs
        List<String[]> values = getJobList(as400, userName);

        // Create table columns
        List<String> columns = new ArrayList<String>();
        columns.add("Job Name");
        columns.add("Job Number");
        columns.add("Start Date");

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Begin GUI portion
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        System.out.println("Constructing GUI for job selection");

        // Set up JFrame
        setContentPane(panel1); setTitle("Create Job Alert"); setSize(450, 300);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); // X button closes window
        setLocationRelativeTo(null); // Centered on screen

        // Set username label text
        lblUser.setText(userName);

        // Create JTable
        TableModel tableModel = new DefaultTableModel(values.toArray(new Object[][]{}), columns.toArray());
        tblSpoolFiles.setModel(tableModel);

        System.out.println("Showing job selection dialogue to user\nAwaiting selection...");
        setVisible(true);
        btnOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int iRow = tblSpoolFiles.getSelectedRow();
                String jobName = tblSpoolFiles.getValueAt(iRow, 0).toString();
                String jobNumber = tblSpoolFiles.getValueAt(iRow, 1).toString();
                System.out.println("User selected the following job:");
                System.out.println("    Job name: " + jobName + "; Job number: " + jobNumber);
                setVisible(false);
                try {
                    monitorJob(as400, jobName, userName, jobNumber);
                } catch (AS400SecurityException ex) {throw new RuntimeException(ex);} catch (ObjectDoesNotExistException ex) {throw new RuntimeException(ex);} catch (IOException ex) {throw new RuntimeException(ex);} catch (InterruptedException ex) {throw new RuntimeException(ex);} catch (ErrorCompletingRequestException ex) {throw new RuntimeException(ex);} catch (LineUnavailableException ex) {throw new RuntimeException(ex);}
            }
        });
        btnRfsh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the current list of active jobs
                List<String[]> values2;
                try {
                    values2 = getJobList(as400, userName);
                } catch (PropertyVetoException ex) {
                    throw new RuntimeException(ex);
                } catch (AS400SecurityException ex) {
                    throw new RuntimeException(ex);
                } catch (ObjectDoesNotExistException ex) {
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                } catch (ErrorCompletingRequestException ex) {
                    throw new RuntimeException(ex);
                }

                // Create a dataset. Will be added to table in next step
                TableModel tableModel = new DefaultTableModel(values2.toArray(new Object[][]{}), columns.toArray());

                // Populate swing table with dataset
                tblSpoolFiles.setModel(tableModel);
                setVisible(true);
            }
        });
    }

    public void monitorJob(AS400 as400, String jobName, String userName, String jobNumber) throws AS400SecurityException, ObjectDoesNotExistException, IOException, InterruptedException, ErrorCompletingRequestException, LineUnavailableException {

        // Get job
        Job job = new Job(as400, jobName, userName, jobNumber);

        // Get job status as string
        String jbStatus = job.getStatus().toString();
        System.out.println("Job status is: " + jbStatus.trim() + "\n" + "Beginning job monitor...");

        while (!job.getStatus().equals(Job.JOB_STATUS_OUTQ)) {
            // Wait a while.
            Thread.sleep(1000);
            // Refresh the attribute values.
            job.loadInformation();
        }

        LocalDateTime finishTime = LocalDateTime.now();

        ///////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////
        // Alert user when job has finished

        // Open the audio file
        InputStream in = new FileInputStream("assets/alert1.wav");
        AudioStream as = new AudioStream(in);
        // Play alert sound
        AudioPlayer.player.start(as);
        Thread.sleep(1500);

        String infoMessage = finishTime + "; Job status is: " + job.getStatus();
        System.out.println(infoMessage);

        messageBox(infoMessage);

        setVisible(true);
    }
    public void messageBox (String message){
        String html = "<html><body style='width: %1spx'>%1s";  // Format dialogue
        JOptionPane.showMessageDialog(null, String.format(html, 300, message));
        System.out.println("User clicked \"OK\"");
    }
    public static void main(String[] args) throws AS400SecurityException, ObjectDoesNotExistException, IOException, InterruptedException, ErrorCompletingRequestException, PropertyVetoException {
        // Create AS400 object
        AS400 as400 = new AS400("CHRZNTST");
        System.out.println("AS400 object created");

        as400.connectService(as400.DATAQUEUE);

        // Run the task
        tblViewer myTableDialog = new tblViewer(as400);

        // Disconnect from AS400
        as400.disconnectService(AS400.COMMAND);

    }
}
