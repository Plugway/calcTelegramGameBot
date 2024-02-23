import java.awt.*;

public class Coordinates {
    public final int x;
    public final int y;
    public Coordinates(int x, int y){
        Rectangle bounds = MouseMovementProgram.screen.getDefaultConfiguration().getBounds();
        this.x = x + bounds.x;
        this.y = y + bounds.y;
    }
}
