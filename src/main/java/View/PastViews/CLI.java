package View.PastViews;

import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CLI {
    private static final Options options = new Options();

    static {
        // LOGIN, MAIN, PROFILE MENU PARAMETERS
        options.addOption("u", "username", true, "");
        options.addOption("p", "password", true, "");
        options.addOption("n", "nickname", true, "");
        options.addOption("m", "menu", true, "");
        Option option1 = new Option("P", "player", true, "players for new game");
        option1.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(option1);
        options.addOption("np", "newPassword", true, "");
        // GAME MENU PARAMETERS
        Option option = new Option("l", "location", true, ""); // [x] [y]
        option.setArgs(2);
        options.addOption(option);
        options.addOption("id", "id", true, "");
        options.addOption("a", "amount", true, "");
        options.addOption("cn", "name", true, "City name"); // in show, select city
        options.addOption("c", "count", true, "how much to move in a direction");
        options.addOption("t", "type", true, "can represent any type of anything");
        Option option2 = new Option("d", "direction", true, ""); // urdl <n>
        option2.setArgs(2);
        options.addOption(option2);
    }

    /**
     * @param command       input command from user
     * @param parameterKeys variable number of parameter keys
     * @return arrayList of parameters' values, returns null with error or invalid command
     */
    public static ArrayList<String> getParameters(String command, String... parameterKeys) {
        ArrayList<String> values = new ArrayList<>();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, command.split(" "));
            for (String parameterKey : parameterKeys) {
                if (!cmd.hasOption(parameterKey)) return null;
                if (parameterKey.equals("P") || parameterKey.equals("player")) {
                    String[] rawValues = cmd.getOptionValues("P");
                    List<String> playersUsernames = java.util.Arrays.stream(rawValues, 1, rawValues.length).toList();
                    values.addAll(playersUsernames);
                } else if (parameterKey.equals("d") || parameterKey.equals("direction")) {
                    String[] rawValues = cmd.getOptionValues("d");
                    String direction = rawValues[0];
                    if (!direction.matches("^[urld]$")) return null;
                    try {
                        int amount = Integer.parseInt(rawValues[1]);
                        values.addAll(Arrays.stream(rawValues).toList());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else if (parameterKey.equals("l") || parameterKey.equals("location")) {
                    String[] rawValues = cmd.getOptionValues("l");
                    try {
                        int row = Integer.parseInt(rawValues[0]);
                        int column = Integer.parseInt(rawValues[1]);
                        values.addAll(Arrays.stream(rawValues).toList());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else values.add(cmd.getOptionValue(parameterKey));
            }
            return values;
        } catch (ParseException e) {
            System.err.println("error occurred while parsing");
            e.printStackTrace();
            return null;
        }
    }
}
