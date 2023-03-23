package backend.service;

import backend.exception.TurnException;
import backend.model.AvailableMoves;
import backend.model.Board;
import backend.model.Turn;


/**
 * Each incoming payload will contain a Move object, with start square and target square
 * <br/>
 * <br/>
 * Each response will contain:
 * <ul>
 *      <li>An updated board
 *      <br/>
 *      (Should we send this as an array of integers(as it is stored in memory) or should we convert to fen(encoded string) for the front end to interpret as needed?)</li>
 *      <li>A list of possible moves of type Move</li>
 *      <li>A list of pieces taken by each side</li>
 *      <li>The game status (eg ongoing, check, checkmate, or stalemate)</li>
 * </ul>
 * I may try to create a PGN notation generator, which will make it easier to track moves
 */

public interface BoardService {
    void createGame(String player1, String player2);

    AvailableMoves getValidMoves(Board board, String pieceId);

    AvailableMoves getValidMoves(String gameId, String pieceId);

    Board addTurn(Turn request) throws TurnException;
}
