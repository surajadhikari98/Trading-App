package io.reactivestax.repository.jdbc;

import io.reactivestax.contract.repository.SecuritiesReferenceRepository;
import io.reactivestax.utils.DBUtils;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JDBCSecuritiesReferenceRepository implements SecuritiesReferenceRepository {
    private static JDBCSecuritiesReferenceRepository instance;

    public static synchronized JDBCSecuritiesReferenceRepository getInstance() {
        if (instance == null) {
            instance = new JDBCSecuritiesReferenceRepository();
        }
        return instance;
    }


    @Override
    public boolean lookupSecurities(String cusip) throws FileNotFoundException, SQLException {
        String lookupQueryForSecurity = "SELECT 1 FROM securities_reference WHERE cusip = ?";
        try (Connection connection = DBUtils.getInstance().getConnection();
             PreparedStatement lookUpStatement = connection.prepareStatement(lookupQueryForSecurity);) {
            lookUpStatement.setString(1, cusip);
            return lookUpStatement.executeQuery().next();
        }
    }
}
