package Controller;

import Model.City;
import Model.Player;
import Model.Units.Troop;
import Model.Units.Unit;
import enums.Types.BuildingType;
import enums.Types.CombatType;
import enums.Types.UnitType;

import java.util.Arrays;

public class CombatController {

    private static double getBonus(Troop t1, Troop t2) {
        if ((t1.getUnitType() == UnitType.PIKEMAN || t1.getUnitType() == UnitType.SPEARMAN) && t2.getCombatType() == CombatType.MOUNTED) {
            return 2; // vs mounted bonus
        }
        if (t1.getUnitType() == UnitType.ANTI_TANK_GUN && t2.getUnitType() == UnitType.TANK) {
            return 1.1; // anti tank bonus
        }
        return 1;
    }

    private static double getCityBonus(City c){
        double bonus = 1.0;
        if(c.hasBuildingByType(BuildingType.WALLS)) bonus *= 1.05;
        if(c.hasBuildingByType(BuildingType.CASTLE)) bonus *= 1.075;
        if(c.hasBuildingByType(BuildingType.MILITARY_BASE)) bonus *= 1.12;
        return bonus;
    }

    public static void meleeAttack(Troop attacker, Troop defender) {
        double attPower = attacker.getMeleeStrength() * (attacker.getHP() / attacker.getHealth());
        attPower *= 1 + attacker.getTile().getTerrain().getCombat();
        attPower *= getBonus(attacker, defender);
        double defPower = defender.getMeleeStrength() * (defender.getHP() / defender.getHealth());
        defPower *= 1 + defender.getTile().getTerrain().getCombat();
        defPower *= getBonus(defender, attacker);
        if (attacker.getUnitType().combatType == CombatType.MOUNTED) attacker.setMP(attacker.getMP() - 1);
        else attacker.setMP(0);
        attacker.setFortifyBonus(0);

        defender.setHP(defender.getHP() - defender.getHealth() * (attPower / (defPower * 2)));
        attacker.setHP(attacker.getHP() - attacker.getHealth() * (defPower / (attPower * 2)));

        if (attacker.getHP() <= 0) {
            attacker.getOwner().getUnits().remove(attacker);
            attacker.getOwner().addNotification(GameController.getTurn() + ": a unit of yours has died!");
            attacker.destroy();
        }
        if (defender.getHP() <= 0) {
            defender.getOwner().getUnits().remove(defender);
            defender.getOwner().addNotification(GameController.getTurn() + ": a unit of yours has died!");
            defender.destroy();
        }
        PlayerController.declareWar(attacker.getOwner(), defender.getOwner());

        PlayerController.updateFieldOfView(attacker.getOwner());
        PlayerController.updateFieldOfView(defender.getOwner());
    }

    public static void meleeAttack(Troop attacker, City defender) {
        double attPower = attacker.getMeleeStrength() * (attacker.getHP() / attacker.getHealth());
        attPower *= 1 + attacker.getTile().getTerrain().getCombat();
        if (attacker.getUnitType().equals(UnitType.TANK)) attPower *= 0.9;
        double defPower = defender.getStrength() * getCityBonus(defender);

        if (attacker.getUnitType().combatType == CombatType.MOUNTED) attacker.setMP(attacker.getMP() - 1);
        else attacker.setMP(0);
        attacker.setFortifyBonus(0);

        if (defPower >= 0) defender.setHP(defender.getHP() - defender.getHealth() * (attPower / (defPower * 2)));
        else defender.setHP(0);
        attacker.setHP(attacker.getHP() - attacker.getHealth() * (defPower / (attPower * 2)));

        if (attacker.getHP() <= 0) {
            attacker.destroy();
            attacker.getOwner().addNotification(GameController.getTurn() + ": a unit of yours has died!");
        } else if (defender.getHP() <= 0) {
            attacker.placeIn(defender.getCapitalTile(), GameController.getMap());
            captureCity(attacker.getOwner(), defender);
        }
        PlayerController.declareWar(attacker.getOwner(), defender.getOwner());

        PlayerController.updateFieldOfView(attacker.getOwner());
        PlayerController.updateFieldOfView(defender.getOwner());
    }

    public static void rangedAttack(Troop attacker, Troop defender) {
        double attPower = attacker.getRangedStrength() * (attacker.getHP() / attacker.getHealth());
        attPower *= 1 + attacker.getTile().getTerrain().getCombat();
        if (attacker.getCombatType().equals(CombatType.SIEGE)) attPower *= 1.1;
        double defPower = defender.getMeleeStrength() * (defender.getHP() / defender.getHealth());

        attacker.setMP(0);
        attacker.setFortifyBonus(0);

        defender.setHP(defender.getHP() - defender.getHealth() * (attPower / (defPower * 2)));

        if (defender.getHP() <= 0) {
            defender.getOwner().getUnits().remove(defender);
            defender.getOwner().addNotification(GameController.getTurn() + ": a unit of yours has died!");
            defender.destroy();
        }
        PlayerController.declareWar(attacker.getOwner(), defender.getOwner());

        PlayerController.updateFieldOfView(attacker.getOwner());
        PlayerController.updateFieldOfView(defender.getOwner());
    }

    public static void rangedAttack(Troop attacker, City defender) {
        double attPower = attacker.getRangedStrength() * (attacker.getHP() / attacker.getHealth());
        attPower *= 1 + attacker.getTile().getTerrain().getCombat();
        if (attacker.getCombatType().equals(CombatType.SIEGE)) attPower *= 1.1;
        double defPower = defender.getStrength() * getCityBonus(defender);

        attacker.setMP(0);
        attacker.setFortifyBonus(0);

        if (defPower >= 0) defender.setHP(defender.getHP() - defender.getHealth() * (attPower / (defPower * 2)));
        else defender.setHP(0);
        defender.setHP(Math.max(defender.getHP(), 1));

        PlayerController.declareWar(attacker.getOwner(), defender.getOwner());

        PlayerController.updateFieldOfView(attacker.getOwner());
        PlayerController.updateFieldOfView(defender.getOwner());
    }

    public static void rangedAttack(City attacker, Troop defender) {
        double attPower = attacker.getStrength();
        double defPower = defender.getMeleeStrength() * getCityBonus(attacker);
        defPower *= 1 + defender.getTile().getTerrain().getCombat();
        defPower *= 1 + 0.25 * defender.getFortifyBonus();
        if (defender.getCombatType().equals(CombatType.SIEGE)) defPower *= 1.1;
        if (defender.getUnitType().equals(UnitType.TANK)) defPower *= 0.9;

        attacker.setHasAttacked(true);
        defender.setHP(defender.getHP() - defender.getHealth() * (attPower / (defPower * 2)));

        if (defender.getHP() <= 0) {
            defender.destroy();
            defender.getOwner().addNotification(GameController.getTurn() + ": a unit of yours has died!");
        }
        PlayerController.declareWar(attacker.getOwner(), defender.getOwner());

        PlayerController.updateFieldOfView(attacker.getOwner());
        PlayerController.updateFieldOfView(defender.getOwner());
    }

    public static void captureCity(Player player, City city) {
        int turn = GameController.getTurn();
        city.getOwner().addNotification(turn + ": you have lost the city of " + city.getName());
        city.getOwner().removeCity(city);
        city.setOwner(player);
        player.addCity(city);
        player.addNotification(turn + ": you have have captured the city of" + city.getName());
        if(GameController.isEnded()){
            GameController.endGame();
        }
    }

    public static void captureUnit(Troop attacker, Unit defender) {
        defender.getOwner().removeUnit(defender);
        defender.getOwner().addNotification(GameController.getTurn() + ": a unit of yours has been captured!");
        defender.setOwner(attacker.getOwner());
        attacker.getOwner().addUnit(defender);
    }
}
