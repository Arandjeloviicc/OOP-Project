DROP INDEX idx_nutrition_goals_user_start_date;

CREATE UNIQUE INDEX uk_nutrition_goals_user_start_date
    ON nutrition_goals (user_id, start_date);