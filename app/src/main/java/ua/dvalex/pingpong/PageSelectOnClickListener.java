package ua.dvalex.pingpong;

import android.support.v4.view.ViewPager;
import android.view.View;

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
