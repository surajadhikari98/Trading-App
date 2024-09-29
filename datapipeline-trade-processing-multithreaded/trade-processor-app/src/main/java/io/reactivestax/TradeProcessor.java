package io.reactivestax;

import io.reactivestax.hikari.DataSource;

import java.sql.*;
import java.util.concurrent.LinkedBlockingQueue;

public class TradeProcessor implements Runnable {
    public LinkedBlockingQueue<String> queue;
    static Connection connection;

    static {
        try {
            connection = DataSource.getConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TradeProcessor(LinkedBlockingQueue<String> queue){
        this.queue =queue;
    }

    @Override
    public void run() {
        try {
            readFromQueueAndQueryPayload();
        } catch (InterruptedException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void readFromQueueAndQueryPayload() throws InterruptedException, SQLException {
        while(!this.queue.isEmpty()) {
            String tradeId = this.queue.take();
            String lookupQuery = "SELECT payload FROM trade_payloads WHERE trade_id = ?";
            String insertQuery = "INSERT INTO journal_entries (trade_id, trade_date, account_number,cusip,direction, quantity, price) VALUES (?, ?, ?, ?, ?, ?, ?)";
            String lookupQueryForSecurity = "SELECT * FROM securities_reference WHERE cusip = ?";
            PreparedStatement stmt = connection.prepareStatement(lookupQuery);
            PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
            PreparedStatement lookUpStatement = connection.prepareStatement(lookupQueryForSecurity);
            stmt.setString(1, tradeId);
            ResultSet resultSet = stmt.executeQuery();
            if(resultSet.next()) {
                String payload = resultSet.getString(1);
                String[] payloads = payload.split(",");
//                System.out.println("result journal"+ payload);
                lookUpStatement.setString(1,payloads[3]);
                ResultSet lookUpResult = lookUpStatement.executeQuery();
                if(!lookUpResult.next()) {
                    System.out.println("No security found....");
                    continue;
                }
                insertStatement.setString(1, payloads[0]);
                insertStatement.setString(2, payloads[1]);
                insertStatement.setString(3, payloads[2]);
                insertStatement.setString(4, payloads[3]);
                insertStatement.setString(5, payloads[4]);
                insertStatement.setString(6, payloads[5]);
                insertStatement.setString(7, payloads[6]);
                int i = insertStatement.executeUpdate();
                System.out.println("insertionResult "+ i);
            }

        }
    }


}
