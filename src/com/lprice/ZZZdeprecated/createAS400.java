package com.lprice.ZZZdeprecated;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;

import java.io.IOException;

public class createAS400 {

    public static AS400 openConnection() throws AS400SecurityException, IOException {
        // Create AS400 object
        AS400 as400 = new AS400("CHRZNPRD");
        System.out.println("AS400 object created");
        return as400;
    }

}
