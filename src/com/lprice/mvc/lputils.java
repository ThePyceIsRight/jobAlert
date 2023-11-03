package com.lprice.mvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class lputils {
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

    public static void printList(int[] msg){
        for (int i : msg) {System.out.println(now()+" "+i);}
    }

}
