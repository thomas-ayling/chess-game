package org.example.util;

import java.util.ArrayList;
import java.util.List;

public class PrecomputedMoveData {
    public static final List<String> squareMap = precomputeSquareMapValues();

    // north, south, east, west, south-east, north-west, south-west, north-east
    public static final int[] directionOffsets = {8, -8, -1, 1, 7, -7, 9, -9};

    public static final int[] knightOffsets = {15, 17, 10, 6};

    public static final int[][] numSquaresToEdge = precomputeMoveData();

    public static List<String> precomputeSquareMapValues() {
        List<String> squareMap = new ArrayList<>();
        // Compute squareMap values
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                squareMap.add(String.valueOf((char) (97 + j)) + (i + 1));
            }
        }
        return squareMap;
    }

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
