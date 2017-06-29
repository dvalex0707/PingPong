package ua.dvalex.pingpong.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DB {
    public static final String TABLE_ALL_PLAYERS = "all_players";
    public static final String TABLE_CURRENT_PLAYERS = "current_players";
    public static final String TABLE_MATCHES = "matches";
    public static final String TABLE_GAMES = "games";
    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String PLAYER_ID = "player_id";
    public static final String GAME_INDEX = "game_index";
    public static final String DATE = "date";
    public static final String MATCH = "match";
    public static final String TIMESTAMP = "timestamp";
    public static final String PLAYER1 = "player1";
    public static final String PLAYER2 = "player2";
    public static final String SCORE1 = "score1";
    public static final String SCORE2 = "score2";

    private static DB ourInstance = new DB();

    public static DB getInstance() {
        return ourInstance;
    }

    private SQLiteDatabase sqLiteDatabase;

    private DB() {
    }

    public void open(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        sqLiteDatabase = dbHelper.getWritableDatabase();
    }

    public SQLiteDatabase get() {
        return sqLiteDatabase;
    }
}
