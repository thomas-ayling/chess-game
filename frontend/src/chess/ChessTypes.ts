export class Move {
  private startSquare: number;
  private targetSquare: number;

  constructor(startSquare: number, targetSquare: number) {
    this.startSquare = startSquare;
    this.targetSquare = targetSquare;
  }

  getStartSquare() {
    return this.startSquare;
  }

  getTargetSquare() {
    return this.targetSquare;
  }
}
