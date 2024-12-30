-- Indexes for employee table
CREATE INDEX idx_employee_email ON task_management_project.employee (email);
CREATE INDEX idx_employee_employee_type ON task_management_project.employee (employee_type);
CREATE INDEX idx_employee_gitlab_username ON task_management_project.employee (gitlab_username);

-- Indexes for team table
CREATE INDEX idx_team_name ON task_management_project.team (team_name);

-- Indexes for employee_team table
CREATE INDEX idx_employee_team_employee_name ON task_management_project.employee_team (employee_name);
CREATE INDEX idx_employee_team_team_id ON task_management_project.employee_team (team_id);

-- Indexes for sprint table
CREATE INDEX idx_sprint_team_id ON task_management_project.sprint (team_id);
CREATE INDEX idx_sprint_status ON task_management_project.sprint (status);

-- Indexes for ticket table
CREATE INDEX idx_ticket_reporter ON task_management_project.ticket (reporter);
CREATE INDEX idx_ticket_assignee ON task_management_project.ticket (assignee);
CREATE INDEX idx_ticket_sprint_id ON task_management_project.ticket (sprint_id);
CREATE INDEX idx_ticket_parent_ticket_id ON task_management_project.ticket (parent_ticket_id);
CREATE INDEX idx_ticket_status ON task_management_project.ticket (status);
