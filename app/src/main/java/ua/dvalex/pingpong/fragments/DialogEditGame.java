package ua.dvalex.pingpong.fragments;

import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;

import ua.dvalex.pingpong.R;
import ua.dvalex.pingpong.Utils;
import ua.dvalex.pingpong.controls.PlayersPairSpinnersControl;
import ua.dvalex.pingpong.db.CursorLoaderHelper;
import ua.dvalex.pingpong.db.DB;
import ua.dvalex.pingpong.db.GameSaveHelper;

/**
 * Created by alex on 06.08.17
 */
public class DialogEditGame extends DialogFragment {

    private final static String GAME_ID_KEY = "GameIdKey";
    private final static String MATCH_ID_KEY = "MatchIdKey";
    private final SQLiteDatabase db = DB.getInstance().get();
    private CursorLoaderHelper loaderHelper;
    private EditText etDate, etScore1, etScore2;
    private TextView tvDateTimeFormatError;
    private Button btnDelete, btnSave, btnCancel;
    private PlayersPairSpinnersControl playersPairSpinnersControl;

    private Long gameId = null, matchId = null;
    private long timestamp, playerId1, playerId2;
    private int score1, score2;

    public static void startAddGame(Fragment fragment, long id, CursorLoaderHelper loaderHelper) {
        start(fragment, id, loaderHelper, MATCH_ID_KEY);
    }

    public static void startEditGame(Fragment fragment, long id, CursorLoaderHelper loaderHelper) {
        start(fragment, id, loaderHelper, GAME_ID_KEY);
    }

    private static void start(Fragment fragment, long id, CursorLoaderHelper loaderHelper, String key) {
        Bundle args = new Bundle();
        args.putLong(key, id);
        DialogEditGame dialog = new DialogEditGame();
        dialog.setLoaderHelper(loaderHelper);
        dialog.setArguments(args);
        fragment.getFragmentManager().beginTransaction().add(dialog, null).addToBackStack(null).commit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments().containsKey(GAME_ID_KEY)) {
            gameId = getArguments().getLong(GAME_ID_KEY);
        } else {
            matchId = getArguments().getLong(MATCH_ID_KEY);
        }
        final Dialog dialog = getDialog();
        dialog.setTitle(R.string.titleEditGame);
        View view = inflater.inflate(R.layout.dialog_edit_game, container, false);
        findViews(view);
        try {
            loadGame();
            setupListeners();
        } catch (PlayersPairSpinnersControl.NoEnoughPlayersException e) {
            showNoEnoughPlayersAlert(dialog);
        }
        return view;
    }

    private void setLoaderHelper(CursorLoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
    }

    private void findViews(View v) {
        etDate = (EditText) v.findViewById(R.id.etDate);
        etScore1 = (EditText) v.findViewById(R.id.etScore1);
        etScore2 = (EditText) v.findViewById(R.id.etScore2);
        tvDateTimeFormatError = (TextView) v.findViewById(R.id.tvDateTimeFormatError);
        btnDelete = (Button) v.findViewById(R.id.btnDelete);
        if (gameId == null) {
            btnDelete.setVisibility(View.GONE);
        }
        btnSave = (Button) v.findViewById(R.id.btnSave);
        btnCancel = (Button) v.findViewById(R.id.btnCancel);
        playersPairSpinnersControl = new PlayersPairSpinnersControl(getContext(),
                (Spinner) v.findViewById(R.id.spPlayer1),
                (Spinner) v.findViewById(R.id.spPlayer2));
        playersPairSpinnersControl.setup(new PlayerChangedListener());
    }

    private class PlayerChangedListener implements  PlayersPairSpinnersControl.OnSelectionChangedListener {
        @Override
        public void change() {
            setSaveEnabled();
        }
    }

    private void setSaveEnabled() {
        boolean enabled = false;
        int dateTimeParseErrorMsgVisibility = View.INVISIBLE;
        try {
            enabled = checkEnabled();
        } catch (ParseException e) {
            dateTimeParseErrorMsgVisibility = View.VISIBLE;
        }
        tvDateTimeFormatError.setVisibility(dateTimeParseErrorMsgVisibility);
        btnSave.setEnabled(enabled);
    }

    private boolean checkEnabled() throws ParseException {
        String dateString = etDate.getText().toString();
        long ts = Utils.stringToTimestamp(getContext(), dateString);
        if (!dateString.equals(Utils.timestampToString(getContext(), ts)))
            throw new ParseException("", 0);
        return gameId == null || ts != timestamp ||
                playerId1 != playersPairSpinnersControl.getPlayerId(playersPairSpinnersControl.getName1()) ||
                playerId2 != playersPairSpinnersControl.getPlayerId(playersPairSpinnersControl.getName2()) ||
                score1 != getScore(etScore1) || score2 != getScore(etScore2);
    }

    private int getScore(EditText etScore) {
        try {
            return Integer.valueOf(etScore.getText().toString());
        } catch (Exception e){
            return 0;
        }
    }

    private void loadGame() throws PlayersPairSpinnersControl.NoEnoughPlayersException {
        if (gameId != null) {
            Cursor cursor = db.query(DB.TABLE_GAMES, null, DB.ID + " = ?",
                    new String[] {String.valueOf(gameId)}, null, null, null);
            cursor.moveToFirst();
            if (cursor.getCount() != 1) throw new RuntimeException();
            matchId = cursor.getLong(cursor.getColumnIndex(DB.MATCH));
            timestamp = cursor.getLong(cursor.getColumnIndex(DB.TIMESTAMP)) / 1000 * 1000;
            playerId1 = cursor.getLong(cursor.getColumnIndex(DB.PLAYER1));
            playerId2 = cursor.getLong(cursor.getColumnIndex(DB.PLAYER2));
            score1 = cursor.getInt(cursor.getColumnIndex(DB.SCORE1));
            score2 = cursor.getInt(cursor.getColumnIndex(DB.SCORE2));
            cursor.close();
            playersPairSpinnersControl.loadPlayers(playerId1, playerId2);
            playersPairSpinnersControl.reload(null);

            playersPairSpinnersControl.selectPlayer1ById(playerId1);
            playersPairSpinnersControl.selectPlayer2ById(playerId2);
            etScore1.setText(String.valueOf(score1));
            etScore2.setText(String.valueOf(score2));
        } else {
            timestamp = System.currentTimeMillis();
            playersPairSpinnersControl.loadPlayers();
            playersPairSpinnersControl.reload(null);
        }
        etDate.setText(Utils.timestampToString(getContext(), timestamp));
    }

    private void setupListeners() {
        EditGameTextWatcher watcher = new EditGameTextWatcher();
        etDate.addTextChangedListener(watcher);
        etScore1.addTextChangedListener(watcher);
        etScore2.addTextChangedListener(watcher);
        ButtonListener buttonListener = new ButtonListener();
        btnDelete.setOnClickListener(buttonListener);
        btnSave.setOnClickListener(buttonListener);
        btnCancel.setOnClickListener(buttonListener);
    }

    private class EditGameTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            setSaveEnabled();
        }
    }

    private class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnDelete:
                    deleteGame();
                    break;
                case R.id.btnSave:
                    saveGame();
                    break;
                case R.id.btnCancel:
                    getDialog().dismiss();
                    break;
            }
        }
    }

    private void deleteGame() {
        AlertDialog dialog = new AlertDialog();
        dialog.setup(this, R.string.titleDeleteGame, R.string.msgConfirmDeleteGame)
                .setIsRed().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });
        dialog.start();
    }

    private void delete() {
        db.delete(DB.TABLE_GAMES, DB.ID + " = ?", new String[]{String.valueOf(gameId)});
        loaderHelper.forceLoad();
        getDialog().dismiss();
    }

    private void saveGame() {
        try {
            if (gameId != null && Utils.stringToTimestamp(getContext(), etDate.getText().toString()) != timestamp) {
                AlertDialog dialog = new AlertDialog();
                dialog.setup(this, R.string.titleEditGame, R.string.msgConfirmChangeDateTime)
                        .setIsRed().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmAndSave();
                    }
                });
                dialog.start();
            } else {
                confirmAndSave();
            }
        } catch (ParseException e) {
            throw new RuntimeException();
        }
    }

    private void confirmAndSave() {
        if (gameId != null) {
            AlertDialog dialog = new AlertDialog();
            dialog.setup(this, R.string.titleEditGame, R.string.msgConfirmEditGame)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            save();
                        }
                    });
            dialog.start();
        } else {
            save();
        }
    }

    private void save() {
        try {
            GameSaveHelper saver = new GameSaveHelper(DialogEditGame.this, loaderHelper);
            saver.setGameId(gameId).setMatchId(matchId)
                    .setTimestamp(Utils.stringToTimestamp(getContext(), etDate.getText().toString()))
                    .setPlayer1Id(playersPairSpinnersControl.getPlayerId(playersPairSpinnersControl.getName1()))
                    .setPlayer2Id(playersPairSpinnersControl.getPlayerId(playersPairSpinnersControl.getName2()))
                    .setScore1(getScore(etScore1)).setScore2(getScore(etScore2));
            saver.save();
            getDialog().dismiss();
        } catch (ParseException e) {
            throw new RuntimeException();
        }
    }

    private void showNoEnoughPlayersAlert(final Dialog dialog) {
        AlertDialog alertDialog = new AlertDialog();
        alertDialog.setup(this, R.string.titleTooFewPlayers, R.string.msgAtLeastTwoPlayersShouldBeSelectedOnPlayersTab)
                .setIsRed().setWithoutCancel().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        alertDialog.start();
    }
}
