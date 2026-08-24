-- Create a separate application user with DML-only privileges.
-- Flyway migrations run as the 'verbonden' superuser (DDL).
-- The application connects as 'verbonden_app' (DML only).

CREATE USER verbonden_app WITH PASSWORD 'verbonden_app';

-- Grant connect and usage
GRANT CONNECT ON DATABASE verbonden TO verbonden_app;
GRANT USAGE ON SCHEMA public TO verbonden_app;

-- Grant DML on all existing tables and sequences
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO verbonden_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO verbonden_app;

-- Ensure future tables/sequences created by 'verbonden' also get DML grants
ALTER DEFAULT PRIVILEGES FOR USER verbonden IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO verbonden_app;
ALTER DEFAULT PRIVILEGES FOR USER verbonden IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO verbonden_app;
