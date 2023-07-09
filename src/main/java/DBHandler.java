import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class DBHandler {
    private static String url = "jdbc:postgresql://localhost:5432/postgres";
    private static String user = "postgres";
    private static String password = "12345";

    private static String selectQuery = "select voucher_serial, voucher_pin from voucher where voucher_package = ?";

    public static HashMap<Long, Integer> performSQLSelectQuery(int packageID) throws SQLException {

        try (Connection connection = DriverManager.getConnection(url, user, password)){
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
                preparedStatement.setInt(1, packageID);
                ResultSet resultSet = preparedStatement.executeQuery();
                System.out.println("voucher_serial     voucher_pin");
                HashMap<Long, Integer> vouchers = new HashMap<>();
                while (resultSet.next()) {
                    vouchers.put(resultSet.getLong("voucher_serial"), resultSet.getInt("voucher_pin"));
                    System.out.print(resultSet.getLong("voucher_serial") + "         ");
                    System.out.println(resultSet.getInt("voucher_pin"));
                }
                return vouchers;
            }
        }
    }
}
