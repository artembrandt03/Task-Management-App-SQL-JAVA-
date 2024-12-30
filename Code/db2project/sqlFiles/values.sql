--INSERTING INTO TABLES

INSERT INTO task_management_project.employee (employee_name, email, full_name, gitlab_username, employee_type, hash, salt)
VALUES
    ('db_kid', 'db_kid@dawscorp.qc.ca', 'Alice Thompson', '@dbkid1928', 'contributor', NULL, NULL),
    ('binary_ben', 'ben10@dawscorp.qc.ca', 'Benoît Legrand', '@ben10', 'contributor', NULL, NULL),
    ('cparenteau', 'cparenteau@dawscorp.qc.ca', 'Chloé Parenteau', '@cpar', 'contributor', NULL, NULL),
    ('daksha', 'daksha@dawscorp.qc.ca', 'Daksha Choudhury', NULL, 'contributor', NULL, NULL),
    ('eng', 'engornar@dawscorp.qc.ca', 'Erzsébet Nagy', '@engie', 'contributor', NULL, NULL),
    ('adeoyeF', 'adeoye@dawscorp.qc.ca', 'Florian Adeoye', '@adeoye', 'contributor', NULL, NULL),
    ('gll', 'gll@dawscorp.qc.ca', 'Gabriella Luis Lorca', '@gabby', 'contributor', NULL, NULL),
    ('himi', 'himi@dawscorp.qc.ca', 'Himiko Ito', NULL, 'contributor', NULL, NULL),
    ('wiz_of_oz', 'ibrahim.osman@dawscorp.qc.ca', 'Ibrahim Osman', '@ozzy', 'contributor', NULL, NULL);

INSERT INTO task_management_project.team (team_id, team_name) VALUES
    (1, 'Infra'),
    (2, 'SW'),
    --(3, 'Infrastructure'),
    (4, 'Management'),
    (5, 'IT Admin');

INSERT INTO task_management_project.employee_team (employee_name, team_id) VALUES
    ('db_kid', 1),       -- Infra
    ('binary_ben', 2),    -- SW
    ('cparenteau', 2),    -- SW
    ('cparenteau', 1),    -- Infrastructure
    ('daksha', 4),        -- Management
    ('eng', 2),           -- SW
    ('adeoyeF', 1),       -- Infra
    ('gll', 2),           -- SW
    ('himi', 5),          -- IT Admin
    ('wiz_of_oz', 1),     -- Infra
    ('wiz_of_oz', 4);     -- Management

INSERT INTO task_management_project.sprint(sprint_name, start_date, due_date, status, team_id) VALUES
    ('SW1', '2024-10-01', '2024-10-08', 'Done', 2),
    ('SW2', '2024-10-08', '2024-10-15', 'Done', 2),
    ('SW3', '2024-10-15', '2024-10-22', 'Done', 2),
    ('SW4', '2024-10-22', '2024-10-29', 'In Progress', 2),
    ('SW5', Null, Null, 'To Do', 2),                           
    ('Infra1', '2024-10-01', '2024-10-08', 'Done', 1),
    ('Infra2', '2024-10-08', '2024-10-15', 'Done', 1),
    ('Infra3', '2024-10-15', '2024-10-22', 'Done', 1),
    ('Infra4', '2024-10-22', '2024-10-29', 'In Progress', 1),
    ('Infra5', Null, Null, 'To Do', 1);    

INSERT INTO task_management_project.ticket(title, parent_ticket_id, type, status, sprint_id, assignee, reporter, description) VALUES
    ('Set up apache server', Null, 'Epic', 'Done', 6, 'db_kid', 'wiz_of_oz', Null),
    ('Update apache server', NULL, 'Task', 'In Progress', 6, 'db_kid', 'wiz_of_oz', Null),
    ('Add load balancer', NULL, 'Task', 'In Progress', 6, 'db_kid', 'wiz_of_oz', Null),
    ('Write basic CRUD API', NULL, 'Task', 'In Progress', 4, 'binary_ben', 'daksha', Null),
    ('Set up Virtual Hosts for locale management', NULL, 'Task', 'In Progress', 3, 'cparenteau', 'daksha', Null),
    ('Create RPC interface', NULL, 'Task', 'In Progress', 3, 'cparenteau', 'daksha', Null),
    ('Write database connection manager', NULL, 'Task', 'In Progress', 4, 'eng', 'daksha', Null),
    ('Setup remote postgres server', NULL, 'Task', 'In Progress', 4, 'adeoyeF', 'wiz_of_oz', Null),
    ('Add roles and set up permissions', NULL, 'Task', 'In Progress', 4, 'adeoyeF', 'wiz_of_oz', Null),
    ('Fix front end bug that causes 404', NULL, 'Bug', 'In Progress', 5, 'gll', 'daksha', Null),
    ('Assess system security', NULL, 'Task', 'Done', 3, 'himi', 'himi', Null),
    ('Setup Gabriella\''s laptop', NULL, 'Task', 'In Progress', 5, 'himi', 'gll', Null),
    ('Develop migration plan to cloud based infra', NULL, 'Task', 'In Progress', 4, 'wiz_of_oz', 'wiz_of_oz', Null),
    ('Set up flask for backend', NULL, 'Task', 'Done', 3, 'cparenteau', 'wiz_of_oz', Null),
    ('Assess possible server side tools', NULL, 'Task', 'Done', 3, 'cparenteau', 'wiz_of_oz', Null);

