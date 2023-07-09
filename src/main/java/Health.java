import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "CheckHealth", urlPatterns = "/getStaticJson")
public class Health extends HttpServlet {
    private static String url = "jdbc:postgresql://localhost:5432/postgres";
    private static String user = "postgres";
    private static String password = "12345";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("content-type", "application/json");
        PrintWriter writer = response.getWriter();
        writer.write(String.format("{\"voucher_serial\":\"%d\"}", 123));
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (CallableStatement callableStatement = connection.prepareCall("{? := call get_voucher(?)}")) {
                /*callableStatement.registerOutParameter(1, Types.BIGINT);
                callableStatement.setInt(2, voucherPIN);
                callableStatement.execute();
                return callableStatement.getLong(1);*/
            }
        }
        catch(SQLException e)
        {
            writer.write("{\"ERROR!\":\"Couldn't connect to DB!\"}");
            writer.write(String.format("{\"ERROR!\":\"%s\"}", e.toString()));
        }
    }

}
