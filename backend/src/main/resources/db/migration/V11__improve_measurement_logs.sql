-- ── Weight Logs Constraints ──────────────────────────────────────
ALTER TABLE weight_logs
    ADD CONSTRAINT chk_weight_logs_weight
        CHECK (weight > 0);


-- ── Body Measurement Logs Indexes ───────────────────────────────
DROP INDEX idx_body_measurement_logs_user_logged_at;

CREATE INDEX idx_body_measurement_logs_user_logged_at_id
    ON body_measurement_logs (
          user_id,
          logged_at DESC,
          id DESC
    );