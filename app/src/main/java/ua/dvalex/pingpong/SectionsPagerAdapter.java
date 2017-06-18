package ua.dvalex.pingpong;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;

import java.util.Locale;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private final Context context;

    public SectionsPagerAdapter(AppCompatActivity activity) {
        super(activity.getSupportFragmentManager());
        context = activity;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FragmentPlayers();
            case 1:
                return new FragmentGames();
            case 2:
                return new FragmentStatistics();
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
