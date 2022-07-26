package Controller;

import Model.*;
import Model.Units.Troop;
import Model.Units.Unit;
import enums.Types.BuildingType;
import enums.Types.CombatType;
import enums.Responses.InGameResponses;
import enums.Types.UnitType;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class CityController {

    // updates all there is about a city accordingly
    public static void updateCity(City city) {
        // gold updates is PlayerController.updateGold
        city.setHP(Math.min(city.getHP() + 1, 20));
        updateFood(city);
        updateInProgressBuildsTurns(city);
    }

    private static void updateFood(City city) {
        int foodIncome = 0;
        foodIncome += city.getTilesFoodIncome();
        ArrayList<BuildingType> buildingTypes = city.getBuildings().stream().map(Building::getBuildingType).collect(Collectors.toCollection(ArrayList::new));
        if (buildingTypes.contains(BuildingType.GRANARY)) foodIncome += 2;
        if (buildingTypes.contains(BuildingType.WATER_MILL) && city.hasRiver()) {
            foodIncome += 2;
        }
        int foodConsumption = city.getPopulation() * 2;
        if (foodConsumption > foodIncome) {
            city.setPopulation(foodIncome / 2);
            city.setFoodIncome(0);
            return;
        }
        if (city.getUnitInProgress() != null && city.getUnitInProgress().getUnitType().equals(UnitType.SETTLER)) {
            city.setFoodIncome(0);
            return;
        }
        city.setFoodIncome(foodIncome - foodConsumption);
        city.updateNewCitizenStoredFood();
    }

    private static void updateInProgressBuildsTurns(City city) {
        Player player = city.getOwner();
        // Building
        Building inProgressBuilding = city.getBuildingInProgress();
        if (inProgressBuilding != null) {
            inProgressBuilding.setRemainingCost(inProgressBuilding.getRemainingCost() - city.getProductionIncome()); // production should be completed
            if (inProgressBuilding.getRemainingCost() <= 0) {
                city.addBuilding(inProgressBuilding);
                city.setBuildingInProgress(null);
                player.addNotification(GameController.getTurn() + ": " + inProgressBuilding.getBuildingType() + " was built in " + city.getName());
            }
        }
        // Unit
        Unit inProgressUnit = city.getUnitInProgress();
        if (inProgressUnit != null) {
            inProgressUnit.setRemainingCost(inProgressUnit.getRemainingCost() - city.getProductionIncome());
            if (inProgressUnit.getRemainingCost() <= 0) {
                inProgressUnit.setRemainingCost(0);
                if (!city.getCapitalTile().canFit(inProgressUnit)) {
                    System.err.println("unit can't be made because there exists a unit on the tile already");
                    return;
                }
                player.addUnit(inProgressUnit);
                city.setUnitInProgress(null);
                city.getCapitalTile().putUnit(inProgressUnit);
                inProgressUnit.setTile(city.getCapitalTile());
                inProgressUnit.setDestination(city.getCapitalTile());
                player.addNotification(GameController.getTurn() + ": " + inProgressUnit.getUnitType() + " was built in " + city.getName());
            }
        }
    }

    /**
     * gets a UnitType and builds it. if this type is half-built continues it. moves currently inProgress building to
     * inComplete buildings of city
     *
     * @param unitType type of the desired unit
     */
    public static InGameResponses.Unit buildUnit(UnitType unitType) {
        City city = GameController.getSelectedCity();
        if (city == null) return InGameResponses.Unit.CITY_NOT_SELECTED;
        Player player = city.getOwner();
        if (player != GameController.getCurrentPlayer()) {
            return InGameResponses.Unit.CITY_NOT_IN_POSSESS;
        }
        if (unitType.neededTech != null && player.getTechnologyByType(unitType.neededTech) == null) {
            return InGameResponses.Unit.DO_NOT_HAVE_TECH;
        }
        if (unitType.neededResource != null &&
                player.getCities().stream().noneMatch(t -> t.getWorkingResources().contains(unitType.neededResource))) {
            return InGameResponses.Unit.DO_NOT_HAVE_RESOURCE;
        }
        // adding the previous one to the unfinished list
        if (city.getUnitInProgress() != null) {
            if (city.getUnitInProgress().getUnitType() == unitType) {
                return InGameResponses.Unit.UNIT_ALREADY_IN_MAKING;
            }
            city.addIncompleteUnit(city.getUnitInProgress());
        }

        Unit unit;
        // continue the half-built unit if possible
        unit = city.getIncompleteUnitByType(unitType);
        if (unit != null) {
            city.removeIncompleteUnit(unit);
        } else {
            if (unitType.combatType == CombatType.CIVILIAN) {
                unit = new Unit(null, GameController.getCurrentPlayer(), unitType);
            } else {
                unit = new Troop(null, GameController.getCurrentPlayer(), unitType);
            }
            if(city.hasBuildingByType(BuildingType.STABLE) && unit.getCombatType().equals(CombatType.MOUNTED)){
                unit.setRemainingCost((int) (unit.getCost() * 0.75));
            }
            if(city.hasBuildingByType(BuildingType.FORGE)){
                unit.setRemainingCost((int) (unit.getCost() * 0.75));
            }
            if(city.hasBuildingByType(BuildingType.ARSENAL)){
                unit.setRemainingCost((int) (unit.getCost() * 0.80));
            }
        }

        city.setUnitInProgress(unit);
        return InGameResponses.Unit.UNIT_BUILDING_SUCCESSFUL;
    }

    public static InGameResponses.Unit pauseInProgressUnit() {
        City city = GameController.getSelectedCity();
        if (city == null) return InGameResponses.Unit.CITY_NOT_SELECTED;
        if (city.getOwner() != GameController.getCurrentPlayer()) {
            return InGameResponses.Unit.CITY_NOT_IN_POSSESS;
        }
        Unit unit = city.getUnitInProgress();
        city.addIncompleteUnit(unit);
        return InGameResponses.Unit.UNIT_BUILDING_PAUSED;
    }

    /**
     * gets a BuildingType and builds it. if this type is half-built continues it. moves currently inProgress building to
     * inComplete buildings of city
     *
     * @param buildingType type of the desired building
     */
    public static InGameResponses.Building buildBuilding(BuildingType buildingType) {
        City city = GameController.getSelectedCity();
        if (city == null) return InGameResponses.Building.CITY_NOT_SELECTED;

        if (city.getOwner() != GameController.getCurrentPlayer()) {
            return InGameResponses.Building.CITY_NOT_IN_POSSESS;
        }

        for (Building building : city.getBuildings()) {
            if (building.getBuildingType() == buildingType)
                return InGameResponses.Building.ALREADY_EXISTS;
        }
        // adding previous inProgress to incomplete
        if (city.getBuildingInProgress() != null) {
            city.addIncompleteBuilding(city.getBuildingInProgress());
        }

        // restating previously half-built if exists
        for (Building incompleteBuilding : city.getIncompleteBuildings()) {
            if (incompleteBuilding.getBuildingType() == buildingType) {
                city.setBuildingInProgress(incompleteBuilding);
                city.removeIncompleteBuilding(incompleteBuilding);
                return InGameResponses.Building.IN_PROGRESS_BUILDING_CHANGED;
            }
        }

        city.setBuildingInProgress(new Building(buildingType));
        if(city.hasBuildingByType(BuildingType.WORKSHOP)){
            city.getBuildingInProgress().setRemainingCost((int) (city.getBuildingInProgress().getCost() * 0.80));
        }
        return InGameResponses.Building.IN_PROGRESS_BUILDING_CHANGED;
    }

    public static InGameResponses.Building pauseInProgressBuilding() {
        City city = GameController.getSelectedCity();
        if (city == null) return InGameResponses.Building.CITY_NOT_SELECTED;
        if (city.getOwner() != GameController.getCurrentPlayer()) return InGameResponses.Building.CITY_NOT_IN_POSSESS;
        if (city.getBuildingInProgress() == null) return InGameResponses.Building.NO_BUILDING_IN_PROGRESS;
        city.addIncompleteBuilding(city.getBuildingInProgress());
        city.setBuildingInProgress(null);
        return InGameResponses.Building.BUILDING_PAUSED;
    }

    public static InGameResponses.City buyUnit(UnitType unitType) {
        City city = GameController.getSelectedCity();
        if (city == null) return InGameResponses.City.NO_CITY_SELECTED;
        if (city.getOwner() != GameController.getCurrentPlayer()) {
            return InGameResponses.City.CITY_NOT_IN_POSSESS;
        }
        Tile pooch = new Tile(-1, -1, null, null, null); // this tile is temporary
        Unit unit;
        if (unitType.combatType == CombatType.CIVILIAN) {
            unit = new Unit(pooch, city.getOwner(), unitType);
        } else {
            unit = new Troop(pooch, city.getOwner(), unitType);
        }
        if (!city.getCapitalTile().canFit(unit)) {
            return InGameResponses.City.CAPITAL_IS_FULL;
        }
        Player player = city.getOwner();
        if (player.getGold() < unit.getCost()) {
            return InGameResponses.City.NOT_ENOUGH_GOLD;
        }
        player.setGold(player.getGold() - unit.getCost());
        unit.placeIn(city.getCapitalTile(), GameController.getMap());
        unit.setRemainingCost(0);
        return InGameResponses.City.UNIT_BUY_SUCCESSFUL;
    }

    public static InGameResponses.City assignCitizenToTile(int row, int column) {
        City city = GameController.getSelectedCity();
        if (city == null) return InGameResponses.City.NO_CITY_SELECTED;
        if (city.getOwner() != GameController.getCurrentPlayer()) {
            return InGameResponses.City.CITY_NOT_IN_POSSESS;
        }
        if (city.getFreeCitizens() == 0) {
            return InGameResponses.City.NO_FREE_CITIZEN;
        }
        Tile tile = GameController.getMap().getTile(row, column);
        if (tile == null) {
            return InGameResponses.City.LOCATION_NOT_VALID;
        }
        if (tile.getCity() != city) {
            return InGameResponses.City.TILE_NOT_IN_TERRITORY;
        }
        if (tile.isHasCitizen()) {
            return InGameResponses.City.TILE_ALREADY_FULL;
        }
        tile.setHasCitizen(true);
        city.setFreeCitizens(city.getFreeCitizens() - 1);
        city.getOwner().addNotification("A Citizen was assigned to " + tile.getRow() + " , " + tile.getColumn());
        return InGameResponses.City.ASSIGNMENT_SUCCESSFUL;
    }

    public static InGameResponses.City freeCitizenFromTile(int row, int column) {
        City city = GameController.getSelectedCity();
        if (city == null) return InGameResponses.City.NO_CITY_SELECTED;
        if (city.getOwner() != GameController.getCurrentPlayer()) {
            return InGameResponses.City.CITY_NOT_IN_POSSESS;
        }
        Tile tile = GameController.getMap().getTile(row, column);
        if (tile == null) {
            return InGameResponses.City.LOCATION_NOT_VALID;
        }
        if (tile.getCity() != city) {
            return InGameResponses.City.TILE_NOT_IN_TERRITORY;
        }
        if (!tile.isHasCitizen()) {
            return InGameResponses.City.TILE_IS_EMPTY;
        }
        tile.setHasCitizen(false);
        city.setFreeCitizens(city.getFreeCitizens() + 1);
        return InGameResponses.City.FREEING_SUCCESSFUL;
    }

    public static InGameResponses.City buyTile(int row, int column) {
        Map map = GameController.getMap();
        City city = GameController.getSelectedCity();
        if (city == null) {
            return InGameResponses.City.NO_CITY_SELECTED;
        }
        if (city.getOwner() != GameController.getCurrentPlayer()) {
            return InGameResponses.City.CITY_NOT_IN_POSSESS;
        }
        Tile tile = GameController.getMap().getTile(row, column);
        if (tile == null) {
            return InGameResponses.City.LOCATION_NOT_VALID;
        }
        if (tile.getCity() != null) {
            return InGameResponses.City.TILE_ALREADY_BOUGHT;
        }
        boolean isClose = false;
        for (Tile neighbour : tile.getNeighbouringTiles(map)) {
            if (city.getTerritory().contains(neighbour)) {
                isClose = true;
            }
        }
        if (!isClose) {
            return InGameResponses.City.TILE_TOO_FAR;
        }
        tile.setCity(city);
        city.addTile(tile);
        return InGameResponses.City.TILE_BUY_SUCCESSFUL;
    }

    public static InGameResponses.City attack(int row, int column) {
        Map map = GameController.getMap();
        City city = GameController.getSelectedCity();
        if (city == null) return InGameResponses.City.NO_CITY_SELECTED;
        if (city.getOwner() != GameController.getCurrentPlayer()) {
            return InGameResponses.City.CITY_NOT_IN_POSSESS;
        }
        Tile tile = map.getTile(row, column);
        if (tile.getTroop() == null) {
            return InGameResponses.City.TILE_EMPTY;
        }
        if (tile.getTroop().getOwner() == city.getOwner()) {
            return InGameResponses.City.OWN_TARGET;
        }
        if (tile.getCity() != city) {
            return InGameResponses.City.TILE_NOT_IN_TERRITORY;
        }
        if (map.getDistanceTo(city.getCapitalTile(), tile) > 2) {
            return InGameResponses.City.TILE_OUT_OF_RANGE;
        }
        CombatController.rangedAttack(city, tile.getTroop());
        return InGameResponses.City.ATTACK_SUCCESSFUL;
    }

    public static InGameResponses.City delete() {
        City city = GameController.getSelectedCity();
        if (city == null) return InGameResponses.City.NO_CITY_SELECTED;
        if (city.getOwner() != GameController.getCurrentPlayer()) {
            return InGameResponses.City.CITY_NOT_IN_POSSESS;
        }
        Player player = city.getOwner();
        int addedGold = city.getGoldIncome() * 5;
        addedGold += city.getPopulation();
        addedGold += city.getTerritory().size() * 5;
        addedGold += city.getBuildings().size() * 3;
        player.setGold(player.getGold() + addedGold);
        city.destroy();
        PlayerController.updateFieldOfView(player);
        return InGameResponses.City.DELETE_SUCCESSFUL;
    }
}
