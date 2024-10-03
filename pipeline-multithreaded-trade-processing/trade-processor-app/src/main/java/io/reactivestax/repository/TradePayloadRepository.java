package io.reactivestax.repository;

import io.reactivestax.contract.repository.PayloadRepository;
import io.reactivestax.hikari.DataSource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.PreparedStatement;

import static io.reactivestax.utils.Utility.checkValidity;

public class TradePayloadRepository implements PayloadRepository {
    @Override
    public String[] insertTradeIntoTradePayloadTable(String filePath) throws Exception {
        String insertQuery = "INSERT INTO trade_payloads (trade_id, status, status_reason, lookup_status, posted_status, position_status, payload) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String[] split = new String[0];

        try(PreparedStatement statement = DataSource.getConnection().prepareStatement(insertQuery)) {
            String line;
            statement.setString(4, "");
            statement.setString(5, "");
            statement.setString(5, "");
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    split = line.split(",");
                    statement.setString(1, split[0]);
                    statement.setString(2, checkValidity(split) ? "valid" : "inValid");
                    statement.setString(3, checkValidity(split) ? "All field present " : "Fields missing");
                    statement.setString(6, line);
                    statement.executeUpdate();
                    System.out.println("successfully inserted into db = " + reader);
//                    writeToTradeQueue(split);
                }
            }
        }
        return split;
    }
}
