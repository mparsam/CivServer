package View.PastViews;

import Controller.UserController;
import Model.User;
import enums.Responses.Response;

import java.util.ArrayList;
import java.util.Scanner;

public class ProfileMenu extends Menu {
    public static void run(Scanner scanner) {
        String command;
        while (true) {
            command = scanner.nextLine();
            if (command.startsWith("back")) {
                back(command);
                return;
            } else if (command.startsWith("change password")) {
                changePassword(command);
            } else if (command.startsWith("change nickname")) {
                changeNickname(command);
            } else if (command.startsWith("change username")) {
                System.out.println("sorry, you can't change your username, I know it sucks :)");
            } else if (command.startsWith("delete account")) {
                deleteAccount(command);
            } else if (command.startsWith("show scoreboard")) {
                showScoreboard(command);
            } else if (command.startsWith("current menu")) {
                showCurrentMenu();
            } else {
                invalidCommand();
            }
        }
    }

    public static void back(String command) {
        setCurrentMenu(MenuType.MAIN_MENU);
    }

    public static void changePassword(String command) {
        ArrayList<String> parameters = CLI.getParameters(command, "p", "np");
        if (parameters == null) {
            invalidCommand();
            return;
        }
        Response.ProfileMenu response = UserController.changePassword(parameters.get(0), parameters.get(1));
        System.out.println(response.getString());
    }

    public static void changeNickname(String command) {
        ArrayList<String> parameters = CLI.getParameters(command, "n");
        if (parameters == null) {
            invalidCommand();
            return;
        }
        Response.ProfileMenu response = UserController.changeNickname(parameters.get(0));
        if (response.equals(Response.ProfileMenu.NICKNAME_EXISTS)) {
            System.out.println(response.getString(parameters.get(0)));
        } else System.out.println(response.getString());
    }

    public static void deleteAccount(String command) {
        ArrayList<String> parameters = CLI.getParameters(command, "p");
        if (parameters == null) {
            invalidCommand();
            return;
        }
        Response.ProfileMenu response = UserController.removeUser();
        System.out.println(response.getString());
    }

    public static void showScoreboard(String command) {
        ArrayList<User> scoreboard = UserController.getScoreboard();
        int rank = 1;
        for (User user : scoreboard) {
            System.out.println(rank + "- " + user.getNickname() + ":" + user.getBestScore());
            rank++;
        }
    }
}
