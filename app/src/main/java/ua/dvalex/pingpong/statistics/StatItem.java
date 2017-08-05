package ua.dvalex.pingpong.statistics;

/**
 * Created by alex on 02.08.17
 */
public class StatItem {
    private static final int EMPTY_HASH = "".hashCode();

    private final String player1, player2;
    private int games = 0, wins1 = 0, wins2 = 0, totalScores = 0, scores1 = 0, scores2 = 0;
    private float percWins1, percWins2, avgScore1forGame, avgScore2forGame;

    public StatItem(String player1, String player2, int score1, int score2) {
        if (swapRequired(player1, player2)) {
            this.player1 = player2;
            this.player2 = player1;
            int s = score1;
            score1 = score2;
            score2 = s;
        } else {
            this.player1 = player1;
            this.player2 = player2;
        }
        addScores(this.player1 == null, score1, score2);
    }

    private static boolean swapRequired(String player1, String player2) {
        return player2 != null && (player1 == null || player1.compareTo(player2) > 0);
    }

    public static int getKey(String player1, String player2) {
        if(swapRequired(player1, player2)) {
            String p = player1;
            player1 = player2;
            player2 = p;
        }
        if (player1 == null) return EMPTY_HASH;

        int h1 = player1.hashCode();
        if (player2 == null) return h1;

        return String.format("%d%d", h1, player2.hashCode()).hashCode();
    }

    public int compare(StatItem item) {
        if (player1 == null || (player2 == null && item.player2 != null)) return -1;
        if (item.player1 == null || (player2 != null && item.player2 == null)) return 1;
        int c = player1.compareTo(item.player1);
        if (c != 0) return c;
        if (player2 == null) return 0;
        return player2.compareTo(item.player2);
    }

    public void add(String player1, String player2, int score1, int score2) {
        if (score1 == score2) throw new RuntimeException();

        if (swapRequired(player1, player2)) {
            String p = player1;
            player1 = player2;
            player2 = p;
            int s = score1;
            score1 = score2;
            score2 = s;
        }

        if (!checkEquals(this.player1, player1) || !checkEquals(this.player2, player2)) throw new RuntimeException();
        addScores(player1 == null, score1, score2);
    }

    private void addScores(boolean isTotal, int score1, int score2) {
        games++;
        totalScores += (score1 + score2);

        if (isTotal) return;
        if (score1 > score2) {
            wins1++;
        } else {
            wins2++;
        }
        scores1 += score1;
        scores2 += score2;
    }

    private static boolean checkEquals(String s1, String s2) {
        return (s1 == null && s2 == null) || (s1 != null && s2 != null && s1.equals(s2));
    }

    public void calc() {
        percWins1 = wins1 * 100f / games;
        percWins2 = wins2 * 100f / games;
        avgScore1forGame = (float) scores1 / games;
        avgScore2forGame = (float) scores2 / games;
    }


    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public int getGames() {
        return games;
    }

    public int getWins1() {
        return wins1;
    }

    public int getWins2() {
        return wins2;
    }

    public int getTotalScores() {
        return totalScores;
    }

    public int getScores1() {
        return scores1;
    }

    public int getScores2() {
        return scores2;
    }

    public float getPercWins1() {
        return percWins1;
    }

    public float getPercWins2() {
        return percWins2;
    }

    public float getAvgScore1forGame() {
        return avgScore1forGame;
    }

    public float getAvgScore2forGame() {
        return avgScore2forGame;
    }
}
