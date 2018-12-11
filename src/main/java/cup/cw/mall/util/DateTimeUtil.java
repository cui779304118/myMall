package cup.cw.mall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * Created by cuiwei.
 */
public class DateTimeUtil {

    //joda-time

    //String -> Date
    //Date -> String
    public static final  String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public  static Date strToDate(String dateTimeStr,String formatStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(formatStr);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return  dateTime.toDate();
    }

    public  static  String dateToStr(Date date,String formatStr){
        if(date == null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return  dateTime.toString(formatStr);
    }
    public  static Date strToDate(String dateTimeStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(STANDARD_FORMAT);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return  dateTime.toDate();
    }
    public  static  String dateToStr(Date date){
        return  dateToStr(date,STANDARD_FORMAT);
    }

    public static void main(String[] args) {
        System.out.println(DateTimeUtil.dateToStr(new Date(),"yyyyMMddHHmmss"));
    }
}
