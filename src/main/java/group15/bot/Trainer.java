package group15.bot;

import java.io.IOException;

public class Trainer {

    public static void main(String[] args) throws IOException {
        System.out.println("Starting training...");

        // Create instances of GameSituation and NeuralNetwork
        GameSituation initialGame = new GameSituation();
        NeuralNetwork myNet = new NeuralNetwork();
        myNet.initNet(); // Initialize the neural network

        // Create an instance of Simulator and run the simulation
        Simulator simulator = new Simulator(initialGame, myNet);
        simulator.runSimulation(500,100);

        System.out.println("Training completed!");
    }
}