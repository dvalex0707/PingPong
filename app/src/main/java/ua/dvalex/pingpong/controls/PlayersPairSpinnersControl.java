package ua.dvalex.pingpong.controls;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.dvalex.pingpong.db.DB;

/**
 * Created by alex on 06.08.17
 */
public class PlayersPairSpinnersControl {

    public interface OnSelectionChangedListener {
        void change();
    }

    public class NoEnoughPlayersException extends Exception {}

    private final SQLiteDatabase db = DB.getInstance().get();
    private final Context context;
    private final Spinner spPlayer1, spPlayer2;
    private final List<String> players = new ArrayList<>();
    private final Map<String, Long> nameToIdMap = new HashMap<>();
    private OnSelectionChangedListener onSelectionChangedListener = null;

    public PlayersPairSpinnersControl(Context context, Spinner spPlayer1, Spinner spPlayer2) {
        this.context = context;
        this.spPlayer1 = spPlayer1;
        this.spPlayer2 = spPlayer2;
    }

    public void setup(OnSelectionChangedListener onSelectionChangedListener) {
        this.onSelectionChangedListener = onSelectionChangedListener;
        PlayersItemClickListener listener = new PlayersItemClickListener();
        spPlayer1.setOnItemSelectedListener(listener);
        spPlayer2.setOnItemSelectedListener(listener);
    }

    public void loadPlayers(Long... requiredIds) throws NoEnoughPlayersException {
        Set<Long> requiredIdsSet = new HashSet<>(Arrays.asList(requiredIds));
        players.clear();
        nameToIdMap.clear();
        Cursor cursor = db.query(DB.TABLE_CURRENT_PLAYERS, null, null, null, null, null, DB.GAME_INDEX);
        getPlayers(cursor, DB.PLAYER_ID, requiredIdsSet);
        addRequiredPlayers(requiredIdsSet);
        if (players.size() < 2) throw new NoEnoughPlayersException();
    }

    private void getPlayers(Cursor cursor, String idColumn, Set<Long> requiredIdsSet) {
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            int colId = cursor.getColumnIndex(idColumn);
            int colName = cursor.getColumnIndex(DB.NAME);
            do {
                String name = cursor.getString(colName);
                players.add(name);
                long id = cursor.getLong(colId);
                nameToIdMap.put(name, id);
                if (requiredIdsSet != null) {
                    requiredIdsSet.remove(id);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void addRequiredPlayers(Set<Long> ids) {
        if (ids.isEmpty()) return;
        StringBuilder selection = new StringBuilder(DB.ID).append(" in (");
        boolean next = false;
        for (long id : ids) {
            if (next) {
                selection.append(", ");
            }
            selection.append(id);
            next = true;
        }
        selection.append(")");
        Cursor cursor = db.query(DB.TABLE_ALL_PLAYERS, null, selection.toString(), null, null, null, null);
        getPlayers(cursor, DB.ID, null);
    }

    public void reload(String lastWinner) {
        PlayersAdapter adapter1 = new PlayersAdapter(context, players, lastWinner);
        spPlayer1.setAdapter(adapter1);
        PlayersAdapter adapter2 = new PlayersAdapter(context, players, null);
        spPlayer2.setAdapter(adapter2);
        checkSpinners(true);
    }

    public void selectPlayer1ById(long id) {
        selectById(spPlayer1, id);
    }

    public void selectPlayer2ById(long id) {
        selectById(spPlayer2, id);
    }

    private void selectById(Spinner spinner, long id) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (getPlayerId((String) spinner.getItemAtPosition(i)) == id) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    public String getName1() {
        return (String) spPlayer1.getSelectedItem();
    }

    public String getName2() {
        return (String) spPlayer2.getSelectedItem();
    }

    public Long getPlayerId(String name) {
        return nameToIdMap.get(name);
    }

    private class PlayersAdapter extends ArrayAdapter<String> {

        public PlayersAdapter(Context context, List<String> list, String winner) {
            super(context, android.R.layout.simple_spinner_dropdown_item);
            if (winner != null && nameToIdMap.containsKey(winner)) {
                add(winner);
            }
            for (String name : list) {
                if (winner == null || !name.equals(winner)) {
                    add(name);
                }
            }
        }
    }

    private void checkSpinners(boolean selectedFirstSpinner) {
        String player1 = (String) spPlayer1.getSelectedItem();
        String player2 = (String) spPlayer2.getSelectedItem();
        if (!player1.equals(player2)) return;
        String playerToCompare = (selectedFirstSpinner ? player1 : player2);
        Spinner spinnerToChange = (selectedFirstSpinner ? spPlayer2: spPlayer1);
        for (int i = 0; i < spinnerToChange.getCount(); i++) {
            if (!spinnerToChange.getItemAtPosition(i).equals(playerToCompare)) {
                spinnerToChange.setSelection(i);
                return;
            }
        }
    }

    private class PlayersItemClickListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            checkSpinners(parent == spPlayer1);
            if (onSelectionChangedListener != null) {
                onSelectionChangedListener.change();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    }
}
