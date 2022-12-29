package wonderland.api.gateway.dto.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JoystickInputEvent { //extends InputEvent {
    private double relativeDeltaX;
    private double relativeDeltaY;
    private String direction;
}
