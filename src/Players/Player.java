package Players;

import Controller.Game;
import Model.InfoBlock;
import Model.Move;

import java.awt.*;

/**
 * Created by Bozerg on 11/25/2014.
 */
public abstract class Player implements Runnable {
    protected InfoBlock currentState;
    protected volatile boolean turnOver = true;
    protected Move move;
    private String name = "Player";
    private final int playerNumber;
    private final int turnTime;
    private Thread t;
    private final Color color;


    public Player(Color color, int playerNumber, int turnTime, String name){
        this.playerNumber = playerNumber;
        if(!name.equals("")) {
            this.name = name;
        }
        this.color = color;
        this.turnTime = turnTime;
    }

    public Player(Player player){
        this.playerNumber = player.playerNumber;
        if(!name.equals("")) {
            this.name = player.name;
        }
        this.color = player.color;
        this.turnTime = player.getTurnTime();
    }

    //called by GameRunner to get the player's move.  Nothing but GameRunner should ever call this!
    public final Move getMove() {
        turnFinished();
        return move;
    }

    protected final void endTurn(){
        turnOver = true;
        Game.endTurn();
    }

    public final int getTurnTime(){
        return this.turnTime;
    }

    public final String getName(){
        return this.name;
    }

    public final int getPlayerNumber(){
        return this.playerNumber;
    }

    protected final void setMove(Move move) {
        this.move = move;
    }

    //kills the player's thread, called by getMove() and nothing else ever!
    private void turnFinished() {
        turnOver = true;
        Game.endTurn();
        t.interrupt();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //called by GameRunner to start the the player's turn.  Nothing but GameRunner should need to call this!
    public final void nextTurn(InfoBlock info) {
        turnOver = false;
        move = new Move(this.getColor(), null);
        currentState = info;
        t = new Thread(this);
        t.start();
    }

    public final Color getColor() {
        return new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue());
    }

    @Override
    public String toString(){
        return name + ", player: " + playerNumber + ", " + color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;

        Player player = (Player) o;

        if (playerNumber != player.playerNumber) return false;
        if (color != null ? !color.equals(player.color) : player.color != null) return false;
        return(name != null ? !name.equals(player.name) : player.name != null);
    }

    @Override
    public int hashCode() {
        int result = currentState != null ? currentState.hashCode() : 0;
        result = 31 * result + (turnOver ? 1 : 0);
        result = 31 * result + (move != null ? move.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + playerNumber;
        result = 31 * result + turnTime;
        result = 31 * result + (t != null ? t.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }
}
