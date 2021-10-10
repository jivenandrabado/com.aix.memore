package com.aix.memore.utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {

    public static String formatDate(String date){
        SimpleDateFormat postFormater = new SimpleDateFormat("MMMM dd, yyyy");
        DateFormat dateFormat = new SimpleDateFormat ("dd/MM/yyyy");
// Parsing the date
//        Date datee = dateParser.parse(date);
        return dateFormat.format(date);
    }
}
