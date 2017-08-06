package ua.dvalex.pingpong.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import ua.dvalex.pingpong.R;
import ua.dvalex.pingpong.fragments.AlertDialog;

/**
 * Created by alex on 12.08.17
 */
public class GameSaveHelper {
    private final SQLiteDatabase db = DB.getInstance().get();
    private final Fragment fragment;
    private final CursorLoaderHelper loaderHelper;
    private Long gameId = null, matchId = null;
    private long timestamp, player1Id, player2Id;
    private int score1, score2;

    public GameSaveHelper(Fragment fragment, CursorLoaderHelper loaderHelper) {
        this.fragment = fragment;
        this.loaderHelper = loaderHelper;
    }

    public GameSaveHelper setGameId(Long gameId) {
        this.gameId = gameId;
        return this;
    }

    public GameSaveHelper setMatchId(long matchId) {
        this.matchId = matchId;
        return this;
    }

    public GameSaveHelper setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public GameSaveHelper setPlayer1Id(long player1Id) {
        this.player1Id = player1Id;
        return this;
    }

    public GameSaveHelper setPlayer2Id(long player2Id) {
        this.player2Id = player2Id;
        return this;
    }

    public GameSaveHelper setScore1(int score1) {
        this.score1 = score1;
        return this;
    }

    public GameSaveHelper setScore2(int score2) {
        this.score2 = score2;
        return this;
    }

    public Long save() {
        try {
            db.beginTransaction();
            if (gameId == null) {
                gameId = db.insertOrThrow(DB.TABLE_GAMES, null, createGameContentValues());
            } else {
                db.update(DB.TABLE_GAMES, createGameContentValues(), DB.ID + " = ?",
                        new String[]{String.valueOf(gameId)});
            }
            db.setTransactionSuccessful();
            loaderHelper.forceLoad();
        } catch (Exception e) {
            AlertDialog alertDialog = new AlertDialog();
            alertDialog.setup(fragment, R.string.titleSaveError, R.string.msgSaveError)
                    .setIsRed().setWithoutCancel().start();
        } finally {
            db.endTransaction();
        }
        return gameId;
    }

    @NonNull
    private ContentValues createGameContentValues() {
        ContentValues cv = new ContentValues();
        if (matchId != null) {
            cv.put(DB.MATCH, matchId);
        }
        cv.put(DB.TIMESTAMP, timestamp);
        cv.put(DB.PLAYER1, player1Id);
        cv.put(DB.PLAYER2, player2Id);
        cv.put(DB.SCORE1, score1);
        cv.put(DB.SCORE2, score2);
        return cv;
    }
}
