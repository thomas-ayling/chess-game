package backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.logic.MoveGenerator;

@AllArgsConstructor
@Getter
@Setter
public class MoveRequest {
    private final MoveGenerator.Move move;
    private final Integer promotingTo;
}
