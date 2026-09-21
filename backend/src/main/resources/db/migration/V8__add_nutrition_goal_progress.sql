-- ── Nutrition Goal Progress ──────────────────────────────────────
ALTER TABLE nutrition_goals
    ADD COLUMN progress_start_weight double precision,
    ADD COLUMN progress_started_at timestamptz(6);


-- Backfill existing development data using the old progress semantics.
UPDATE nutrition_goals ng
SET
    progress_start_weight = (
        SELECT wl.weight
        FROM weight_logs wl
        WHERE wl.user_id = ng.user_id
          AND wl.logged_at <= ng.created_at
        ORDER BY wl.logged_at DESC, wl.id DESC
    LIMIT 1
    ),
    progress_started_at = ng.created_at;


ALTER TABLE nutrition_goals
    ALTER COLUMN progress_start_weight SET NOT NULL,
ALTER COLUMN progress_started_at SET NOT NULL;


ALTER TABLE nutrition_goals
    ADD CONSTRAINT chk_nutrition_goals_progress_start_weight
        CHECK (progress_start_weight > 0);