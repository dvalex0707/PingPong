package ua.dvalex.pingpong.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import ua.dvalex.pingpong.R;
import ua.dvalex.pingpong.Utils;
import ua.dvalex.pingpong.controls.MatchesSpinnerControl;
import ua.dvalex.pingpong.statistics.StatItem;
import ua.dvalex.pingpong.statistics.Statistics;

public class FragmentStatistics extends Fragment {

    private static final String MATCH_ID_KEY = "MatchIdKey";
    private final int STATISTICS_LOADER_ID = hashCode();
    private MatchesSpinnerControl matchesSpinnerControl;
    private LayoutInflater layoutInflater;
    private Spinner spMatches;
    private CheckBox cbAllMatches;
    private ListView lvStatistics;
    private Loader<List<StatItem>> statisticsListViewLoader;
    private boolean isGathering = false;
    private Long matchIdToGather = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layoutInflater = inflater;
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        findViews(view);
        cbAllMatches.setOnCheckedChangeListener(getOnCheckedChangedListener());
        setupStatisticsListView();
        return view;
    }

    private void findViews(View view) {
        spMatches = (Spinner) view.findViewById(R.id.spMatches);
        cbAllMatches = (CheckBox) view.findViewById(R.id.cbAllMatches);
        lvStatistics = (ListView) view.findViewById(R.id.lvStatistics);
        matchesSpinnerControl = new MatchesSpinnerControl(getActivity(), layoutInflater, spMatches);
        matchesSpinnerControl.setOnSelect(new MatchesSpinnerControl.OnSelect() {
            @Override
            public void onSelect() {
                gatherStatistics();
            }
        });
    }

    @NonNull
    private CompoundButton.OnCheckedChangeListener getOnCheckedChangedListener() {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                spMatches.setEnabled(!isChecked);
                gatherStatistics();
            }
        };
    }

    private void setupStatisticsListView() {
        statisticsListViewLoader = getActivity().getSupportLoaderManager().
                initLoader(STATISTICS_LOADER_ID, null, new StatisticsLoaderCallbacks());
    }

    private class StatisticsLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<StatItem>> {

        @Override
        public AsyncTaskLoader<List<StatItem>> onCreateLoader(int id, Bundle args) {
            return id == STATISTICS_LOADER_ID ? new StatisticsLoader(getContext(), args) : null;
        }

        @Override
        public void onLoadFinished(Loader<List<StatItem>> loader, List<StatItem> data) {
            if (data == null) return;
            lvStatistics.setAdapter(new StatisticsAdapter(data));
            lvStatistics.invalidate();
            isGathering = false;
        }

        @Override
        public void onLoaderReset(Loader<List<StatItem>> loader) {

        }
    }

    private static class StatisticsLoader extends AsyncTaskLoader<List<StatItem>> {
        private final Bundle arguments;
        private Statistics statistics;

        public StatisticsLoader(Context context, Bundle arguments) {
            super(context);
            this.arguments = arguments;
        }

        @Override
        public List<StatItem> loadInBackground() {
            if (arguments == null || !arguments.containsKey(MATCH_ID_KEY)) return null;
            long matchId = arguments.getLong(MATCH_ID_KEY);
            statistics = new Statistics(matchId == -1 ? null : matchId);
            statistics.gather();
            return statistics.getList();
        }

        @Override
        protected boolean onCancelLoad() {
            if (statistics != null) {
                statistics.cancel();
            }
            return super.onCancelLoad();
        }
    }

    private class StatisticsAdapter extends BaseAdapter {

        private final List<StatItem> statItemList;

        public StatisticsAdapter(List<StatItem> statItemList) {
            this.statItemList = statItemList;
        }

        @Override
        public int getCount() {
            return statItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return statItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.statistics_list_item, null);
            }
            TextView player = (TextView) convertView.findViewById(R.id.player);
            TextView textByGames = (TextView) convertView.findViewById(R.id.textByGames);
            TextView byGames = (TextView) convertView.findViewById(R.id.statByGames);
            TextView textByScores = (TextView) convertView.findViewById(R.id.textByScores);
            TextView byScores = (TextView) convertView.findViewById(R.id.statByScores);
            LinearLayout averageLine = (LinearLayout) convertView.findViewById(R.id.averageLine);
            TextView avgForGame = (TextView) convertView.findViewById(R.id.statAverageScore);
            StatItem item = statItemList.get(position);

            if(item.getPlayer1() == null) {
                player.setText(getString(R.string.total));
                textByGames.setVisibility(View.GONE);
                byGames.setText(getString(R.string.totalGames, item.getGames()));
                textByScores.setVisibility(View.GONE);
                byScores.setText(getString(R.string.totalScores, item.getTotalScores()));
                averageLine.setVisibility(View.GONE);
            } else {
                player.setText(item.getPlayer2() == null ?
                        item.getPlayer1() :
                        String.format("%s : %s", item.getPlayer1(), item.getPlayer2()));

                textByGames.setVisibility(View.VISIBLE);
                byGames.setText(getString(R.string.byGamesFormat,
                        item.getWins1(), item.getWins2(),
                        item.getPercWins1(), item.getPercWins2(),
                        item.getGames()));

                textByScores.setVisibility(View.VISIBLE);
                byScores.setText(getString(R.string.byScoresFormat,
                        item.getScores1(), item.getScores2(), item.getTotalScores()));

                averageLine.setVisibility(View.VISIBLE);
                avgForGame.setText(String.format("%.1f : %.1f",
                        item.getAvgScore1forGame(), item.getAvgScore2forGame()));
            }
            return convertView;
        }
    }

    public void onOpenTab() {
        spMatches.setEnabled(true);
        cbAllMatches.setChecked(false);
        matchesSpinnerControl.forceLoad();
        gatherStatistics();
    }

    private void gatherStatistics() {
        Long matchId = cbAllMatches.isChecked() ? null : matchesSpinnerControl.getSelectedId();
        if (gatheringNow(matchId)) return;
        if (isGathering) {
            statisticsListViewLoader.cancelLoad();
        }
        matchIdToGather = matchId;
        isGathering = true;
        Bundle arguments = new Bundle();
        arguments.putLong(MATCH_ID_KEY, Utils.resolveNull(matchId, -1L));
        statisticsListViewLoader = getLoaderManager().restartLoader(STATISTICS_LOADER_ID, arguments, new StatisticsLoaderCallbacks());
        statisticsListViewLoader.forceLoad();
    }

    private boolean gatheringNow(Long matchId) {
        if (!isGathering || (matchId == null) != (matchIdToGather == null)) return false;
        return matchId == null || matchId.equals(matchIdToGather);
    }
}
