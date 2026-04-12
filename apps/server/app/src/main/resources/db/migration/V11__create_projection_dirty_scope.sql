CREATE TABLE projection_dirty_scope (
    scope_type VARCHAR(32) NOT NULL,
    scope_id VARCHAR(64) NOT NULL,
    marked_at BIGINT NOT NULL,
    PRIMARY KEY (scope_type, scope_id)
);

CREATE INDEX idx_projection_dirty_scope_marked_at
    ON projection_dirty_scope (marked_at, scope_type, scope_id);
