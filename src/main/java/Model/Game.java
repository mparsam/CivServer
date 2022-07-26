package Model;

import Controller.GameController;
import Controller.PlayerController;
import Model.Units.Troop;
import Model.Units.Unit;

import java.io.Serializable;
import java.util.ArrayList;

public class Game implements Serializable {
    public static final long serialVersionUID = 69L;
    private Map map;
    private ArrayList<Player> players;
    private int currentPlayerID;
    int turnCount;

    public Game(Map map, ArrayList<Player> players) {
        this.map = map;
        this.players = players;
        turnCount = 1;
        currentPlayerID = 0;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public int getTurnCount() {
        return turnCount;
    }

    public void setTurnCount(int turnCount) {
        this.turnCount = turnCount;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerID);
    }

    public void nextTurn() {
        currentPlayerID = currentPlayerID + 1;
        if(currentPlayerID == players.size()) {
            currentPlayerID = 0;
            turnCount++;
        }
    }

    public void refillData(){
        for (Player player : players) {
            System.err.println(player.getUsername() + ":");
            for (Unit unit : player.getUnits()) {
                unit.setTile(map.getTile(unit.getRow(), unit.getColumn()));
                unit.setOwner(player);
                if(Troop.troopify(unit) != null) unit = Troop.troopify(unit);
                unit.getTile().putUnit(unit);
                System.err.println(unit.getTile().getRow() + " " + unit.getColumn());
            }
            for (City city : player.getCities()) {
                System.err.println(city.getName());
                city.setOwner(player);
                city.setCapitalTile(map.getTile(city.getCapitalTile().getRow(), city.getCapitalTile().getColumn()));
                city.setGarrisonedTroop(city.getCapitalTile().getTroop());
                for (Tile tile : city.getTerritory()) {
                    tile = map.getTile(tile.getRow(), tile.getColumn());
                    tile.setCity(city);
                }
            }
        }
        for (Player player : players) {
            PlayerController.updateFieldOfView(player);
        }
    }

}
