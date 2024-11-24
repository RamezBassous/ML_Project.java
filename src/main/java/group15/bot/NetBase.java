package group15.bot;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

// Base class NetBase
public abstract class NetBase {
    protected double[][] weightsInputHidden; // Weights from input layer to hidden layer
    protected double[][] weightsHiddenOutput; // Weights from hidden layer to output layer
    protected double[] hiddenLayer; // Output from hidden layer
    protected double output; // Output value from the output layer

    /**
     * Initializes the neural network with input, hidden, and output nodes
     * @param inputNodes Number of input nodes
     * @param hiddenNodes Number of hidden nodes
     * @param outputNodes Number of output nodes
     */
    public void initNet(int inputNodes, int hiddenNodes, int outputNodes) {
        weightsInputHidden = new double[inputNodes][hiddenNodes]; // Weights from input to hidden layer
        weightsHiddenOutput = new double[hiddenNodes][outputNodes]; // Weights from hidden to output layer
        hiddenLayer = new double[hiddenNodes]; // Output of hidden layer

        // Initialize weights randomly
        initializeWeights(inputNodes, hiddenNodes, outputNodes);
    }

    /**
     * Randomly initializes the weights
     * @param inputNodes Number of input nodes
     * @param hiddenNodes Number of hidden nodes
     * @param outputNodes Number of output nodes
     */
    private void initializeWeights(int inputNodes, int hiddenNodes, int outputNodes) {
        Random rand = new Random();
        for (int i = 0; i < inputNodes; i++) {
            for (int j = 0; j < hiddenNodes; j++) {
                weightsInputHidden[i][j] = rand.nextDouble() * 2 - 1; // Range [-1, 1]
            }
        }
        for (int i = 0; i < hiddenNodes; i++) {
            for (int j = 0; j < outputNodes; j++) {
                weightsHiddenOutput[i][j] = rand.nextDouble() * 2 - 1; // Range [-1, 1]
            }
        }
    }

    public abstract void initNet();

    /**
     * Forward propagation to calculate the output value
     * @param input Input array
     * @return Output value
     */
    public double forward(double[] input) {
        // Check if input array size matches expected size
        if (input.length != weightsInputHidden.length) {
            throw new IllegalArgumentException("Input size does not match expected size.");
        }

        // Calculate hidden layer values
        for (int i = 0; i < hiddenLayer.length; i++) {
            hiddenLayer[i] = 0;
            for (int j = 0; j < weightsInputHidden.length; j++) {
                hiddenLayer[i] += input[j] * weightsInputHidden[j][i];
            }
            hiddenLayer[i] = sigmoid(hiddenLayer[i]); // Apply sigmoid activation function
        }

        // Calculate output layer value
        output = 0;
        for (int i = 0; i < hiddenLayer.length; i++) {
            output += hiddenLayer[i] * weightsHiddenOutput[i][0];
        }
        output = sigmoid(output); // Apply sigmoid activation function

        return output;
    }

    /**
     * Loads neural network from file
     * @param directoryPath Path to the directory for loading the network
     * @throws IOException If an I/O error occurs
     */
    public void loadNet(String directoryPath) throws IOException {
        File file = new File(directoryPath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        // Read weights from input to hidden layer
        for (int i = 0; i < weightsInputHidden.length; i++) {
            line = br.readLine();
            String[] values = line.split(","); // Assumes comma-separated values
            for (int j = 0; j < weightsInputHidden[i].length; j++) {
                weightsInputHidden[i][j] = Double.parseDouble(values[j]);
            }
        }

        // Read weights from hidden to output layer
        for (int i = 0; i < weightsHiddenOutput.length; i++) {
            line = br.readLine();
            String[] values = line.split(","); // Assumes comma-separated values
            for (int j = 0; j < weightsHiddenOutput[i].length; j++) {
                weightsHiddenOutput[i][j] = Double.parseDouble(values[j]);
            }
        }

        br.close();
    }

    /**
     * Saves the neural network to a file and returns the filename
     * @param directoryPath Path to the directory for saving the network
     * @return The name of the saved file
     * @throws IOException If an I/O error occurs
     */
    public String saveNet(String directoryPath) throws IOException {
        // Get current time and format it as "yyyyMMddHHmmss"
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = now.format(formatter);

        String fileName = timestamp + ".txt"; // Use timestamp as file name
        File file = new File(directoryPath, fileName);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            // Save weights from input to hidden layer
            for (double[] row : weightsInputHidden) {
                for (int j = 0; j < row.length; j++) {
                    bw.write(String.valueOf(row[j]));
                    if (j < row.length - 1) {
                        bw.write(","); // Comma-separated values
                    }
                }
                bw.newLine();
            }

            // Save weights from hidden to output layer
            for (double[] row : weightsHiddenOutput) {
                for (int j = 0; j < row.length; j++) {
                    bw.write(String.valueOf(row[j]));
                    if (j < row.length - 1) {
                        bw.write(","); // Comma-separated values
                    }
                }
                bw.newLine();
            }
        }

        return fileName; // Return the saved file name
    }

    /**
     * Sigmoid activation function
     * @param x Input value
     * @return Sigmoid value
     */
    protected double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    /**
     * Backpropagation to update weights
     * @param target Target output
     */
    public void backward(double target) {
        double outputError = target - output; // Calculate output error
        double deltaOutput = outputError * output * (1 - output); // Output layer gradient

        // Update weights from hidden to output layer
        for (int i = 0; i < hiddenLayer.length; i++) {
            weightsHiddenOutput[i][0] += deltaOutput * hiddenLayer[i];
        }

        // Update weights from input to hidden layer
        for (int i = 0; i < weightsInputHidden.length; i++) {
            for (int j = 0; j < hiddenLayer.length; j++) {
                double hiddenError = deltaOutput * weightsHiddenOutput[j][0]; // Hidden layer error
                double deltaHidden = hiddenError * hiddenLayer[j] * (1 - hiddenLayer[j]); // Hidden layer gradient
                weightsInputHidden[i][j] += deltaHidden;
            }
        }
    }
}
