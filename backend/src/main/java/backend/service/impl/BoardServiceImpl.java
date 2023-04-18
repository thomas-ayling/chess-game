//package backend.service.impl;
//
//import backend.exception.TurnException;
//import backend.model.AvailableMoves;
//import backend.model.Board;
//import backend.model.Turn;
//import backend.service.BoardService;
//import org.example.logic.MoveGenerator;
//
//public class BoardServiceImpl implements BoardService {
//
//    private Board board;
//    //possible repo
//    //private BoardRepository repository;
//    private MoveGenerator moveGenerator;
//
//
//    public BoardServiceImpl(Board board) {
//        //or pass encoded board (String) and transform
//        // this.board = getFromEnconded(encondedString)
//        this.board = board;
//        this.moveGenerator = new MoveGenerator(board);
//    }
//
//    public BoardServiceImpl(String boardId) {
//        //retrieve board from repo.
//        this.moveGenerator = new MoveGenerator();
//    }
//
//    @Override
//    public void createGame(String player1, String player2) {
//        board = new Board();
//    }
//
//    public void makeMove(int startPos, int targetPos) {
//        board.move(startPos, targetPos);
//    }
//
//    @Override
//    public AvailableMoves getValidMoves(Board board, String pieceId) {
//        return null;
//    }
//
//    @Override
//    public AvailableMoves getValidMoves(String gameId, String pieceId) {
//        return null;
//    }
//
//    @Override
//    public Board addTurn(Turn request) throws TurnException {
//        return new Board();
//    }
//}
