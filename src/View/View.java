package View;

import Model.Board;
import Players.Player;

import java.util.ArrayList;

/**
 * Created by Bozerg on 11/28/2014.
 */
public abstract class View {
    protected Board board;
    protected ArrayList<Player> players;
    public View(Board board,ArrayList<Player> players){
        this.board = board;
        this.players = new ArrayList<Player>(players);

    }

    //Should display the board
    public abstract void display();

    public final void setBoard(Board board){
        this.board = board;
    }

    public void setPlayers(ArrayList<Player> players){
        this.players = players;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof View)) return false;

        View view = (View) o;

        return !(board != null ? !board.equals(view.board) : view.board != null);

    }

    @Override
    public int hashCode() {
        return board != null ? board.hashCode() : 0;
    }
}
