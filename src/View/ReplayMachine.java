package View;

import Model.Board;
import Model.InfoBlock;
import Players.Player;

import java.util.HashMap;

/**
 * Created by Bozerg on 11/28/2014.
 */
public class ReplayMachine {
    private final View view;
    private final Player player;
    private final HashMap<Integer, Board> replay;

    public ReplayMachine(Player player, HashMap<Integer, Board> replay, View view) {
        this.player = player;
        this.view = view;
        this.replay = replay;
    }

    public void runReplay(int milliSeconds) throws InterruptedException {
        for (int i = 0; i < replay.size(); i++) {
            if (player == null) {
                view.setBoard(replay.get(i));
            } else {
                InfoBlock visibleInfo = replay.get(i).getInfoForPlayer(player.getColor());
                view.setBoard(visibleInfo.getVisibleBoard());
                if (visibleInfo.getVisiblePoints().isEmpty()) {
                    i = replay.size();
                }
            }
            view.display();
            Thread.sleep(milliSeconds);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReplayMachine)) return false;

        ReplayMachine that = (ReplayMachine) o;

        if (player != null ? !player.equals(that.player) : that.player != null) return false;
        if (replay != null ? !replay.equals(that.replay) : that.replay != null) return false;
        return !(view != null ? !view.equals(that.view) : that.view != null);

    }

    @Override
    public int hashCode() {
        int result = view != null ? view.hashCode() : 0;
        result = 31 * result + (player != null ? player.hashCode() : 0);
        result = 31 * result + (replay != null ? replay.hashCode() : 0);
        return result;
    }
}
