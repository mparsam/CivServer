package View.PastViews;

import java.util.Scanner;

public class Menu {
    public static MenuType currentMenu;

    public enum MenuType {
        MAIN_MENU("mainMenu"),
        LOGIN_MENU("loginMenu"),
        GAME_MENU("gameMenu"),
        PROFILE_MENU("profileMenu"),
        EXIT("exit");

        String name;

        MenuType(String name) {
            this.name = name;
        }
    }

    public static MenuType getCurrentMenu() {
        return currentMenu;
    }

    public static void setCurrentMenu(MenuType currentMenu) {
        Menu.currentMenu = currentMenu;
    }

    public static void run(Scanner scanner) {
        currentMenu = MenuType.LOGIN_MENU;
        while (currentMenu != MenuType.EXIT) {
            switch (currentMenu) {
                case LOGIN_MENU -> LoginMenu.run(scanner);
                case MAIN_MENU -> MainMenu.run(scanner);
                case GAME_MENU -> GameMenu.run(scanner);
                case PROFILE_MENU -> ProfileMenu.run(scanner);
            }
        }
    }

    public static MenuType getType(String name) { // returns menuType based on name
        for (MenuType type : MenuType.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }

    protected static void showCurrentMenu(String... command) {
        System.out.println(currentMenu.name);
    }

    protected static void invalidCommand() {
        System.out.println("invalid command!");
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
