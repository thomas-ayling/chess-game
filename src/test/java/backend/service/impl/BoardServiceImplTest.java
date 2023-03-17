package backend.service.impl;

import backend.exception.TurnException;
import backend.model.Board;
import backend.model.Turn;
import backend.service.BoardService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardServiceImplTest {

    BoardService boardService;


    @Test
    void createGame() {
    }

    @Test
    void getValidMoves() {
    }

    @Test
    void testGetValidMoves() {
    }

    @Test
    void addTurn() throws TurnException {
        String encodedBoard = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
        Board board = new Board(); //or generate board from encoded
        Board expectedResult = new Board();

        boardService = new BoardServiceImpl(encodedBoard);

        //perform test

         Board result =boardService.addTurn(Turn.builder().build());

        Assertions.assertEquals(result,expectedResult );
    }
}
