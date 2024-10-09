package io.reactivestax;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HelperClass {
    Connection connection;

    public HelperClass(Connection connection) {
        this.connection = connection;
    }

    public void clearTable(String tableName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("TRUNCATE " + tableName)) {
            statement.execute();
        }
    }
}
