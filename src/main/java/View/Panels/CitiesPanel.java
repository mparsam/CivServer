package View.Panels;

import Controller.GameController;
import Model.City;
import View.PastViews.CLI;
import View.PastViews.GameMenu;
import enums.Responses.InGameResponses;

import java.util.ArrayList;

public class CitiesPanel extends GameMenu {
    public static void run(String command) {
        if (command.startsWith("show panel")) {

        } else if(command.startsWith("open city ")){
            openCity(command);
        } else {
            invalidCommand();
        }
    }


    private static void openCity(String command){
        ArrayList<String> parameters = CLI.getParameters(command, "id");
        if (parameters == null || !parameters.get(0).matches("-?\\d+")) {
            invalidCommand();
            return;
        }
        int id = Integer.parseInt(parameters.get(0)) - 1;
        if (id < 0 || id >= GameController.getCurrentPlayer().getCities().size()) {
            System.out.println(InGameResponses.Info.INVALID_ID.getString());
            return;
        }
        City city = GameController.getCurrentPlayer().getCities().get(id);
        GameController.moveCameraToCity(city);
        GameController.setSelectedCity(city);
        currentPanel = PanelType.CITY_SELECTED_PANEL;
        System.out.println("city opened successfully");
    }

    private static String printRow(String s1, String s2, String s3) {
        String format = "|%1$-5s|%2$-15s|%3$-15s|";
        String result = "";
        result += String.format(format, s1, s2, s3) + "\n";
        return result;
    }
}
