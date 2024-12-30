DROP TABLE if exists task_management_project.ticket Cascade ;
DROP TABLE if exists task_management_project.employee_team Cascade ;
DROP TABLE if exists task_management_project.team Cascade ;
DROP TABLE if exists task_management_project.employee Cascade ;
DROP TABLE if exists task_management_project.sprint Cascade;

CREATE TABLE employee (
    employee_name VARCHAR(50) PRIMARY KEY NOT NULL,
    email VARCHAR(50) NOT NULL,
    full_name VARCHAR(50) NOT NULL,
    gitlab_username VARCHAR(50),
    employee_type VARCHAR(15) NOT NULL CHECK (employee_type IN ('contributor', 'administrator')),
    hash BYTEA,
    salt BYTEA,
    active BOOLEAN DEFAULT FALSE
);

CREATE TABLE team (
    team_id SERIAL PRIMARY KEY NOT NULL,
    team_name VARCHAR(50) NOT NULL
);

CREATE TABLE employee_team (
    employee_name VARCHAR(50),
    team_id int,
    FOREIGN KEY (employee_name) REFERENCES employee(employee_name),
    FOREIGN KEY (team_id) REFERENCES team(team_id)
);

CREATE table sprint(
    sprint_id Serial PRIMARY KEY NOT NULL,
    sprint_name varchar(50) NOT NULL,
    start_date date,
    due_date date,
    status varchar(20) CHECK (status IN ('In Progress', 'Done', 'To Do')),
    team_id int NOT NULL,
    foreign key(team_id) references team(team_id)
);

CREATE TABLE ticket(
    ticket_id Serial PRIMARY KEY NOT NULL,
    title varchar(50) NOT NULL,
    description text,
    type varchar(5) CHECK (type IN ('Bug', 'Task', 'Story', 'Epic')),
    status varchar(20) CHECK (status IN ('In Progress', 'Done', 'To Do')),
    git_branch varchar(50),
    reporter varchar(50) NOT NULL,
    assignee varchar(50),          --Changed to possible null
    sprint_id int,
    parent_ticket_id int,
    foreign key(reporter) references employee(employee_name),
    foreign key(assignee) references employee(employee_name),
    foreign key(sprint_id) references sprint(sprint_id)
);
