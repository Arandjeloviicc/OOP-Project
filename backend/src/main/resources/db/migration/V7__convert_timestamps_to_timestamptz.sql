-- ── Users ────────────────────────────────────────────────────────
ALTER TABLE users
ALTER COLUMN created_at
        TYPE timestamptz(6)
        USING created_at AT TIME ZONE 'UTC';


-- ── User Profiles ────────────────────────────────────────────────
ALTER TABLE user_profiles
ALTER COLUMN created_at
        TYPE timestamptz(6)
        USING created_at AT TIME ZONE 'UTC',

    ALTER COLUMN updated_at
        TYPE timestamptz(6)
        USING updated_at AT TIME ZONE 'UTC';


-- ── Weight Logs ──────────────────────────────────────────────────
ALTER TABLE weight_logs
ALTER COLUMN created_at
        TYPE timestamptz(6)
        USING created_at AT TIME ZONE 'UTC',

    ALTER COLUMN logged_at
        TYPE timestamptz(6)
        USING logged_at AT TIME ZONE 'UTC',

    ALTER COLUMN updated_at
        TYPE timestamptz(6)
        USING updated_at AT TIME ZONE 'UTC';


-- ── Foods ────────────────────────────────────────────────────────
ALTER TABLE foods
ALTER COLUMN created_at
        TYPE timestamptz(6)
        USING created_at AT TIME ZONE 'UTC',

    ALTER COLUMN updated_at
        TYPE timestamptz(6)
        USING updated_at AT TIME ZONE 'UTC';


-- ── Meals ────────────────────────────────────────────────────────
ALTER TABLE meals
ALTER COLUMN created_at
        TYPE timestamptz(6)
        USING created_at AT TIME ZONE 'UTC',

    ALTER COLUMN updated_at
        TYPE timestamptz(6)
        USING updated_at AT TIME ZONE 'UTC';


-- ── Meal Items ───────────────────────────────────────────────────
ALTER TABLE meal_items
ALTER COLUMN created_at
        TYPE timestamptz(6)
        USING created_at AT TIME ZONE 'UTC',

    ALTER COLUMN updated_at
        TYPE timestamptz(6)
        USING updated_at AT TIME ZONE 'UTC';


-- ── Nutrition Goals ──────────────────────────────────────────────
ALTER TABLE nutrition_goals
ALTER COLUMN created_at
        TYPE timestamptz(6)
        USING created_at AT TIME ZONE 'UTC',

    ALTER COLUMN updated_at
        TYPE timestamptz(6)
        USING updated_at AT TIME ZONE 'UTC';


-- ── Body Measurement Logs ────────────────────────────────────────
ALTER TABLE body_measurement_logs
ALTER COLUMN logged_at
        TYPE timestamptz(6)
        USING logged_at AT TIME ZONE 'UTC',

    ALTER COLUMN created_at
        TYPE timestamptz(6)
        USING created_at AT TIME ZONE 'UTC',

    ALTER COLUMN updated_at
        TYPE timestamptz(6)
        USING updated_at AT TIME ZONE 'UTC';