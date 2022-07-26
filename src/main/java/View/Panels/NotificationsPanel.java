package View.Panels;

import View.PastViews.GameMenu;

public class NotificationsPanel extends GameMenu {
    public static void run(String command) {
        if (command.startsWith("show panel")) {

        } else if (command.startsWith("show recent")) {

        } else {
            invalidCommand();
        }
    }

}
