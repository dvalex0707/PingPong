package ua.dvalex.pingpong.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.dvalex.pingpong.MainActivity;
import ua.dvalex.pingpong.MatchController;
import ua.dvalex.pingpong.R;
import ua.dvalex.pingpong.Utils;
import ua.dvalex.pingpong.db.ColumnProvider;
import ua.dvalex.pingpong.db.CursorLoaderHelper;
import ua.dvalex.pingpong.db.DB;
import ua.dvalex.pingpong.settings.SPConst;
import ua.dvalex.pingpong.settings.SettingsProvider;

public class FragmentGames extends Fragment implements SPConst {

    private final static String SQL_QUERY_GAMES = "select " +
            DB.TABLE_GAMES + "." + DB.ID + ", " +
            DB.TABLE_GAMES + "." + DB.TIMESTAMP + ", " +
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
            DB.TABLE_GAMES + "." + DB.PLAYER2 + "=" + DB.PLAYER2 + "." + DB.ID + " and " +
            DB.TABLE_GAMES + "." + DB.MATCH + " = ?";
    private final SQLiteDatabase db = DB.getInstance().get();
    private final List<String> players = new ArrayList<>();
    private final Map<String, Integer> nameToIdMap = new HashMap<>();
    private MatchController matchController;
    private String lastWinner;
    private Button btnStartMatch;
    private LinearLayout llMsgTooFewPlayers, llAddNewGame;
    private ListView lvGames;
    private Spinner spPlayer1, spPlayer2;
    private ImageButton btnPlayer1Wins, btnPlayer2Wins, btnSave;
    private EditText etScore1, etScore2;
    private TextView lnkGoToPlayersTab;
    private CursorLoaderHelper loaderHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);
        matchController = MatchController.getInstance();
        matchController.setGamesView(view);
        lastWinner = SettingsProvider.getInstance().getString(LAST_WINNER, null);
        findViews(view);
        setupGamesList(inflater);
        setClickListenerForButtons();
        setTextWatchers();
        setupSpinners();
        return view;
    }

    private void findViews(View v) {
        btnStartMatch = (Button) v.findViewById(R.id.btnStartMatch);
        lvGames = (ListView) v.findViewById(R.id.lvGames);
        llMsgTooFewPlayers = (LinearLayout) v.findViewById(R.id.llMsgTooFewPlayers);
        llAddNewGame = (LinearLayout) v.findViewById(R.id.llAddNewGame);
        spPlayer1 = (Spinner) v.findViewById(R.id.spPlayer1);
        spPlayer2 = (Spinner) v.findViewById(R.id.spPlayer2);
        btnPlayer1Wins = (ImageButton) v.findViewById(R.id.btnPlayer1Wins);
        etScore1 = (EditText) v.findViewById(R.id.etScore1);
        etScore2 = (EditText) v.findViewById(R.id.etScore2);
        btnPlayer2Wins = (ImageButton) v.findViewById(R.id.btnPlayer2Wins);
        btnSave = (ImageButton) v.findViewById(R.id.btnSave);
        setEnableButton(btnSave, false);
        lnkGoToPlayersTab = (TextView) v.findViewById(R.id.lnkGoToPlayersTab);
        setUnderline(lnkGoToPlayersTab);
    }

    private void setUnderline(TextView textView) {
        SpannableString spannableString = new SpannableString(textView.getText());
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), 0);
        textView.setText(spannableString);
    }

    private void setupGamesList(LayoutInflater inflater) {
        CursorAdapter adapter = new GamesAdapter(inflater);
        lvGames.setAdapter(adapter);
        loaderHelper = new CursorLoaderHelper(getActivity(), adapter, new GamesHelperLoader());
        loaderHelper.setListViewToScrollDown(lvGames);
    }

    private class GamesHelperLoader implements CursorLoaderHelper.HelperLoader {

        @Override
        public Cursor loadInBackground() {
            return db.rawQuery(SQL_QUERY_GAMES, new String[] { String.valueOf(matchController.getCurrentMatch()) });
        }
    }

    private class GamesAdapter extends CursorAdapter {

        private final LayoutInflater inflater;
        private final ColumnProvider columnProvider = new ColumnProvider(DB.TIMESTAMP,
                DB.PLAYER1, DB.PLAYER2, DB.SCORE1, DB.SCORE2);

        public GamesAdapter(LayoutInflater inflater) {
            super(getContext(), null, false);
            this.inflater = inflater;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            columnProvider.read(cursor);
            View view = inflater.inflate(R.layout.games_list_item, parent, false);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            columnProvider.read(cursor);
            long timestamp = cursor.getLong(columnProvider.get(DB.TIMESTAMP));
            String humanReadableDate = Utils.humanReadableDate(getContext(), timestamp);
            ((TextView) view.findViewById(R.id.gameTimestamp)).setText(humanReadableDate);
            String player1 = cursor.getString(columnProvider.get(DB.PLAYER1));
            ((TextView) view.findViewById(R.id.player1)).setText(player1);
            String player2 = cursor.getString(columnProvider.get(DB.PLAYER2));
            ((TextView) view.findViewById(R.id.player2)).setText(player2);

            TextView tvScore1 = (TextView) view.findViewById(R.id.score1);
            TextView tvScore2 = (TextView) view.findViewById(R.id.score2);
            int s1 = cursor.getInt(columnProvider.get(DB.SCORE1));
            int s2 = cursor.getInt(columnProvider.get(DB.SCORE2));
            tvScore1.setText(String.valueOf(s1));
            tvScore2.setText(String.valueOf(s2));
            tvScore1.setTextColor(getResources().getColor(s1 > s2 ? R.color.red : R.color.black));
            tvScore2.setTextColor(getResources().getColor(s2 > s1 ? R.color.red : R.color.black));
        }
    }

    private void setClickListenerForButtons() {
        GamesOnClickListener gamesOnClickListener = new GamesOnClickListener();
        View[] views = new View[] {
                btnStartMatch,
                btnSave,
                btnPlayer1Wins,
                btnPlayer2Wins,
                lnkGoToPlayersTab
        };
        for (View view : views) {
            view.setOnClickListener(gamesOnClickListener);
        }
    }

    private class GamesOnClickListener implements View.OnClickListener {

        private String name1, name2;
        private int score1, score2;

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnStartMatch:
                    matchController.startMatch();
                    setupSpinners();
                    loaderHelper.forceLoad();
                    break;
                case R.id.btnSave:
                    save();
                    break;
                case R.id.btnPlayer1Wins:
                    win(etScore1);
                    etScore2.requestFocus();
                    break;
                case R.id.btnPlayer2Wins:
                    win(etScore2);
                    etScore1.requestFocus();
                    break;
                case R.id.lnkGoToPlayersTab:
                    MainActivity.PageSelectOnClickListener.PLAYERS.onClick(null);
                    break;
            }
        }

        private void win(EditText editText) {
            setEnableButton(btnPlayer1Wins, false);
            setEnableButton(btnPlayer2Wins, false);
            editText.setText("11");
            editText.setTypeface(Typeface.DEFAULT_BOLD);
            editText.setTextColor(getResources().getColor(R.color.red));
            editText.setEnabled(false);
            Utils.showSoftwareKeyboard(editText);
        }

        private void save() {
            matchController.saveMatchIfNeed();
            name1 = (String) spPlayer1.getSelectedItem();
            name2 = (String) spPlayer2.getSelectedItem();
            score1 = Integer.valueOf(etScore1.getText().toString().trim());
            score2 = Integer.valueOf(etScore2.getText().toString().trim());

            setLastWinner();
            ContentValues gameContentValues = createGameContentValues();
            long gameIndex = saveGame(gameContentValues);
            saveGameIndex(gameIndex);
            resetScores();
            setupSpinners();
        }

        private void setLastWinner() {
            if (score1 > score2) {
                lastWinner = name1;
            } else if (score2 > score1) {
                lastWinner = name2;
            } else {
                lastWinner = null;
            }
            SettingsProvider.getInstance().set(LAST_WINNER, lastWinner);
        }

        @NonNull
        private ContentValues createGameContentValues() {
            ContentValues cv = new ContentValues();
            cv.put(DB.MATCH, matchController.getCurrentMatch());
            cv.put(DB.TIMESTAMP, System.currentTimeMillis());
            cv.put(DB.PLAYER1, nameToIdMap.get(name1));
            cv.put(DB.PLAYER2, nameToIdMap.get(name2));
            cv.put(DB.SCORE1, score1);
            cv.put(DB.SCORE2, score2);
            return cv;
        }

        private long saveGame(ContentValues gameContentValues) {
            long gameIndex = 0;
            try {
                db.beginTransaction();
                gameIndex = db.insertOrThrow(DB.TABLE_GAMES, null, gameContentValues);
                db.setTransactionSuccessful();
                loaderHelper.forceLoad();
            } finally {
                db.endTransaction();
            }
            return gameIndex;
        }

        private void saveGameIndex(long gameIndex) {
            ContentValues gameIndexContentValues = new ContentValues();
            gameIndexContentValues.put(DB.GAME_INDEX, gameIndex);
            try {
                db.beginTransaction();
                db.update(DB.TABLE_CURRENT_PLAYERS, gameIndexContentValues, DB.NAME + " in (?, ?)",
                        new String[]{name1, name2});
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    private void setEnableButton(ImageButton button, boolean enable) {
        button.setAlpha(enable ? 1f : .2f);
        button.setEnabled(enable);
    }

    public void setupSpinners() {
        loadPlayers();
        if (!matchController.isMatchStarted()) return;

        boolean enoughPlayers = players.size() >= 2;
        llMsgTooFewPlayers.setVisibility(enoughPlayers ? View.GONE : View.VISIBLE);
        llAddNewGame.setVisibility(enoughPlayers ? View.VISIBLE : View.GONE);
        if (!enoughPlayers) return;

        PlayersAdapter adapter1 = new PlayersAdapter(getContext(), players, lastWinner);
        spPlayer1.setAdapter(adapter1);
        PlayersAdapter adapter2 = new PlayersAdapter(getContext(), players, null);
        spPlayer2.setAdapter(adapter2);
        checkSpinners(true);
        PlayersItemClickListener listener = new PlayersItemClickListener();
        spPlayer1.setOnItemSelectedListener(listener);
        spPlayer2.setOnItemSelectedListener(listener);
    }

    private void loadPlayers() {
        players.clear();
        nameToIdMap.clear();
        Cursor cursor = DB.getInstance().get()
                .query(DB.TABLE_CURRENT_PLAYERS, null, null, null, null, null, DB.GAME_INDEX);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            int colId = cursor.getColumnIndex(DB.PLAYER_ID);
            int colName = cursor.getColumnIndex(DB.NAME);
            do {
                String name = cursor.getString(colName);
                players.add(name);
                nameToIdMap.put(name, cursor.getInt(colId));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private class PlayersAdapter extends ArrayAdapter<String> {

        public PlayersAdapter(Context context, List<String> list, String winner) {
            super(context, android.R.layout.simple_spinner_dropdown_item);
            if (winner != null) {
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
        PlayersAdapter adapter = (PlayersAdapter) spinnerToChange.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (!adapter.getItem(i).equals(playerToCompare)) {
                spinnerToChange.setSelection(i);
                return;
            }
        }
    }

    private void setTextWatchers() {
        GamesTextWatcher gamesTextWatcher = new GamesTextWatcher();
        etScore1.addTextChangedListener(gamesTextWatcher);
        etScore2.addTextChangedListener(gamesTextWatcher);
    }

    private class GamesTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String t1 = etScore1.getText().toString().trim();
            String t2 = etScore2.getText().toString().trim();
            if (t1.isEmpty() || t2.isEmpty() || t1.equals(t2)) {
                setEnableButton(btnSave, false);
            } else {
                int s1 = strToInt(t1), s2 = strToInt(t2);
                setEnableButton(btnSave, s1 > -1 && s2 > -1 && s1 != s2);
            }
        }

        private int strToInt(String s) {
            try {
                return Integer.valueOf(s);
            } catch (Exception e) {
                return -1;
            }
        }
    }

    private class PlayersItemClickListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            checkSpinners(parent == spPlayer1);
            resetScores();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    }

    private void resetScores() {
        setEnableButton(btnPlayer1Wins, true);
        setEnableButton(btnPlayer2Wins, true);
        resetEditText(etScore1);
        resetEditText(etScore2);
    }

    private void resetEditText(EditText editText) {
        editText.setText("");
        editText.setTypeface(Typeface.DEFAULT);
        editText.setTextColor(Color.BLACK);
        editText.setEnabled(true);
    }
}
