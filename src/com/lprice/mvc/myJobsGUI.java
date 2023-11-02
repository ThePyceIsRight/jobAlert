package com.lprice.mvc;
import static com.lprice.mvc.as400Handler.*;

import com.ibm.as400.access.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class myJobsGUI implements ActionListener, ComponentListener {
    JPanel cards; //a panel that uses CardLayout
    CardLayout cl; //a cardlayout
    final static String LOGIN = "Card with card with login";
    final static String MAIN = "Card with main menu";
    final static String STATUS = "Card with job status";
    final static String POPUP = "Card with popup message";
    JButton toggleButton;
    JButton refreshButton;
    JButton btnSignon;

    JTextField userField;
    JPasswordField passField;
    JLabel validation;
    AS400 as400;

    JTable tblSpool;
    JLabel usrLabel;

    // set up easy date time methods
    public static String now(){
        LocalDateTime localDT = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        return localDT.format(format);
    }

    // set up easy print methods
    public static void print(String msg){System.out.println(now()+" "+msg);}
    public static void print(String[] msg){for (String s : msg) {System.out.println(now()+" "+s);}}
    public static void print(int[] msg){for (int i : msg) {System.out.println(now()+" "+i);}}


    public void addComponentToPane(Container pane) {
        //Put the toggle button in a JPanel to get a nicer look.
        JPanel topMenuPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topMenuPanel.setBackground(Color.GRAY);
        toggleButton = new JButton("Toggle");
        toggleButton.addActionListener(this);
        topMenuPanel.add(usrLabel = new JLabel("TOGGLE BUTTON FOR DEBUGGING"));
        topMenuPanel.add(toggleButton);
        //==========================================================
        //Create the "cards".
        //==========================================================

        //card 1====================
        JPanel card1 = new JPanel();
        card1.setName(LOGIN);
        card1.setLayout(new BoxLayout(card1, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        userField = new JTextField(10);
        userField.addComponentListener(this);
        panel.add(userField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(passField = new JPasswordField(10), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.EAST;
        btnSignon = new JButton("submit");
        btnSignon.addActionListener(new signon());
        panel.add(btnSignon, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.WEST;
        validation = new JLabel("Please ensure both fields are entered");
        validation.setVisible(false);
        panel.add(validation, gbc);

        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.add(panel, new GridBagConstraints());


        card1.add(wrapperPanel);

        // card 2====================
        JPanel card2 = new JPanel();
        card2.setName(MAIN);
        card2.setLayout(new BoxLayout(card2, BoxLayout.PAGE_AXIS));
        card2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        refreshButton = new JButton("Refresh Data");
        refreshButton.addActionListener(new refreshListener());

        String[] columnNames = {"job", "status", "started"};
        Object[][] data = {{"job1", "status1", "started1"}};
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        tblSpool = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(tblSpool);
        scrollPane.setPreferredSize(new Dimension(card2.getWidth(), 200)); // Set preferred size here

        JButton btnSubmit = new JButton("submit");
        btnSubmit.addActionListener(new submitListener());

        card2.add(refreshButton);
        card2.add(Box.createVerticalStrut(10));
        card2.add(scrollPane);
        card2.add(Box.createVerticalStrut(10));
        card2.add(btnSubmit);


        // card 3====================
        JPanel card3 = new JPanel();
        card3.setName(STATUS);
        card3.setLayout(new BoxLayout(card3, BoxLayout.PAGE_AXIS));
        JButton btnBack = new JButton("back");
        btnBack.addActionListener(new backListener());

        card3.add(new JTextField("Beginning JOB monitor for the following JOB: {jobNameHere}"));
        card3.add(btnBack);


        // card 4====================
        JPanel card4 = new JPanel();
        card4.setName(POPUP);
        card4.setLayout(new BoxLayout(card4, BoxLayout.PAGE_AXIS));
        JLabel loginWarning = new JLabel("your login failed");
        JButton btnReturn = new JButton("back");
        btnReturn.addActionListener(new returnListener());

        card4.add(loginWarning);
        card4.add(btnReturn);

        //==========================================================
        // create the panel that contains the "cards".
        //==========================================================


        cards = new JPanel(new CardLayout());
        cards.setBackground(Color.green);
        cards.add(card1, LOGIN);
        cards.add(card2, MAIN);
        cards.add(card3, STATUS);
        cards.add(card4, POPUP);

        //this event listener just helps set the component focus when certain cards are shown
        cards.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                // If LOGIN card is shown
                if (e.getComponent().getName().equals(LOGIN)) {
                    userField.requestFocusInWindow();
                    print("card listener was triggered - attempted to set the textfield in focus");
                }
            }
        });

        pane.add(topMenuPanel, BorderLayout.PAGE_START);
        pane.add(cards, BorderLayout.CENTER);
        panel.getRootPane().setDefaultButton(btnSignon); // allow submit button to be triggered with "enter"
    }

    @Override
    public void componentResized(ComponentEvent e) {

    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {
        print("ok so the implement for component shown worked");
    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    public class signon implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            if (userField.getText().equals("") || new String(passField.getPassword()).equals("")) {
                validation.setVisible(true);
                print(userField.getText()+"\n"+new String(passField.getPassword()));
                print("user failed to log in");

            } else {
                //auth
                as400 = new AS400("CHRZNPRD", userField.getText(), new String(passField.getPassword()));
                try {
                    updateSpoolTable(as400, tblSpool);
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
                validation.setVisible(false);
                CardLayout cl = (CardLayout)(cards.getLayout());
                cl.show(cards, MAIN);
                print("user logged in");
            }
        }
    }
    public class submitListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            CardLayout cl = (CardLayout)(cards.getLayout());
            cl.show(cards, STATUS);
            print("viewing status menu");
        }
    }
    public class backListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            CardLayout cl = (CardLayout)(cards.getLayout());
            cl.show(cards, MAIN);
            print("going back to main menu");
        }
    }

    public class returnListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            CardLayout cl = (CardLayout)(cards.getLayout());
            cl.show(cards, LOGIN);
            print("exiting notice");
        }
    }

    public void actionPerformed(ActionEvent evt) {
        CardLayout cl = (CardLayout)(cards.getLayout());
        cl.next(cards);
        print("toggled");
    }

    public class refreshListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                updateSpoolTable(as400, tblSpool);
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
        }
    }

    // Create the GUI and show it.
    public static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("myJobsGUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        myJobsGUI demo = new myJobsGUI();

        demo.addComponentToPane(frame.getContentPane());

        //Display the window.
        frame.pack();
        showOnScreen(1,frame);
        frame.setVisible(true);
    }

//    /**
//     * method updates the JTable with list of current user's active jobs. Omits QPVA jobs.
//     * @param as400 the AS400 object to get the spool data from
//     * @throws PropertyVetoException
//     * @throws AS400SecurityException
//     * @throws ObjectDoesNotExistException
//     * @throws IOException
//     * @throws InterruptedException
//     * @throws ErrorCompletingRequestException
//     */
//    private void updateSpoolTable(AS400 as400) throws PropertyVetoException, AS400SecurityException, ObjectDoesNotExistException, IOException, InterruptedException, ErrorCompletingRequestException {
//        // Create list of all jobs on server
//        JobList jList = new JobList(as400);
////        System.out.println("Created list of all jobs on system");
//
//        // Filter job list to current user only
//        jList.addJobSelectionCriteria(JobList.SELECTION_USER_NAME, JobList.SELECTION_USER_NAME_CURRENT); // Filter job list to active user
//        jList.addJobSelectionCriteria(JobList.SELECTION_PRIMARY_JOB_STATUS_OUTQ, false); // Filter job list to active only
////        System.out.println("Job list filtered to current user");
//
//        // Create enumeration of job list
//        Enumeration jobs = jList.getJobs();
//
//        // Iterate through job list enumeration to print the status of each job
////        System.out.println("Beginning iteration through user jobs");
//
//        // Create array of values (will be used to populate the dialogue window)
//        java.util.List<String> columns = new ArrayList<>();
//        List<String[]> values = new ArrayList<>();
//
//        // create column name list
//        columns.add("Job Name"); columns.add("Job Number"); columns.add("Start Date");
//
//        // add job data rows to array
//        if (jobs.hasMoreElements()) {
//            while (jobs.hasMoreElements()) {
//                Job job = (Job) jobs.nextElement(); // Advance the enumeration forward
//                String jName = job.getName(); // Get job name
//                String jNum = job.getNumber(); // Get job number
//                Date jDate = job.getDate(); // Get date
//                values.add(new String[]{jName, jNum, jDate.toString()});
//            }
//        }
//
////        //print contents of horizon job array list to console
////        for (String[] arr : values) {System.out.println(Arrays.toString(arr));}
////
////        System.out.println("Iteration complete");
//
//        // Create a dataset. Will be added to table in next step
//        TableModel tableModel = new DefaultTableModel(values.toArray(new Object[][]{}), columns.toArray());
//
//        // Populate swing table with dataset
//        tblSpool.setModel(tableModel);
//
//        print("spool file table updated");
//    }

    /** method centers the Jframe on specified monitor in a multi-mointor setup
     * @param screen the screen number that you wish to display the Jframe on
     * @param frame the frame that you wish to center on screen
     */
    public static void showOnScreen(int screen, JFrame frame) {

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        if (screen > -1 && screen <= gd.length) {
            Rectangle bounds = gd[screen].getDefaultConfiguration().getBounds();
            int x1 = (bounds.width/2)-(frame.getWidth()/2);
            int y1 = (bounds.height/2)-(frame.getHeight()/2);
            frame.setLocation(bounds.x + x1, bounds.y + y1);
        } else if (gd.length > 0) {
            frame.setLocationRelativeTo(null);
        } else {
            throw new RuntimeException("No Screens Found");
        }

    }

    public static void main(String[] args) {
        print("app started");

        String laf = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-laf")) {
                laf = args[i + 1];
            }
        }
        try {
            if (Objects.equals(laf, "") || Objects.equals(laf, "windows")) {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } else if (Objects.equals(laf, "nimbus")) {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } else if (Objects.equals(laf, "motif")) {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        print("look and feel: "+UIManager.getLookAndFeel().getName());

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
