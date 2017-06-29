package ua.dvalex.pingpong.db;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;

public class CursorLoaderHelper {

    public interface HelperLoader {
        public Cursor loadInBackground();
    }

    public static class StandardLoader implements  HelperLoader {
        private final String table;
        private String[] columns = null;
        private String selection = null;
        private String[] selectionArgs = null;
        private String groupBy = null;
        private String having = null;
        private String orderBy = null;

        public StandardLoader(String table) {
            this.table = table;
        }

        public StandardLoader setSelection(String selection, String... selectionArgs) {
            this.selection = selection;
            if (selectionArgs.length > 0) {
                this.selectionArgs = selectionArgs;
            }
            return this;
        }

        public StandardLoader setGroupBy(String groupBy) {
            this.groupBy = groupBy;
            return this;
        }

        public StandardLoader setHaving(String having) {
            this.having = having;
            return this;
        }

        public StandardLoader setOrderBy(String orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        @Override
        public Cursor loadInBackground() {
            return DB.getInstance().get().query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        }
    }

    private final int id = hashCode();
    private final FragmentActivity activity;
    private final CursorAdapter adapter;
    private final HelperLoader loader;

    public CursorLoaderHelper(FragmentActivity activity, CursorAdapter adapter, HelperLoader loader) {
        this.activity = activity;
        this.adapter = adapter;
        this.loader = loader;
        activity.getSupportLoaderManager().initLoader(id, null, new InternalLoaderCallbacks());
    }

    public void forceLoad() {
        activity.getSupportLoaderManager().getLoader(id).forceLoad();
    }

    private class InternalLoaderCallbacks implements LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new InternalLoader(activity, loader);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            adapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    private static class InternalLoader extends CursorLoader {

        private final HelperLoader loader;

        public InternalLoader(Context context, HelperLoader loader) {
            super(context);
            this.loader = loader;
        }

        @Override
        public Cursor loadInBackground() {
            return loader.loadInBackground();
        }
    }
}
