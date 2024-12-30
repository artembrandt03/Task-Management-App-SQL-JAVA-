--CREATE OR REPLACE view find_all_incomplete_tickets(user_name)

--ARTEM'S PART
--1.VIEW: backlog_tickets
----All the tickets that have just been created and are yet to be assigned
----to a sprint go to the backlog
CREATE VIEW task_management_project.backlog_tickets AS
SELECT 
    ticket_id,
    title,
    description,
    type,
    status,
    reporter,
    assignee,
    parent_ticket_id
FROM 
    task_management_project.ticket
WHERE 
    sprint_id IS NULL; --meaning 'in backlog'

--2.VIEW: all_tickets_overview
----Simply shows a summary of all tickets
CREATE VIEW task_management_project.all_tickets_overview AS
SELECT 
    ticket_id,
    title,
    description,
    type,
    status,
    reporter,
    assignee,
    sprint_id,
    parent_ticket_id
FROM 
    task_management_project.ticket;

--3.VIEW: employee usernames
CREATE VIEW task_management_project.view_employee_usernames AS
SELECT
    employee_name
FROM
    task_management_project.employee;
