package View.PastViews;

import Controller.UserController;
import enums.Responses.Response;

import java.util.ArrayList;
import java.util.Scanner;

public class LoginMenu extends Menu {
    public static void run(Scanner scanner) {
        String command;
        while (true) {
            command = scanner.nextLine();
            if (command.startsWith("login")) {
                login(command);
                return;
            } else if (command.startsWith("register")) {
                register(command);
            } else if (command.startsWith("exit")) {
                exit(command);
                return;
            } else if (command.startsWith("current menu")) {
                showCurrentMenu();
            } else {
                invalidCommand();
            }
        }

    }

    public static void login(String command) {
        ArrayList<String> parameters = CLI.getParameters(command, "u", "p");
        if (parameters == null) {
            invalidCommand();
            return;
        }
        Response.LoginMenu response = UserController.login(parameters.get(0), parameters.get(1), "  ");
        System.out.println(response.getString());
    }

    public static void register(String command) {
        ArrayList<String> parameters = CLI.getParameters(command, "u", "p", "n");
        if (parameters == null) {
            invalidCommand();
            return;
        }
        Response.LoginMenu response = UserController.register(parameters.get(0), parameters.get(1), parameters.get(2), null);
        if (response.equals(Response.LoginMenu.USERNAME_EXISTS)) {
            System.out.println(response.getString(parameters.get(0)));
        } else if (response.equals(Response.LoginMenu.NICKNAME_EXISTS)) {
            System.out.println(response.getString(parameters.get(2)));
        } else {
            System.out.println(response.getString());
        }
    }

    public static void exit(String command) {
        UserController.saveUsers();
        setCurrentMenu(MenuType.EXIT);
    }
}
