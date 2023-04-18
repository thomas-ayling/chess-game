import React from 'react'
import ChessBoard from './ChessBoard'
import { Move } from './ChessTypes';

const ChessUi = () => {
    const encodedBoard = 'rnbqkbnr/ppppppPp/8/8/8/8/PPPPPPpP/RNBQKBNR';
    const mockMoves: Move[] = [new Move(8, 16), new Move(1, 16), new Move(1, 18), new Move(8, 24), new Move(9, 25), new Move(14, 6), new Move(54, 62)];
  
  return (
    <div style={{display:'flex', gap:'50px', backgroundColor:'green', padding:'80px'}}>
        <ChessBoard boardSize={600} playingWhite={true} encodedBoard={encodedBoard} possibleMoves={mockMoves} />
        <ChessBoard boardSize={600} playingWhite={false} encodedBoard={encodedBoard} possibleMoves={mockMoves} />
    </div>
  )
}

export default ChessUi
