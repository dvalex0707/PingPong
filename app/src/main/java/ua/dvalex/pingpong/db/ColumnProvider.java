package ua.dvalex.pingpong.db;

import android.database.Cursor;

import java.util.HashMap;

public class ColumnProvider extends HashMap<String, Integer> {

    private final String[] fields;
    private boolean ready = false;

    public ColumnProvider(String... fields) {
        super(fields.length);
        this.fields = fields;
    }

    public void read(Cursor cursor) {
        if (cursor == null || ready) return;
        for (String field : fields) {
            put(field, cursor.getColumnIndex(field));
        }
        ready = true;
    }
}
