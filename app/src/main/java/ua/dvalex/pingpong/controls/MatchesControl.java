package ua.dvalex.pingpong.controls;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.util.Date;

import ua.dvalex.pingpong.Utils;
import ua.dvalex.pingpong.db.ColumnProvider;
import ua.dvalex.pingpong.db.CursorLoaderHelper;
import ua.dvalex.pingpong.db.DB;

/**
 * Created by alex on 12.08.17
 */
public class MatchesControl {

    public interface OnSelect {
        void onSelect();
    }

    private final FragmentActivity activity;
    private final LayoutInflater layoutInflater;
    private final Spinner spMatches;
    private OnSelect onSelect = null;
    private CursorLoaderHelper spinnerCursorLoader;

    public MatchesControl(FragmentActivity activity, LayoutInflater layoutInflater, Spinner spMatches) {
        this.activity = activity;
        this.layoutInflater = layoutInflater;
        this.spMatches = spMatches;
        setupMatchesSpinner();
    }

    public void setOnSelect(OnSelect onSelect) {
        this.onSelect = onSelect;
    }

    public void forceLoad() {
        spinnerCursorLoader.forceLoad();
    }

    private void setupMatchesSpinner() {
        MatchesSpinnerAdapter adapter = new MatchesSpinnerAdapter();
        spMatches.setAdapter(adapter);
        spinnerCursorLoader = new CursorLoaderHelper(activity, adapter,
                new CursorLoaderHelper.StandardLoader(DB.TABLE_MATCHES).setOrderBy(DB.DATE));
        spinnerCursorLoader.setRunOnFinish(new CursorLoaderHelper.RunOnFinish() {
            @Override
            public void run() {
                spMatches.setSelection(spMatches.getCount() - 1);
            }
        });
        spMatches.setOnItemSelectedListener(getOnItemSelectedListener());
    }

    private class MatchesSpinnerAdapter extends CursorAdapter {

        private final ColumnProvider columnProvider = new ColumnProvider(DB.ID, DB.DATE);

        public MatchesSpinnerAdapter() {
            super(activity, null, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            columnProvider.read(cursor);
            return layoutInflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            view.setTag(cursor.getLong(columnProvider.get(DB.ID)));
            String dateString = cursor.getString(columnProvider.get(DB.DATE));
            try {
                Date date = Utils.dbFormatToDate(dateString);
                dateString = Utils.timestampToString(activity, date.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ((TextView) view.findViewById(android.R.id.text1)).setText(dateString);
        }
    }

    @NonNull
    private AdapterView.OnItemSelectedListener getOnItemSelectedListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (onSelect != null) {
                    onSelect.onSelect();
                }
                //gatherStatistics();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }

    public Long getSelectedId() {
        View view = spMatches.getSelectedView();
        return view == null ? null : (long) view.getTag();
    }
}
