DROP FUNCTION insert_employee(employee_name VARCHAR(50), email VARCHAR(50), full_name VARCHAR(50), gitlab_username VARCHAR(50), employee_type VARCHAR(15), password VARCHAR(100), hash BYTEA, salt BYTEA, team_id_input INT);
DROP FUNCTION find_employee_info(employee_name VARCHAR(50));
DROP FUNCTION create_sprint(sprint_name_inp VARCHAR(50), team_id_inp INT);
DROP FUNCTION calculate_sprint_duration(sprint_id_inp INT);
DROP FUNCTION start_sprint(sprint_id_inp INT);
DROP FUNCTION end_sprint(sprint_id_inp INT);
DROP FUNCTION find_team_sprint(team_id_inp INT);
DROP FUNCTION find_employee_teams(employee_name_inp VARCHAR(50));
DROP FUNCTION check_in_progress_sprints() CASCADE;
DROP TRIGGER before_sprint_change ON sprint;
DROP FUNCTION get_sprint_id(sprint_name_inp VARCHAR(50), team_id_inp INT);
DROP FUNCTION delete_sprint(sprint_id_inp INT);
DROP FUNCTION get_team_id(team_name VARCHAR(50));
DROP FUNCTION find_sprint_tickets(sprintId INT);
DROP FUNCTION count_teams(employeeName VARCHAR(50));
DROP FUNCTION IF EXISTS task_management_project.toggle_online_status(VARCHAR);
DROP FUNCTION create_new_ticket(v_title VARCHAR(50), v_description TEXT, v_type VARCHAR(5), v_reporter VARCHAR(50));
DROP FUNCTION modify_ticket(v_ticket_id INT, v_title VARCHAR(50), v_description TEXT, v_type VARCHAR(5), v_parent_ticket_id INT);
DROP FUNCTION assign_ticket(v_ticket_id INT, v_assignee VARCHAR(50));
DROP FUNCTION complete_ticket(v_ticket_id INT, v_git_branch VARCHAR(50));
--RITIK'S PART
--TO DO:
--CREATE OR REPLACE function insert_employee(employee_name, email, full_name, gitlab_username, employee_type, password, hash, salt, team_id)

--DONE:
--1. Function to insert a new employee
--Specifications:
----Inserts an employee with details like name, email, GitLab username, type, and team ID.
----Password, hash, and salt are stored for authentication purposes.
CREATE OR REPLACE FUNCTION insert_employee(
    employee_name_inp VARCHAR(50),
    email_inp VARCHAR(50),
    full_name_inp VARCHAR(50),
    gitlab_username_inp VARCHAR(50),
    employee_type_inp VARCHAR(15),
    hash_inp BYTEA,
    salt_inp BYTEA,
    team_id_input INT
)
RETURNS VOID AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM task_management_project.team
        WHERE team_id = team_id_input
    ) THEN
        RAISE EXCEPTION 'Team with ID % does not exist', team_id_input;
    END IF;
    INSERT INTO task_management_project.employee (
        employee_name, 
        email, 
        full_name, 
        gitlab_username, 
        employee_type,
        hash, 
        salt
    )
    VALUES (
        employee_name_inp,
        email_inp,
        full_name_inp,
        gitlab_username_inp,
        employee_type_inp,
        hash_inp,
        salt_inp
    );
    INSERT INTO task_management_project.employee_team (
        employee_name, 
        team_id
    )
    VALUES (
            employee_name,
            team_id_input
           );
EXCEPTION
    WHEN others THEN
        RAISE Notice 'Failed to insert employee: %', SQLERRM;
END;
$$ LANGUAGE plpgsql;

--2. Function to retrieve employee information
--Specifications:
----Fetches the employee's name based on their username.
CREATE OR REPLACE FUNCTION find_employee_info(
    employee_name_input VARCHAR(50)
)
RETURNS TABLE (
    employeeName VARCHAR(50),
    emailAddress VARCHAR(50),
    fullName VARCHAR(50),
    gitLabUserName VARCHAR(50),
    type VARCHAR(15)
) AS $$
BEGIN
    RETURN QUERY
    SELECT employee_name, email, full_name, gitlab_username, employee_type
    FROM task_management_project.employee
    WHERE employee_name = employee_name_input;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Employee with username % does not exist', employee_name_input;
    END IF;
END;
$$ LANGUAGE plpgsql;

--3. Function to create a new sprint
--Specifications:
----Inserts a sprint record with the sprint name and associated team ID.
CREATE OR REPLACE FUNCTION create_sprint(
    sprint_name_inp VARCHAR(50),
    team_id_inp INT
)
RETURNS text AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM task_management_project.sprint
        WHERE sprint_name = sprint_name_inp
              AND team_id = team_id_inp
    )
    THEN
        RAISE EXCEPTION 'Sprint with name % already exists for team %', sprint_name_inp, team_id_inp;
    END IF;
    INSERT INTO task_management_project.sprint (
        sprint_name, 
        team_id,
        status
    )
    VALUES (
        sprint_name_inp,
        team_id_inp,
        'To Do'
    );
    RETURN 'Sprint Create';
END;
$$ LANGUAGE plpgsql;

--4. Function to calculate sprint duration
--Specifications:
----Calculates the duration of a sprint in days.
CREATE OR REPLACE FUNCTION calculate_sprint_duration(
    sprint_id_inp INT
)
RETURNS TEXT AS $$
DECLARE
    duration INT;
BEGIN
IF NOT EXISTS (
    SELECT 1
    FROM task_management_project.sprint
    WHERE sprint_id = sprint_id_inp
      AND start_date IS NOT NULL
      AND due_date IS NOT NULL
) THEN
    RAISE EXCEPTION 'Sprint with ID % does not exist or has missing start or end date', sprint_id_inp;
END IF;
    SELECT due_date - start_date INTO duration
    FROM task_management_project.sprint 
    WHERE sprint_id = sprint_id_inp;
    RETURN (duration || ' days');
END;
$$ LANGUAGE plpgsql;


--5. Function to start a sprint
--Specifications:
----Sets the start date to the current timestamp and marks the sprint as 'In Progress'.
CREATE OR REPLACE FUNCTION start_sprint(
    sprint_id_inp INT
)
RETURNS text AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM task_management_project.sprint
        WHERE sprint_id = sprint_id_inp
        AND status = 'To Do'
    )
    THEN
        RAISE EXCEPTION 'Sprint with ID % is not in To Do status', sprint_id_inp;
    END IF;
    UPDATE task_management_project.sprint 
    SET 
        start_date = NOW(),
        status = 'In Progress'
    WHERE sprint_id = sprint_id_inp;
    RETURN 'Sprint Started';
END;
$$ LANGUAGE plpgsql;

--6. Function to end a sprint
--Specifications:
----Sets the end date to the current timestamp and marks the sprint as 'Done'.
CREATE OR REPLACE FUNCTION end_sprint(
    sprint_id_inp INT
)
RETURNS text AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM task_management_project.sprint
        WHERE sprint_id = sprint_id_inp
        AND status = 'In Progress'
        OR status = 'To Do'
    )
    THEN
        UPDATE task_management_project.sprint
        SET
            due_date = NOW(),
            status = 'Done'
        WHERE sprint_id = sprint_id_inp;
    ELSE
        RAISE EXCEPTION 'Sprint with ID % is not in In Progress or To Do status', sprint_id_inp;
    END if;
    RETURN 'Sprint Ended';
END;
$$ LANGUAGE plpgsql;

--7. Function to find sprints associated with a team
--Specifications:
--Returns a table with sprint IDs and sprint names associated with a given team ID.
CREATE OR REPLACE FUNCTION find_team_sprint(
    team_id_inp INT
)
RETURNS TABLE (
    sprintId INT,
    sprintName VARCHAR(50),
    startDate date,
    dueDate date,
    status VARCHAR(20),
    teamId INT
) AS $$
BEGIN
    RETURN QUERY
    SELECT * 
    FROM task_management_project.sprint
    WHERE team_id = team_id_inp;
END;
$$ LANGUAGE plpgsql;

--8. Function to find a user's teams 
--Specifications:
--Returns a table with team IDs and team names associated with a given user ID.
CREATE OR REPLACE FUNCTION find_employee_teams(
    employee_name_inp VARCHAR(50)
)
RETURNS TABLE (
    team_id_out INT,
    team_name_out VARCHAR(50)
) AS $$
BEGIN
    RETURN QUERY
    SELECT team_id, team_name
    FROM task_management_project.employee
    INNER JOIN task_management_project.employee_team USING (employee_name)
    INNER JOIN task_management_project.team USING (team_id)
    WHERE employee_name = employee_name_inp;
END;
$$ LANGUAGE plpgsql;

--9. Function to check if a team has a sprint in progress
--Specifications:
----Checks if a team has a sprint in progress.
CREATE OR REPLACE FUNCTION check_in_progress_sprints()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'In Progress' THEN
        IF EXISTS ( 
            SELECT 1
            FROM task_management_project.sprint
            WHERE team_id = NEW.team_id 
                AND status = 'In Progress'
        ) THEN 
            RAISE EXCEPTION 'Team with ID % already has a sprint in progress', NEW.team_id;
        END IF;
    END IF;
    RETURN NEW;
EXCEPTION 
    WHEN others 
    THEN
        RAISE Notice 'Failed to set sprint to In Progress: %', SQLERRM;
        RETURN null;
END;
$$ LANGUAGE plpgsql;
--Trigger for checking if a team has a sprint in progress
Create trigger before_sprint_change
before update on task_management_project.sprint
for each row
execute procedure check_in_progress_sprints();

--10. Function to get the sprint ID associated with a given sprint name
--Specifications:
----Returns the sprint ID associated with a given sprint name.
CREATE OR REPLACE FUNCTION get_sprint_id(
    sprint_name_inp VARCHAR(50),
    team_id_inp INT
)
RETURNS INT AS $$
DECLARE
    sprint_id_out INT;
BEGIN
    SELECT sprint_id INTO sprint_id_out
    FROM task_management_project.sprint
    WHERE sprint_name = sprint_name_inp
        AND team_id = team_id_inp;
    IF NOT FOUND 
    THEN
        RAISE EXCEPTION 'Sprint with name % does not exist', sprint_name_inp;
    END IF;
    RETURN sprint_id_out;
END;
$$ LANGUAGE plpgsql;

--11. Function to delete a sprint
--Specifications:
----Deletes a sprint from the database.
CREATE OR REPLACE FUNCTION delete_sprint(
    sprint_id_inp INT
)
RETURNS text AS $$
BEGIN
    DELETE FROM task_management_project.ticket
    WHERE sprint_id = sprint_id_inp;
    DELETE FROM task_management_project.sprint
    WHERE sprint_id = sprint_id_inp;
    RETURN 'Deleted Sprint and all tickets associated with it';
END;
$$ LANGUAGE plpgsql;

--12. Function to find the team ID associated with a given team name
--Specifications:
----Returns the team ID associated with a given team name.
CREATE OR REPLACE FUNCTION get_team_id(team_name_inp VARCHAR(50))
RETURNS INT AS $$
DECLARE
    team_id_out INT;
BEGIN
    SELECT team_id INTO team_id_out
    FROM task_management_project.team
    WHERE team_name = team_name_inp;
    IF NOT FOUND 
    THEN
        RAISE EXCEPTION 'Team with name % does not exist', team_name_inp;
    END IF;
    RETURN team_id_out;
END;
$$ LANGUAGE plpgsql;

--13. Function to find the tickets associated with a given sprint ID
--Specifications:
----Returns a table of tickets associated with a given sprint ID.
CREATE OR REPLACE FUNCTION find_sprint_tickets(sprintId_inp INT)
RETURNS TABLE (
    ticket_id INT,
    title VARCHAR(50),
    description TEXT,
    type VARCHAR(5),
    status VARCHAR(20),
    git_branch VARCHAR(50),
    reporter VARCHAR(50),
    assignee VARCHAR(50),
    sprint_id_out INT,
    team_id INT
) AS $$
BEGIN
    RETURN QUERY
    SELECT * 
    FROM task_management_project.ticket
    WHERE sprint_id = sprintId_inp;
END;
$$ LANGUAGE plpgsql;

--14. Function to count the number of teams associated with a given employee name
--Specifications:
----Returns the number of teams associated with a given employee name.
CREATE OR REPLACE FUNCTION count_teams(employeeName_inp VARCHAR(50))
RETURNS INT AS $$
BEGIN
    RETURN (SELECT COUNT(*) FROM task_management_project.employee_team WHERE employee_name = employeeName_inp);
END;
$$ LANGUAGE plpgsql;

--15. Function to ticket id for a given ticket title
--Specifications:
----Returns the ticket ID associated with a given ticket title.
CREATE OR REPLACE FUNCTION get_ticket_id(title_inp VARCHAR(50))
RETURNS INT AS $$
BEGIN
    RETURN (SELECT ticket_id FROM task_management_project.ticket WHERE title = title_inp);
END;
$$ LANGUAGE plpgsql;


--16. Function to start a ticket
--Specifications:
----Sets the status of a ticket to "In Progress".
CREATE OR REPLACE FUNCTION start_ticket(ticket_id_inp INT)
RETURNS text AS $$
DECLARE 
    ticket record;
BEGIN
    SELECT * INTO ticket FROM task_management_project.ticket WHERE ticket_id = ticket_id_inp;
    IF ticket.assignee IS NULL THEN
        RAISE EXCEPTION 'Ticket with title ''%'' does not have an assignee', ticket.title;
    ELSEIF ticket.status != 'To Do' THEN
        RAISE EXCEPTION 'Ticket with title ''%'' is either in progress or already completed', ticket.title;
    ELSE
        UPDATE task_management_project.ticket
        SET status = 'In Progress'
        WHERE ticket_id = ticket_id_inp;
        RETURN 'Started ticket';
    END IF;
EXCEPTION
    WHEN others THEN
        RAISE EXCEPTION 'Failed to start ticket: %', SQLERRM;
        RETURN SQLERRM;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION end_ticket(ticket_id_inp INT)
RETURNS text AS $$
BEGIN
    UPDATE task_management_project.ticket
    SET status = 'Completed'
    WHERE ticket_id = ticket_id_inp;
    RETURN 'Ended ticket';
END;
$$ LANGUAGE plpgsql;

Create or replace function delete_ticket(ticket_id_inp INT)
returns text as $$
begin
    delete from task_management_project.ticket where ticket_id = ticket_id_inp;
    return 'Deleted ticket';
end;
$$ language plpgsql;

--ARTEM'S PART
--TO DO:
--CREATE OR REPLACE function compare_passwords(password, hash, salt)

--DONE:
--1.Function to create new ticket
--Specifications:
----The user who creates the ticket should be set as the reporter. 
----The ticket should default to unassigned on creation.
----The ticket should not be associated with a sprint on creation (i.e. it should be on the backlog)
CREATE OR REPLACE FUNCTION create_new_ticket(
    --input variables
    v_title VARCHAR(50), 
    v_description TEXT, 
    v_type VARCHAR(5),  -- 'Bug', 'Task', 'Story', or 'Epic'
    v_reporter VARCHAR(50)  -- The employee who creates the ticket in the application
)
RETURNS TEXT AS $$
BEGIN
    INSERT INTO task_management_project.ticket (
        title, 
        description, 
        type, 
        status,  -- Default status will be 'To Do'
        git_branch,
        reporter, 
        assignee,  -- Default is NULL (which is unassigned)
        sprint_id,  -- Default is NULL (it first goes to backlog)
        parent_ticket_id  -- Optional, depends on the ticket type (null by default)
    )
    VALUES (
        v_title,
        v_description, 
        v_type, 
        'To Do',  -- Default
        NULL,
        v_reporter, 
        NULL,  -- Unassigned
        NULL,  -- Not associated with a sprint initially
        NULL  -- No parent ticket unless specified
    );
    RETURN 'Created new ticket';
END;
$$ LANGUAGE plpgsql;

--2.Function to modify a ticket
--Specifications:
----Modifies the title, description, and the type of an existing ticket.
----Also assigns a parent ticket (optional)
----All modifications are optional, hence why I switched to a case basis update statement
CREATE OR REPLACE FUNCTION modify_ticket(
    --Input variables
    v_ticket_id INT, 
    v_title VARCHAR(50), 
    v_description TEXT, 
    v_type VARCHAR(5), 
    v_parent_ticket_id INT  --Allows to assign as a 'child' of another ticket
)
RETURNS TEXT AS $$
BEGIN
    -- Update only the title, description, type, and parent_ticket_id if they are not NULL
    UPDATE task_management_project.ticket
    SET 
        title = CASE WHEN v_title IS NOT NULL THEN v_title ELSE title END,
        description = CASE WHEN v_description IS NOT NULL THEN v_description ELSE description END,
        type = CASE WHEN v_type IS NOT NULL THEN v_type ELSE type END,
        parent_ticket_id = CASE WHEN v_parent_ticket_id IS NOT NULL THEN v_parent_ticket_id ELSE parent_ticket_id END
    WHERE ticket_id = v_ticket_id;
    RETURN 'Ticket successfully modified.';
END;
$$ LANGUAGE plpgsql;

--3.Function to assign a ticket to a user and mark it as 'In Progress'
--Specifications:
----Assigns a ticket to a user and marks it as 'In Progress'.
----!The ticket can only be marked as 'In Progress' if it has been assigned to a user.
CREATE OR REPLACE FUNCTION assign_ticket(
    v_ticket_id INT, 
    v_assignee VARCHAR(50)  -- The employee assigned to the ticket
)
RETURNS TEXT AS $$
BEGIN
    -- Check if the assignee exists in the employee table
    IF NOT EXISTS (SELECT 1 FROM task_management_project.employee 
                   WHERE employee_name = v_assignee) 
    THEN
        RAISE EXCEPTION 'Indicated assignee % does not exist!', v_assignee;
    END IF;
     -- Assign the ticket to the employee and mark it as 'In Progress'
    UPDATE task_management_project.ticket
    SET 
        assignee = v_assignee, 
        status = 'In Progress'  -- Set the ticket status to 'In Progress'
        WHERE ticket_id = v_ticket_id;
    --Extra validation: check if update was successful
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Ticket with ID % does not exist', v_ticket_id;
    END IF;
    RETURN 'Ticket successfully assigned.';
END;
$$ LANGUAGE plpgsql;

--4.Function to mark a ticket as complete and associate it with a git branch
-- Specifications:
---- Marks a ticket as 'Complete' and associates it with a git branch.
---- The ticket must have a non-NULL git branch before it can be marked as 'Complete'.
---- The git branch must be provided as an input variable.
---- Updates the ticket's status to 'Done' and the git branch to the provided value.

CREATE OR REPLACE FUNCTION complete_ticket(
    v_ticket_id INT,     
    v_git_branch VARCHAR(50)  -- The git branch that will be associated with the ticket
)
RETURNS TEXT AS $$
BEGIN
    UPDATE task_management_project.ticket
    SET 
        status = 'Done',    
        git_branch = v_git_branch  -- Set to the provided git branch
    WHERE ticket_id = v_ticket_id;

    --Extra validation: check if update was successful
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Ticket with ID % does not exist', v_ticket_id;
    END IF;

    IF v_git_branch IS NULL OR v_git_branch = '' THEN
    RAISE EXCEPTION 'Git branch must be provided to mark ticket as complete';
    END IF;
    RETURN 'Ticket completed.';
END;
$$ LANGUAGE plpgsql;

--5. Function to toggle the "Online" status of an employee
--Specifications:
----Toggles the "Online" status for a given employee.
----If the employee is currently "offline" (Online = false), it will be set to "online" (Online = true).
----If the employee is currently "online" (Online = true), it will be set to "offline" (Online = false).
----The employee's name must be provided as an input variable

CREATE OR REPLACE FUNCTION toggle_online_status(
    employee_name_input VARCHAR  -- The employee name whose status will be toggled
)
RETURNS TEXT AS $$  -- Changed return type to TEXT for message return
BEGIN
    -- Update the "active" status by toggling the current value
    UPDATE task_management_project.employee
    SET active = NOT active  -- Toggle the current "active" value
    WHERE employee_name = employee_name_input;

    -- Extra validation: check if the employee exists
    IF NOT FOUND THEN
        RETURN 'Employee with name ' || employee_name_input || ' does not exist';  -- Return error message
    END IF;

    -- Success message
    RETURN 'Employee ' || employee_name_input || ' status toggled successfully';
END;
$$ LANGUAGE plpgsql;

--6. Function to return all employee names
-- Specifications:
---- Returns a list of all employee names from the employee table.
CREATE OR REPLACE FUNCTION get_all_employee_names()
RETURNS TABLE(employee_name VARCHAR) AS $$
BEGIN
    RETURN QUERY
    SELECT employee_name
    FROM task_management_project.employee;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION create_new_employee(
    employee_name_inp VARCHAR(50),
    email_inp VARCHAR(50),
    full_name_inp VARCHAR(50),
    gitlab_username_inp VARCHAR(50),
    hash_inp BYTEA,
    salt_inp BYTEA
)
RETURNS TEXT AS $$
BEGIN
    INSERT INTO task_management_project.employee (
        employee_name, 
        email, 
        full_name, 
        gitlab_username,
        employee_type,
        hash,
        salt
    )
    VALUES (
        employee_name_inp,
        email_inp,
        full_name_inp,
        gitlab_username_inp,
        'Contributor',
        hash_inp,
        salt_inp
    );
    RETURN 'Employee successfully created.';
END;
$$ LANGUAGE plpgsql;
