package backend.service;

import backend.exception.TurnException;
import backend.model.AvailableMoves;
import backend.model.Turn;
import backend.model.Board;

public interface BoardService {
    void createGame(String player1, String player2);
    AvailableMoves getValidMoves(Board board, String pieceId);
    AvailableMoves getValidMoves(String gameId, String pieceId);
    Board addTurn(Turn request) throws TurnException;
}
