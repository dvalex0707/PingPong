package ua.dvalex.pingpong;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ua.dvalex.pingpong.db.ColumnProvider;
import ua.dvalex.pingpong.db.CursorLoaderHelper;
import ua.dvalex.pingpong.db.DB;

public class DialogAddPlayers extends DialogFragment {

    private final SQLiteDatabase db = DB.getInstance().get();
    private CursorLoaderHelper parentLoaderHelper, loaderHelper;
    private ListView lstAllPlayers;
    private EditText etNewPlayer;
    private ImageButton btnNewPlayer;
    private Map<String, Player> players = new TreeMap<>();

    private class Player {
        private final long id;
        private boolean isCurrent = false;
        private boolean selected = false;

        public Player(long id) {
            this.id = id;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        loadAllPlayers();
        loadCurrentPlayers();
        getDialog().setTitle(R.string.btnAddPlayers);
        View view = inflater.inflate(R.layout.dialog_add_players, container, false);
        findViews(view);

        CursorAdapter adapter = new AddPlayersAdapter(inflater);
        lstAllPlayers.setAdapter(adapter);
        loaderHelper = new CursorLoaderHelper(getActivity(), adapter,
                new CursorLoaderHelper.StandardLoader(DB.TABLE_ALL_PLAYERS).setOrderBy(DB.NAME));

        etNewPlayer.addTextChangedListener(new AddPlayersTextWatcher());

        AddPlayersClickListener clickListener = new AddPlayersClickListener();
        btnNewPlayer.setOnClickListener(clickListener);

        view.findViewById(R.id.btnOk).setOnClickListener(clickListener);
        view.findViewById(R.id.btnCancel).setOnClickListener(clickListener);

        return view;
    }

    public void setParentLoaderHelper(CursorLoaderHelper parentLoaderHelper) {
        this.parentLoaderHelper = parentLoaderHelper;
    }

    private void loadAllPlayers() {
        Cursor cursor = db.query(DB.TABLE_ALL_PLAYERS, null, null, null, null, null, null);
        int colId = cursor.getColumnIndex(DB.ID), colName = cursor.getColumnIndex(DB.NAME);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            do {
                String name = cursor.getString(colName);
                players.put(name, new Player(cursor.getLong(colId)));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void loadCurrentPlayers() {
        Cursor cursor = db.query(DB.TABLE_CURRENT_PLAYERS, null, null, null, null, null, null);
        int colName = cursor.getColumnIndex(DB.NAME);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            do {
                Player player = players.get(cursor.getString(colName));
                player.isCurrent = true;
                player.selected = true;
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void findViews(View view) {
        lstAllPlayers = (ListView) view.findViewById(R.id.lstAllPlayers);
        etNewPlayer = (EditText) view.findViewById(R.id.etNewPlayer);
        btnNewPlayer = (ImageButton) view.findViewById(R.id.btnNewPlayer);
    }

    private class AddPlayersTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            boolean visible = !s.toString().trim().isEmpty();
            btnNewPlayer.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private class AddPlayersAdapter extends CursorAdapter {

        private final LayoutInflater inflater;
        private final ColumnProvider columnProvider = new ColumnProvider(DB.ID, DB.NAME);
        private final View.OnClickListener checkBoxClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                players.get(checkBox.getText().toString()).selected = checkBox.isChecked();
            }
        };

        public AddPlayersAdapter(LayoutInflater inflater) {
            super(getContext(), null, false);
            this.inflater = inflater;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            columnProvider.read(cursor);
            final View view = inflater.inflate(R.layout.add_players_list_item, parent, false);
            view.setOnClickListener(checkBoxClickListener);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            columnProvider.read(cursor);
            CheckBox checkBox = (CheckBox) view;
            int id = cursor.getInt(columnProvider.get(DB.ID));
            view.setTag(id);
            String name = cursor.getString(columnProvider.get(DB.NAME));
            checkBox.setText(name);
            checkBox.setChecked(players.get(name).selected);
        }
    }

    private class AddPlayersClickListener implements View.OnClickListener {

        private StringBuilder deleteStringBuilder;
        private List<ContentValues> addItems;
        private boolean deleteRequired;

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnNewPlayer:
                    btnNewPlayerClick();
                    break;
                case R.id.btnOk:
                    clickOk();
                case R.id.btnCancel:
                    getDialog().dismiss();
                    break;
            }
        }

        private void btnNewPlayerClick() {
            String name = etNewPlayer.getText().toString().trim();
            Player player = players.get(name);
            if (player == null) {
                addNewPlayer(name);
            } else {
                selectExistingPlayer(player);
            }
            etNewPlayer.getText().clear();

        }

        private void addNewPlayer(String name) {
            ContentValues cv = new ContentValues();
            cv.put(DB.NAME, name);
            db.beginTransaction();
            try {
                long id = db.insertOrThrow(DB.TABLE_ALL_PLAYERS, null, cv);
                db.setTransactionSuccessful();
                Player player = new Player(id);
                player.selected = true;
                players.put(name, player);
                loaderHelper.forceLoad();
            } finally {
                db.endTransaction();
            }
        }

        private void selectExistingPlayer(Player player) {
            Toast.makeText(getContext(),
                    player.selected ? R.string.toastAlreadySelected : R.string.toastSelectNow,
                    Toast.LENGTH_SHORT).show();
            if (!player.selected) {
                player.selected = true;
                loaderHelper.forceLoad();
            }
        }

        private void clickOk() {
            processSelectedPlayers();
            if (!deleteRequired && addItems.isEmpty()) return;
            syncCurrentPlayers();
            parentLoaderHelper.forceLoad();
        }

        private void processSelectedPlayers() {
            deleteRequired = false;
            deleteStringBuilder = new StringBuilder(DB.PLAYER_ID + " in (");
            addItems = new ArrayList<>();
            for (Map.Entry<String, Player> entry : players.entrySet()) {
                String name = entry.getKey();
                Player player = entry.getValue();
                if (player.isCurrent == player.selected) continue;
                if (player.selected) {
                    collectSelected(name, player);
                } else {
                    collectUnselected(player);
                }
            }
        }

        private void collectSelected(String name, Player player) {
            ContentValues cv = new ContentValues();
            cv.put(DB.PLAYER_ID, player.id);
            cv.put(DB.NAME, name);
            addItems.add(cv);
        }

        private void collectUnselected(Player player) {
            if (deleteRequired) {
                deleteStringBuilder.append(", ");
            }
            deleteStringBuilder.append(player.id);
            deleteRequired = true;
        }

        private void syncCurrentPlayers() {
            db.beginTransaction();
            try {
                if (deleteRequired) {
                    db.delete(DB.TABLE_CURRENT_PLAYERS, deleteStringBuilder.append(")").toString(), null);
                }
                for (ContentValues cv : addItems) {
                    db.insert(DB.TABLE_CURRENT_PLAYERS, null, cv);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }
}
