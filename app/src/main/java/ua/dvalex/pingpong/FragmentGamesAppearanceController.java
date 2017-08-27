package ua.dvalex.pingpong;

import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import ua.dvalex.pingpong.controls.MatchesSpinnerControl;
import ua.dvalex.pingpong.db.CursorLoaderHelper;

/**
 * Created by alex on 13.08.17
 */
public class FragmentGamesAppearanceController {
    private static FragmentGamesAppearanceController ourInstance = new FragmentGamesAppearanceController();

    public static FragmentGamesAppearanceController getInstance() {
        return ourInstance;
    }

    private FragmentGamesAppearanceController() {
    }

    private LinearLayout llGamesLayout = null, llMsgTooFewPlayers, llAddNewGame, llHistoryLayout;
    private Button btnStartMatch;
    private MenuItem actionFinish = null, actionHistory, actionDeleteMatch;
    private String historyTitle;
    private boolean historyMode = false, matchStarted = false, enoughPlayers = false, isGamesTab = false;
    private MatchesSpinnerControl spinnerControl;
    private CursorLoaderHelper matchCursorLoaderHelper;

    public void reset() {
        historyMode = false;
        matchStarted = false;
        enoughPlayers = false;
        isGamesTab = false;
    }

    public void setMenu(Menu menu) {
        actionFinish = menu.findItem(R.id.action_finish_match);
        actionHistory = menu.findItem(R.id.action_history);
        actionDeleteMatch = menu.findItem(R.id.action_delete_match);
        historyTitle = actionHistory.getTitle().toString();
    }

    public void setGamesView(View view) {
        llGamesLayout = (LinearLayout) view.findViewById(R.id.llGamesLayout);
        llMsgTooFewPlayers = (LinearLayout) view.findViewById(R.id.llMsgTooFewPlayers);
        llAddNewGame = (LinearLayout) view.findViewById(R.id.llAddNewGame);
        llHistoryLayout = (LinearLayout) view.findViewById(R.id.llHistoryLayout);
        btnStartMatch = (Button) view.findViewById(R.id.btnStartMatch);
        setUnderline((TextView) view.findViewById(R.id.lnkGoToPlayersTab));
    }

    public void setSpinnerControl(MatchesSpinnerControl spinnerControl) {
        this.spinnerControl = spinnerControl;
    }

    public void setMatchCursorLoaderHelper(CursorLoaderHelper matchCursorLoaderHelper) {
        this.matchCursorLoaderHelper = matchCursorLoaderHelper;
    }

    private void setUnderline(TextView textView) {
        SpannableString spannableString = new SpannableString(textView.getText());
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), 0);
        textView.setText(spannableString);
    }

    public void toggleHistoryMode() {
        historyMode = !historyMode;
        update();
    }

    public boolean isHistoryMode() {
        return historyMode;
    }

    public void setMatchStarted(boolean matchStarted) {
        this.matchStarted = matchStarted;
        update();
    }

    public void setEnoughPlayers(boolean enoughPlayers) {
        this.enoughPlayers = enoughPlayers;
        update();
    }

    public void setIsGamesTab(boolean isGamesTab) {
        this.isGamesTab = isGamesTab;
        update();
    }

    public void update() {
        if (actionFinish != null) {
            actionFinish.setVisible(isGamesTab && !historyMode && matchStarted);
            actionHistory.setVisible(isGamesTab);
            if (isGamesTab) {
                actionHistory.setTitle((historyMode ? "< " : "") + historyTitle);
                actionHistory.setCheckable(historyMode);
                actionHistory.setShowAsAction(historyMode ?
                        MenuItem.SHOW_AS_ACTION_IF_ROOM :
                        MenuItem.SHOW_AS_ACTION_NEVER);
            }
            actionDeleteMatch.setVisible(isGamesTab && historyMode);
        }
        if (!isGamesTab) return;
        if (llGamesLayout != null) {
            setVisibleOrGone(llGamesLayout, historyMode || matchStarted);
            setVisibleOrGone(llMsgTooFewPlayers, !historyMode && matchStarted && !enoughPlayers);
            setVisibleOrGone(llAddNewGame, !historyMode && matchStarted && enoughPlayers);
            setVisibleOrGone(llHistoryLayout, historyMode);
            setVisibleOrGone(btnStartMatch, !historyMode && !matchStarted);
        }
        if (matchCursorLoaderHelper != null && !historyMode && matchStarted) {
            matchCursorLoaderHelper.forceLoad();
        }
    }

    private void setVisibleOrGone(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public MatchesSpinnerControl getSpinnerControl() {
        return spinnerControl;
    }
}
