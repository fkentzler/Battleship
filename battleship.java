import java.util.*;
import java.util.Scanner;

// Position ist eine Datenstruktur um die Zeilen/Spalten (Koordinaten) des Spiels zu spezifizieren.
// Es werden die Integers row und column initialisiert.
class Position {
    public int row;
    public int column;

    Position(int row, int column) {
        this.row = row;
        this.column = column;
    }
}

// "enum" ist ein aufzählbarer Datentyp, der genau einen der beiden Zustände annehmen kann.
// South steht für "nach unten" und east für "nach rechts".
enum Orientation {  
    South,         
    East
}

// Es werden die Zustände der Felder definiert. Entweder weiß man nicht ob dieses Feld getroffen wurde,
// oder ein Schiff auf diesem Feld wurde getroffen oder nicht.
enum HitStatus {
    HitUnknown,
    HitSuccess,
    HitMiss
}

// Der Datentyp GameStatus definiert PlayerHasWon für der menschlihe Spieler hat gewonnen, EnemyHasWon für
// der Computer hat gewonnen und GameInProgress für das Spiel läuft noch.
enum GameStatus {
    PlayerHasWon,
    EnemyHasWon,
    GameInProgress
}

// Hier wird definiert, dass eine Schissposition immer aus den Koordinaten der Position, 
// sowie einer Ausrichtung bestehen muss.
class ShipLocation {
    Position position;
    Orientation orientation;

    ShipLocation(Position pos, Orientation orientation) {
        this.position = pos;
        this.orientation = orientation;
    }
}

/*
 * Settings enthält die statische Konfiguration des Spieles und eine Hilfsfunktion zum
 * zufälligen erstellen von Schiffen auf einem Spielfeld.
 */
class Settings {
    // Size gibt die Anzahl der Zeilen und Spalten des Spielfeldes an.
    public static int Size = 10;

    // shipSettings gibt die Länge der Schiffe an. Die Anzahl der Anzahl der Elemente
    // ergibt die Anzahl der Schiffe eines Spielfeldes.
    public static int[] shipSettings = {1, 1, 2, 2, 3, 4, 5}; 
    
    // Erzeugt eine zufällige Zahl zwischen min und max.
    public static int randomWithRange(int min, int max) {
        int range = (max - min) + 1;     
        return (int)(Math.random() * range) + min;
    }

    // Testet ob auf dem Spielfeld (map) auf den Positionen zwischen (row, column1) bis
    // (row, column2) in horizantaler Richtung alles frei ist. 
    private static boolean isAvailableEast(int row, int column1, int column2, boolean[][] map) {
        for (int column = column1; column <= column2; column++) 
            if (map[row][column] == true)
                return false;
        
        return true;
    }

    // Testet ob auf dem Spielfeld (map) auf den Positionen zwischen (row1, column) bis
    // (row2, column) in vertikaler Richtung alles frei ist.
    private static boolean isAvailableSouth(int column, int row1, int row2, boolean[][] map) {
        for (int row = row1; row <= row2; row++) 
            if (map[row][column] == true)
                return false;
        
        return true;
    }

    // Wählt eine zufällige Position und Orientation auf dem Spielfeld, 
    // welche noch nicht belegt ist und spawnt dort ein Schiff. 
    //
    // Die Funktion garantiert dass ein Schiff immer gesetzt wird,
    // allerdings kann es zu einer Endlosschleife (Endlosrekursion) kommen,
    // wenn die o.g. Konfiguration mehr Schiffe zulässt, als Platz auf dem Spielfeld ist.
    private static ShipLocation spawn (int length, boolean[][] map) {
        Position pos = new Position(randomWithRange(0, Size - 1), randomWithRange(0, Size - 1));   
        Orientation orientation = (randomWithRange(1, 2) % 2) != 0 ? Orientation.South : Orientation.East;

        if (orientation == Orientation.South &&
                pos.row + length - 1 < Size && 
                isAvailableSouth (pos.column, pos.row, pos.row + length - 1, map)) 
            return new ShipLocation(pos, orientation);
    
        if (orientation == Orientation.East &&
                pos.column + length - 1 < Size &&
                isAvailableEast(pos.row, pos.column, pos.column + length - 1, map))
            return new ShipLocation(pos, orientation);
    
        return spawn(length, map);
    }

    // Spawnt ein Schiff an einer freien Position in der Map und belegt den dazugehörigen
    // Platz in dieser Map.
    private static void spawnRandom (int length, boolean[][] map) {
        ShipLocation location = spawn(length, map);
        if (location.orientation == Orientation.South)
            for (int row = location.position.row; row < length + location.position.row; row++)
                map[row][location.position.column] = true;
    }

    // Erstellt ein Spielfeld und initialisiert Schiffe an zufälliger Position
    // nach o.g. Konfiguration.
    public static boolean[][] spawnRandomShips () {
        boolean[][] map = new boolean[Size][Size];
       
        for (int shipLength : shipSettings)
            spawnRandom(shipLength, map);

        return map;
    }
}

class Controller {
    interface TurnFunction {
        Position turn(Controller playerController);
    }
    
    private boolean[][] myMap;
    private HitStatus[][] enemyMap;
    private String playerName;
    private TurnFunction turnFn;

    public String getPlayerName() {
        return this.playerName;
    }

    Controller (String playerName, TurnFunction turn, boolean[][] myMap) {
        this.myMap = myMap;
        this.playerName = playerName;
        this.turnFn = turn;

        this.enemyMap = new HitStatus[Settings.Size][Settings.Size];
        for (int row = 0; row < Settings.Size; row++)
            for (int column = 0; column < Settings.Size; column++)
                this.enemyMap[row][column] = HitStatus.HitUnknown;
    }

    // Macht den Spielzug, in dem es die zur Konstrunktion des Cotrollers
    // turnFn Funktion aufruft und dessen erhaltene Position zurück gibt.
    Position turn() {
        return turnFn.turn(this);
    }

    // Überprüft ob an gegebener Position ein Schiff ist und wenn ja, dann wird das
    // Schiff an dieser Position zerstört und HitSuccess zurückgegeben, andernfalls
    // HitMiss, wenn an dieser Position kein Schiff ist und die Position auch auf
    // Map ist, andernfalls HitUnknown.
    HitStatus takeHit(Position position) {
        if (position.row < 0 || position.row >= Settings.Size
                || position.column < 0 || position.column >= Settings.Size)
            return HitStatus.HitUnknown;

        if (this.myMap[position.row][position.column] == false) 
            return HitStatus.HitMiss;
        
        this.myMap[position.row][position.column] = false;
        return HitStatus.HitSuccess;
    }

    // Diese Funktion wird aufgerufen nachdem ein Spielzug durchgeführt wurde und dessen
    // Ergebnis zum Spieler zurückgegeben wird. 
    // 
    // Paramenter position: Die Position an der ursprünglich der Spielzug gemacht worden ist.
    // Parameter hitStatus: Reflektiert das Ergebnis ob an der obigen Position ein Schiff
    //                      getroffen wurde oder nicht.
    void feedback(Position position, HitStatus hitStatus) {
        this.enemyMap[position.row][position.column] = hitStatus;
    }

    // Geht durch alle Zeilen und Spalten durch und zählt die Anzahl der Schiffe pro Koordinate 
    // auf dem Weg.
    int presenceCount () {
        int count = 0;

        for (int row = 0; row < Settings.Size; row++) 
            for (int column = 0; column < Settings.Size; column++)
                if (this.myMap[row][column] == true)
                    count++;
        
        return count;
    }

    // Gibt den HitStatus der enemyMap zurück.
    public HitStatus getHitStatus(int row, int column) {
        return this.enemyMap[row][column];
    }
}

// Die statische Klasse gruppiert alle AI bezogenen Funktionen zusammen.
class AI {
    // Implementiert die turn Funktion für eine AI auf Basis von Zufallszahlen.
    public static Position turnRandom(Controller controller) {
        int row = Settings.randomWithRange(0, Settings.Size - 1);
        int column = Settings.randomWithRange(0, Settings.Size - 1);

        if (controller.getHitStatus(row, column) == HitStatus.HitUnknown)
            return new Position(row, column);

        return turnRandom(controller);
    } 
}

class GameEngine {
    Controller player;
    Controller enemy;

    public GameEngine (Controller player, Controller enemy) {
        this.player = player;
        this.enemy = enemy;
    }

    public boolean isKilled(Controller player) {
        return player.presenceCount() == 0;
    }

    private void turn(Controller player, Controller enemy) {
        Position position = player.turn();
        HitStatus hitStatus = enemy.takeHit(position);
        player.feedback(position, hitStatus);
    }

    public GameStatus run() {
        this.turn(this.player, this.enemy);
        if (this.isKilled (this.enemy))
            return GameStatus.PlayerHasWon;
        else {
            this.turn(this.enemy, this.player);
            if (this.isKilled (this.player))
                return GameStatus.EnemyHasWon;
            else 
                return GameStatus.GameInProgress;
        }
    }
}

class TerminalBattleship {
    public showState (Controller player, Controller enemy, String title, boolean extendet) {
        // TODO
    }

    public showPlayerState (Controller player) {
        // TODO
    }

    public Position turn(Controller enemy, Controller player) {
        // TODO
    }

    private static Controller play(GameEngine engine, int round) {
        GameStatus status = engine.run();
        switch (status) {
            case PlayerHasWon:
                return engine.player;
            case EnemyHasWon:
                return engine.enemy;
            case GameInProgress:
            default:
                return play(engine, round + 1);
        }
    }

    public static void main() {
        Controller enemy = new Controller("Alice", AI::turnRandom, Settings.spawnRandomShips());  
        Controller player = new Controller("Bob", AI::turnRandom, Settings.spawnRandomShips());
        
        GameEngine engine = new GameEngine(player, enemy);
        Controller winner = play(engine, 1);
        System.out.printf("%s hat gewonnen.", winner.getPlayerName());
    }
}

class Main {
    public static void main (String[] args) {
        TerminalBattleship.main();
    }
}
