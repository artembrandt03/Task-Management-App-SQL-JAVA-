package database_course;

import java.sql.*;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;


public class Admin {
    
    public static void main(String[] args) {
        //First admin - Artem
        String employeeName1 = "2139953";  
        String email1 = "artem.brandt@dawscorp.qc.ca";  
        String fullName1 = "Artem Brandt";  
        String gitlabUsername1 = "@ArtemBrandt";  
        String employeeType1 = "administrator";  
        String plainPassword1 = "Admin123";  

        //Second admin - Ritik
        String employeeName2 = "2338322";  
        String email2 = "ritik.daswanidawscorp.qc.ca";  
        String fullName2 = "Ritik Daswani";  
        String gitlabUsername2 = "@RitikDaswani";  
        String employeeType2 = "administrator";  
        String plainPassword2 = "Admin123"; 

        try {
            // Hash the password (this method is in PasswordManager.java)
            byte[] hashedPassword1 = PasswordManager.hashPassword(plainPassword1.toCharArray());
            byte[] salt1 = new byte[16];

            // Set up the database connection
            String url = "jdbc:postgresql://cspostgres.dawsoncollege.qc.ca:5432/2139953";
            Properties props = new Properties();
            props.setProperty("user", "2139953");  // Use your student id here
            props.setProperty("password", "");  // Use your password here
            Connection conn = DriverManager.getConnection(url, props);

            // SQL query to insert the hashed password into the admin table
            String sql = "INSERT INTO task_management_project.employee (employee_name, email, full_name, gitlab_username, employee_type, hash, salt) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);

            // Set the values for the employee fields
            statement.setString(1, employeeName1);  // Set the employee's username (your student ID)
            statement.setString(2, email1);  // Set the employee's email
            statement.setString(3, fullName1);  // Set your full name
            statement.setString(4, gitlabUsername1);  // Set the GitLab username (nullable)
            statement.setString(5, employeeType1);  // Set the employee type (administrator or contributor)
            statement.setBytes(6, hashedPassword1);  // Set the hashed password (with salt)
            statement.setBytes(7, salt1);  // Set the salt (this should match the one used during hashing)

            // Execute the insertion for the first admin
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Employee 1 (Artem Brandt) created successfully!");
            }

            byte[] hashedPassword2 = PasswordManager.hashPassword(plainPassword2.toCharArray());
            byte[] salt2 = new byte[16];  // Salt for the second admin

            //Insert the second admin into the employee table
            statement = conn.prepareStatement(sql);
            statement.setString(1, employeeName2);  // Set the second employee's username (second student ID)
            statement.setString(2, email2);  // Set the second employee's email
            statement.setString(3, fullName2);  // Set the second employee's full name
            statement.setString(4, gitlabUsername2);  // Set the second employee's GitLab username (optional)
            statement.setString(5, employeeType2);  // Set the second employee's type (administrator)
            statement.setBytes(6, hashedPassword2);  // Set the hashed password (with salt)
            statement.setBytes(7, salt2);  // Set the salt (this should match the one used during hashing)

            //Execute the insertion for the second admin
            rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Employee 2 (Ritik Daswani) created successfully!");
            }

            updateEmployeesWithDefaultPassword(conn);

            //Close the connection and statement
            statement.close();
            conn.close();

        } 
        catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        } 
        catch (NoSuchAlgorithmException e) {
            System.err.println("Error while hashing password: " + e.getMessage());
        }
    }

    // Method to update all employees with a default password
    private static void updateEmployeesWithDefaultPassword(Connection conn) throws SQLException, NoSuchAlgorithmException {
        // Default password for all employees
        String defaultPassword = "employee12345";
        
        // Hash the default password
        byte[] hashedPassword = PasswordManager.hashPassword(defaultPassword.toCharArray());
        byte[] salt = new byte[16];  // You can generate a proper salt here (e.g., using SecureRandom)

        // SQL query to update employees with hashed password and salt
        String sql = "UPDATE task_management_project.employee SET hash = ?, salt = ? WHERE hash IS NULL";

        //Prepare the statement
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setBytes(1, hashedPassword);  // Set the hashed password
        statement.setBytes(2, salt);  // Set the salt
        // Execute the update
        int rowsUpdated = statement.executeUpdate();
        
        // Check how many rows were updated
        if (rowsUpdated > 0) {
            System.out.println("All employees' passwords have been updated successfully with a deafult password!");
        } else {
            System.out.println("No employees were updated.");
        }
    }
}
