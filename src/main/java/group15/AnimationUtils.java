package group15;

import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Utility class providing methods to animate nodes with scale transitions.
 * Currently used to scale a node up on mouse enter and scale it back down on mouse exit.
 * This class can be extended in the future to simplify controller animation logic.
 */
public class AnimationUtils {

    /**
     * Sets up a scale transition for the given node. The node will scale up when the mouse enters,
     * then scale down and back to normal size when the mouse exits.
     *
     * @param node The node to which the scale transition is applied.
     *             This node will be animated when the mouse enters and exits.
     */
    // Not used yet might come in use later to simplify controller animation logic
    public static void setupScaleTransition(Node node) {
        // Create a scale transition to scale the node up (to 1.1 times its original size)
        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.2), node);
        scaleUp.setToX(1.1);
        scaleUp.setToY(1.1);

        // Create a scale transition to scale the node down (to 0.9 times its original size)
        ScaleTransition scaleDown = new ScaleTransition(Duration.seconds(0.2), node);
        scaleDown.setToX(0.9);
        scaleDown.setToY(0.9);

        // Create a scale transition to return the node to its original size (1.0)
        ScaleTransition scaleBackToNormal = new ScaleTransition(Duration.seconds(0.2), node);
        scaleBackToNormal.setToX(1.0);
        scaleBackToNormal.setToY(1.0);

        // When the scale-down transition finishes, start the scale-back-to-normal transition
        scaleDown.setOnFinished(event -> scaleBackToNormal.play());

        // Play the scale-up animation when the mouse enters the node and it is visible
        node.setOnMouseEntered(event -> {
            if (node.isVisible()) scaleUp.play();
        });

        // Play the scale-down animation when the mouse exits the node and it is visible
        node.setOnMouseExited(event -> {
            if (node.isVisible()) scaleDown.play();
        });
    }
}
