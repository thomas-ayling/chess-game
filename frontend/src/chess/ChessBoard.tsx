import React, {useEffect, useState} from 'react';
import Sketch from 'react-p5';
import p5Types from 'p5'; //Import this for typechecking and intellisense
import {Move} from './ChessTypes';

interface Props {
    boardSize: number;
    // white = true, black = false
    playingWhite: boolean;
    encodedBoard: string;
    possibleMoves: Move[];
}

const ChessBoard: React.FC<Props> = ({boardSize, playingWhite, encodedBoard, possibleMoves}: Props) => {
    type PieceImageMapType = {
        p: p5Types.Image;
        r: p5Types.Image;
        n: p5Types.Image;
        b: p5Types.Image;
        q: p5Types.Image;
        k: p5Types.Image;
        P: p5Types.Image;
        R: p5Types.Image;
        N: p5Types.Image;
        B: p5Types.Image;
        Q: p5Types.Image;
        K: p5Types.Image;
    };

    const [pieceImageMap, setPieceImageMap] = useState<PieceImageMapType>();
    const [selectedSquare, setSelectedSquare] = useState<number>();
    const [selectedMoves, setSelectedMoves] = useState<Array<Move>>();
    const [promoting, setPromoting] = useState<boolean>(false);

    const [pieceToPromote, setPieceToPromote] = useState<string>();
    const [promotingTrigger, setPromotingTrigger] = useState<boolean>(false);
    const [finalMove, setFinalMove] = useState<Move>();

    // const [encodedBoard, setEncodedBoard] = useState('RNBQK11R/PPP1NnPP/11111111/11111111/1B111111/11p11111/pp1Pbppp/rnbq1k1r');
    const [encodedBoards, setEncodedBoards] = useState(encodedBoard);
    console.log('encodedBoards.length', encodedBoards.length);
    const [encodedBoardIndex, setEncodedBoardIndex] = useState(0);

    const squareSize = boardSize / 8;
    const imageSize = squareSize - squareSize / 3;
    const imageOffset = squareSize / 3 / 2;
    const selectedSquareMarkerOpacity = 50;

    const darkColor = '#34364A';
    const opaqueDarkColor = '#34364A99';
    const lightColor = '#F5F7FA';

    const loadImages = (p5: p5Types) => {
        setPieceImageMap({
            p: p5.loadImage('./assets/black-pawn.png'),
            r: p5.loadImage('./assets/black-rook.png'),
            n: p5.loadImage('./assets/black-knight.png'),
            b: p5.loadImage('./assets/black-bishop.png'),
            q: p5.loadImage('./assets/black-queen.png'),
            k: p5.loadImage('./assets/black-king.png'),
            P: p5.loadImage('./assets/white-pawn.png'),
            R: p5.loadImage('./assets/white-rook.png'),
            N: p5.loadImage('./assets/white-knight.png'),
            B: p5.loadImage('./assets/white-bishop.png'),
            Q: p5.loadImage('./assets/white-queen.png'),
            K: p5.loadImage('./assets/white-king.png'),
        });
    };

    useEffect(() => {
        setEncodedBoards(encodedBoards[encodedBoardIndex]);
    }, [encodedBoardIndex]);

    useEffect(() => {
        console.log('pieceToPromote', pieceToPromote);
        return () => {
            setPieceToPromote(undefined);
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [promotingTrigger]);

    useEffect(() => {
        console.log('finalMove', finalMove);
        if (finalMove?.getTargetSquare() === 24) {
        }
        return () => {
            setFinalMove(undefined);
        };
    }, [finalMove]);

    const setup = (p5: p5Types, canvasParentRef: Element) => {
        loadImages(p5);
        p5.createCanvas(boardSize, boardSize).parent(canvasParentRef);
    };

    const draw = (p5: p5Types) => {
        p5.background(p5.color(darkColor));
        p5.noStroke();

        drawBoard(p5);
        drawSelectedSquare(p5);
        drawPieces(p5);
        drawSelectedMoves(p5);
        if (promoting) {
            handlePromotion(p5);
            return;
        }
    };

    const keyPressed = (p5: p5Types) => {
        if (p5.keyCode === p5.LEFT_ARROW) {
            setEncodedBoardIndex(encodedBoardIndex - 1);
        } else if (p5.keyCode === p5.RIGHT_ARROW) {
            setEncodedBoardIndex(encodedBoardIndex + 1);
        }
    };

    const mouseClicked = (p5: p5Types) => {
        let mouseX = p5.mouseX;
        let mouseY = p5.mouseY;
        if (mouseX < 0 || mouseX > boardSize || mouseY < 0 || mouseY > boardSize) {
            return;
        }

        if (promoting && pieceToPromote) {
            setPromotingTrigger(!promotingTrigger);
            setPromoting(false);
            return;
        }

        let file = Math.floor(mouseX / squareSize);
        let rank = Math.floor(mouseY / squareSize);

        let tempSelectedSquare = playingWhite ? rank * 8 + file : 63 - (rank * 8 + file);

        if (selectedSquare && selectedMoves) {
            for (let move of selectedMoves) {
                if (selectedSquare === move.getStartSquare() && tempSelectedSquare === move.getTargetSquare()) {
                    checkForPromotion(move);
                    setFinalMove(move);
                }
            }
        }

        let tempSelectedMoves = new Array<Move>();
        for (let move of possibleMoves) {
            if (tempSelectedSquare === move.getStartSquare()) {
                tempSelectedMoves.push(move);
            }
        }
        setSelectedMoves(tempSelectedMoves);
        if (tempSelectedMoves.length > 0) {
            setSelectedSquare(tempSelectedSquare);
            return;
        }
        setSelectedSquare(undefined);
    };

    const checkForPromotion = (move: Move) => {
        if ((playingWhite && move.getTargetSquare() < 8) || (!playingWhite && move.getTargetSquare() > 55)) {
            let i = 0;
            for (let char of encodedBoard) {
                if (char === '/') {
                    continue;
                }
                if (parseInt(char)) {
                    i += parseInt(char);
                    continue;
                }
                if (playingWhite && i === move.getStartSquare()) {
                    if (char === 'P') {
                        promote();
                        return;
                    }
                }

                if (!playingWhite && i === move.getStartSquare()) {
                    if (char === 'p') {
                        promote();
                        return;
                    }
                }
                i++;
            }
        }
    };

    const drawBoard = (p5: p5Types) => {
        const textOffset = 5;

        for (let x = 0; x < 8; x++) {
            for (let y = 0; y < 8; y++) {
                if ((x + y) % 2 === 0) {
                    p5.fill(p5.color(lightColor));
                    p5.rect(x * squareSize, y * squareSize, squareSize, squareSize);
                }

                p5.fill((x + y) % 2 === 1 ? p5.color(lightColor) : p5.color(darkColor));

                p5.textSize(12);
                p5.textStyle('bold');
                if (x === 0) {
                    p5.textSize(12);
                    const textY = y * squareSize + textOffset * 3;
                    p5.text(playingWhite ? 8 - y : y + 1, textOffset, textY);
                }
                if (y === 7) {
                    p5.textSize(13);
                    const textX = x * squareSize + squareSize - 14;
                    p5.text(String.fromCharCode(playingWhite ? 97 + x : 104 - x), textX, boardSize - 7);
                }
            }
        }
    };

    const drawPieces = (p5: p5Types) => {
        if (!pieceImageMap) {
            return;
        }
        let xIndex = 0;
        let yIndex = 0;
        for (let char of encodedBoard) {
            if (parseInt(char)) {
                xIndex += parseInt(char);
                continue;
            }
            if (char === '/') {
                xIndex = 0;
                yIndex += 1;
                continue;
            }
            let xPos = xIndex * squareSize + imageOffset;
            let yPos = playingWhite ? boardSize - ((yIndex + 1) * squareSize - imageOffset) : yIndex * squareSize + imageOffset;
            p5.image(pieceImageMap[char as keyof PieceImageMapType], xPos, yPos, imageSize, imageSize);
            xIndex++;
        }
    };

    const drawSelectedSquare = (p5: p5Types) => {
        if (!selectedSquare) {
            return;
        }

        let x = playingWhite ? (selectedSquare % 8) * squareSize + 3 : boardSize - ((selectedSquare % 8) + 1) * squareSize + 3;
        let y = playingWhite ? Math.floor(selectedSquare / 8) * squareSize + 3 : boardSize - (Math.floor(selectedSquare / 8) + 1) * squareSize + 3;
        let size = squareSize - 6;

        p5.fill((x + y) % 2 === 0 ? p5.color(0, 0, 0, selectedSquareMarkerOpacity) : p5.color(255, 255, 255, selectedSquareMarkerOpacity));
        p5.rect(x, y, size, size);
    };

    const drawSelectedMoves = (p5: p5Types) => {
        if (!selectedMoves) {
            return;
        }
        const selectedSquareMarkerSize = 25;
        const selectedSquareMarkerOffset = squareSize / 2;

        for (let move of selectedMoves) {
            let targetSquare = move.getTargetSquare();
            let x = playingWhite ? (targetSquare % 8) * squareSize + selectedSquareMarkerOffset : boardSize - ((targetSquare % 8) + 1) * squareSize + selectedSquareMarkerOffset;
            let y = playingWhite ? Math.floor(targetSquare / 8) * squareSize + selectedSquareMarkerOffset : boardSize - (Math.floor(targetSquare / 8) + 1) * squareSize + selectedSquareMarkerOffset;

            p5.fill((x + y) % 2 === 0 ? p5.color(255, 255, 255, selectedSquareMarkerOpacity) : p5.color(0, 0, 0, selectedSquareMarkerOpacity));
            p5.ellipse(x, y, selectedSquareMarkerSize, selectedSquareMarkerSize);
        }
    };

    const promote = () => {
        setPromoting(true);
    };

    const handlePromotion = (p5: p5Types) => {
        const borderSize = 15;
        const innerPopupSize = squareSize * 2 + 4;
        const popupSize = innerPopupSize + borderSize * 2;
        const popupX = boardSize / 2 - popupSize / 2;
        const popupY = 100;

        const mouseX = p5.mouseX;
        const mouseY = p5.mouseY;

        const piecesToPromoteTo = playingWhite ? 'RNBQ' : 'rnbq';

        drawPromotionPopup(p5, mouseX, mouseY, borderSize, popupSize, popupX, popupY, innerPopupSize, piecesToPromoteTo);
        listenForPromotion(mouseX, mouseY, popupX, popupY, borderSize, piecesToPromoteTo);
    };

    const drawPromotionPopup = (p5: p5Types, mouseX: number, mouseY: number, borderSize: number, popupSize: number, popupX: number, popupY: number, innerPopupSize: number, piecesToPromoteTo: string) => {
        if (!pieceImageMap) {
            return;
        }

        p5.strokeWeight(2);
        p5.stroke(darkColor);

        p5.fill(lightColor);
        p5.rect(popupX, popupY, popupSize, popupSize);
        p5.fill(opaqueDarkColor);
        p5.rect(popupX, popupY, popupSize, popupSize);

        p5.stroke(lightColor);

        for (let i = 0; i < 4; i++) {
            let innerX = popupX + borderSize + squareSize * (i % 2) + 2;
            let innerY = popupY + borderSize + squareSize * Math.floor(i / 2) + 2;
            p5.fill(darkColor);
            p5.rect(innerX, innerY, squareSize, squareSize);

            let mouseIsInSquare = mouseX > innerX && mouseX < innerX + squareSize && mouseY > innerY && mouseY < innerY + squareSize;
            if (mouseIsInSquare) {
                setPieceToPromote(piecesToPromoteTo[i]);
                p5.fill(255, 255, 255, selectedSquareMarkerOpacity);
                p5.rect(innerX, innerY, squareSize, squareSize);
            }
            p5.image(pieceImageMap[piecesToPromoteTo[i] as keyof PieceImageMapType], popupX + borderSize + squareSize * (i % 2) + imageOffset + 2, popupY + borderSize + squareSize * Math.floor(i / 2) + imageOffset + 2, imageSize, imageSize);
        }
        p5.stroke(darkColor);
        p5.noFill();
        p5.rect(popupX + borderSize, popupY + borderSize, innerPopupSize, innerPopupSize);
    };

    const listenForPromotion = (mouseX: number, mouseY: number, popupX: number, popupY: number, borderSize: number, piecesToPromoteTo: string) => {
        for (let i = 0; i < 4; i++) {
            let innerX = popupX + borderSize + squareSize * (i % 2) + 2;
            let innerY = popupY + borderSize + squareSize * Math.floor(i / 2) + 2;

            if (mouseX > innerX && mouseX < innerX + squareSize && mouseY > innerY && mouseY < innerY + squareSize) {
                setPieceToPromote(piecesToPromoteTo[i]);
                return;
            }
            setPieceToPromote(undefined);
        }
    };

    return <Sketch setup={setup} draw={draw} mouseClicked={mouseClicked} keyPressed={keyPressed}/>;
};

export default ChessBoard;
