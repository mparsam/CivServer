import Controller.UserController;
import Model.User;
import enums.Responses.Response;
import org.junit.Assert;
import org.junit.Test;

import static enums.Responses.Response.ProfileMenu.*;

public class UserControllerTest {

    User user = new User("username", "password", "niki");

    @Test
    public void testIsPasswordStrong() {
        Assert.assertFalse(UserController.isPasswordStrong("password"));
        Assert.assertFalse(UserController.isPasswordStrong("password1"));
        Assert.assertFalse(UserController.isPasswordStrong("password1%"));
        Assert.assertTrue(UserController.isPasswordStrong("Password123%"));
    }

    @Test
    public void testChangePassword() {
        String oldPass = "password";
        String newPass = "pP123123%";
        UserController.setCurrentUser(user);
        Assert.assertEquals(UserController.changePassword("wrongPass", "pass"), Response.ProfileMenu.WRONG_OLD_PASSWORD);
        Assert.assertEquals(UserController.changePassword(oldPass, " sadf sadf sadf"), INVALID_NEW_PASSWORD_FORMAT);
        Assert.assertEquals(UserController.changePassword(oldPass, "123"), WEAK_NEW_PASSWORD);
        Assert.assertEquals(UserController.changePassword(oldPass, "pP123123%"), SUCCESSFUL_PASSWORD_CHANGE);
    }


    @Test
    public void testNicknameChange() {
        UserController.setCurrentUser(user);
        UserController.addUser("ali", "ali", "ali");
        Assert.assertEquals(UserController.changeNickname(" "), INVALID_NICKNAME_FORMAT);
        Assert.assertEquals(UserController.changeNickname("ali"), NICKNAME_EXISTS);
        Assert.assertEquals(UserController.changeNickname("jkdshfas"), SUCCESSFUL_NICKNAME_CHANGE);
    }

    @Test
    public void logoutTest() {
        Assert.assertEquals(UserController.logout(), Response.MainMenu.SUCCESSFUL_LOGOUT);
    }

    @Test
    public void registerTest() {
        UserController.addUser("ali", "ali", "ali");
        Assert.assertEquals(UserController.register("", "", "", null), Response.LoginMenu.INVALID_USERNAME_FORMAT);
        Assert.assertEquals(UserController.register("skljafd", "asdfasd", "", null), Response.LoginMenu.INVALID_NICKNAME_FORMAT);
        Assert.assertEquals(UserController.register("skljafd", "", "asdfasdf", null), Response.LoginMenu.INVALID_PASSWORD_FORMAT);
        Assert.assertEquals(UserController.register("ali", "asdf", "asdfasdf", null), Response.LoginMenu.USERNAME_EXISTS);
    }

    @Test
    public void LoginTest() {
        UserController.addUser("ali", "ali", "ali");
        Assert.assertEquals(UserController.login("ali", "ali", ""), Response.LoginMenu.LOGIN_SUCCESSFUL);
        Assert.assertEquals(UserController.login("ali", "ali34", ""), Response.LoginMenu.USERNAME_PASSWORD_DONT_MATCH);
    }
}
