//package backend.service.impl;
//
//import backend.exception.TurnException;
//import backend.model.MoveRequest;
//import backend.model.MoveResponse;
//import backend.service.MoveService;
//import org.example.logic.board.Board;
//
//public class MoveServiceImpl implements MoveService {
//    @Override
//    public MoveResponse move(MoveRequest request) {
//        Board board = Board.getInstance();
//        board.move(request.getMove());
//    }
//}
