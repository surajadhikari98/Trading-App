package io.reactivestax.hikari;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Isolation {

    public static void readCommited(String studentName, String courseName) throws Exception {
        String courseSelectQuery = "SELECT course_id FROM Courses WHERE course_name = ?";
        String studentSelectQuery = "SELECT student_id FROM Students WHERE student_name = ?";
        String enrollmentQuery = " INSERT  INTO Enrollments (course_id, student_id) VALUES (?, ?)";

        try (Connection connection = DataSource.getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            PreparedStatement enrollmentStatement = connection.prepareStatement(enrollmentQuery);
            PreparedStatement courseSelectStatement = connection.prepareStatement(courseSelectQuery);
            PreparedStatement studentSelectStatement = connection.prepareStatement(studentSelectQuery);

            //Begin Transaction
            connection.setAutoCommit(false);

            courseSelectStatement.setString(1, courseName);
            studentSelectStatement.setString(1, studentName);
            ResultSet resultSet = courseSelectStatement.executeQuery();
            ResultSet resultSet1 = studentSelectStatement.executeQuery();
            try {
                if(resultSet1.next() && resultSet.next()) {
                    enrollmentStatement.setInt(1, resultSet.getInt("course_id"));
                    enrollmentStatement.setInt(2, resultSet1.getInt("student_id"));
                    enrollmentStatement.addBatch();
                    int[] ints = enrollmentStatement.executeBatch();
//                    connection.commit();
                    System.out.println("Enrollments is added." + ints[0]);
                }
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void readSerilizable() throws Exception {
        // Step 1: Establish a connection
        Connection connection = DataSource.getConnection();

        // Step 2: Set the isolation level to SERIALIZABLE
        connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        connection.setAutoCommit(false); // Start a transaction

        // Step 3: Execute a SELECT query
        String query = "SELECT * FROM Students WHERE student_id > 1";
        PreparedStatement stmt = connection.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();

        // Display the selected rows
        System.out.println("Session 1: Reading data...");
        while (rs.next()) {
            System.out.println("ID: " + rs.getInt("student_id") + ", Name: " + rs.getString("student_name"));
        }

        // Hold this transaction without committing, simulating a long-running transaction
        System.out.println("Session 1: Holding transaction. Insert data in Session 2 now.");
        Thread.sleep(2000000); // Sleep for 2 minutes to allow time for Session 2 to execute

        // Step 5: Commit the transaction after sleep
        connection.commit();
        System.out.println("Session 1: Transaction committed.");

        // Close the connection
        rs.close();
        stmt.close();
        connection.close();

    }

    public static void main(String[] args) throws Exception {
//        readCommited("John", "Science");
        readSerilizable();
    }
}
