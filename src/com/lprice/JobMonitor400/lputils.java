package com.lprice.JobMonitor400;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class lputils {

    static String msgSpace = "   ";

    // set up easy date time methods
    public static String now(){
        LocalDateTime localDT = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        return localDT.format(format);
    }

    // set up easy print methods
    public static void print(String msg){System.out.println(now()+msgSpace+msg);}
    public static void print(String[] msg){for (String s : msg) {System.out.println(now()+msgSpace+s);}}
    public static void print(int msg){System.out.println(now()+msgSpace+msg);}
    public static void print(int[] msg){for (int i : msg) {System.out.println(now()+msgSpace+i);}}

//    public static void printList(int[] msg){
//        for (int i : msg) {System.out.println(now()+msgSpace+i);}
//    }


    public static void printBorder(String msg) {
        int charBuffer = 4;
        int lineLen = msg.length() + charBuffer;

        StringBuilder sb = new StringBuilder();

        sb.append("┌");
        for (int i = 0; i < lineLen; i++) {sb.append("─");}
        sb.append("┐\n│");

        for (int i = 0; i < charBuffer/2; i++) {sb.append(" ");}
        sb.append(msg);
        for (int i = 0; i < charBuffer/2; i++) {sb.append(" ");}

        sb.append("│\n└");
        for (int i = 0; i < lineLen; i++) {sb.append("─");}
        sb.append("┘");

        System.out.println(sb);
    }

}
