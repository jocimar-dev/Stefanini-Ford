CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_created_at ON tasks(created_at);
ALTER TABLE tasks ADD CONSTRAINT chk_tasks_status CHECK (status IN ('PENDING','IN_PROGRESS','DONE'));
