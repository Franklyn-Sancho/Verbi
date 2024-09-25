REVOKE CONNECT ON DATABASE verbidb FROM public;
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'verbidb' AND pid <> pg_backend_pid();
REVOKE
