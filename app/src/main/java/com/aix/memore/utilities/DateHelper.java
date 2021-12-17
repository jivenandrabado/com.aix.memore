package com.aix.memore.utilities;

import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class DateHelper {

    public static String formatDate(Date date){
        SimpleDateFormat spf=new SimpleDateFormat("MMMM dd, yyyy",Locale.ENGLISH);
        return spf.format(date);
    }

    public static String formatDate2(Date date){
        SimpleDateFormat spf=new SimpleDateFormat("MM/dd/yyyy",Locale.ENGLISH);
        return spf.format(date);
    }


    public static Date stringToDate(String date){
        DateFormat formatter = new SimpleDateFormat("MM/dd/yy",Locale.ENGLISH);
        Date date1 = null;
        try {
            date1 = formatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }

    public static Date stringToDate2(String date){
        DateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        Date date1 = null;
        try {
            date1 = formatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }
}
