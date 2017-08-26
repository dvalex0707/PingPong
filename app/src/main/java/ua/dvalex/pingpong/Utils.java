package ua.dvalex.pingpong;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    private static final SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("yyyy/MM/dd");

    public static String dateToDbFormat(Date date) {
        return DB_DATE_FORMAT.format(date);
    }

    public static Date dbFormatToDate(String string) throws ParseException {
        return DB_DATE_FORMAT.parse(string);
    }

    public static String humanReadableDate(Context context, long timestamp) {
        Date date = new Date(timestamp);
        String date_only = DATE_ONLY_FORMAT.format(date);
        String today = DATE_ONLY_FORMAT.format(new Date());
        boolean dateIsToday = date_only.equals(today);
        int formatId = dateIsToday ? R.string.shortDateFormat : R.string.fullDateFormat;
        String formatString = context.getString(formatId);
        SimpleDateFormat dateFormat = new SimpleDateFormat(formatString);
        return dateFormat.format(date);
    }

    public static String timestampToString(Context context, long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.fullDateFormat));
        return dateFormat.format(new Date(timestamp));
    }

    public static long stringToTimestamp(Context context, String date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.fullDateFormat));
        return dateFormat.parse(date).getTime();
    }

    public static void showSoftwareKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);
    }

    public static <T> T resolveNull(T obj, T resolution) {
        return obj == null ? resolution : obj;
    }
}
