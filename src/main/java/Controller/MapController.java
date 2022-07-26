package Controller;

import Model.*;
import Model.Units.Troop;
import Model.Units.Unit;
import enums.Types.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MapController {
    private static final int INF = 9999;

    private static Tile nearestTileByFeature(Tile tile1, TerrainFeature feature) {
        Map map = GameController.getMap();
        int height = map.getHeight(), width = map.getWidth();
        Tile tile2 = map.getTile(0, 0);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (map.getTile(i, j).getTerrainFeature() == feature || map.getTile(i, j).getBaseFeature() == feature) {
                    if (map.getDistanceTo(tile1, map.getTile(i, j)) <= map.getDistanceTo(tile1, tile2)) {
                        tile2 = map.getTile(i, j);
                    }
                }
            }
        }
        return tile2;
    }

    private static void drawRiverFromTo(Tile tile1, Tile tile2) {
        Map map = GameController.getMap();
        Tile currentTile = map.getNextMoveTo(tile1, tile2);
        int lastDirection = (tile1.getDirectionTo(currentTile) + 6) % 12;
        while (currentTile != tile2) {
            Tile nextTile = map.getNextMoveTo(currentTile, tile2);
            int direction = currentTile.getDirectionTo(nextTile);
            lastDirection = (lastDirection + 2) % 12;
            while (lastDirection != direction) {
                currentTile.setRiverInDirection(lastDirection, 1); // river value is 1
                Tile oppositeTile = currentTile.getTileInDirection(map, lastDirection);
                oppositeTile.setRiverInDirection((lastDirection + 6) % 12, 1);
                lastDirection = (lastDirection + 2) % 12;
            }
            lastDirection = (lastDirection + 6) % 12;
            currentTile = nextTile;
        }
    }

    public static void randomizeRivers() {
        Map map = GameController.getGame().getMap();
        Tile[][] tiles = map.getTiles();
        int width = map.getWidth(), height = map.getHeight();
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                Tile tile1 = tiles[row][column];
                if (tile1.getTerrain().getTerrainType().equals(TerrainType.MOUNTAIN)) {
                    Tile tile2 = nearestTileByFeature(tile1, TerrainFeature.OCEAN);
                    drawRiverFromTo(tile1, tile2);
                }
            }
        }
    }

    private static void surroundWithOcean(Map map, int height, int width) {
        Tile[][] tiles = map.getTiles();
        // surrounding the map with ocean
        for (int i = 0; i < height; i++) {
            tiles[i][0] = new Tile(i, 0, new Terrain(TerrainType.OCEAN, TerrainFeature.NULL, ResourceType.NULL), FogState.UNKNOWN, null);
            tiles[i][width - 1] = new Tile(i, width - 1, new Terrain(TerrainType.OCEAN, TerrainFeature.NULL, ResourceType.NULL), FogState.UNKNOWN, null);
        }
        for (int i = 0; i < width; i++) {
            tiles[0][i] = new Tile(0, i, new Terrain(TerrainType.OCEAN, TerrainFeature.ICE, ResourceType.NULL), FogState.UNKNOWN, null);
            tiles[height - 1][i] = new Tile(height - 1, i, new Terrain(TerrainType.OCEAN, TerrainFeature.ICE, ResourceType.NULL), FogState.UNKNOWN, null);
        }
    }

    private static ArrayList<TerrainType> getPossibleTerrains(ArrayList<Tile> neighbours) {
        ArrayList<TerrainType> possibleTerrains = new ArrayList<>(Arrays.asList(TerrainType.values()));
        for (Tile tile : neighbours) {
            if (tile == null) continue;
            // increasing the chance of a tile being one of the neighbouring terrains
            possibleTerrains.add(tile.getTerrain().getTerrainType());
            if (!tile.getTerrainType().equals(TerrainType.OCEAN)) {
                possibleTerrains.add(tile.getTerrain().getTerrainType());
            }
        }
        for (Tile tile : neighbours) {
            if (tile == null) continue;
            // making the terrain logical
            if (tile.getTerrainType().equals(TerrainType.DESERT)) {
                possibleTerrains.removeIf(TerrainType.SNOW::equals);
                possibleTerrains.removeIf(TerrainType.TUNDRA::equals);
            }
            if (tile.getTerrainType().equals(TerrainType.SNOW)) {
                possibleTerrains.removeIf(TerrainType.DESERT::equals);
            }
            if (tile.getTerrainType().equals(TerrainType.TUNDRA)) {
                possibleTerrains.removeIf(TerrainType.DESERT::equals);
            }
        }
        return possibleTerrains;
    }

    public static Map randomMap(int height, int width) {
        Map map = new Map(height, width);
        Tile[][] tiles = map.getTiles();

        surroundWithOcean(map, height, width);

        Random random = new Random(System.currentTimeMillis());
        for (int row = 1; row < height - 1; row++) {
            for (int column = 1; column < width - 1; column++) {
                // sets Terrain randomly

                ArrayList<TerrainType> possibleTerrains = getPossibleTerrains(map.getNeighbouringTiles(row, column));
                TerrainType terrainType = possibleTerrains.get(random.nextInt(possibleTerrains.size()));

                TerrainFeature terrainFeature = TerrainFeature.NULL;
                if (random.nextInt(2) == 0) {
                    ArrayList<TerrainFeature> possibleTerrainFeatures = terrainType.possibleTerrainFeatures;
                    if (!possibleTerrainFeatures.isEmpty()) {
                        terrainFeature = possibleTerrainFeatures.get(random.nextInt(possibleTerrainFeatures.size()));
                    }
                }

                ResourceType resourceType = ResourceType.NULL;
                if (random.nextInt(6) == 0) {
                    ArrayList<ResourceType> possibleResources = terrainType.baseFeature.possibleResources;
                    if (terrainFeature != null) possibleResources.addAll(terrainFeature.possibleResources);
                    if (!possibleResources.isEmpty()) {
                        resourceType = possibleResources.get(random.nextInt(possibleResources.size()));
                    }
                }
                if (resourceType == null) resourceType = ResourceType.NULL;

                Ruin ruin = null;
                if(resourceType == ResourceType.NULL && random.nextInt(6) == 0){
                    ruin = Ruin.getRandomRuin();
                }

                Terrain terrain = new Terrain(terrainType, terrainFeature, resourceType);
                tiles[row][column] = new Tile(row, column, terrain, FogState.UNKNOWN, ruin); // note: the main map is foggy
            }
        }

        return map;
    }

    public static void BuildCity(Unit unit, String name) {
        Map map = GameController.getMap();
        Tile tile = unit.getTile();
        ArrayList<Tile> territory = tile.getNeighbouringTiles(map);
        territory.add(tile);
        City city = new City(name, unit.getOwner(), tile, territory);
        for (Tile tile1 : territory) {
            tile1.setCity(city);
        }
        unit.getOwner().addCity(city);
        unit.getOwner().addNotification(GameController.getTurn() + ": the city of " + name + " has been constructed");
        tile.setCity(city);
        PlayerController.updateFieldOfView(city.getOwner());
    }

    public static void collectRuin(Unit unit) {
        Player player = unit.getOwner();
        Tile tile = unit.getTile();
        Ruin ruin = tile.getRuin();
        if(player.getCapital() != null) player.getCapital().addPopulation(ruin.getPopulation());
        player.setGold(player.getGold() + ruin.getGold());
        if(ruin.getTechnology() != null){
            Technology technology = new Technology(ruin.getTechnology());
            technology.setRemainingCost(0);
            player.addTechnology(technology);
        }
        if(ruin.getUnit() != null){
            if(unit.getCombatType() == CombatType.CIVILIAN){
                player.setGold(player.getGold() + 30);
            } else {
                GameController.cheatPutUnit(ruin.getUnit(), tile.getRow(), tile.getColumn());
            }
        }
        tile.setRuin(null);
    }

}
