package group15.bot;

import java.io.IOException;

public class NeuralNetwork extends NetBase {

    // Define constants
    private static final int INPUT_NODES = 25;   // Number of input nodes
    private static final int HIDDEN_NODES = 50;  // Number of hidden nodes
    private static final int OUTPUT_NODES = 1;   // Number of output nodes

    /**
     * Initialize the network and set input and hidden layer nodes
     */
    @Override
    public void initNet() {
        super.initNet(INPUT_NODES, HIDDEN_NODES, OUTPUT_NODES);
    }

    /**
     * Forward propagation to calculate the output layer value
     * @param input Input array
     * @param target Target output
     * @return Output layer value
     */
    public double forward(int[] input) {
        // Convert int[] to double[] and divide each element by 2
        double[] doubleInput = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            doubleInput[i] = input[i] / 2.0; // Divide by 2 and convert to double type
        }
        // Call the forward method from the parent class
        return super.forward(doubleInput);
    }

    /**
     * Load the neural network
     * @param directoryPath Path of the directory to load
     * @throws IOException If an I/O error occurs
     */
    @Override
    public void loadNet(String directoryPath) throws IOException {
        super.loadNet(directoryPath);
    }

    /**
     * Backward propagation to update weights
     * @param target Target output
     */
    @Override
    public void backward(double output) {

        // double outputError = target - output; // Calculate output error
        // double deltaOutput = outputError * output * (1 - output); // Calculate gradient of output layer
        double target = 100;
        double outputError = target - output; // Calculate output error
        double deltaOutput = (outputError / target) * (output / target) * (1 - output / target); // Calculate gradient of output layer

        // Update weights from hidden layer to output layer
        for (int i = 0; i < hiddenLayer.length; i++) {
            weightsHiddenOutput[i][0] += deltaOutput * hiddenLayer[i]; // Update weights
        }

        // Update weights from input layer to hidden layer
        for (int i = 0; i < weightsInputHidden.length; i++) {
            for (int j = 0; j < hiddenLayer.length; j++) {
                double hiddenError = deltaOutput * weightsHiddenOutput[j][0]; // Calculate hidden layer error
                double deltaHidden = hiddenError * hiddenLayer[j] * (1 - hiddenLayer[j]); // Calculate gradient of hidden layer
                weightsInputHidden[i][j] += deltaHidden; // Update weights
            }
        }
    }

    /**
     * Sigmoid activation function
     * @param x Input value
     * @return Sigmoid value
     */
    @Override
    protected double sigmoid(double x) {
        return super.sigmoid(x);
    }
}