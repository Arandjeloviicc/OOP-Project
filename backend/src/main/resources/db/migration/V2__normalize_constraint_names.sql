DO $$
BEGIN

    -- Users
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'users_pkey'
          AND conrelid = 'public.users'::regclass
    ) THEN
        ALTER TABLE public.users
            RENAME CONSTRAINT users_pkey TO pk_users;
    END IF;


    -- User Profiles
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'user_profiles_pkey'
          AND conrelid = 'public.user_profiles'::regclass
    ) THEN
        ALTER TABLE public.user_profiles
            RENAME CONSTRAINT user_profiles_pkey TO pk_user_profiles;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'user_profiles_fkey'
          AND conrelid = 'public.user_profiles'::regclass
    ) THEN
        ALTER TABLE public.user_profiles
            RENAME CONSTRAINT user_profiles_fkey TO fk_user_profiles_user;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'user_profiles_activity_level_check'
          AND conrelid = 'public.user_profiles'::regclass
    ) THEN
        ALTER TABLE public.user_profiles
            RENAME CONSTRAINT user_profiles_activity_level_check
            TO chk_user_profiles_activity_level;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'user_profiles_gender_check'
          AND conrelid = 'public.user_profiles'::regclass
    ) THEN
        ALTER TABLE public.user_profiles
            RENAME CONSTRAINT user_profiles_gender_check
            TO chk_user_profiles_gender;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'user_profiles_goal_type_check'
          AND conrelid = 'public.user_profiles'::regclass
    ) THEN
        ALTER TABLE public.user_profiles
            RENAME CONSTRAINT user_profiles_goal_type_check
            TO chk_user_profiles_goal_type;
    END IF;


    -- Weight Logs
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'weight_logs_pkey'
          AND conrelid = 'public.weight_logs'::regclass
    ) THEN
        ALTER TABLE public.weight_logs
            RENAME CONSTRAINT weight_logs_pkey TO pk_weight_logs;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'weight_logs_fkey'
          AND conrelid = 'public.weight_logs'::regclass
    ) THEN
        ALTER TABLE public.weight_logs
            RENAME CONSTRAINT weight_logs_fkey TO fk_weight_logs_user;
    END IF;


    -- Foods
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'foods_pkey'
          AND conrelid = 'public.foods'::regclass
    ) THEN
        ALTER TABLE public.foods
            RENAME CONSTRAINT foods_pkey TO pk_foods;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk8wc6dwycvdi2myrltglfci4og'
          AND conrelid = 'public.foods'::regclass
    ) THEN
        ALTER TABLE public.foods
            RENAME CONSTRAINT fk8wc6dwycvdi2myrltglfci4og
            TO fk_foods_created_by_user;
    END IF;


    -- Meals
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'meals_pkey'
          AND conrelid = 'public.meals'::regclass
    ) THEN
        ALTER TABLE public.meals
            RENAME CONSTRAINT meals_pkey TO pk_meals;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk677c66qpjr7234luomahc1ale'
          AND conrelid = 'public.meals'::regclass
    ) THEN
        ALTER TABLE public.meals
            RENAME CONSTRAINT fk677c66qpjr7234luomahc1ale
            TO fk_meals_user;
    END IF;


    -- Meal Items
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'meal_items_pkey'
          AND conrelid = 'public.meal_items'::regclass
    ) THEN
        ALTER TABLE public.meal_items
            RENAME CONSTRAINT meal_items_pkey TO pk_meal_items;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fkchoxjvcjim16ipnv0se5w3uyu'
          AND conrelid = 'public.meal_items'::regclass
    ) THEN
        ALTER TABLE public.meal_items
            RENAME CONSTRAINT fkchoxjvcjim16ipnv0se5w3uyu
            TO fk_meal_items_food;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fkbkra8m7kb523wvlyg55di7ecx'
          AND conrelid = 'public.meal_items'::regclass
    ) THEN
        ALTER TABLE public.meal_items
            RENAME CONSTRAINT fkbkra8m7kb523wvlyg55di7ecx
            TO fk_meal_items_meal;
    END IF;

END
$$;