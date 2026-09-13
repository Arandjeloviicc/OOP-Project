CREATE UNIQUE INDEX IF NOT EXISTS uk_meals_daily_user_date_name
ON meals (user_id, meal_date, name)
WHERE kind = 'DAILY';