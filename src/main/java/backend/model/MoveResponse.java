package backend.model;

import lombok.AllArgsConstructor;
import org.example.logic.MoveGenerator;

import java.util.List;

@AllArgsConstructor
public class MoveResponse {
    private final int[] board;
    private final String colour;
    private final List<MoveGenerator.Move> possibleMoves;
    private final List<Integer> whiteCaptures;
    private final List<Integer> blackCaptures;
}
