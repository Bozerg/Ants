package Training;

import Controller.GameRunner;
import Model.Board;
import Model.BoardGenerator;
import Model.GameInfoBlock;
import Players.*;
import View.GraphicalView;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

public class GeneticAlgorithm implements Runnable, Observer{
	
	private static final Random r = new Random();
	private static final int num_per_generation = 15;
	private static final int num_iterations = 200;
	private static GameInfoBlock currentGameInfo;
	private static boolean gameRunning = false;
	private static final GeneticAlgorithm theGAInstance = new GeneticAlgorithm();
	//private ArrayList<double[]> generation = new ArrayList<double[]>();

	private GeneticAlgorithm() {

	}
	
	public static void main(String[] args) {
		Thread t = new Thread(theGAInstance);
		t.start();
	}
	
	@Override
	public void run() {
		ArrayList<double[]> generation = read_generation_from_file("output.derp");
		if (generation == null) {
			generation = new ArrayList<double[]>();
			for (int i=0; i<num_per_generation; i++) {
				double[] temp = generate_weights();
				generation.add(temp);
			}
		}
		for (int i=0; i < num_iterations; i++) {
			generation = compete(generation);
			write_to_file(generation);
		}
	}
	
	private ArrayList<double[]> read_generation_from_file(String filename) {
		ArrayList<double[]> toReturn = new ArrayList<double[]>();
		FileReader fileReader;
		int counter = 0;
		try {
			fileReader = new FileReader(new File(filename));
			BufferedReader br = new BufferedReader(fileReader);
			String line;
			double[] temp = new double[Players.PlayerForces.num_params*3];
			// if no more lines the readLine() returns null
			while ((line = br.readLine()) != null) {
				double value = Double.parseDouble(line);
				temp[counter] = value;
				counter++;
				if (counter == Players.PlayerForces.num_params*3) {
					toReturn.add(temp);
					temp = new double[Players.PlayerForces.num_params*3];
					counter = 0;
				}
			}
			fileReader.close();
		} catch (IOException e) {
			return null;
		}
		return toReturn;
	}

	private double mutate(double mutatee) {
		//increase or decrease by 10%
		double mutate_amt = 0.9 + 0.2*(r.nextInt(2));
		if (r.nextInt() % 3 == 0) mutate_amt = -mutate_amt; //33 percent chance to flip sign
		return mutatee*mutate_amt;
	}
	
	private double[] recombine(double[] winner, double[] loser) {
		double recomb_rate = 0.5;
		double mutate_rate = 0.1;
		for (int i=0; i<winner.length; i++) {
			if (Math.random() < recomb_rate) loser[i] = winner[i];
			if (Math.random() < mutate_rate) loser[i] = mutate(loser[i]);
		}
		return loser;
	}
	
	private double evaluate_weights(double[] weights) {
		int turnTime = 10;
		ArrayList<Color> colors = GameRunner.getColors();
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(new PlayerForces(colors.get(0), 0, turnTime, weights));
		players.add(new PlayerKamikaze(colors.get(1), 1, turnTime));
		players.add(new PlayerHunter(colors.get(2), 2, turnTime));
		players.add(new PlayerHomeguard(colors.get(3), 3, turnTime));

		ArrayList<Player> startingPlayers = new ArrayList<Player>();
		startingPlayers.add(new PlayerForces(colors.get(0), 0, turnTime, weights));
		startingPlayers.add(new PlayerKamikaze(colors.get(1), 1, turnTime));
		startingPlayers.add(new PlayerHunter(colors.get(2), 2, turnTime));
		startingPlayers.add(new PlayerHomeguard(colors.get(3), 3, turnTime));
		double score = 0.0;

		currentGameInfo = null;
		BoardGenerator generator = new BoardGenerator(50, 30, players.size(), false, 4);
		Board board = generator.generateBoard();
		GraphicalView view = new GraphicalView(board, players);
		GameRunner game = new GameRunner(board, view, players, turnTime, false, 2500);
		game.addObserver(this);
		gameRunning = true;
		Thread t = new Thread(game);
		t.start();
		while(gameRunning || currentGameInfo == null){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		view.closeWindow();
		if (!currentGameInfo.earlyTermination) {
			if (currentGameInfo.placing[0].getColor().equals(startingPlayers.get(0).getColor())) {
				score = 10000d;
				score -= (double) currentGameInfo.turnCount;
			} else if (currentGameInfo.placing[1].getColor().equals(startingPlayers.get(0).getColor())) {
				score = 1000d;
				score += (double) currentGameInfo.whenEliminated.get(startingPlayers.get(0).getColor());
			} else {
				score = 100d;
				score += (double) currentGameInfo.whenEliminated.get(startingPlayers.get(0).getColor());
			}
			System.out.println("Forces score: " + score);
		}
		return score;
	}
	
	private ArrayList<double[]> compete(ArrayList<double[]> generation) {
		int L = generation.size();
		int A = r.nextInt(L);
		int B = (A + r.nextInt(L-1) + 1) % L;
		double[] pA = generation.get(A);
		double[] pB = generation.get(B);
		double A_score = evaluate_weights(pA);
		System.out.println("Index in generation: " + String.valueOf(A));
		double B_score = evaluate_weights(pB);
		System.out.println("Index in generation: " + String.valueOf(B));
		int winner;
		int loser;
		if (A_score > B_score) {
			winner = A;
			loser = B;
		} else {
			winner = B;
			loser = A;
		}
		double[] newLoserWeights = recombine(generation.get(winner), generation.get(loser));
		generation.set(loser, newLoserWeights);
		/*
		for(int i = 0; i < generation.size(); i++){
			for(int j = 0; j < generation.get(i).length; j++) {
				System.out.print(generation.get(i)[j] + " ");
			}
			System.out.println();
		}
		*/
		return generation;
	}
	
	private double[] generate_weights() {
		double[] toReturn = new double[PlayerForces.num_params*3];
		for (int i=0; i<PlayerForces.num_params*3; i++) {
			if (i%3 == 0) toReturn[i] = r.nextInt(100)-50;
			else toReturn[i] = 5*Math.random()-2.5; 
		}
		return toReturn;
	}
	
	private void write_to_file(ArrayList<double[]> generation) {
		try {
			FileWriter output_file = new FileWriter("output.derp");
			for (double[] weights : generation) {
				for (double weight : weights) {
					String str_ = String.valueOf(weight) + '\n';
					output_file.write(str_);
				}
			}
			output_file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		currentGameInfo = (GameInfoBlock) arg;
		gameRunning = false;
	}
}
