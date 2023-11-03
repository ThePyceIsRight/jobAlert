package com.lprice.mvc;
import static com.lprice.mvc.as400Handler.*;
import static com.lprice.mvc.lputils.print;

import com.ibm.as400.access.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class myJobsGUI implements ActionListener, ComponentListener {
    final static String LOGIN = "Card with card with login";
    final static String MAIN = "Card with main menu";
    final static String STATUS = "Card with job status";
    final static String POPUP = "Card with popup message";
    static int monitor = 2; // the monitor to display the gui on

    AS400 as400; // database object to use for all db actions in app

    CardLayout cl; // the card layout used

    JPanel cards; //a panel that uses CardLayout
    JButton toggleButton;
    JButton refreshButton;
    JButton btnSignon;
    JTextField userField;
    JPasswordField passField;
    JLabel validation;
    JTable tblActiveSpool;
    JTable tblInactiveSpool;
    JLabel usrLabel;
    
    String userName;
    
    String[] columnNames = {"", "", ""};
    Object[][] data = {{"", "", ""}};


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
        card2.setPreferredSize(new Dimension((int) Math.round(pane.getWidth()*0.8),200));

        // refresh button
        refreshButton = new JButton("Refresh Data");
        refreshButton.addActionListener(new refreshListener());

        // create tabbed pane for different tables
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel activeJobPanel = new JPanel();
        JPanel inactiveJobPanel = new JPanel();

        activeJobPanel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        inactiveJobPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        activeJobPanel.setLayout(new BorderLayout());
        inactiveJobPanel.setLayout(new BorderLayout());
//        activeJobPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));

        inactiveJobPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollActive = new JScrollPane();
        JScrollPane scrollInactive = new JScrollPane();

        scrollActive.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//        scrollActive.setViewportBorder(BorderFactory.createLineBorder(Color.GREEN));


        // create table
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        tblActiveSpool = new JTable(model);
        tblActiveSpool.setBorder(BorderFactory.createLineBorder(Color.RED));

        tblInactiveSpool = new JTable(model);

        int width = (int) Math.round(activeJobPanel.getWidth()*0.8);
        scrollActive.setPreferredSize(new Dimension(200, 100)); // Set preferred size here
        scrollInactive.setPreferredSize(new Dimension(width, 200)); // Set preferred size here


        scrollActive.setViewportView(tblActiveSpool);
        scrollInactive.setViewportView(tblInactiveSpool);

        activeJobPanel.add(scrollActive);
        inactiveJobPanel.add(scrollInactive);

        // submit button
        JButton btnSubmit = new JButton("submit");
        btnSubmit.addActionListener(new submitListener());

        inactiveJobPanel.add(btnSubmit);

        // add the two panels to tabbed pane
        tabbedPane.addTab("Active Jobs", activeJobPanel);
        tabbedPane.addTab("Inactive Jobs", inactiveJobPanel);


        // construct the card
        card2.add(refreshButton);
        card2.add(Box.createVerticalStrut(10));
        card2.add(tabbedPane);
        card2.add(Box.createVerticalStrut(10));
        card2.add(btnSubmit);
        card2.revalidate();

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
        card4.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JTabbedPane tabPane = new JTabbedPane();

        JPanel panelScroll = new JPanel();
        panelScroll.setLayout(new BorderLayout());

        JScrollPane myScroll = new JScrollPane();

        TableModel myModel = new DefaultTableModel(new String[][] {{"data1","data2","data3"}},new String[] {"col1","col2","col3"});
        JTable myTable = new JTable(myModel);

        int width2 = (int) Math.round(tabPane.getWidth()*0.8);


        myScroll.setViewportView(myTable);
        panelScroll.setPreferredSize(new Dimension(width2, 200));

        panelScroll.add(myScroll);
        tabPane.add("tab1",panelScroll);


        card4.add(tabPane);


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
                userName = userField.getText();
                try {
                    updateSpoolTable(as400, tblActiveSpool, false, false);
                    updateSpoolTable(as400, tblInactiveSpool, true, false);

                } catch (PropertyVetoException | ErrorCompletingRequestException | InterruptedException | IOException |
                         AS400SecurityException | ObjectDoesNotExistException | ParseException ex) {
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
            int iRow = tblActiveSpool.getSelectedRow();
            String jobName = tblActiveSpool.getValueAt(iRow, 0).toString();
            String jobNumber = tblActiveSpool.getValueAt(iRow, 1).toString();
            print("User selected the following job -> Job name: " + jobName + "; Job number: " + jobNumber);

            try {
                monitorJob(as400, jobName, userName, jobNumber);
            } catch (AS400SecurityException | LineUnavailableException | ErrorCompletingRequestException |
                     InterruptedException | ObjectDoesNotExistException | IOException ex) {
                throw new RuntimeException(ex);
            }

            // go to the job monitor card
            cl = (CardLayout)(cards.getLayout());
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
                updateSpoolTable(as400, tblActiveSpool, false, false);
                updateSpoolTable(as400, tblInactiveSpool, true, false);
            } catch (PropertyVetoException | AS400SecurityException | ObjectDoesNotExistException | IOException |
                     InterruptedException | ErrorCompletingRequestException | ParseException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    // Create the GUI and show it.
    public static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("myJobsGUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(400,500)); // set the app dimension

        //Create and set up the content pane.
        myJobsGUI demo = new myJobsGUI();

        demo.addComponentToPane(frame.getContentPane());

        //Display the window.
        frame.pack();
        showOnScreen(monitor,frame);
        frame.setVisible(true);
    }

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
            if (args[i].equals("-m")) {
                monitor = Integer.parseInt(args[i + 1]);
            }
        }

        // set up look and feel
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
            public void run() {createAndShowGUI();}
        });
    }
}
