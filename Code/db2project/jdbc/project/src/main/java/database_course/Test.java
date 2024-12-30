package database_course;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import java.awt.Image;
import java.security.NoSuchAlgorithmException;
import javax.swing.ImageIcon; 


public class Test {

    public static void main( String[] args )
    {
        //Custom pop up window for database connection
        ImageIcon icon = new ImageIcon(Test.class.getResource("assets/dawson_icon.png"));  // Load dawson icon
        String userId = (String) JOptionPane.showInputDialog(null, "Enter your Dawson College student id: ", "DawsCorp Database Login", JOptionPane.INFORMATION_MESSAGE, icon, null, "");

        //Second image 
        ImageIcon icon2 = new ImageIcon(Test.class.getResource("assets/password.png")); //load password icon
        Image img = icon2.getImage();  //transform it
        Image newImg = img.getScaledInstance(40, 40,  java.awt.Image.SCALE_SMOOTH);  //scale it the smooth way
        icon2 = new ImageIcon(newImg); //transform it back
        
        //Password handling
        JPasswordField pf = new JPasswordField();
        int option = JOptionPane.showOptionDialog(null, pf, "Enter your password: ", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, icon2, null, null);

        String userPassword = ""; 
        if (option == JOptionPane.OK_OPTION) {
            userPassword = new String(pf.getPassword());  // Get entered password
        } else {
            System.out.println("No password entered, exiting.");
            return;
        }

        //Connecting to DB
        String url = "jdbc:postgresql://cspostgres.dawsoncollege.qc.ca:5432/ritik_artem_project";
        Properties props = new Properties();
        props.setProperty("user", userId);
        props.setProperty("password", userPassword);

        //TESTING ADMIN PASSWORD HASHING
        try {
            // Connect to the database to validate user
            Connection conn = DriverManager.getConnection(url, props);
            System.out.println("Database connection successful for user: " + userId);

            // Simulate retrieving the stored password hash for validation
            String storedPasswordHashQuery = "SELECT hash, salt FROM task_management_project.employee WHERE employee_name = ?";
            PreparedStatement statement = conn.prepareStatement(storedPasswordHashQuery);
            statement.setString(1, userId);  // Assuming userId is used to fetch the admin's password hash
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                byte[] storedPasswordHash = result.getBytes("hash");
                byte[] storedSalt = result.getBytes("salt");

                // Validate the entered password by comparing the entered hash with the stored hash
                boolean isValid = PasswordManager.validatePassword(userPassword.toCharArray(), storedPasswordHash, storedSalt);

                if (isValid) {
                    System.out.println("Password is correct! Proceeding with database connection.");
                    // Continue with further database operations
                } else {
                    System.out.println("Invalid password.");
                }
            } else {
                System.out.println("No user found with ID: " + userId);
            }

            statement.close();
            conn.close();

        } 
        catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        } 
        catch (NoSuchAlgorithmException e) {
            System.err.println("Error while hashing password: " + e.getMessage());
        }


        // //TESTING FUNCTION CALLS
        // try {
        //     // Connect
        //     Connection conn = DriverManager.getConnection(url, props);
        
        //     // Issue a query and store its results
        //     PreparedStatement statement = conn.prepareStatement("SELECT * FROM task_management_project.employee;"); 
        //     ResultSet result = statement.executeQuery();
            
        //     // Loop through the results and print the employee name for each result
        //     System.out.println("Employee Names:");
        //     while(result.next()) {
        //         System.out.println(result.getString("employee_name"));
        //     }
            
        //     // Close the result set
        //     result.close();
            
        //     // Initialize SQLManager with connection
        //     SQLManager sqlManager = new SQLManager(conn);
            
        //     //TESTS

        //     // Test: Find employee info
        //     System.out.println("\n--- Test: Find Employee Info ---");
        //     System.out.println(sqlManager.findEmployeeInfo("db_kid"));
            
        //     // Test: Calculate sprint duration
        //     System.out.println("\n--- Test: Calculate Sprint Duration ---");
        //     System.out.println(sqlManager.calculateSprintDuration(1));
        //     // Test: Create a new sprint (commented out because it requires valid parameters)
        //     //System.out.println("\n--- Test: Create Sprint ---");
        //     // sqlManager.create_sprint("test_sprint", 1);  // This will be a successful message from stored procedure
        
        //     // // Test: Find sprint ID
        //     // System.out.println("\n--- Test: Find Sprint ID ---");
        //     // int sprintId = sqlManager.findSprintId("test_sprint", 1);
        //     // System.out.println("Sprint ID for 'test_sprint': " + sprintId);
        
        //     // // Test: Start sprint
        //     // System.out.println("\n--- Test: Start Sprint ---");
        //     // sqlManager.start_sprint(sprintId);
        
        //     // // Test: End sprint
        //     // System.out.println("\n--- Test: End Sprint ---");
        //     // sqlManager.end_sprint(sprintId);
        
        //     // // Test: Find team sprint information
        //     // System.out.println("\n--- Test: Find Team Sprint ---");
        //     // System.out.println(sqlManager.find_team_sprint("test_sprint", 1));
        
        //     // Test: Create new ticket
        //     System.out.println("\n--- Test: Create New Ticket ---");
        //     String createTicketResult = sqlManager.createNewTicket("New Bug Ticket", "This is a new bug ticket", "Bug", "db_kid");
        //     System.out.println(createTicketResult);  // Should print the result of ticket creation

        //     // Test: Modify ticket
        //     System.out.println("\n--- Test: Modify Ticket ---");
        //     String modifyTicketResult = sqlManager.modifyTicket(1, "Modified Ticket Title", "Updated description", "Task", null);
        //     System.out.println(modifyTicketResult);  // Should print the result of ticket modification

        //     // Test: Assign ticket to user
        //     System.out.println("\n--- Test: Assign Ticket ---");
        //     String assignTicketResult = sqlManager.assignTicket(1, "binary_ben");
        //     System.out.println(assignTicketResult);  // Should print the result of ticket assignment

        //     // Test: Complete ticket
        //     System.out.println("\n--- Test: Complete Ticket ---");
        //     String completeTicketResult = sqlManager.completeTicket(1, "feature_branch_123");
        //     System.out.println(completeTicketResult);  // Should print the result of ticket completion
        
        //     // Close the statement and connection
        //     statement.close();
        //     conn.close();
        
        // } catch (SQLException e) {
        //     System.err.println(e.toString());
        // }
        
    }
    
}
