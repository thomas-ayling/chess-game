package backend.model;

public enum PieceType {
    NONE(0),
    KING(1),
    PAWN(2),
    KNIGHT(3),
    BISHOP(4),
    ROOK(5),
    QUEEN(6);

    public final int code;

    PieceType(int code) {
        this.code = code;
    }

    public String label() {
        switch (this.code) {
            case (1):
                return "King";
            case (2):
                return "Pawn";
            case (3):
                return "Knight";
            case (4):
                return "Bishop";
            case (5):
                return "Rook";
            case (6):
                return "Queen";
            default:
                return null;
        }
    }
}
