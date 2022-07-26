package Server;

import java.sql.*;

public class DB {
    /**
     * Connect to a sample database
     */


    private Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:src/main/resources/login";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void insert(String username, String password) {
        String sql = "INSERT INTO User (username,password) VALUES(?,?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setToken(String username, String token) {
        String sql = "UPDATE User SET token = ? WHERE username = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, token);
            pstmt.setString(2, username);
            // execute the delete statement
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public String getPass(String username) {
        String sql = "SELECT password FROM User WHERE username='" + username + "'";

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // loop through the result set
            if (!rs.next()) return null;
            return rs.getString("password");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }


    public void delete(String username) {
        String sql = "DELETE FROM User WHERE id = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, username);
            // execute the delete statement
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void replace(String username, String password) {
        String sql = "UPDATE User SET password = ? WHERE username = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, password);
            pstmt.setString(2, username);
            // execute the delete statement
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DB app = new DB();
        // insert three new rows
        app.replace("parsa", "joojoo123");
        System.out.println(app.getPass("parsa"));
    }
}
