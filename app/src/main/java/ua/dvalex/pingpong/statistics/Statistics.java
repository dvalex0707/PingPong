package ua.dvalex.pingpong.statistics;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ua.dvalex.pingpong.db.ColumnProvider;
import ua.dvalex.pingpong.db.DB;

/**
 * Created by alex on 05.08.17
 */
public class Statistics {

    private final static String SQL_QUERY_FOR_STATISTICS = "select " +
            DB.PLAYER1 + "." + DB.NAME + " " + DB.PLAYER1 + ", " +
            DB.PLAYER2 + "." + DB.NAME + " " + DB.PLAYER2 + ", " +
            DB.TABLE_GAMES + "." + DB.SCORE1 + ", " +
            DB.TABLE_GAMES + "." + DB.SCORE2 +
            " from " +
            DB.TABLE_GAMES + ", " +
            DB.TABLE_ALL_PLAYERS + " " + DB.PLAYER1 + ", " +
            DB.TABLE_ALL_PLAYERS + " " + DB.PLAYER2 +
            " where " +
            DB.TABLE_GAMES + "." + DB.PLAYER1 + "=" + DB.PLAYER1 + "." + DB.ID + " and " +
            DB.TABLE_GAMES + "." + DB.PLAYER2 + "=" + DB.PLAYER2 + "." + DB.ID;
    private final SQLiteDatabase db = DB.getInstance().get();
    private final Long matchId;
    private final Map<Integer, StatItem> hashMap = new HashMap<>();
    private final Set<StatItem> treeSet = new TreeSet<>(createComparator());
    private boolean cancelRequired = false;

    public Statistics(Long matchId) {
        this.matchId = matchId;
    }

    @NonNull
    private Comparator<StatItem> createComparator() {
        return new Comparator<StatItem>() {
            @Override
            public int compare(StatItem lhs, StatItem rhs) {
                return lhs.compare(rhs);
            }
        };
    }

    public void gather() {
        String sql = SQL_QUERY_FOR_STATISTICS;
        String[] selectionArgs = null;
        if (matchId != null) {
            sql += " and " + DB.TABLE_GAMES + "." + DB.MATCH + " = ?";
            selectionArgs = new String[] { String.valueOf(matchId) };
        }
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            ColumnProvider cp = new ColumnProvider(DB.PLAYER1, DB.PLAYER2, DB.SCORE1, DB.SCORE2);
            cp.read(cursor);
            do {
                add(
                        cursor.getString(cp.get(DB.PLAYER1)),
                        cursor.getString(cp.get(DB.PLAYER2)),
                        cursor.getInt(cp.get(DB.SCORE1)),
                        cursor.getInt(cp.get(DB.SCORE2)));
                if (cancelRequired) break;
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (!cancelRequired) {
            for (StatItem item : treeSet) {
                item.calc();
            }
        }
    }

    private void add(String player1, String player2, int score1, int score2) {
        addItem(null, null, score1, score2);
        addItem(player1, null, score1, score2);
        addItem(null, player2, score1, score2);
        addItem(player1, player2, score1, score2);
    }

    private void addItem(String player1, String player2, int score1, int score2) {
        int key = StatItem.getKey(player1, player2);
        StatItem item = hashMap.get(key);
        if (item == null) {
            StatItem statItem = new StatItem(player1, player2, score1, score2);
            hashMap.put(key, statItem);
            treeSet.add(statItem);
        } else {
            item.add(player1, player2, score1, score2);
        }
    }

    public List<StatItem> getList() {
        return new ArrayList<>(treeSet);
    }

    public void cancel() {
        cancelRequired = true;
    }
}
