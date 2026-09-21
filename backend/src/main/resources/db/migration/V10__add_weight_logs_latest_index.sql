CREATE INDEX idx_weight_logs_user_logged_at_id
    ON weight_logs (
        user_id,
        logged_at DESC,
        id DESC
    );