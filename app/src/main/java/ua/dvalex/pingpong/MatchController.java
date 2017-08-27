package ua.dvalex.pingpong;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;

import java.util.Date;

import ua.dvalex.pingpong.controls.MatchesSpinnerControl;
import ua.dvalex.pingpong.db.DB;
import ua.dvalex.pingpong.fragments.AlertDialog;
import ua.dvalex.pingpong.fragments.FragmentGames;
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
    private FragmentGames fragmentGames;
    private SettingsProvider settingsProvider;
    private SQLiteDatabase db;
    private String matchDate;
    private long currentMatch;

    public void startController(FragmentGames fragmentGames) {
        this.fragmentGames = fragmentGames;
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
        fragmentGamesAppearanceController.getSpinnerControl().forceLoad();
    }

    public void finishMatch() {
        new AlertDialog()
                .setup(fragmentGames, R.string.action_finish_match, R.string.msgConfirmFinishMatch)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        internalFinishMatch();
                    }
                })
                .start();
    }

    private void internalFinishMatch() {
        matchDate = null;
        currentMatch = -1;
        settingsProvider.set(MATCH_DATE, null);
        settingsProvider.set(CURRENT_MATCH, null);
        settingsProvider.set(LAST_WINNER, null);
        fragmentGamesAppearanceController.setMatchStarted(false);
    }

    public void deleteMatch() {
        final MatchesSpinnerControl spinnerControl = fragmentGamesAppearanceController.getSpinnerControl();
        if (spinnerControl == null) return;
        final Long id = spinnerControl.getSelectedId();
        if (id == null) return;
        new AlertDialog()
                .setup(fragmentGames, R.string.action_delete_match, R.string.msgConfirmDeleteMatch)
                .setIsRed()
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean success = false;
                        try {
                            db.beginTransaction();
                            String[] whereArgs = {String.valueOf(id)};
                            db.delete(DB.TABLE_GAMES, DB.MATCH + " = ?", whereArgs);
                            db.delete(DB.TABLE_MATCHES, DB.ID + " = ?", whereArgs);
                            db.setTransactionSuccessful();
                            spinnerControl.forceLoad();
                            success = true;
                        } finally {
                            db.endTransaction();
                        }
                        if (success && id == currentMatch) {
                            internalFinishMatch();
                        }
                    }
                })
                .start();
    }

    public long getCurrentMatch() {
        return currentMatch;
    }

    public boolean isMatchStarted() {
        return matchDate != null;
    }
}
