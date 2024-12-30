package database_course;

import java.awt.Image;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class App 
{
    private static Scanner reader = new Scanner(System.in);
    private static String employeeName = "";

    //Main method
    public static void main(String[] args) {
        SQLManager sqlManager = null;
        boolean appOn = true;
        while (appOn) {
            // 1: ADMIN LOGIN

            // Custom pop up window for database connection
            ImageIcon icon = new ImageIcon(Test.class.getResource("assets/dawson_icon.png"));  // Load dawson icon
            String userId = (String) JOptionPane.showInputDialog(null, "Enter your Dawson College student id: ", "DawsCorp Database Login", JOptionPane.INFORMATION_MESSAGE, icon, null, "");

            // Second image 
            ImageIcon icon2 = new ImageIcon(Test.class.getResource("assets/password.png")); // load password icon
            Image img = icon2.getImage();  // transform it
            Image newImg = img.getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);  // scale it the smooth way
            icon2 = new ImageIcon(newImg); // transform it back

            // Password handling
            JPasswordField pf = new JPasswordField();
            int option = JOptionPane.showOptionDialog(null, pf, "Enter your password: ", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, icon2, null, null);

            String userPassword = "";
            if (option == JOptionPane.OK_OPTION) {
                userPassword = new String(pf.getPassword());  // Get entered password
            } else {
                System.out.println("No password entered, exiting.");
                return;
            }

            // Connecting to DB
            String url = "jdbc:postgresql://cspostgres.dawsoncollege.qc.ca:5432/2139953";
            Properties props = new Properties();
            props.setProperty("user", userId);
            props.setProperty("password", userPassword);

            // Declare variables for connection and password validation
            Connection conn = null;
            byte[] storedPasswordHash = null;
            byte[] storedSalt = null;
            boolean isValid = false;

            // ADMIN PASSWORD HASHING
            try {
                // Connect to the database to validate user
                conn = DriverManager.getConnection(url, props);
                sqlManager = new SQLManager(conn);
                System.out.println("Database connection successful for user: " + userId);

                // Retrieving the stored password hash for validation
                String storedPasswordHashQuery = "SELECT hash, salt, employee_type FROM task_management_project.employee WHERE employee_name = ?";
                PreparedStatement statement = conn.prepareStatement(storedPasswordHashQuery);
                statement.setString(1, userId);  // Assuming userId is used to fetch the admin's password hash
                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    storedPasswordHash = result.getBytes("hash");
                    storedSalt = result.getBytes("salt");
                    String employeeType = result.getString("employee_type");

                    // Validate the entered password by comparing the entered hash with the stored hash
                    isValid = PasswordManager.validatePassword(userPassword.toCharArray(), storedPasswordHash, storedSalt);

                    if (isValid) {
                        System.out.println("Password is correct! Proceeding with database connection.");
                        employeeName = userId;

                        // Ask user if they want to stay as admin or switch
                        System.out.println("You are logged in as: " + employeeType);
                        System.out.println("Would you like to:");
                        System.out.println("1. Stay as " + employeeType);
                        System.out.println("2. Log in as a different employee (Contributor)");

                        int choice = Integer.parseInt(reader.nextLine());
                        if (choice == 1) {
                            // Continue as an admin
                        } 
                        else if (choice == 2) {
                            // Fetch all employee names that are contributors
                            System.out.println("Fetching employee list...");
                            String fetchContributorsQuery = "SELECT employee_name FROM task_management_project.employee WHERE employee_type = 'contributor'";

                            // Create a scrollable ResultSet
                            try (PreparedStatement stmt = conn.prepareStatement(fetchContributorsQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                                 ResultSet rs = stmt.executeQuery()) {
                            
                                System.out.println("Select a Contributor from the list:");
                            
                                // Display the list of contributor names
                                int index = 1;
                                while (rs.next()) {
                                    String employeeName = rs.getString("employee_name");
                                    System.out.println(index + ". " + employeeName);
                                    index++;
                                }
                            
                                // Prompt user to select an employee
                                System.out.println("Enter the number of the employee you want to log in as:");
                                int contributorChoice = Integer.parseInt(reader.nextLine());
                            
                                // Retrieve the selected contributor's name
                                String selectedEmployee = "";
                                rs.beforeFirst();  // Reset the ResultSet cursor to the beginning
                                index = 1;
                                while (rs.next()) {
                                    if (index == contributorChoice) {
                                        selectedEmployee = rs.getString("employee_name");
                                        break;
                                    }
                                    index++;
                                }
                            
                                if (selectedEmployee.isEmpty()) {
                                    System.out.println("Invalid choice.");
                                    break;
                                }
                            
                                // Prompt for the password for the selected contributor
                                System.out.println("Enter the password for " + selectedEmployee + ":");
                                pf = new JPasswordField();
                                option = JOptionPane.showOptionDialog(null, pf, "Enter your password: ", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, icon2, null, null);
                            
                                String contributorPassword = "";
                                if (option == JOptionPane.OK_OPTION) {
                                    contributorPassword = new String(pf.getPassword());  // Get entered password
                                } else {
                                    System.out.println("No password entered, exiting.");
                                    return;
                                }
                            
                                // Validate the password for the selected contributor
                                String passwordValidationQuery = "SELECT hash, salt FROM task_management_project.employee WHERE employee_name = ?";
                                PreparedStatement validationStmt = conn.prepareStatement(passwordValidationQuery);
                                validationStmt.setString(1, selectedEmployee);
                                ResultSet validationResult = validationStmt.executeQuery();
                            
                                if (validationResult.next()) {
                                    byte[] contributorStoredPasswordHash = validationResult.getBytes("hash");
                                    byte[] contributorStoredSalt = validationResult.getBytes("salt");
                            
                                    // Validate the entered password
                                    boolean contributorIsValid = PasswordManager.validatePassword(contributorPassword.toCharArray(), contributorStoredPasswordHash, contributorStoredSalt);
                            
                                    if (contributorIsValid) {
                                        System.out.println("Contributor login successful for: " + selectedEmployee);
                                        employeeName = selectedEmployee;  // Set the employee name as the logged-in user
                                    } else {
                                        System.out.println("Invalid password for contributor.");
                                        break;
                                    }
                                } else {
                                    System.out.println("Contributor not found or password is incorrect.");
                                }
                            
                            } catch (SQLException e) {
                                System.err.println("Database error: " + e.getMessage());
                            }
                        }
                        else {
                            break;
                        }
                    } 
                    else {
                        System.out.println("Invalid password.");
                        break;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Database connection error: " + e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                System.err.println("Error while hashing password: " + e.getMessage());
            }
            //MAIN APP LOGIC
            System.out.print("\033c");
            System.out.println(employeeName);

            sqlManager.toggleEmployeeOnlineStatus(employeeName);  // Toggle active status for the employee

            System.out.println("Got to the point where we can work in the application.");
            welcomeScreen(sqlManager);
        }
    }

    public static void welcomeScreen(SQLManager sqlManager) {
        System.out.println("Hello " + employeeName);
        System.out.println("Welcome to the Ticket Management System!");
        System.out.println("---------------------------------------------------------");
        welcomeScreenOptions(sqlManager);
    }
    public static void welcomeScreenOptions(SQLManager sqlManager) {
        System.out.println("What would you like to do?");
        System.out.println("1. View your Employee Info");
        System.out.println("2. View your teams");
        System.out.println("3. Exit");
        int choice = Integer.parseInt(reader.nextLine());
        if (choice == 1) {
            System.out.println(sqlManager.findEmployeeInfo(employeeName));
            System.out.println("---------------------------------------------------------");
            System.out.println("Click enter to continue.");
            reader.nextLine();
            System.out.print("\033c");
            welcomeScreenOptions(sqlManager);
        }
        else if (choice == 2) {
            showTeamsScreen(sqlManager);
            System.out.print("\033c");
        }
        else if (choice == 3) {
            sqlManager.toggleEmployeeOnlineStatus(employeeName); // Log the user as offline
            System.out.println("Exiting, please log in again.");
            employeeName = "";  // Reset employeeName to prompt for re-login
            return;  // Exit the loop to re-trigger login
        }
    }

    public static void showTeamsScreen(SQLManager sqlManager) {
        String allTeams = sqlManager.findEmployeeTeam(employeeName);
        String[] teams = allTeams.split("\n");
        if (teams.length != 1) {
            System.out.println("These are your teams: ");
            System.out.println("---------------------------------------------------------");
        }
        System.out.println(allTeams);
        teamScreenOptions(teams, sqlManager);
    }

    public static void teamScreenOptions(String[] teams, SQLManager sqlManager) {
        System.out.println("What would you like to do?");
        if (teams.length == 1) {
            System.out.println("1. View your teams sprints");
        }
        else{
            System.out.println("1. View a team's sprints");
        }
        System.out.println("2. Go back");
        System.out.println("3. Home page");
        int choice = Integer.parseInt(reader.nextLine());
        if (choice == 1) {
            String teamName = "";
            if (teams.length != 1) {
                System.out.println("Which team's sprints would you like to view?");
                int selectedTeam = Integer.parseInt(reader.nextLine());
                teamName = teams[selectedTeam - 1].split(". ")[1];
            }
            else {
                teamName = teams[0].split(". ")[1];
            }
            int teamId = sqlManager.getTeamId(teamName);
            sprintsScreen(teamId, sqlManager);
        }
        else if (choice == 2) {
            welcomeScreenOptions(sqlManager);
        }
        else if (choice == 3) {
            welcomeScreen(sqlManager);
        }
    }
    public static void sprintsScreen(int teamId, SQLManager sqlManager) {
        System.out.println("These are your team's sprints: ");
        String allSprints = sqlManager.findTeamSprint(teamId);
        System.out.println(allSprints);
        String[] sprints = allSprints.split("\n");
        System.out.println("---------------------------------------------------------");
        
        sprintScreenOptions(teamId, sprints, sqlManager);    
    }
    public static void sprintScreenOptions(int teamId, String[] sprints, SQLManager sqlManager) {
        System.out.println("What would you like to do?");
        System.out.println("1. View a sprint's tickets");
        System.out.println("2. Create Sprint");
        System.out.println("3. Delete Sprint");
        System.out.println("4. Edit Sprint");
        System.out.println("5. Go back to teams");
        System.out.println("6. Home page");
        int choice = Integer.parseInt(reader.nextLine());
        if (choice == 1) {
            int sprintId = chooseSprint(teamId, sprints, sqlManager);
            ticketsScreen(sprintId, sqlManager, teamId);
        }
        else if (choice == 2) {
            System.out.println("What would you like to name your sprint?");
            String sprintName = reader.nextLine();
            System.out.println("Sprint name: " + sprintName + " team id: " + teamId);
            sqlManager.createSprint(sprintName, teamId);
            System.out.print("Enter to continue.");
            reader.nextLine();
            System.out.print("\033c");
            sprintsScreen(teamId, sqlManager);
        }
        else if (choice == 3) {
            int sprintId = chooseSprint(teamId, sprints, sqlManager);
            System.out.println("Are you sure you want to delete this sprint?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            int choice2 = Integer.parseInt(reader.nextLine());
            if (choice2 == 1) {
                reader.nextLine();
                sqlManager.deleteSprint(sprintId);
                reader.nextLine();
                //TODO error confusion
            }
            else if (choice2 == 2) {
                sprintsScreen(teamId, sqlManager);
            }
        }
        else if (choice == 4) {
            int sprintId = chooseSprint(teamId, sprints, sqlManager);
            editSprintOptions(sprintId, sqlManager, teamId);
            sprintsScreen(teamId, sqlManager);
        }
        else if (choice == 5) {
            showTeamsScreen(sqlManager);
        }
        else if (choice == 6) {
            welcomeScreen(sqlManager);
        }
    }

    public static int chooseSprint(int teamId, String[] sprints, SQLManager sqlManager) {
        System.out.println("Which sprint?");
        int selectedSprint = Integer.parseInt(reader.nextLine());
        String sprintName = sprints[selectedSprint - 1];
        String[] parts = sprintName.split("\\|\\|"); 
        String sprintPart = parts[0].trim(); 
        sprintName = sprintPart.substring(sprintPart.indexOf(":") + 1).trim(); 
        
        return sqlManager.findSprintId(sprintName, teamId);
    }
    public static void ticketsScreen(int sprintId, SQLManager sqlManager, int teamId) {
        System.out.println("These are your team's tickets: ");
        String allTickets = sqlManager.getSprintTickets(sprintId);
        System.out.println(allTickets);
        String[] tickets = allTickets.split("\n");
        System.out.println("---------------------------------------------------------");
        ticketScreenOptions(sprintId, tickets, sqlManager, teamId);
    }

    public static void editSprintOptions(int sprintId, SQLManager sqlManager ,int teamId) {
        System.out.println("What would you like to do?");
        System.out.println("1. Start a sprint");
        System.out.println("2. End a sprint");
        System.out.println("3. Go back");
        int choice = Integer.parseInt(reader.nextLine());
        if (choice == 1) {
            sqlManager.startSprint(sprintId);
        }
        else if (choice == 2) {
            sqlManager.endSprint(sprintId);
        }
        else {
            return;
        }
    }
    public static void ticketScreenOptions(int sprintId, String[] tickets, SQLManager sqlManager, int teamId) {
        System.out.println("What would you like to do?");
        System.out.println("1. Change ticket Status");
        System.out.println("2. Create ticket");
        System.out.println("3. Delete ticket");
        System.out.println("4. View backlog tickets");
        System.out.println("5. Go back to sprints");
        System.out.println("6. Home page");
        int choice = Integer.parseInt(reader.nextLine());
        //TODO complete this function
        if (choice == 1) {
            int ticketId = chooseTicket(sprintId, tickets, sqlManager);
            System.out.println("1. Start");
            System.out.println("2. End");
            int choice2 = Integer.parseInt(reader.nextLine());
            if (choice2 == 1) {
                sqlManager.startTicket(ticketId);
            }
            else if (choice2 == 2) {
                sqlManager.endTicket(ticketId);
            }
        }
        else if (choice == 2) {
            System.out.println("What would you like to name your ticket?");
            String ticketName = reader.nextLine();
            System.out.println("Ticket name: " + ticketName + " sprint id: " + sprintId);
            System.out.println("What would you like to describe your ticket?");
            String description = reader.nextLine();
            System.out.println("What type of ticket is this?");
            System.out.println("1. Bug");
            System.out.println("2. Task");
            System.out.println("3. Story");
            System.out.println("4. Epic");
            int selectTickettype = Integer.parseInt(reader.nextLine());
            String[] types = {"Bug", "Task", "Story", "Epic"};
            String type = types[selectTickettype - 1];
            sqlManager.createNewTicket(ticketName, description, type, employeeName);
            System.out.print("Enter to continue.");
            reader.nextLine();
            System.out.print("\033c");
            ticketsScreen(sprintId, sqlManager, teamId);
        }
        else if (choice == 3) {
            System.out.println("Are you sure you want to delete this ticket?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            int choice2 = Integer.parseInt(reader.nextLine());
            if (choice2 == 2) {
                System.out.print("Enter to continue.");
                reader.nextLine();
                System.out.print("\033c");
                ticketsScreen(sprintId, sqlManager, teamId);
            }
            else if (choice2 == 1) {
                sqlManager.deleteTicket(chooseTicket(sprintId, tickets, sqlManager));
            }
        }
        else if (choice == 4) {
            System.out.println("Viewing backlog tickets...");
            sqlManager.viewBacklogTickets(teamId);
        }
        else if (choice == 5) {
            System.out.print("Enter to continue.");
            reader.nextLine();
            System.out.print("\033c");
            sprintsScreen(teamId, sqlManager);
        }
        else if (choice == 6) {
            System.out.print("Enter to continue.");
            reader.nextLine();
            System.out.print("\033c");
            welcomeScreen(sqlManager);
        }
    }
    public static int chooseTicket(int sprintId, String[] tickets, SQLManager sqlManager) {
        System.out.println("Which ticket?");
        int selectedTicket = Integer.parseInt(reader.nextLine());
        String ticketTitle = tickets[selectedTicket - 1].split(". ")[1];
        System.out.println("You are now working on " + ticketTitle);
        return sqlManager.getTicketId(ticketTitle);
    }
}
