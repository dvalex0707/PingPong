package ua.dvalex.pingpong.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import ua.dvalex.pingpong.FragmentGamesAppearanceController;
import ua.dvalex.pingpong.MainActivity;
import ua.dvalex.pingpong.MatchController;
import ua.dvalex.pingpong.R;
import ua.dvalex.pingpong.Utils;
import ua.dvalex.pingpong.controls.MatchesSpinnerControl;
import ua.dvalex.pingpong.controls.PlayersPairSpinnersControl;
import ua.dvalex.pingpong.db.ColumnProvider;
import ua.dvalex.pingpong.db.CursorLoaderHelper;
import ua.dvalex.pingpong.db.DB;
import ua.dvalex.pingpong.db.GameSaveHelper;
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
    private final MatchController matchController = MatchController.getInstance();
    private final FragmentGamesAppearanceController fragmentGamesAppearanceController =
            FragmentGamesAppearanceController.getInstance();
    private String lastWinner;
    private Button btnStartMatch, btnAddGame;
    private ListView lvGames;
    private PlayersPairSpinnersControl playersPairSpinnersControl;
    private ImageButton btnPlayer1Wins, btnPlayer2Wins, btnSave;
    private EditText etScore1, etScore2;
    private TextView lnkGoToPlayersTab;
    private MatchesSpinnerControl matchesSpinnerControl;
    private CursorLoaderHelper loaderHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);
        fragmentGamesAppearanceController.setGamesView(view);
        fragmentGamesAppearanceController.setMatchStarted(matchController.isMatchStarted());
        lastWinner = SettingsProvider.getInstance().getString(LAST_WINNER, null);
        findViews(inflater, view);
        setupGamesList(inflater);
        setClickListenerForButtons();
        setTextWatchers();
        reloadControls();
        return view;
    }

    private void findViews(LayoutInflater inflater, View v) {
        matchesSpinnerControl = new MatchesSpinnerControl(getActivity(), inflater,
                (Spinner) v.findViewById(R.id.spMatches));
        matchesSpinnerControl.setOnSelect(new MatchesSpinnerControl.OnSelect() {
            @Override
            public void onSelect() {
                loaderHelper.forceLoad();
            }
        });
        btnAddGame = (Button) v.findViewById(R.id.btnAddGame);
        btnStartMatch = (Button) v.findViewById(R.id.btnStartMatch);
        lvGames = (ListView) v.findViewById(R.id.lvGames);
        playersPairSpinnersControl = new PlayersPairSpinnersControl(getContext(),
                (Spinner) v.findViewById(R.id.spPlayer1),
                (Spinner) v.findViewById(R.id.spPlayer2));
        playersPairSpinnersControl.setup(new PlayerChangedListener());
        btnPlayer1Wins = (ImageButton) v.findViewById(R.id.btnPlayer1Wins);
        etScore1 = (EditText) v.findViewById(R.id.etScore1);
        etScore2 = (EditText) v.findViewById(R.id.etScore2);
        btnPlayer2Wins = (ImageButton) v.findViewById(R.id.btnPlayer2Wins);
        btnSave = (ImageButton) v.findViewById(R.id.btnSave);
        setEnableButton(btnSave, false);
        lnkGoToPlayersTab = (TextView) v.findViewById(R.id.lnkGoToPlayersTab);
    }

    private class PlayerChangedListener implements PlayersPairSpinnersControl.OnSelectionChangedListener {
        @Override
        public void change() {
            resetScores();
        }
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

    private void setupGamesList(LayoutInflater inflater) {
        CursorAdapter adapter = new GamesAdapter(inflater);
        lvGames.setAdapter(adapter);
        loaderHelper = new CursorLoaderHelper(getActivity(), adapter, new GamesHelperLoader());
        loaderHelper.setRunOnFinish(new CursorLoaderHelper.RunOnFinish() {
            @Override
            public void run() {
                lvGames.smoothScrollToPosition(lvGames.getMaxScrollAmount());
            }
        });
        fragmentGamesAppearanceController.setMatchCursorLoaderHelper(loaderHelper);
    }

    private class GamesHelperLoader implements CursorLoaderHelper.HelperLoader {

        @Override
        public Cursor loadInBackground() {
            return db.rawQuery(SQL_QUERY_GAMES, new String[] { String.valueOf(getDisplayedMatchId()) });
        }
    }

    private long getDisplayedMatchId() {
        return fragmentGamesAppearanceController.isHistoryMode() ?
                Utils.resolveNull(matchesSpinnerControl.getSelectedId(), -1L) :
                matchController.getCurrentMatch();
    }

    private class GamesAdapter extends CursorAdapter {

        private final LayoutInflater inflater;
        private final ColumnProvider columnProvider = new ColumnProvider(DB.ID, DB.TIMESTAMP,
                DB.PLAYER1, DB.PLAYER2, DB.SCORE1, DB.SCORE2);

        public GamesAdapter(LayoutInflater inflater) {
            super(getContext(), null, false);
            this.inflater = inflater;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            columnProvider.read(cursor);
            View view = inflater.inflate(R.layout.games_list_item, parent, false);
            view.setLongClickable(true);
            view.setOnLongClickListener(new GameLongClickListener());
            return view;
        }

        private class GameLongClickListener implements View.OnLongClickListener {

            @Override
            public boolean onLongClick(View v) {
                DialogEditGame.startEditGame(FragmentGames.this, (long) v.getTag(), loaderHelper);
                return true;
            }
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
            view.setTag(cursor.getLong(columnProvider.get(DB.ID)));
        }
    }

    private void setClickListenerForButtons() {
        GamesOnClickListener gamesOnClickListener = new GamesOnClickListener();
        View[] views = new View[] {
                btnAddGame,
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
                case R.id.btnAddGame:
                    DialogEditGame.startAddGame(FragmentGames.this, getDisplayedMatchId(), loaderHelper);
                    break;
                case R.id.btnStartMatch:
                    matchController.startMatch();
                    reloadControls();
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
            name1 = playersPairSpinnersControl.getName1();
            name2 = playersPairSpinnersControl.getName2();
            score1 = Integer.valueOf(etScore1.getText().toString().trim());
            score2 = Integer.valueOf(etScore2.getText().toString().trim());

            setLastWinner();
            Long gameIndex = saveGame();
            saveGameIndex(gameIndex);
            resetScores();
            reloadControls();
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

        private long saveGame() {
            GameSaveHelper saver = new GameSaveHelper(FragmentGames.this, loaderHelper);
            saver.setMatchId(matchController.getCurrentMatch()).setTimestamp(System.currentTimeMillis())
                    .setPlayer1Id(playersPairSpinnersControl.getPlayerId(name1))
                    .setPlayer2Id(playersPairSpinnersControl.getPlayerId(name2))
                    .setScore1(score1).setScore2(score2);
            return saver.save();
        }

        private void saveGameIndex(Long gameIndex) {
            if (gameIndex == null) return;
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

    public void reloadControls() {
        if (matchController.isMatchStarted()) {
            try {
                playersPairSpinnersControl.loadPlayers();
                playersPairSpinnersControl.reload(lastWinner);
                fragmentGamesAppearanceController.setEnoughPlayers(true);
            } catch (PlayersPairSpinnersControl.NoEnoughPlayersException e) {
                fragmentGamesAppearanceController.setEnoughPlayers(false);
            }
        } else {
            fragmentGamesAppearanceController.update();
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
}
