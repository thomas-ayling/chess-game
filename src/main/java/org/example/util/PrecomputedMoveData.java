package org.example.util;

public class PrecomputedMoveData {
    // north, south, east, west, south-east, north-west, south-west, north-east
    public static final int[] directionOffsets = {8, -8, -1, 1, 7, -7, 9, -9};

    public static final int[] knightOffsets = {15, 17, 10, 6};

    public static final int[][] numSquaresToEdge = precomputeMoveData();

    private static int[][] precomputeMoveData() {
        int[][] numSquaresToEdge = new int[64][8];
        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                int numNorth = 7 - rank;
                int numSouth = rank;
                int numWest = file;
                int numEast = 7 - file;

                int squareIndex = rank * 8 + file;

                numSquaresToEdge[squareIndex] = new int[]{
                        numNorth,
                        numSouth,
                        numWest,
                        numEast,
                        Math.min(numNorth, numWest),
                        Math.min(numSouth, numEast),
                        Math.min(numNorth, numEast),
                        Math.min(numSouth, numWest),
                };
            }
        }
        return numSquaresToEdge;
    }

}
