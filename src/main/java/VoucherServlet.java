import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.*;

@WebServlet(name = "VoucherServlet", urlPatterns = "/getVouchers")
public class VoucherServlet extends HttpServlet {
    private static String url = "jdbc:postgresql://localhost:5432/postgres";
    private static String user = "postgres";
    private static String password = "12345";
    //select GET insert POST delete DELETE update PUT
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader =  request.getReader();
        JSONTokener tokener = new JSONTokener(reader);
        JSONObject json = new JSONObject(tokener);
        String requestType = json.getString("operation");
        response.setHeader("content-type", "application/json");
        PrintWriter writer =  response.getWriter();
        switch(requestType.toLowerCase()) {
            case "select":
                int voucherPIN = json.getInt("voucher_pin");
                try {
                    Voucher voucher = getVoucher(voucherPIN);
                    writer.write(
                        String.format(
                            "{ \"voucher_serial\":\"%d\", \"voucher_pin\":\"%d\", \"voucher_package\":\"%d\", \"created_at\":\"%s\", \"created_by\":\"%s\"}", 
                            voucher.voucher_serial,
                            voucher.voucher_pin,
                            voucher.voucher_package,
                            voucher.created_at.toString(),
                            voucher.created_by
                        )
                    );
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            case "insert":
                Voucher voucher = new Voucher(
                    json.getLong("voucher_serial"),
                    json.getInt("voucher_pin"),
                    json.getInt("voucher_package"),
                    Timestamp.valueOf(json.getString("created_at")),
                    json.getString("created_by"));
                try {
                    int result = insertVoucher(voucher);
                    writer.write(String.format("{\"result\":\"%d\"}", result));
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            case "update":
                long voucherSerial = json.getLong("voucher_serial");
                voucherPIN = json.getInt("voucher_pin");
                try {
                    int result = updateVoucher(voucherSerial, voucherPIN);
                    writer.write(String.format("{\"result\":\"%d\"}", result));
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            case "delete":
                voucherPIN = json.getInt("voucher_pin");
                try {
                    int result = deleteVoucher(voucherPIN);
                    writer.write(String.format("{\"result\":\"%d\"}", result));
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
        }
    }

    private Voucher getVoucher(int voucherPIN) throws SQLException {
        final String selectQuery = "select * from voucher where voucher_pin = ?";
        try (Connection connection = DriverManager.getConnection(url, user, password)){
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
                preparedStatement.setInt(1, voucherPIN);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    Voucher voucher = new Voucher(
                        resultSet.getLong("voucher_serial"), 
                        resultSet.getInt("voucher_pin"), 
                        resultSet.getInt("voucher_package"), 
                        resultSet.getTimestamp("created_at"), 
                        resultSet.getString("created_by")
                    );
                    return voucher;
                }
                else {
                    return new Voucher();
                }
            }
        }
    }

    private int insertVoucher(Voucher voucher) throws SQLException {
        final String insertQuery = "insert into voucher" +
            "  (voucher_serial, voucher_pin, voucher_package, created_at, created_by) values " +
            " (?, ?, ?, ?, ?);";
        try (Connection connection = DriverManager.getConnection(url, user, password)){
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                preparedStatement.setLong(1, voucher.voucher_serial);
                preparedStatement.setInt(2, voucher.voucher_pin);
                preparedStatement.setInt(3, voucher.voucher_package);
                preparedStatement.setTimestamp(4, voucher.created_at);
                preparedStatement.setString(5, voucher.created_by);
                return preparedStatement.executeUpdate();
            }
        }
    }

    private int updateVoucher(long voucherSerial, int voucherPIN) throws SQLException {
        final String updateQuery = "update voucher set voucher_serial = ? where voucher_pin = ?";
        try (Connection connection = DriverManager.getConnection(url, user, password)){
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                preparedStatement.setLong(1, voucherSerial);
                preparedStatement.setInt(2, voucherPIN);
                return preparedStatement.executeUpdate();
            }
        }
    }

    private int deleteVoucher(int voucherPIN) throws SQLException {
        final String deleteQuery = "delete from voucher where voucher_pin = ?";
        try (Connection connection = DriverManager.getConnection(url, user, password)){
            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
                preparedStatement.setInt(1, voucherPIN);
                return preparedStatement.executeUpdate();
            }
        }
    }
}
