package groupXX.bot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulator class is responsible for running a simulation of the game over multiple generations,
 * where each generation consists of several matches between a network and its opponent. The simulator
 * tracks the scores, saves networks, and identifies the best network based on performance.
 */
public class Simulator {

    private final GameSituation initialGame;
    private final NeuralNetwork myNet;

    /**
     * Constructs a Simulator with the given initial game situation and neural network.
     *
     * @param initialGame The initial game state for simulation.
     * @param myNet       The neural network to be used in the simulation.
     */
    public Simulator(GameSituation initialGame, NeuralNetwork myNet) {
        this.initialGame = initialGame;
        this.myNet = myNet;
    }

    /**
     * Runs the simulation for the specified number of generations and matches per generation.
     * For each match, the simulator plays a game between the neural network and a new opponent network,
     * calculates the score, and saves the network's performance to a file.
     *
     * @param totalGenerations The number of generations to simulate.
     * @param matchesPerGeneration The number of matches to play in each generation.
     * @throws IOException If an error occurs during file operations.
     */
    public void runSimulation(int totalGenerations, int matchesPerGeneration) throws IOException {
        long totalStartTime = System.currentTimeMillis(); // Start time for the entire simulation

        String dirPath = "nets";
        File dir = new File(dirPath);

        // Clear train.txt file if it exists
        File trainFile = new File("train.txt");
        if (trainFile.exists()) {
            trainFile.delete();
        }

        // Clear the nets directory at the beginning of the simulation
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
        } else {
            dir.mkdir();
        }

        // List to keep track of all networks and their scores
        List<String> networkScores = new ArrayList<>();

        // Outer loop for generations
        for (int generation = 1; generation <= totalGenerations; generation++) {
            System.out.println("=========== Generation " + generation + " start ===========");

            double totalScore = 0;
            long generationStartTime = System.currentTimeMillis();

            // Inner loop for matches
            for (int i = 0; i < matchesPerGeneration; i++) {
                System.out.println("--------------------------------Match " + (i + 1) + " begin...");

                long startTime = System.currentTimeMillis();

                NeuralNetwork opponentNet = new NeuralNetwork();
                opponentNet.initNet();

                Match match = new Match(initialGame, myNet, opponentNet);
                double result = match.playOneMatch();
                totalScore += result;

                long endTime = System.currentTimeMillis();
                long timeTaken = endTime - startTime;

                System.out.println("Match " + (i + 1) + " result: " + result);
                System.out.println("Time taken for Match " + (i + 1) + ": " + timeTaken + " ms");
                System.out.println("--------------------------------Match " + (i + 1) + " end...");
            }

            long generationEndTime = System.currentTimeMillis();
            long generationTimeTaken = generationEndTime - generationStartTime;

            System.out.println("Generation " + generation + " completed! Total Score: " + totalScore);
            System.out.println("Total time taken for " + matchesPerGeneration + " matches in Generation " + generation + ": " + generationTimeTaken + " ms");

            String netFileName = myNet.saveNet(dirPath);
            networkScores.add(netFileName + " - Score: " + totalScore);

            try (FileWriter writer = new FileWriter("train.txt", true)) {
                writer.write(netFileName + " - Score: " + totalScore + "\n");
            }

            System.out.println("Current myNet saved to " + dirPath + "/" + netFileName);
            System.out.println("=========== Generation " + generation + " end ===========\n");

            myNet.backward(totalScore);
        }

        // Now, find the best network based on the highest score
        String bestNetFileName = findBestNetwork(networkScores);

        // Clear the bestnet directory before saving the best network
        File bestNetDir = new File("bestnet");
        if (bestNetDir.exists()) {
            for (File file : bestNetDir.listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
        } else {
            bestNetDir.mkdir();
        }

        // Copy the best network to the bestnet directory
        File bestNetFile = new File("nets/" + bestNetFileName);
        File destination = new File("bestnet/" + bestNetFileName);
        try {
            Files.copy(bestNetFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Best network copied to bestnet directory: " + destination.getPath());
        } catch (IOException e) {
            System.out.println("Failed to copy the best network to the bestnet directory.");
            e.printStackTrace();
        }

        long totalEndTime = System.currentTimeMillis(); // End time for the entire simulation
        long totalTimeTaken = totalEndTime - totalStartTime;

        System.out.println("All generations completed!");
        System.out.println("Total time taken for all generations: " + totalTimeTaken + " ms");
    }

    /**
     * Helper method to find the best network based on the highest score from a list of network scores.
     *
     * @param networkScores The list of network scores in the format "filename - Score: score".
     * @return The file name of the best network.
     */
    private String findBestNetwork(List<String> networkScores) {
        double maxScore = Double.NEGATIVE_INFINITY;
        String bestNetFileName = "";
        for (String entry : networkScores) {
            String[] parts = entry.split(" - Score: ");
            double score = Double.parseDouble(parts[1]);
            if (score > maxScore) {
                maxScore = score;
                bestNetFileName = parts[0];
            }
        }
        return bestNetFileName;
    }
}
