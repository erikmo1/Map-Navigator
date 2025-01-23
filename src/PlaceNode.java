import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.List;

public class PlaceNode extends Circle {
    private String placeName;

    public PlaceNode(String placeName, double centerX, double centerY, double radius) {
        super(centerX, centerY, radius);
        this.placeName = placeName;
        setId(placeName);
        setFill(Color.BLUE);


    }

    public String getPlaceName() {
        return placeName;
    }

    public String getName() {
        return placeName;
    }
}


