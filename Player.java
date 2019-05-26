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
            g.buildOutput();
            g.output();
        }
    }
}

class Game {
    List<Unit> units;
    List<Building> buildings;
    int gold;
    int income;
    List<Command> output;
    
    public Game() {
        units = new ArrayList<Unit>();
        buildings = new ArrayList<Building>();
        gold = 0;
        income = 0;
        output = new ArrayList<Command>();
    }
    
    public void init(Scanner in) {
        int numberMineSpots = in.nextInt(); // From Wood 1

        for(int i = 0; i < numberMineSpots; i++) {
            int x = in.nextInt();
            int y = in.nextInt();
        }
    }
    
    public void update(Scanner in) {
        // Clears data from the previous turn
        units.clear();
        buildings.clear();
        output.clear();

        // My data
        gold = in.nextInt();
        int income = in.nextInt();

        // My opponent data
        int opponentGold = in.nextInt();
        int opponentIncome = in.nextInt();

        // The map
        for(int i = 0; i < 12; i++) {
            String line = in.next();
            //System.err.println(line);
        }

        // The buildings
        int buildingCount = in.nextInt();

        for(int i = 0; i < buildingCount; i++) {
            int owner = in.nextInt(); // 0 mine, 1 enemy
            int buildingType = in.nextInt(); // 0 HQ
            int x = in.nextInt();
            int y = in.nextInt();
            buildings.add(new Building(x, y, buildingType, owner));
        }

        // The units
        int unitCount = in.nextInt();

        for(int i = 0; i < unitCount; i++) {
            int owner = in.nextInt(); // 0 mine, 1 enemy
            int unitId = in.nextInt();
            int level = in.nextInt();
            int x = in.nextInt();
            int y = in.nextInt();
            units.add(new Unit(x, y, level, owner, unitId));
        }
    }
    
    public void buildOutput() {
        trainUnits();
        moveUnits();
    }
    
    private void trainUnits() {
        Position trainingPosition = findTrainingPosition();

        if (gold > 10) {
            output.add(new Command(CommandType.TRAIN, trainingPosition, 1));
        }
    }
    
    // Move to the center
    private void moveUnits() {
        Position center = new Position(5, 5);

        units.stream().filter(u -> u.isOwned()).forEach(myUnit -> output.add(new Command(CommandType.MOVE, center, myUnit.id))
            );
    }
    
    // Train near the HQ for now
    private Position findTrainingPosition() {
        Building HQ = getHQ();

        if(HQ.p.x == 0) {
            return new Position(0, 1);
        }
        return new Position(11, 10);
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
    int idOrLevel;
    String msg;
    
    public Command(CommandType t, Position p, int idOrLevel) {
        this.t = t;
        this.p = p;
        this.idOrLevel = idOrLevel;
        this.msg = "";
    }

    public Command(CommandType t, Position p, int idOrLevel, String msg) {
        this.t = t;
        this.p = p;
        this.idOrLevel = idOrLevel;
        this.msg = msg;
    } 
    
    public String get() {
        String command = t.toString() + " " + idOrLevel + " " + p.x + " " + p.y + ";";
        if(!this.msg.equalsIgnoreCase("")) {
            command += "MSG" + " " + msg;   
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
        System.err.println("Unit of level " + level + " at " + p.x + " " + p.y + " owned by " + owner);
    }
    
    public boolean isOwned() {
        return owner == 0;
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
        System.err.println(t + " at " + p.x + " " + p.y + " owned by " + owner);
    }
    
    public boolean isHQ() {
        return t.equals(BuildingType.HQ);
    }
    
    public boolean isOwned() {
        return owner == 0;
    }
}

enum BuildingType {
    HQ;
    
    public static BuildingType get(int type) {
        BuildingType buildingType = null;
        switch(type) {
            case 0:
                buildingType = HQ;
                break;
            default:
                buildingType = null;
        }
        return buildingType;
    }
}

enum CommandType {
    MOVE,
    TRAIN;
}