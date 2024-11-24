package group15;

import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimationUtils {
    // Not used yet might come in use later to simplify controller animation logic
    public static void setupScaleTransition(Node node) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.2), node);
        scaleUp.setToX(1.1);
        scaleUp.setToY(1.1);

        ScaleTransition scaleDown = new ScaleTransition(Duration.seconds(0.2), node);
        scaleDown.setToX(0.9);
        scaleDown.setToY(0.9);

        ScaleTransition scaleBackToNormal = new ScaleTransition(Duration.seconds(0.2), node);
        scaleBackToNormal.setToX(1.0);
        scaleBackToNormal.setToY(1.0);

        scaleDown.setOnFinished(event -> scaleBackToNormal.play());

        node.setOnMouseEntered(event -> {
            if (node.isVisible()) scaleUp.play();
        });

        node.setOnMouseExited(event -> {
            if (node.isVisible()) scaleDown.play();
        });
    }
}
