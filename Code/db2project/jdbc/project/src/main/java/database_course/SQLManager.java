package database_course;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class SQLManager {
    private Connection conn;
    private CallableStatement callableStatement;
    public SQLManager(Connection conn) {
        this.conn = conn;
        this.callableStatement = null;
    }
    /**
     * Inserts an employee into the database.
     * @param employee_name the employee name
     * @param email the employee email
     * @param full_name the employee full name
     * @param gitlab_username the employee gitlab username
     * @param employee_type the employee type (contributor or administrator)
     * @param password the employee password
     * @param hash the employee password hash
     * @param salt the employee password salt
     * @param team_id the employee team id
     */
    public String insertEmployee(String employee_name, String email, String full_name, String gitlab_username, String employee_type, String password, byte[] hash, byte[] salt, int team_id) {
        try {
            String sql = "? = CALL insert_employee(?, ?, ?, ?, ?, ?, ?, ?, ?)";
            callableStatement = conn.prepareCall(sql);
            callableStatement.registerOutParameter(1, Types.VARCHAR);
            callableStatement.setString(2, employee_name);
            callableStatement.setString(3, email);
            callableStatement.setString(4, full_name);
            callableStatement.setString(5, gitlab_username);
            callableStatement.setString(6, employee_type);
            callableStatement.setString(7, password);
            callableStatement.setBytes(8, hash);
            callableStatement.setBytes(9, salt);
            callableStatement.setInt(10, team_id);
            callableStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to insert employee";
        }
        return "Added Employee";
    }

    /**
     * Finds an employee based on their name and returns their information as a string.
     * @param employee_name the employee name
     * @return a string containing the employee's information or an error message
     */
    public String findEmployeeInfo(String employee_name) {
        String employeeInfo = "";
        try {
            String sql = "SELECT * FROM task_management_project.find_employee_info(?);";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, employee_name);
    
            ResultSet resultSet = preparedStatement.executeQuery();
    
            if (resultSet.next()) {
                employeeInfo = "Employee Name: " + resultSet.getString("employeeName") + "\n" +
                               "Email: " + resultSet.getString("emailAddress") + "\n" +
                               "Full Name: " + resultSet.getString("fullName") + "\n" +
                               "GitLab Username: " + (resultSet.getString("gitLabUserName") != null ? resultSet.getString("gitLabUserName") : "N/A") + "\n" +
                               "Employee Type: " + resultSet.getString("type") + "\n" ;
            } else {
                employeeInfo = "No employee found with the name: " + employee_name;
            }
    
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            if (e.getMessage().contains("Employee with username")) {
                employeeInfo = "No employee found with the name: " + employee_name; // Custom message
            } else {
                e.printStackTrace();
                return "Failed to find employee";
            }
        }
        return employeeInfo;
    }
    
    /**
     * Creates a new sprint with the given name and team id.
     * @param sprint_name_inp the sprint name
     * @param team_id_inp the team id
     */
    public void createSprint(String sprint_name_inp, int team_id_inp){
        try {
            String sql = "{? = CALL task_management_project.create_sprint(?, ?)}";
            callableStatement = conn.prepareCall(sql);
            callableStatement.registerOutParameter(1, Types.VARCHAR);
            callableStatement.setString(2, sprint_name_inp);
            callableStatement.setInt(3, team_id_inp);
            callableStatement.execute();
            System.out.println(callableStatement.getString(1));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Calculates the duration of a sprint in days.
     * @param sprint_id_inp the sprint id
     * @return a string containing the duration of the sprint in days
     */
    public String calculateSprintDuration(int sprint_id_inp){
        try {
            String sql = "{? = CALL task_management_project.calculate_sprint_duration(?)}";
            callableStatement = conn.prepareCall(sql);
            callableStatement.registerOutParameter(1, Types.VARCHAR);
            callableStatement.setInt(2, sprint_id_inp);
            callableStatement.execute();
            return callableStatement.getString(1);
        } catch (SQLException e) {
            return e.getMessage();
        }
    }
    
    /**
     * Starts a sprint with the given sprint id.
     * @param sprint_id_inp the sprint id
     */
    public void startSprint(int sprint_id_inp){
        try {
            String sql = "{? = CALL task_management_project.start_sprint(?)}";
            callableStatement = conn.prepareCall(sql);
            callableStatement.registerOutParameter(1, Types.VARCHAR);
            callableStatement.setInt(2, sprint_id_inp);
            callableStatement.execute();
            System.out.println(callableStatement.getString(1));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Ends a sprint with the given sprint id.
     * @param sprint_id_inp the sprint id
     */
    public void endSprint(int sprint_id_inp){
        try {
            String sql = "{? = CALL task_management_project.end_sprint(?)}";
            callableStatement = conn.prepareCall(sql);
            callableStatement.registerOutParameter(1, Types.VARCHAR);
            callableStatement.setInt(2, sprint_id_inp);
            callableStatement.execute();
            System.out.println(callableStatement.getString(1));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Finds the sprint associated with the given team id.
     * @param team_id_inp the team id
     * @return the sprint name, start date, due date, and status associated with the given team id
     */
    public String findTeamSprint(int team_id_inp){
        String sprint = "";
        try {
            String sql = "SELECT * FROM task_management_project.find_team_sprint(?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, team_id_inp);
    
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                sprint += "Sprint Name: " + resultSet.getString("sprintName") + "  ||  Start Date: " + resultSet.getString("startDate") + "  ||  Due Date: " + resultSet.getString("dueDate") + "  ||  Status: " + resultSet.getString("status") + "\n";
            }
            return sprint;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    
    /**
     * Finds the team names associated with the given employee name.
     * @param employee_name_inp the employee name
     * @return the team names associated with the given employee name
     */
    public String findEmployeeTeam(String employee_name_inp){
        String teams = "";
        int counter = 1;
        try {
            String sql = "SELECT * FROM task_management_project.find_employee_teams(?);";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, employee_name_inp);
    
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                teams += counter + ". " + resultSet.getString("team_name_out") + "\n";
                counter++;
            }
            return teams;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }
    
    /**
     * Finds the sprint id associated with the given sprint name and team id.
     * @param sprint_name_inp the sprint name
     * @param team_id_inp the team id
     * @return the sprint id associated with the given sprint name and team id
     */
    public int findSprintId(String sprint_name_inp, int team_id_inp){
        try {
            String sql = "{? = CALL task_management_project.get_sprint_id(?, ?)}";
            callableStatement = conn.prepareCall(sql);
            callableStatement.registerOutParameter(1, Types.INTEGER);
            callableStatement.setString(2, sprint_name_inp);
            callableStatement.setInt(3, team_id_inp);
            callableStatement.execute();
            return callableStatement.getInt(1);
        } catch (SQLException e) {
            return 0;
        }
    }
    

    /**
     * Deletes a sprint and all associated tickets from the database using the given sprint id.
     * @param sprint_id_inp the sprint id
     */
    public void deleteSprint(int sprint_id_inp){
        try {
            String sql = "{? = CALL task_management_project.delete_sprint(?)}";
            callableStatement = conn.prepareCall(sql);
            callableStatement.registerOutParameter(1, Types.VARCHAR);
            callableStatement.setInt(2, sprint_id_inp);
            callableStatement.execute();
            System.out.println(callableStatement.getString(1));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Finds the team id associated with the given team name.
     * @param teamName the team name
     * @return the team id associated with the given team name
     */
    public int getTeamId(String teamName){
        try {
            callableStatement = conn.prepareCall("{? = CALL task_management_project.get_team_id(?)}");
            callableStatement.registerOutParameter(1, Types.INTEGER);
            callableStatement.setString(2, teamName);
            callableStatement.execute();
            return callableStatement.getInt(1);
        } 
        catch (SQLException e) {
            return 0;
        }
    }

    /**
     * Finds the tickets associated with the given sprint id.
     * @param sprintId the sprint id
     * @return the tickets associated with the given sprint id
     */
    public String getSprintTickets(int sprintId){
        try {
            String sql = "SELECT * FROM task_management_project.find_sprint_tickets(?);";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, sprintId);
    
            ResultSet resultSet = preparedStatement.executeQuery();
            String tickets = "";
            while(resultSet.next()){
                tickets += resultSet.getString("ticket_id") + "  ||  " + resultSet.getString("title") + "  ||  " + resultSet.getString("description") + "  ||  " + resultSet.getString("type") + "  ||  " + resultSet.getString("status") + "  ||  " + resultSet.getString("git_branch") + "  ||  " + resultSet.getString("reporter") + "  ||  " + resultSet.getString("assignee") +  resultSet.getInt("team_id") + "\n";
            }
            return tickets;
        } 
        catch (SQLException e) {
            return e.getMessage();
        } 
    }

    /**
     * Counts the number of teams associated with the given employee name.
     * 
     * @param employeeName the name of the employee
     * @return the number of teams associated with the given employee name
     */
    public int countTeams(String employeeName){
        try {
            callableStatement = conn.prepareCall("{? = CALL task_management_project.count_teams(?)}");
            callableStatement.registerOutParameter(1, Types.INTEGER);
            callableStatement.setString(2, employeeName);
            callableStatement.execute();
            return callableStatement.getInt(1);
        } 
        catch (SQLException e) {
            return 0;
        }
    }

    /**
     * Toggles the "Online" status of an employee in the database.
     * @param employee_name the name of the employee whose status will be toggled
     * @return a message indicating success or failure
     */
    public String toggleEmployeeOnlineStatus(String employee_name) {
        try {
            // SQL query to call the toggle_online_status function
            String sql = "{? = CALL task_management_project.toggle_online_status(?)}";
            callableStatement = conn.prepareCall(sql);
    
            // Register the out parameter (to receive the returned message)
            callableStatement.registerOutParameter(1, Types.VARCHAR);
    
            // Set the input parameter for the employee name
            callableStatement.setString(2, employee_name);
    
            // Execute the statement
            callableStatement.execute();
    
            // Retrieve and return the success or error message from the function
            return callableStatement.getString(1);
    
        } catch (SQLException e) {
            e.printStackTrace();
            return "Failed to toggle employee status: " + e.getMessage();
        }
    }

    public int getTicketId(String ticketTitle){
        try {
            String sql = "SELECT ticket_id FROM task_management_project.get_ticket_id(?)";
            callableStatement = conn.prepareCall(sql);
            callableStatement.registerOutParameter(1, Types.INTEGER);
            callableStatement.setString(2, ticketTitle);
            callableStatement.execute();
            return callableStatement.getInt(1);
        } 
        catch (SQLException e) {
            return 0;
        }
    }
    
    public String startTicket(int ticketId){
        try {
            String sql = "{? = CALL task_management_project.start_ticket(?)}";
            callableStatement = conn.prepareCall(sql);
            callableStatement.registerOutParameter(1, Types.VARCHAR);
            callableStatement.setInt(2, ticketId);
            callableStatement.execute();
            return callableStatement.getString(1);
        } 
        catch (SQLException e) {
            return e.getMessage();
        }
    }

    public String endTicket(int ticketId){
        try {
            String sql = "{? = CALL task_management_project.end_ticket(?)}";
            callableStatement = conn.prepareCall(sql);
            callableStatement.registerOutParameter(1, Types.VARCHAR);
            callableStatement.setInt(2, ticketId);
            callableStatement.execute();
            return callableStatement.getString(1);
        } 
        catch (SQLException e) {
            return e.getMessage();
        }
    }
    //Artems Part
    //Function to create new ticket
    /**
     * Creates a new ticket in the task management system.
     * 
     * @param title the title of the ticket
     * @param description the description of the ticket
     * @param type the type of the ticket (e.g., 'Bug', 'Task', 'Story', or 'Epic')
     * @param reporter the name of the employee who is reporting the ticket
     * @return a message indicating the success or failure of the ticket creation
     */
    public String createNewTicket(String title, String description, String type, String reporter) {
        try {
            String sql = "{? = CALL task_management_project.create_new_ticket(?, ?, ?, ?)}";
            callableStatement = conn.prepareCall(sql);
            callableStatement.registerOutParameter(1, Types.VARCHAR);
            callableStatement.setString(2, title);
            callableStatement.setString(3, description);
            callableStatement.setString(4, type);
            callableStatement.setString(5, reporter);
            callableStatement.execute();
            return callableStatement.getString(1);
        } 
        catch (SQLException e) {
            e.printStackTrace();
            return "Failed to create ticket";
        }
    }

    public String deleteTicket(int ticketId) {
        try {
            String sql = "{? = CALL task_management_project.delete_ticket(?)}";
            callableStatement = conn.prepareCall(sql);
            callableStatement.registerOutParameter(1, Types.VARCHAR);
            callableStatement.setInt(2, ticketId);
            callableStatement.execute();
            return callableStatement.getString(1);
        } 
        catch (SQLException e) {
            e.printStackTrace();
            return "Failed to delete ticket";
        }
    }
    
    
    // Function to modify a ticket
    
    /**
     * Modifies the details of an existing ticket in the task management system.
     * @param ticketId
     * @param title
     * @param description
     * @param type
     * @param parentTicketId
     * @return a message indicating the success or failure of the ticket modification
     */
    public String modifyTicket(int ticketId, String title, String description, String type, Integer parentTicketId) {
        try {
            String sql = "{? = CALL task_management_project.modify_ticket(?, ?, ?, ?, ?)}";
            callableStatement = conn.prepareCall(sql);
            callableStatement.registerOutParameter(1, Types.VARCHAR);
            callableStatement.setInt(2, ticketId);
            callableStatement.setString(3, title);
            callableStatement.setString(4, description);
            callableStatement.setString(5, type);
            callableStatement.setObject(6, parentTicketId, Types.INTEGER);  // parentTicketId can be null
            callableStatement.execute();
            return callableStatement.getString(1);
        } 
        catch (SQLException e) {
            e.printStackTrace();
            return "Failed to modify ticket";
        }
    }
    
    /**
     * Assigns a ticket to a specific employee
     * @param ticketId
     * @param assignee
     * @return a message indicating the success or failure of the ticket assignment
     */
    public String assignTicket(int ticketId, String assignee) {
        try {
            String sql = "{? = CALL task_management_project.assign_ticket(?, ?)}";
            callableStatement = conn.prepareCall(sql);
            callableStatement.registerOutParameter(1, Types.VARCHAR);
            callableStatement.setInt(2, ticketId);
            callableStatement.setString(3, assignee);
            callableStatement.execute();
            return callableStatement.getString(1);
        } 
        catch (SQLException e) {
            e.printStackTrace();
            return "Failed to assign ticket";
        }
    }

    // Function to get all employee names as a single string
    /**
     * Retrieves a list of all employee names from the database as a single string,
     * with each employee name on a new line.
     * @return a string containing all employee names, each on a new line
     */
    public String getAllEmployeeNames() {
        StringBuilder employeeNames = new StringBuilder();
        try {
            String sql = "{? = CALL task_management_project.get_all_employee_names()}";
            callableStatement = conn.prepareCall(sql);
            callableStatement.registerOutParameter(1, Types.OTHER); // Use Types.OTHER for a table return type
            callableStatement.execute();
            ResultSet resultSet = (ResultSet) callableStatement.getObject(1);
            while (resultSet.next()) {
                String employeeName = resultSet.getString("employee_name");
                employeeNames.append(employeeName).append("\n"); // Append each name followed by a newline
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employeeNames.toString(); // Return the final string
    }

    public void viewBacklogTickets(int teamId) {
        try {
            String sql = "SELECT t.ticket_id, t.title, t.description, t.type, t.status, t.reporter, t.assignee " +
                         "FROM task_management_project.backlog_tickets t " +
                         "JOIN task_management_project.employee_team et ON t.assignee = et.employee_id " +
                         "WHERE et.team_id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, teamId);
            ResultSet resultSet = preparedStatement.executeQuery();
    
            if (!resultSet.isBeforeFirst()) {  // Check if ResultSet is empty
                System.out.println("No backlog tickets found for the selected team.");
            } else {
                while (resultSet.next()) {
                    int ticketId = resultSet.getInt("ticket_id");
                    String title = resultSet.getString("title");
                    String description = resultSet.getString("description");
                    String type = resultSet.getString("type");
                    String status = resultSet.getString("status");
                    String reporter = resultSet.getString("reporter");
                    String assignee = resultSet.getString("assignee");
    
                    System.out.println("Ticket ID: " + ticketId);
                    System.out.println("Title: " + title);
                    System.out.println("Description: " + description);
                    System.out.println("Type: " + type);
                    System.out.println("Status: " + status);
                    System.out.println("Reporter: " + reporter);
                    System.out.println("Assignee: " + assignee);
                    System.out.println();
                }
            }
    
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createEmployee(String employeeName, String employeeEmail, String full_name, String gitlab_username, byte[] hash, byte[] salt) {
        try {
            String sql = "{? = CALL task_management_project.create_employee(?, ?, ?, ?, ?, ?, ?)}";
            callableStatement = conn.prepareCall(sql);
            callableStatement.registerOutParameter(1, Types.VARCHAR);
            callableStatement.setString(2, employeeName);
            callableStatement.setString(3, employeeEmail);
            callableStatement.setString(4, full_name);
            callableStatement.setString(5, gitlab_username);
            callableStatement.setBytes(7, hash);
            callableStatement.setBytes(8, salt);
            callableStatement.execute();
            System.out.println(callableStatement.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}