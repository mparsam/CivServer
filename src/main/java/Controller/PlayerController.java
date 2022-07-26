package Controller;

import Model.*;
import Model.Units.Troop;
import Model.Units.Unit;
import enums.Responses.InGameResponses;
import enums.Responses.Response;
import enums.Types.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class PlayerController {

    public static void initializePlayers(ArrayList<Player> players) {
        Game game = GameController.getGame();
        //setting camera to an initial tile
        for (Player player : players) {
            int randomRow;
            int randomColumn;
            Tile initialTile;
            // checking if two cities are too close
            do {
                do {
                    randomRow = ThreadLocalRandom.current().nextInt(0, game.getMap().getWidth());
                    randomColumn = ThreadLocalRandom.current().nextInt(0, game.getMap().getHeight());
                    initialTile = game.getMap().getTile(randomRow, randomColumn);
                } while (Arrays.asList(TerrainType.OCEAN, TerrainType.MOUNTAIN).contains(initialTile.getTerrainType()));
            } while (!game.getMap().lookAroundInRange(initialTile, 3).stream().allMatch(t -> t.getTroop() == null));
            player.setCamera(initialTile); // setting camera to capital
            Unit unit = new Unit(initialTile, player, UnitType.SETTLER);
            Troop troop = new Troop(initialTile, player, UnitType.WARRIOR);
            player.addUnit(unit); // adding initial units
            player.addUnit(troop);
            player.setMap(new Map(game.getMap())); // deep copying map
        }
        for (Player player : players) {
            updateFieldOfView(player);
        }
    }

    // TODO: 4/17/2022 War declaration needs confirmation
    public static Response.GameMenu startWarWith(Player player) {
        throw new RuntimeException("NOT IMPLEMENTED FUNCTION");
    }

    public static Response.GameMenu endWarWith(Player player) {
        throw new RuntimeException("NOT IMPLEMENTED FUNCTION");
    }

    public static Response.GameMenu offerTrade(Player player, Trade trade) {
        throw new RuntimeException("NOT IMPLEMENTED FUNCTION");
    }

    public static InGameResponses.Technology researchTech(TechnologyType technologyType) {
        Player player = GameController.getCurrentPlayer();
        if (technologyType == null) {
            return InGameResponses.Technology.TECH_INVALID;
        }
        if (player.getTechnologyByType(technologyType) != null) {
            return InGameResponses.Technology.TECH_RESEARCHED;
        }
        for (TechnologyType neededTech : technologyType.neededTechs) {
            if (player.getTechnologyByType(neededTech) == null) {
                return InGameResponses.Technology.TECH_NOT_YET_READY; // can be dynamic
            }
        }
        if (player.getTechnologyInProgress() != null) {
            player.addIncompleteTechnology(player.getTechnologyInProgress());
        }
        player.setTechnologyInProgress(null);
        Technology technology = new Technology(technologyType);
        if (player.getIncompleteTechnologyByType(technologyType) != null) {
            technology = player.getIncompleteTechnologyByType(technologyType);
            player.removeIncompleteTechnology(technology);
        }
        player.setTechnologyInProgress(technology);
        return InGameResponses.Technology.TECH_RESEARCHED;
    }

    public static void startTurn() {
        // does the necessary stuff at the start of the turn
        // decrease remaining turns in inProgress units, techs, buildings, improvements
        Player player = GameController.getGame().getCurrentPlayer();
        for (Unit unit : player.getUnits()) {
            UnitController.updateUnit(unit);
        }
        for (City city : player.getCities()) {
            CityController.updateCity(city);
        }
        updateTechnology();
        updateScience();
        updateGold(); // gold must be updated after science
        updateHappiness();
        updateFieldOfView();

    }

    private static void updateHappiness() {
        int happiness = 100;
        happiness -= GameController.getCurrentPlayer().getCities().size() * 3;

        ArrayList<ResourceType> playerResources = getPlayerWorkingResourceTypes(GameController.getCurrentPlayer());
        for (ResourceType luxuryResourceType : ResourceType.getLuxuryResourceTypes()) {
            if (playerResources.contains(luxuryResourceType)) happiness += 4;
        }


        ArrayList<BuildingType> buildingTypes = getPlayerBuildingTypes(GameController.getCurrentPlayer());
        if (buildingTypes.contains(BuildingType.BURIAL_TOMB)) happiness += 2;
        if (buildingTypes.contains(BuildingType.CIRCUS)) happiness += 3; // TODO: 5/9/2022
        if (buildingTypes.contains(BuildingType.COLOSSEUM)) happiness += 4;
        if (buildingTypes.contains(BuildingType.SATRAP_COURT)) happiness += 2;

        happiness -= GameController.getCurrentPlayer().getPopulation() / 10;

        GameController.getCurrentPlayer().setHappiness(happiness);
    }

    public static void endTurn() {
        // does the necessary stuff at the end of the turn
        GameController.setSelectedCity(null);
        GameController.setSelectedUnit(null);
    }

    public static Response.GameMenu nextTurn() {
        endTurn();
        GameController.getGame().nextTurn();
        startTurn();
        return Response.GameMenu.TURN_PASSED;
    }

    // basically makes the visible tiles visited
    private static void clearView(Map map) {
        for (int row = 0; row < map.getHeight(); row++) {
            for (int column = 0; column < map.getWidth(); column++) {
                Tile tile = map.getTile(row, column);
                if (tile.getFogState() != FogState.UNKNOWN) {
                    tile.setTroop(null);
                    tile.setUnit(null);
                    tile.setImprovement(null);
                    tile.setFogState(FogState.VISITED);
                } else {
                    tile.setTroop(null);
                    tile.setUnit(null);
                    tile.setImprovement(null);
                    tile.setTerrain(new Terrain(null, null, null));
                    tile.setCity(null);
                    tile.setRoadType(null);
                }
            }
        }
    }

    public static void updateFieldOfView(Player player) {
        Map map = player.getMap();
        Map gameMap = GameController.getMap();

        clearView(map);
        ArrayList<Tile> inSight = new ArrayList<>();
        for (Unit unit : player.getUnits()) {
            inSight.addAll(gameMap.lookAroundInRange(unit.getTile(), unit.getSightRange()));
        }
        for (City city : player.getCities()) {
            for (Tile tile : city.getTerritory()) {
                inSight.addAll(gameMap.lookAroundInRange(tile, city.getSightRange()));
            }
        }
        for (Tile tile : inSight) {
            if (tile == null) continue;
            map.setTile(tile.getRow(), tile.getColumn(), new Tile(tile));
            map.getTile(tile.getRow(), tile.getColumn()).setFogState(FogState.VISIBLE);
        }
        System.out.println("--------->PlayerController.updateFieldOfView");
    }

    public static void updateFieldOfView() {
        Player player = GameController.getCurrentPlayer();
        updateFieldOfView(player);
    }

    public static void updateSupplies() {

        // TODO: 4/17/2022
    }


    private static void updateGold() {
        // handles turn based coin changes - NOT HANDLING TRADES, BUYS, ...
        double goldIncome = 0;
        for (City city : GameController.getGame().getCurrentPlayer().getCities()) {
            // todo: the building effects are applied to gross city gold production not net!, MINT EFFECT APPLIED!
            ArrayList<BuildingType> buildingTypes = city.getBuildings().stream().map(Building::getBuildingType).collect(Collectors.toCollection(ArrayList::new));
            double cityGrossGold = city.getTerritory().stream().
                    mapToInt(t -> (t.getGold() > 0 && buildingTypes.contains(BuildingType.MINT)) ? t.getGold() + 3 : t.getGold()).sum();
            if (buildingTypes.contains(BuildingType.MARKET)) cityGrossGold *= 1.25;
            if (buildingTypes.contains(BuildingType.BANK)) cityGrossGold *= 1.25;
            if (buildingTypes.contains(BuildingType.SATRAP_COURT)) cityGrossGold *= 1.5;
            if (buildingTypes.contains(BuildingType.STOCK_EXCHANGE)) cityGrossGold *= 1.33;
            for (Building building : city.getBuildings()) {
                cityGrossGold -= building.getCost();
            }
            goldIncome += cityGrossGold;
        }
        int unitMaintenanceCost = 1;
        goldIncome -= unitMaintenanceCost * GameController.getCurrentPlayer().getUnits().size();
        GameController.getCurrentPlayer().setGoldIncome((int) goldIncome);
        GameController.getCurrentPlayer().updateGoldByIncome();
        // +: terrains, terrain Features, resources, buildings cost , handling route  and unit cost
    }

    private static void updateTechnology() {
        Player player = GameController.getGame().getCurrentPlayer();
        Technology inProgressTechnology = player.getTechnologyInProgress();
        if (inProgressTechnology != null) {
            inProgressTechnology.setRemainingCost(inProgressTechnology.getRemainingCost() - player.getScienceIncome());
            if (inProgressTechnology.getRequiredCost() <= 0) {
                player.addTechnology(inProgressTechnology);
                player.setTechnologyInProgress(null);
                int turn = GameController.getTurn();
                player.addNotification(turn + ": " + inProgressTechnology.getName() + " technology is fully researched");
            }
        }
    }

    private static void updateScience() {
        Player player = GameController.getCurrentPlayer();
        int scienceIncome = 100000;
        if (player.getCities().size() > 0) scienceIncome += 3;
        for (City city : player.getCities()) {
            scienceIncome += city.getScienceIncome();
        }
        player.setScienceIncome(scienceIncome);
    }

    public static void declareWar(Player player1, Player player2) {
        if(player1.getInWarPlayers().contains(player2) && player2.getInWarPlayers().contains(player1)) return;
        player1.getInWarPlayers().add(player2);
        player2.getInWarPlayers().add(player1);
        player1.addNotification(player2.getName() + "has declared war on you!");
        player2.addNotification(player1.getName() + "has declared war on you!");
        // TODO: 7/21/2022 notify players maybe?
    }

    public static boolean checkIfLost() {
        return GameController.isDead(GameController.getCurrentPlayer());
    }

    public static boolean checkIfWon() {
        return GameController.isEnded() && !checkIfLost();
    }

    public static ArrayList<ResourceType> getPlayerWorkingResourceTypes(Player player) {
        ArrayList<ResourceType> res = new ArrayList<>();
        for (City city : player.getCities()) {
            res.addAll(city.getWorkingResources());
        }
        return res;
    }

    public static ArrayList<BuildingType> getPlayerBuildingTypes(Player player) {
        ArrayList<BuildingType> buildingTypes = new ArrayList<>();
        for (City city : player.getCities()) {
            buildingTypes.addAll(city.getBuildings().stream().map(Building::getBuildingType).toList());
        }
        return buildingTypes;
    }

}
