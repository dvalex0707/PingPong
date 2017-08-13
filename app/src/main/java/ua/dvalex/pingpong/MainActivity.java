package ua.dvalex.pingpong;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Locale;

import ua.dvalex.pingpong.db.DB;
import ua.dvalex.pingpong.fragments.FragmentGames;
import ua.dvalex.pingpong.fragments.FragmentPlayers;
import ua.dvalex.pingpong.fragments.FragmentStatistics;
import ua.dvalex.pingpong.settings.SettingsProvider;

public class MainActivity extends AppCompatActivity {

    private final MatchController matchController = MatchController.getInstance();
    private final FragmentGamesAppearanceController fragmentGamesAppearanceController =
            FragmentGamesAppearanceController.getInstance();
    private FragmentPlayers fragmentPlayers;
    private FragmentGames fragmentGames;
    private FragmentStatistics fragmentStatistics;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new PageChangeListener());

        PageSelectOnClickListener.setViewPager(mViewPager);
        findViewById(R.id.btnPlayers).setOnClickListener(PageSelectOnClickListener.PLAYERS);
        findViewById(R.id.btnGames).setOnClickListener(PageSelectOnClickListener.GAMES);
        findViewById(R.id.btnStatistics).setOnClickListener(PageSelectOnClickListener.STATISTICS);

        DB.getInstance().open(this);
        SettingsProvider.getInstance().setup(this);
        matchController.startController(this);

        fragmentPlayers = new FragmentPlayers();
        fragmentGames = new FragmentGames();
        fragmentStatistics = new FragmentStatistics();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        fragmentGamesAppearanceController.setActionFinish(menu.findItem(R.id.action_finish_match));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_finish_match:
                matchController.finishMatch();
                item.setVisible(false);
                break;
            case R.id.action_history:
                boolean historyMode = !item.isChecked();
                item.setChecked(historyMode);
                fragmentGamesAppearanceController.setHistoryMode(historyMode);
                break;
        }
        return true;
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final Context context;

        public SectionsPagerAdapter(AppCompatActivity activity) {
            super(activity.getSupportFragmentManager());
            context = activity;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return fragmentPlayers;
                case 1:
                    return fragmentGames;
                case 2:
                    return fragmentStatistics;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return context.getString(R.string.btnPlayers).toUpperCase(l);
                case 1:
                    return context.getString(R.string.btnGames).toUpperCase(l);
                case 2:
                    return context.getString(R.string.btnStatistics).toUpperCase(l);
            }
            return null;
        }
    }

    public class PageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 1:
                    fragmentGames.reloadControls();
                    break;
                case 2:
                    fragmentStatistics.onOpenTab();
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {}
    }

    public enum PageSelectOnClickListener implements View.OnClickListener {

        PLAYERS(0),
        GAMES(1),
        STATISTICS(2);

        public static void setViewPager(ViewPager pager) {
            for (PageSelectOnClickListener item : PageSelectOnClickListener.values()) {
                item.pager = pager;
            }
        }

        private final int itemNumber;
        private ViewPager pager;

        PageSelectOnClickListener(int itemNumber) {
            this.itemNumber = itemNumber;
        }

        @Override
        public void onClick(View v) {
            pager.setCurrentItem(itemNumber);
        }
    }
}
