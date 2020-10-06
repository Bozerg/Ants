package View;

import Model.Board;

/**
 * Created by Bozerg on 11/28/2014.
 */
public abstract class View {
    protected Board board;

    public View(Board board){
        this.board = board;
    }

    //Should display the board
    public abstract void display();

    public final void setBoard(Board board){
        this.board = board;
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
