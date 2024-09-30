package io.reactivestax;

import com.zaxxer.hikari.HikariDataSource;
import io.reactivestax.hikari.DataSource;

import java.sql.*;
import java.util.concurrent.LinkedBlockingQueue;

import static io.reactivestax.PositionRepository.*;

public class TradeProcessor implements Runnable {
    public LinkedBlockingQueue<String> queue;
    static Connection connection;
    private final HikariDataSource dataSource = DataSource.getDataSource();

    static {
        try {
            connection = DataSource.getConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TradeProcessor(LinkedBlockingQueue<String> queue) throws Exception {
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
                JournalEntry journalEntry = new JournalEntry(payloads[0], payloads[1], payloads[2], payloads[3], payloads[4], Integer.parseInt(payloads[5]), Double.parseDouble(payloads[6]), Integer.parseInt(payloads[5]));
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
                processPosition(journalEntry);
                System.out.println("insertionResult "+ i);
            }

        }
    }


    // Process each position with optimistic locking and retry logic
    public void processPosition(JournalEntry journalEntry) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            try {
                // Step 1: Check if cusip exists
                int version = getCusipVersion(connection, journalEntry);

                if (version == -1) {
                    // Step 2: If no positions exists, insert it
                    insertPosition(connection, journalEntry);
                } else {
                    updatePosition(connection, journalEntry, version);
                }
            } catch (
                    OptimisticLockingException e) {
                System.err.println(e.getMessage() + journalEntry.getPosition());
//                creditCardTransactionQueue.put(journalEntry);
//                adjustTransactionQueue(journalEntry);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
