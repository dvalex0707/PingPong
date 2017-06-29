package ua.dvalex.pingpong.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = Environment.getExternalStorageDirectory().getPath()
            + "/PingPong.db";

    private static final String[] DB_STRUCTURE = new String[] {
            "create table "+ DB.TABLE_ALL_PLAYERS +"(" +
                    DB.ID + " integer primary key autoincrement," +
                    DB.NAME + " text);",
            "create table "+ DB.TABLE_CURRENT_PLAYERS +"(" +
                    DB.ID + " integer primary key autoincrement," +
                    DB.PLAYER_ID + " integer," +
                    DB.NAME + " text," +
                    DB.GAME_INDEX + " integer);",
            "create table "+ DB.TABLE_MATCHES +"(" +
                    DB.ID + " integer primary key autoincrement," +
                    DB.DATE + " date);",
            "create table "+ DB.TABLE_GAMES +"(" +
                    DB.ID + " integer primary key autoincrement," +
                    DB.MATCH + " integer," +
                    DB.TIMESTAMP + " integer," +
                    DB.PLAYER1 + " integer," +
                    DB.PLAYER2 + " integer," +
                    DB.SCORE1 + " integer," +
                    DB.SCORE2 + " integer);",
            "create index all_players_name on all_players(name);",
            "create index current_players_name on current_players(name);",
            "create index current_players_index on current_players(game_index);",
            "create index matches_date on matches(date);",
            "create index games_match on games(match);",
            "create index games_timestamp on games(timestamp);"
    };

    public DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String sql : DB_STRUCTURE) {
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
