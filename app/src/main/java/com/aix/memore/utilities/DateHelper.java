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
        SimpleDateFormat spf=new SimpleDateFormat("MMMM dd, yyyy");
        return spf.format(date);
    }

    public static Date stringToDate(String date){
        DateFormat formatter = new SimpleDateFormat("MM/dd/yy");
        Date date1 = null;
        try {
            date1 = formatter.parse(date);
            ErrorLog.WriteDebugLog("DATEEE "+date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }
}
