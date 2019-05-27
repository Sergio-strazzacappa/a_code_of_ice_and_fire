import java.util.*;
import java.io.*;
import java.math.*;

class Player {
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        
        Game g = new Game();
        g.init(in);
        
        while(true) {
            g.update(in);
            //g.debug();
            g.gameLogic();
            g.output();
        }
    }
}

class Game {
    List<Unit> units;
    List<Building> buildings;
    int gold;
    int currentGold;
    int income;
    List<Command> output;
    List<Cell> map;
    List<Position> mines;
    int numberOfUnitsOwned;
    
    public Game() {
        this.units = new ArrayList<Unit>();
        this.buildings = new ArrayList<Building>();
        this.gold = 0;
        this.currentGold = 0;
        this.income = 0;
        this.output = new ArrayList<Command>();
        this.map = new ArrayList<Cell>();
        this.mines = new ArrayList<Position>();
        this.numberOfUnitsOwned = 0;
    }
    
    public void init(Scanner in) {
        int numberMineSpots = in.nextInt();

        for(int i = 0; i < numberMineSpots; i++) {
            int x = in.nextInt();
            int y = in.nextInt();

            this.mines.add(new Position(x, y));
        }
    }
    
    public void update(Scanner in) {
        // Clears data from the previous turn
        this.units.clear();
        this.buildings.clear();
        this.output.clear();
        this.map.clear();
        this.gold = 0;
        this.income = 0;
        this.numberOfUnitsOwned = 0;

        // My data
        gold = in.nextInt();
        income = in.nextInt();

        this.currentGold = gold;

        // My opponent data
        int opponentGold = in.nextInt();
        int opponentIncome = in.nextInt();

        // The map
        for(int i = 0; i < 12; i++) {
            String line = in.next();
            //System.err.println(line);

            for(int j = 0; j < 12; j++) {
                Cell c = new Cell(j, i, line.charAt(j));
                this.map.add(c);
                //c.debug();
            }
        }

        // The buildings
        int buildingCount = in.nextInt();

        for(int i = 0; i < buildingCount; i++) {
            int owner = in.nextInt(); // 0 mine, 1 enemy
            int buildingType = in.nextInt(); // 0 HQ
            int x = in.nextInt();
            int y = in.nextInt();
            this.buildings.add(new Building(x, y, buildingType, owner));
        }

        // The units
        int unitCount = in.nextInt();

        for(int i = 0; i < unitCount; i++) {
            int owner = in.nextInt(); // 0 mine, 1 enemy

            if (owner == 0) {
                this.numberOfUnitsOwned++;
            }

            int unitId = in.nextInt();
            int level = in.nextInt();
            int x = in.nextInt();
            int y = in.nextInt();
            this.units.add(new Unit(x, y, level, owner, unitId));
        }
    }
    
    public void gameLogic() {
        buildMines();
        trainUnits();
        moveUnits();
    }

    private void buildMines() {
        List<Position> miningPositions = myTerritory();
        if(currentGold >= 20) {
            Position mine = isAMine(miningPositions);
            if(mine != null) {
                currentGold -= 20;
                output.add(new Command(CommandType.BUILD, mine, 1, true));
            }
        }
    }

    private Position isAMine(List<Position> miningPositions) {
        Position mine = null;
        int i = 0;
        int j = 0;

        while(i < miningPositions.size() && mine == null) {
            j = 0;
            while(j < mines.size() && mine == null) {
                if(miningPositions.get(i).x == mines.get(j).x && miningPositions.get(i).y == mines.get(j).y) {
                    mine = new Position(mines.get(j).x, mines.get(j).y);
                }
                j++;
            }
            i++;
        }
        return mine;
    }
    
    private void trainUnits() {
        List<Position> trainingPositions = frontier();
        //trainingPositions.stream().forEach(p -> System.err.println("x: " + p.x + " " + "y: " + p.y));
        if(this.numberOfUnitsOwned < 5) {
            for(int i = 0; i < trainingPositions.size(); i++) {
                if(currentGold >= 10 && this.income >= 0) {
                    currentGold -= 10;
                    output.add(new Command(CommandType.TRAIN, trainingPositions.get(i), 1, false));
                }
            }
        }
    }
    
    // Tries to expand the frontier
    private void moveUnits() {
        List<Position> movingPositions = frontier();

        for(int i = 0; i < this.units.size(); i++) {
            double minDistance = 999;
            Position target = null;

            if(this.units.get(i).isOwned()) {
                for(int j = 0; j < movingPositions.size(); j++) {
                    double distance = units.get(i).p.distance(movingPositions.get(j));
                    if(distance <= minDistance) {
                        minDistance = distance;
                        target = new Position(movingPositions.get(j).x, movingPositions.get(j).y);
                    }
                }
                output.add(new Command(CommandType.MOVE, target, units.get(i).id, false));
                movingPositions.remove(target);
            }
        }
    }
    
    // Return a list of the cells in my territory
    private List<Position> myTerritory() {
        List positions = new ArrayList<Position>();

        for(int i = 0; i < this.map.size(); i++) {
            if(map.get(i).c == CellType.OWNED_ACTIVE) {
                positions.add(map.get(i).p);
            }
        }
        //System.err.println(positions.toString());
        return positions;
    }

    // Return a list of the adyacent cells that are not mine and active and are not void
    private List<Position> frontier() {
        List positions = new ArrayList<Position>();

        for(int i = 0; i < this.map.size(); i++) {
            Cell c1 = map.get(i);
            if(c1.isOwnedAndActive()) {
                for(int j = 0; j < map.size(); j++) {
                    Cell c2 = map.get(j);
                    if(c1.isAdyacent(c2) && c2.c != CellType.OWNED_ACTIVE && c2.c != CellType.VOID && !positions.contains(c2.p)) {
                        positions.add(c2.p);
                    }
                }
            }
        }
        //System.err.println(positions.toString());
        return positions;
    }
    
    public void output() {
        StringBuilder s = new StringBuilder("WAIT;");
        output.stream().forEach(c -> s.append(c.get()));
        System.out.println(s);
    }

    public void debug() {
        units.stream().forEach(u -> u.debug());
        buildings.stream().forEach(b -> b.debug());
    }
    
    private Building getHQ() {
        return buildings.stream().filter(b -> b.isHQ() && b.isOwned()).findFirst().get();
    }
    
    private Building getOpponentHQ() {
        return buildings.stream().filter(b -> b.isHQ() && !b.isOwned()).findFirst().get();
    }
}

class Command {
    CommandType t;
    Position p;
    int idOrLevelOrBuildingType;
    boolean isBuilding;
    String msg;
    
    public Command(CommandType t, Position p, int idOrLevelOrBuildingType, boolean isBuilding) {
        this.t = t;
        this.p = p;
        this.idOrLevelOrBuildingType = idOrLevelOrBuildingType;
        this.isBuilding = isBuilding;
        this.msg = "";
    }

    public Command(CommandType t, Position p, int idOrLevelOrBuildingType, String msg, boolean isBuilding) {
        this.t = t;
        this.p = p;
        this.idOrLevelOrBuildingType = idOrLevelOrBuildingType;
        this.isBuilding = isBuilding;
        this.msg = msg;
    }

    public String get() {
        String command = "";

        if(!this.isBuilding) {
            command = this.t.toString() + " " + this.idOrLevelOrBuildingType + " " + this.p.x + " " + this.p.y + ";";
        }
        else {
            command = this.t.toString() + " " + BuildingType.get(this.idOrLevelOrBuildingType) + " " + this.p.x + " " + this.p.y + ";";
        }
        if(!this.msg.equalsIgnoreCase("")) {
            command += "MSG" + " " + this.msg;   
        }
        return command;
    }
}

class Position {
    int x;
    int y;
    
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double distance(Position p) {
        //double euclideanDistance = Math.sqrt(Math.pow(this.x + p.x, 2) + Math.pow(this.y + p.y, 2));
        double manhattanDistance = Math.abs(this.x - p.x) + Math.abs(this.y - p.y);
        //System.err.println("The distance between (" + this.x + ", " + this.y + ") and (" + p.x + ", " + p.y + ") is " + manhattanDistance);
        return manhattanDistance;
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }
}

class Unit {
    Position p;
    int level;
    int owner;
    int id;
    
    public Unit(int x, int y, int level, int owner, int id) {
        this.p = new Position(x, y);
        this.id = id;
        this.level = level;
        this.owner = owner;
    }
    
    public void debug() {
        System.err.println("Unit of level " + this.level + " at " + this.p.x + " " + this.p.y + " owned by " + this.owner);
    }
    
    public boolean isOwned() {
        return this.owner == 0;
    }
}

class Building {
    Position p;
    BuildingType t;
    int owner;
    
    public Building(int x, int y, int t, int owner) {
        this.p = new Position(x, y);
        this.t = BuildingType.get(t);
        this.owner = owner;
    }
    
    public void debug() {
        System.err.println(this.t + " at " + this.p.x + " " + this.p.y + " owned by " + this.owner);
    }
    
    public boolean isHQ() {
        return this.t.equals(BuildingType.HQ);
    }
    
    public boolean isOwned() {
        return this.owner == 0;
    }
}

class Cell {
    Position p;
    CellType c;

    public Cell(int x, int y, char c) {
        this.p = new Position(x, y);
        this.c = CellType.get(c);
    }

    public void debug() {
        System.err.println(this.c + " at " + this.p.x + " " + this.p.y);
    }

    public boolean isOwnedAndActive() {
        return this.c == CellType.OWNED_ACTIVE;
    }

    public boolean isAdyacent(Cell c) {
        return this.p.x == c.p.x && this.p.y == c.p.y + 1 ||
            this.p.x == c.p.x && this.p.y == c.p.y - 1 ||
            this.p.x == c.p.x - 1 && this.p.y == c.p.y - 1 ||
            this.p.x == c.p.x - 1 && this.p.y == c.p.y ||
            this.p.x == c.p.x - 1 && this.p.y == c.p.y + 1 ||
            this.p.x == c.p.x + 1 && this.p.y == c.p.y - 1 ||
            this.p.x == c.p.x + 1 && this.p.y == c.p.y ||
            this.p.x == c.p.x + 1 && this.p.y == c.p.y + 1;
    }
}

enum BuildingType {
    HQ,
    MINE,
    TOWER;
    
    public static BuildingType get(int type) {
        BuildingType buildingType = null;

        switch(type) {
            case 0:
                buildingType = HQ;
                break;
            case 1:
                buildingType = MINE;
                break;
            case 2:
                buildingType = TOWER;
                break;
            default:
                buildingType = null;
        }
        return buildingType;
    }
}

enum CommandType {
    MOVE,
    TRAIN,
    BUILD;
}

enum CellType {
    VOID,
    NEUTRAL,
    OWNED_ACTIVE,
    OWNED_INACTIVE,
    OPPONENT_ACTIVE,
    OPPONENT_INACTIVE;

    public static CellType get(char type) {
        CellType cellType = null;

        switch(type) {
            case '#':
                cellType = VOID;
                break;
            case '.':
                cellType = NEUTRAL;
                break;
            case 'O':
                cellType = OWNED_ACTIVE;
                break;
            case 'o':
                cellType = OWNED_INACTIVE;
                break;
            case 'X':
                cellType = OPPONENT_ACTIVE;
                break;
            case 'x':
                cellType = OPPONENT_INACTIVE;
                break;
            default:
                cellType = null;
        }
        return cellType;
    }
}