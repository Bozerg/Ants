package View;

import Controller.Game;

import javax.swing.*;
import java.awt.event.WindowEvent;

/**
 * Created by Graham on 1/17/2015.
 */
public class GameRunnerWindow extends JFrame{

    private final JPanel actualWindow = new JPanel();
    private JFrame configFrame;
    private JPanel configPane;
    private JSpinner moveDelaySpinner;
    private JSpinner numPlayersSpinner;
    private JSpinner playerTypeSpinner;
    private String[] labels;
    private String[] playerTypes;

    public GameRunnerWindow(){
        super("Ants");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.foregroundWindow();
        labels = new String[] {"Number of Players: ", "Type of Players: ","Name of Players", "Time per Turn: ", "Board Width: ", "Board Height: ","Vision Radius: ", "Replay enabled: "};
        playerTypes = new String[] {"Explorer","Forager","Forces","Homeguard","Hunter","Kamikaze","Pathfinder","Random"};
        this.generateConfigWindow();
    }

    protected void generateConfigWindow(){
        configFrame = new JFrame();  //look at ImbeddedFrame
        configPane = new JPanel(new SpringLayout());

        //Number of Players
        JLabel label1 = new JLabel(labels[0], JLabel.TRAILING);
        configPane.add(label1);
        SpinnerNumberModel playerNumbermodel = new SpinnerNumberModel(2, 2, 32, 1);
        numPlayersSpinner = new JSpinner(playerNumbermodel);
        label1.setLabelFor(numPlayersSpinner);
        configPane.add(numPlayersSpinner);

        //Type of Players
        JLabel label2 = new JLabel(labels[1], JLabel.TRAILING);
        configPane.add(label2);
        SpinnerListModel playerTypeModel = new SpinnerListModel(playerTypes);
        playerTypeSpinner = new JSpinner(playerTypeModel);
        label2.setLabelFor(playerTypeSpinner);
        configPane.add(playerTypeSpinner);

        //Name of Players - TextField
        JLabel label3 = new JLabel(labels[2], JLabel.TRAILING);
        configPane.add(label3);


        //Time per turn
        JLabel label4 = new JLabel(labels[3], JLabel.TRAILING);
        configPane.add(label4);
        SpinnerNumberModel moveDelayModel = new SpinnerNumberModel(5, 3, 200, 1);
        moveDelaySpinner = new JSpinner(moveDelayModel);
        label4.setLabelFor(moveDelaySpinner);
        configPane.add(moveDelaySpinner);
        //Width of the board
        JLabel label5 = new JLabel(labels[4], JLabel.TRAILING);
        configPane.add(label5);

        //Height of the board
        JLabel label6 = new JLabel(labels[5], JLabel.TRAILING);
        configPane.add(label6);

        //Vision Radius
        JLabel label7 = new JLabel(labels[6], JLabel.TRAILING);
        configPane.add(label7);

        //replay enabled?
        JLabel label8 = new JLabel(labels[7], JLabel.TRAILING);
        configPane.add(label8);

        configFrame.add(configPane);
        configFrame.pack();
    }

    public void getGameConfiguration(Game game){

        //Lay out the panel.
        this.add(((GraphicalView) game.getView()).getBoardPanel());
        this.pack();//Once you add a JPanel, don't forget to window.pack
    }

    public void closeWindow() {
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    public void foregroundWindow() {
        this.setVisible(true);
        this.setAlwaysOnTop(true);
        this.toFront();
        this.requestFocus();
        this.setAlwaysOnTop(false);
    }

}
