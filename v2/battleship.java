import java.util.*;
import java.util.Scanner;

class Position {
    public int x;
    public int y;
}

// Aufzählbarer Datentyp, der genau einen der beiden Zustände annehmen kann.
// South steht für "nach unten" und east für "nach rechts".
enum Orientation {  
    South,         
    East
}

enum HitStatus {
    HitUnknown,
    HitSuccess,
    HitMiss
}

enum GameStatus {
    PlayerHasWon,
    EnemyHasWon,
    GameInProgress
}

class Settings {
    public static int Size = 10;
    public static int[] shipSettings = {1, 1, 2, 2, 3, 4, 5}; 

    public static boolean[][] spawnRandomShips () {
        boolean[][] map = new boolean[Size][Size];
        // todo
        return map;
    }
}

class Board {
    private boolean[][] fields;

    public Board () {
        fields = new boolean[10][10];

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                fields[i][j] = false;
            }
        }
    }

    // Testet ob n schiff da ist.
    // Gibt true zurück wenn ein schiff da ist,
    // sonst false.
    public boolean hasShipsPresent () {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (fields[i][j] == true) {
                    return true;
                }
            }
        }
        return false;
    }
}

class Ship {

}

class GameEngine {
    Board enemyBoard;
    Board playerBoard;

    public GameEngine () {
        enemyBoard = new Board();
        playerBoard = new Board();
    }
    public void GameEngineStart() {
        System.out.println("Hallo");
    }
}

class TestFrontend {
    public static void main(String[] args) {
        GameEngine x = new GameEngine();
        x.test();
        
    }
}
