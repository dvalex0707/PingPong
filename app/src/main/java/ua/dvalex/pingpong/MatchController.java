package ua.dvalex.pingpong;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

import ua.dvalex.pingpong.db.DB;
import ua.dvalex.pingpong.settings.SPConst;
import ua.dvalex.pingpong.settings.SettingsProvider;

/**
 * Created by alex on 16.07.17
 */
public class MatchController implements SPConst {
    private static MatchController ourInstance = new MatchController();

    public static MatchController getInstance() {
        return ourInstance;
    }

    private MatchController() {
    }

    private final FragmentGamesAppearanceController fragmentGamesAppearanceController =
            FragmentGamesAppearanceController.getInstance();
    private SettingsProvider settingsProvider;
    private SQLiteDatabase db;
    private Context context;
    private String matchDate;
    private long currentMatch;

    public void startController(Context context) {
        this.context = context;
        settingsProvider = SettingsProvider.getInstance();
        db = DB.getInstance().get();
        matchDate = settingsProvider.getString(MATCH_DATE, null);
        currentMatch = settingsProvider.getLong(CURRENT_MATCH, -1);
    }

    public void startMatch() {
        Date date = new Date();
        matchDate = Utils.dateToDbFormat(date);
        currentMatch = -1;
        settingsProvider.set(MATCH_DATE, matchDate);
        settingsProvider.set(CURRENT_MATCH, -1L);
        settingsProvider.set(LAST_WINNER, null);
        fragmentGamesAppearanceController.setMatchStarted(true);
    }

    public void saveMatchIfNeed() {
        if (currentMatch != -1) return;
        if (matchDate == null) throw new RuntimeException();
        try {
            ContentValues cv = new ContentValues();
            cv.put(DB.DATE, matchDate);
            db.beginTransaction();
            currentMatch = db.insertOrThrow(DB.TABLE_MATCHES, null, cv);
            db.setTransactionSuccessful();
            settingsProvider.set(CURRENT_MATCH, currentMatch);
            settingsProvider.set(LAST_WINNER, null);
        } finally {
            db.endTransaction();
        }
    }

    public void finishMatch() {
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle(R.string.action_finish_match).setMessage(R.string.msgConfirmFinishMatch);
        adb.setNegativeButton(R.string.btnCancel, null);
        adb.setPositiveButton(R.string.btnYes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                matchDate = null;
                currentMatch = -1;
                settingsProvider.set(MATCH_DATE, null);
                settingsProvider.set(CURRENT_MATCH, null);
                settingsProvider.set(LAST_WINNER, null);
                fragmentGamesAppearanceController.setMatchStarted(false);
            }
        });
        adb.show();
    }

    public long getCurrentMatch() {
        return currentMatch;
    }

    public boolean isMatchStarted() {
        return matchDate != null;
    }
}
