package ua.dvalex.pingpong.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import ua.dvalex.pingpong.R;
import ua.dvalex.pingpong.db.ColumnProvider;
import ua.dvalex.pingpong.db.CursorLoaderHelper;
import ua.dvalex.pingpong.db.DB;

public class FragmentPlayers extends Fragment {

    private final SQLiteDatabase db = DB.getInstance().get();
    private CursorLoaderHelper loaderHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_players, container, false);
        ListView lstCurrentPlayers = (ListView) view.findViewById(R.id.lstCurrentPlayers);
        CursorAdapter adapter = new AllPlayersAdapter(inflater);
        lstCurrentPlayers.setAdapter(adapter);
        loaderHelper = new CursorLoaderHelper(getActivity(), adapter,
                new CursorLoaderHelper.StandardLoader(DB.TABLE_CURRENT_PLAYERS).setOrderBy(DB.ID));

        view.findViewById(R.id.btnAddPlayers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogAddPlayers fragment = new DialogAddPlayers();
                fragment.setParentLoaderHelper(loaderHelper);
                getFragmentManager().beginTransaction().add(fragment, null).addToBackStack(null).commit();
            }
        });

        return view;
    }

    private class AllPlayersAdapter extends CursorAdapter {

        private final LayoutInflater inflater;
        private final ColumnProvider columnProvider = new ColumnProvider(DB.NAME);
        private final View.OnClickListener deletePlayerClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parent = (View) v.getParent();
                TextView tvPlayerName = (TextView) parent.findViewById(R.id.tvPlayerName);
                deletePlayer(tvPlayerName.getText().toString());
            }
        };

        public AllPlayersAdapter(LayoutInflater inflater) {
            super(getContext(), null, false);
            this.inflater = inflater;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            columnProvider.read(cursor);
            View view = inflater.inflate(R.layout.fragment_players_list_item, parent, false);
            view.findViewById(R.id.btnDelPlayer).setOnClickListener(deletePlayerClickListener);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            columnProvider.read(cursor);
            TextView tvPlayerName = (TextView) view.findViewById(R.id.tvPlayerName);
            tvPlayerName.setText(cursor.getString(columnProvider.get(DB.NAME)));
        }

        private void deletePlayer(String name) {
            db.beginTransaction();
            try {
                db.delete(DB.TABLE_CURRENT_PLAYERS, DB.NAME + " = ?", new String[] { name });
                db.setTransactionSuccessful();
                loaderHelper.forceLoad();
            } finally {
                db.endTransaction();
            }
        }
     }
}
