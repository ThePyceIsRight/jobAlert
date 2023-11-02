package com.lprice;

import javax.swing.*;

public class testGUI extends JFrame {
    private JTable tblSpoolFiles;
    private JPanel panel1;
    private JLabel lblUser;
    private JButton btnOK;
    private JButton btnRfsh;

    public testGUI(){
        // Set up jFrame
        setContentPane(panel1);
        setTitle("Test GUI");
        setSize(450, 300);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set username label text
        lblUser.setText("Billy Bob");

//        // Create a dataset. Will be added to table in next step
//        TableModel tableModel = new DefaultTableModel(values.toArray(new Object[][]{}), columns.toArray());
//
//        // Populate swing table with dataset
//        tblSpoolFiles.setModel(tableModel);

        setVisible(true);
    }

    public static void main(String[] args){

        testGUI myGUI = new testGUI();

    }

}
