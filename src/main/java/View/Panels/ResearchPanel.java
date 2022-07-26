package View.Panels;

import Controller.GameController;
import Controller.PlayerController;
import Model.Player;
import Model.Technology;
import View.PastViews.CLI;
import View.PastViews.GameMenu;
import enums.Responses.InGameResponses;
import enums.Types.TechnologyType;

import java.util.ArrayList;

public class ResearchPanel extends GameMenu {
    public static void run(String command) {
        if (command.startsWith("research")) {
            researchTech(command);
        } else if (command.equals("show current research")) {
            showCurrent();
        } else if (command.equals("show panel")) {
            showPanel();
        } else {
            invalidCommand();
        }
    }

    public static void showTechTree(String command) {

    }

    public static InGameResponses.Technology researchTech(String command) {
        ArrayList<String> parameters = CLI.getParameters(command, "t");
        TechnologyType techType = TechnologyType.getTypeByName(parameters.get(0));
        return PlayerController.researchTech(techType);
    }

    private static void showCurrent() {
        Player player = GameController.getCurrentPlayer();
        Technology technology = player.getTechnologyInProgress();
        if (technology == null) {
            System.out.println("no tech being researched");
            return;
        }
        System.out.println(technology.getName());
        if (player.getScienceIncome() == 0) {
            System.out.println("will be finished when pigs fly");
        } else {
            int turnsLeft = (technology.getRemainingCost() + player.getScienceIncome() - 1) / player.getScienceIncome();
            System.out.println(turnsLeft + " turns left to be completely researched");
        }
        System.out.println("unlocks: " + technology.getUnlocks());
    }

    private static void showPanel() {
        System.out.println("researched technologies:");
        Player player = GameController.getCurrentPlayer();
        for (Technology technology : player.getTechnologies()) {
            System.out.println(technology.getName());
        }
        System.out.println("current research: " + player.getTechnologyInProgress().getName());
    }
}
