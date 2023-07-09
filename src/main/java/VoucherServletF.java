import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.logging.Logger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.*;

@WebServlet(name = "VoucherServletF", urlPatterns = "/getVouchersF")
public class VoucherServletF extends HttpServlet {
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
                    long serial = getVoucher(voucherPIN);
                    writer.write(String.format("{\"voucher_serial\":\"%d\"}", serial));
                } catch (SQLException e) {
                    writer.write(String.format("{\"ERROR!\":\"%s\"}", e.toString()));
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
                    boolean result = insertVoucher(voucher);
                    writer.write(String.format("{\"result\":\"%b\"}", result));
                } catch (SQLException e) {
                    writer.write(String.format("{\"ERROR!\":\"%s\"}", e.toString()));
                }
                break;
            case "update":
                long voucherSerial = json.getLong("voucher_serial");
                voucherPIN = json.getInt("voucher_pin");
                try {
                    int result = updateVoucher(voucherSerial, voucherPIN);
                    writer.write(String.format("{\"result\":\"%d\"}", result));
                } catch (SQLException e) {
                    writer.write(String.format("{\"ERROR!\":\"%s\"}", e.toString()));
                }
                break;
            case "delete":
                voucherPIN = json.getInt("voucher_pin");
                try {
                    int result = deleteVoucher(voucherPIN);
                    writer.write(String.format("{\"result\":\"%d\"}", result));
                } catch (SQLException e) {
                    writer.write(String.format("{\"ERROR!\":\"%s\"}", e.toString()));
                }
                break;
            case "insert_batch":
                JSONArray jsonArray = json.getJSONArray("vouchers");
                int countInserted = 0;
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        voucher = new Voucher(
                            obj.getLong("voucher_serial"),
                            obj.getInt("voucher_pin"),
                            obj.getInt("voucher_package"),
                            Timestamp.valueOf(obj.getString("created_at")),
                            obj.getString("created_by"));
                        countInserted += (insertVoucher(voucher)? 1: 0);
                    } catch (Exception e) {
                        writer.write(String.format("{\"ERROR!\":\"%s\"}", e.toString()));
                    }
                }
                writer.write(String.format("{\"result\":\"%d\"}", countInserted));
                break;
        }
    }

    private long getVoucher(int voucherPIN) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, password)){
            try (CallableStatement callableStatement = connection.prepareCall("{? = call get_voucher(?)}")) {
                callableStatement.registerOutParameter(1, Types.BIGINT);
                callableStatement.setInt(2, voucherPIN);
                callableStatement.execute();
                return callableStatement.getLong(1);
            }
        }
    }

    private boolean insertVoucher(Voucher voucher) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, password)){
            try (CallableStatement callableStatement = connection.prepareCall("{? = call add_voucher(?, ?, ?, ?, ?::timestamp)}")) {
                callableStatement.registerOutParameter(1, Types.BOOLEAN);
                callableStatement.setLong(2, voucher.voucher_serial);
                callableStatement.setInt(3, voucher.voucher_pin);
                callableStatement.setInt(4, voucher.voucher_package);
                callableStatement.setString(5, voucher.created_by);
                callableStatement.setTimestamp(6, voucher.created_at);
                callableStatement.execute();
                return callableStatement.getBoolean(1);
            }
        }
    }

    private int updateVoucher(long voucherSerial, int voucherPIN) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, password)){
            try (CallableStatement callableStatement = connection.prepareCall("{? = call update_voucher(?, ?)}")) {
                callableStatement.registerOutParameter(1, Types.INTEGER);
                callableStatement.setLong(2, voucherSerial);
                callableStatement.setInt(3, voucherPIN);
                callableStatement.execute();
                return callableStatement.getInt(1);
            }
        }
    }

    private int deleteVoucher(int voucherPIN) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, password)){
            try (CallableStatement callableStatement = connection.prepareCall("{? = call delete_voucher(?)}")) {
                callableStatement.registerOutParameter(1, Types.INTEGER);
                callableStatement.setInt(2, voucherPIN);
                callableStatement.execute();
                return callableStatement.getInt(1);
            }
        }
    }
}
