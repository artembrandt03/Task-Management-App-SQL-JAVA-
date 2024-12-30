CREATE DATABASE ritik_artem_project;
CREATE SCHEMA task_management_project;
GRANT CREATE ON SCHEMA task_management_project TO "2139953";
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA task_management_project TO "2139953";
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA task_management_project TO "2139953";
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA task_management_project TO "2139953";
REVOKE ALL PRIVILEGES ON SCHEMA task_management_project FROM PUBLIC;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA task_management_project TO "2338322";

--Run on Artem's SID DB to give all privileges to ritik
GRANT ALL ON SCHEMA task_management_project TO "2338322";
GRANT USAGE ON SCHEMA task_management_project TO "2338322";
GRANT ALL ON ALL TABLES IN SCHEMA task_management_project TO "2338322";
GRANT ALL ON ALL SEQUENCES IN SCHEMA task_management_project TO "2338322";
GRANT CONNECT ON DATABASE "2139953" TO "2338322";