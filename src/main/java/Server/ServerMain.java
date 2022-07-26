package Server;

import Controller.GameController;
import Controller.PlayerController;
import Controller.UserController;
import Model.Player;
import Model.Request;
import Model.User;
import View.Panels.InGameCommandHandler;
import enums.ParameterKeys;
import net.bytebuddy.utility.RandomString;

import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

import static enums.RequestActions.*;

public class ServerMain {
    private static final String chatServerCommand = """
            "C:\\Program Files\\Java\\jdk1.8.0_311\\bin\\java.exe" "-javaagent:E:\\IntelliJ IDEA 2021.3.2\\lib\\idea_rt.jar=1063:E:\\IntelliJ IDEA 2021.3.2\\bin" -Dfile.encoding=UTF-8 -classpath "C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\charsets.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\deploy.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\ext\\access-bridge-64.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\ext\\cldrdata.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\ext\\dnsns.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\ext\\jaccess.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\ext\\jfxrt.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\ext\\localedata.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\ext\\nashorn.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\ext\\sunec.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\ext\\sunjce_provider.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\ext\\sunmscapi.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\ext\\sunpkcs11.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\ext\\zipfs.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\javaws.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\jce.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\jfr.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\jfxswt.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\jsse.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\management-agent.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\plugin.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\resources.jar;C:\\Program Files\\Java\\jdk1.8.0_311\\jre\\lib\\rt.jar;D:\\UNIVERSITY\\2\\AP\\Projects\\Chat\\out\\production\\Chat" Main
            """;

    private static final int SERVER_PORT = 7777;
    private static final HashMap<Long, User> threadIDUser = new HashMap<>();
    private static final HashMap<Long, ObjectOutputStream> outputStreamsByThreadID = new HashMap<>();

    public static void main(String[] args) {
        System.err.println(available(7778));
//        try {
////            Runtime.getRuntime().exec(chatServerCommand);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        ServerSocket ss;
        try {
            ss = new ServerSocket(SERVER_PORT);
            System.out.println("server started: " + ss);
            while (true) {
                Socket socket = ss.accept();
                System.out.println("Connected: " + socket);
                new Thread(() -> {
                    ThreadLocal loggedInUser = new ThreadLocal();
                    try {
                        InputStream inputStream = socket.getInputStream();
                        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                        OutputStream outputStream = socket.getOutputStream();
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                        outputStreamsByThreadID.put(Thread.currentThread().getId(), objectOutputStream);

                        while (true) {
                            Request request = (Request) objectInputStream.readObject();
                            String action = request.action;
                            HashMap<String, String> params = request.params;
                            System.out.println(request);
                            if (action.equals(LOGIN.code)) {
                                String privateToken = RandomString.make(30);
                                HashMap<String, String> paramssss = new HashMap<>();
                                paramssss.put("token", privateToken);
                                sendEnumRequest(UserController.login(params.get(ParameterKeys.USERNAME.code), params.get(ParameterKeys.PASSWORD.code), privateToken), objectOutputStream, paramssss);
                            } else if (action.equals(REGISTER.code)) {
                                sendEnumRequest(UserController.register(params.get(ParameterKeys.USERNAME.code),
                                        params.get(ParameterKeys.PASSWORD.code),
                                        params.get(ParameterKeys.NICKNAME.code), loggedInUser), objectOutputStream);
                            } else if (action.equals(REMOVE_USER.code)) {
                                sendEnumRequest(UserController.removeUser(), objectOutputStream);
                            } else if (action.equals(CHANGE_PROFILE_PICTURE.code)) {
                                sendEnumRequest(UserController.changePicture((ArrayList<Byte>) request.getObj()), objectOutputStream);
                            } else if (action.equals(GET_THIS_USER.code)) {
                                sendRequest(new Request("sent this User", null, UserController.getCurrentUser()), objectOutputStream);
                            } else if (action.equals(CHANGE_PASSWORD.code)) {
                                sendEnumRequest(UserController.changePassword(params.get(ParameterKeys.OLD_PASSWORD.code), params.get(ParameterKeys.NEW_PASSWORD.code)), objectOutputStream);
                            } else if (action.equals(CHANGE_NICKNAME.code)) {
                                sendEnumRequest(UserController.changeNickname(params.get(ParameterKeys.NICKNAME.code)), objectOutputStream);
                            } else if (action.equals(GET_GAME_MAP.code)) {
                                sendRequest(new Request("Game Map Sent", null, GameController.getCurrentPlayerMap()), objectOutputStream);
                            } else if (action.equals(GET_INVITATIONS.code)) {
                                sendRequest(new Request("invitations", null, GameController.getInvitationsOf(getThisThreadUser())), objectOutputStream);
                            } else if (action.equals(SEND_INVITATIONS.code)) {
                                GameController.sendInvitaions((ArrayList<String>) request.getObj());
                                sendRequest(new Request("OK", null), objectOutputStream);
                            } else if (action.equals(ACCEPT_INVITAION.code)) {
                                GameController.acceptInvitaion(getThisThreadUser());
                                sendRequest(new Request("OK", null), objectOutputStream);
                            } else if (action.equals(ARE_INVITATIONS_ACCEPTED.code)) {
                                Request response = new Request("invitation accp? result", null, GameController.areInvitationAccepted());
                                sendRequest(response, objectOutputStream);
                            } else if (action.equals(GET_USER_BY_USERNAME.code)) {
                                sendRequest(new Request("Sending user by username", null, UserController.getUserByUsername((String) request.getObj())), objectOutputStream);
                            } else if (action.equals(IS_MY_TURN.code)) {
                                Boolean value = GameController.isThisUsersTurn(getThisThreadUser());
                                System.err.println("is my turn?: " + value);
                                sendRequest(new Request("is my turn response", null, value), objectOutputStream);
                            } else if (action.equals(NEW_GAME.code)) {
                                GameController.newGame((ArrayList<User>) request.getObj(), Integer.parseInt(params.get("mapSize")));
                                sendRequest(new Request("game started", null), objectOutputStream);
                            } else if (action.equals(PANEL_COMMAND.code)) {
                                sendRequest(new Request("response to panel action", null, InGameCommandHandler.handleCommand((String) request.getObj())), objectOutputStream);
                            } else if (action.equals(PASS_TURN.code)) {
                                if (GameController.getCurrentPlayer().getUser().equals(getThisThreadUser())) {
                                    PlayerController.nextTurn();
                                    sendRequest(new Request("turn passed!", null), objectOutputStream);
                                } else {
                                    sendRequest(new Request("you cant pass turn", null), objectOutputStream);
                                }
                            } else if (action.equals(GET_THIS_PLAYER.code)) {
                                sendRequest(new Request("get player response", null, GameController.getPlayerOfUser(getThisThreadUser())), objectOutputStream);
                            } else if (action.equals(GET_GAME.code)) {
                                sendRequest(new Request("response of get game", null, GameController.getGame()), objectOutputStream);
                            } else if (action.equals(GET_USERS.code)) {
                                ArrayList<User> users = UserController.getUsers();
                                sendRequest(new Request("send Users", null, users), objectOutputStream);
                            } else if (action.equals(GET_SELECTED_CITY.code)) {
                                sendRequest(new Request("send selected city", null, GameController.getSelectedCity()), objectOutputStream);
                            } else if (action.equals(GET_SELECTED_UNIT.code)) {
                                sendRequest(new Request("send selected troop", null, GameController.getSelectedUnit()), objectOutputStream);
                            } else if (action.equals(GET_THIS_PLAYERS_MAP.code)) {
                                Request request1 = new Request("sending this player's map", null, GameController.getCurrentPlayerMap());
                                sendRequest(request1, objectOutputStream);
                            } else if (action.equals(SELECT_CITY.code)) {
                                sendRequest(new Request("select city", null,
                                        GameController.selectCity(Integer.parseInt(params.get("row")), Integer.parseInt(params.get("column")))), objectOutputStream);
                            } else if (action.equals(SELECT_UNIT.code)) {
                                sendRequest(new Request("select unit", null,
                                        GameController.selectUnit(Integer.parseInt(params.get("row")), Integer.parseInt(params.get("column")))), objectOutputStream);
                            } else if (action.equals(SELECT_TROOP.code)) {
                                sendRequest(new Request("select troop", null,
                                        GameController.selectTroop(Integer.parseInt(params.get("row")), Integer.parseInt(params.get("column")))), objectOutputStream);
                            } else if (action.equals(GET_USERS_PROFILE_PIC.code)) {
                                sendRequest(new Request("profile pic", null, UserController.getProfilePicOf(getThisThreadUser())), objectOutputStream);
                            } else if (action.equals(GET_ONLINES.code)) {
                                sendRequest(new Request("onlines", null, threadIDUser), objectOutputStream);
                            } else if (action.equals(UPDATE_FIELD_OF_VIEW.code)) {
                                PlayerController.updateFieldOfView();
                                sendRequest(new Request("fieldUpdate", null), objectOutputStream);
                            } else if (action.equals(IS_GAME_ENDED.code)) {
                                sendRequest(new Request("is ended response", null, GameController.isEnded()), objectOutputStream);
                            } else if (action.equals(IS_PLAYER_DEAD.code)) {
                                sendRequest(new Request("is player dead response", null, GameController.isDead((Player) request.getObj())), objectOutputStream);
                            } else if (action.equals(LOGOUT.code)) {
                                UserController.logout();
                                sendRequest(new Request("logout done", null), objectOutputStream);
                            } else if (action.equals(END_GAME.code)) {
                                GameController.endGame();
                                sendRequest(new Request("end Game response", null), objectOutputStream);
                            } else if (action.equals(AM_I_LOST.code)) {
                                sendRequest(new Request("am i lost ans", null, PlayerController.checkIfLost()), objectOutputStream);
                            } else if (action.equals(AM_I_WON.code)) {
                                sendRequest(new Request("am i won resp", null, PlayerController.checkIfWon()), objectOutputStream);
                            } else {
                                System.err.println("INVALID COMMAND!!!");
                                sendRequest(new Request("INVALID", null), objectOutputStream);
                            }

                        }

                    } catch (SocketException e) {
                        System.out.println("Disconnected: " + socket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        System.err.println("made this thread OFFLINE");
                        try {
                            threadIDUser.get(Thread.currentThread().getId()).setOnlineStatus("Offline");
                            threadIDUser.remove(Thread.currentThread().getId());
                            outputStreamsByThreadID.remove(Thread.currentThread().getId());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            System.err.println("made this thread OFFLINE");
//            threadIDUser.get(Thread.currentThread().getId()).setOnlineStatus("Offline");
//            threadIDUser.remove(Thread.currentThread().getId());
//            outputStreamsByThreadID.remove(Thread.currentThread().getId());
        }
    }

    private static void sendEnumRequest(Enum thisEnum, ObjectOutputStream objectOutputStream) {
        sendRequest(new Request(thisEnum.toString(), null, thisEnum), objectOutputStream);
    }

    private static void sendEnumRequest(Enum thisEnum, ObjectOutputStream objectOutputStream, HashMap<String, String> params) {
        sendRequest(new Request(thisEnum.toString(), params, thisEnum), objectOutputStream);
    }

    private static synchronized void sendRequest(Request request, ObjectOutputStream objectOutputStream) {
        try {
            System.out.println("Response:  action:" + request.action + " params: " + request.params + " user: " + getThisThreadUser().getNickname());
            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
            objectOutputStream.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void addThreadUser(User user) {
        user.setOnlineStatus("Online");
        System.out.println(user.getNickname() + " is now Online");
        System.out.println(UserController.getUserByUsername(user.getUsername()).getOnlineStatus());
        threadIDUser.put(Thread.currentThread().getId(), user);
        System.out.println("user add to: " + threadIDUser);
    }

    public static synchronized ObjectOutputStream getUsersObjOutStream(User user) {
        for (Long aLong : threadIDUser.keySet()) {
            if (threadIDUser.get(aLong).equals(user)) {
                return outputStreamsByThreadID.get(aLong);
            }
        }
        return null;
    }

    public static synchronized User getThisThreadUser() {
        return threadIDUser.get(Thread.currentThread().getId());
    }

    public static synchronized void removeThisThreadUser() {
        threadIDUser.remove(Thread.currentThread().getId());
    }


    public static boolean available(int port) {


        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }
}
