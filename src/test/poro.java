package test;

import database.SqlRetrieveData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Liu Woon Kit on 16/12/2016.
 */
public class poro {
    public static void main(String[] args) {
        // create a new calendar
        Calendar todayInCalendar=Calendar.getInstance().getInstance();
        todayInCalendar.set(Calendar.HOUR_OF_DAY,0);
        todayInCalendar.set(Calendar.MINUTE, 0);
        todayInCalendar.set(Calendar.SECOND, 0);
        Calendar yesterInCalendar=Calendar.getInstance();
        yesterInCalendar.setTime(todayInCalendar.getTime());
        yesterInCalendar.add(Calendar.DATE,-1);
        System.out.println(todayInCalendar.getTime().toString()+" "+yesterInCalendar.getTime().toString());




    }
}
