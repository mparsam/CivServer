package View.Panels;

import Controller.GameController;
import Controller.UnitController;
import Model.Units.Unit;
import View.PastViews.CLI;
import View.PastViews.GameMenu;
import enums.Responses.InGameResponses;
import enums.Responses.Response;
import enums.Types.ImprovementType;
import enums.Types.RoadType;

import java.util.ArrayList;

public class UnitSelectedPanel extends GameMenu {
    public static void run(String command) {
        if (command.startsWith("move unit")) moveTo(command);
        else if (command.startsWith("back")) GameMenu.currentPanel = null;
        else if (command.startsWith("build city")) foundCity(command);
        else if (command.startsWith("sleep")) sleep();
        else if (command.startsWith("alert")) alert();
        else if (command.startsWith("fortify")) fortify();
        else if (command.startsWith("heal")) heal();
        else if (command.startsWith("wake")) wake();
        else if (command.startsWith("delete")) delete();
        else if (command.startsWith("build improvement")) buildImprovement(command);
        else if (command.startsWith("build road")) buildRoad(command);
        else if (command.startsWith("remove forest")) removeForest();
        else if (command.startsWith("remove jungle")) removeJungle();
        else if (command.startsWith("remove marsh")) removeMarsh();
        else if (command.startsWith("remove road")) removeRoute();
        else if (command.startsWith("pillage")) pillage();
        else if (command.startsWith("repair")) repair();
        else if (command.startsWith("set up")) setup();
        else if (command.startsWith("garrison")) garrison();
        else if (command.startsWith("attack")) attack(command);
        else if (command.startsWith("show selected unit")) showSelected();
        else invalidCommand();
    }

    public static void showSelected() {
        Unit unit = GameController.getSelectedUnit();
        System.out.println("location: " + unit.getTile().getRow() + " " + unit.getTile().getColumn());
        System.out.println("type: " + unit.getUnitType().name);
        System.out.println("owner: " + unit.getOwner().getName());
        System.out.println("HP: " + unit.getHP());
        System.out.println("current order status: " + unit.getOrderType().toString());
    }

    public static InGameResponses.Unit moveTo(String command) {
        ArrayList<String> parameters = CLI.getParameters(command, "l");
        int row = Integer.parseInt(parameters.get(0)), column = Integer.parseInt(parameters.get(1));
        return UnitController.moveTo(row, column);
    }

    public static InGameResponses.Unit foundCity(String command) {
        ArrayList<String> parameters = CLI.getParameters(command, "cn");
        return UnitController.foundCity(parameters.get(0));
    }

    public static InGameResponses.Unit sleep() {
        return UnitController.sleep();
    }

    public static InGameResponses.Unit alert() {
        return UnitController.alert();
    }

    public static InGameResponses.Unit fortify() {
        return UnitController.fortify();
    }

    public static InGameResponses.Unit heal() {
        return UnitController.heal();
    }

    public static InGameResponses.Unit wake() {
        return UnitController.wake();
    }

    public static InGameResponses.Unit delete() {
        return UnitController.delete();
    }

    public static InGameResponses.Unit buildImprovement(String command) {
        ArrayList<String> parameters = CLI.getParameters(command, "t");
        return UnitController.buildImprovement(ImprovementType.getTypeByName(parameters.get(0)));
    }

    public static InGameResponses.Unit buildRoad(String command) {
        ArrayList<String> parameters = CLI.getParameters(command, "t");
        return UnitController.buildRoad(RoadType.getTypeByName(parameters.get(0)));
    }

    public static InGameResponses.Unit removeForest() {
        return UnitController.removeForest();
    }

    public static InGameResponses.Unit removeJungle() {
        return UnitController.removeJungle();
    }

    public static InGameResponses.Unit removeMarsh() {
        return UnitController.removeMarsh();
    }

    public static InGameResponses.Unit removeRoute() {
        return UnitController.removeRoute();
    }

    public static InGameResponses.Unit pillage() {
        return UnitController.pillage();
    }

    public static InGameResponses.Unit repair() {
        return UnitController.repair();
    }

    public static InGameResponses.Unit setup() {
        return UnitController.setup();
    }

    public static InGameResponses.Unit garrison() {
        return UnitController.garrison();
    }

    public static InGameResponses.Unit attack(String command) {
        ArrayList<String> parameters = CLI.getParameters(command, "l");
        int row = Integer.parseInt(parameters.get(0)), column = Integer.parseInt(parameters.get(1));
        return UnitController.attack(row, column);
    }

}