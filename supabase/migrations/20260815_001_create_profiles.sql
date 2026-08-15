-- Migration: 20260815_001_create_profiles.sql
-- Description: Create profiles table, constraints, updated_at trigger, handle_new_user auth trigger, and RLS policies.

-- 1. PROFILES TABLE
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username TEXT UNIQUE NOT NULL,
    display_name TEXT NOT NULL,
    avatar_url TEXT NULL,
    timezone TEXT NOT NULL DEFAULT 'UTC',
    created_at TIMESTAMPTZ NOT NULL DEFAULT pg_catalog.now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT pg_catalog.now(),

    -- Constraints
    -- Username: Lowercase alphanumeric + underscore, 3-20 characters
    CONSTRAINT username_format_and_length CHECK (username ~ '^[a-z0-9_]{3,20}$'),
    
    -- Display Name: Trimmed length between 2 and 50 characters
    CONSTRAINT display_name_length CHECK (
        pg_catalog.char_length(pg_catalog.btrim(display_name)) >= 2 
        AND pg_catalog.char_length(pg_catalog.btrim(display_name)) <= 50
    )
);

-- 2. UPDATED_AT TRIGGER FUNCTION (Security Invoker)
CREATE OR REPLACE FUNCTION public.handle_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = pg_catalog.now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER on_profiles_updated
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_updated_at();

-- 3. USER REGISTRATION TRIGGER FUNCTION (Security Definer with strict search_path)
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
SECURITY DEFINER
SET search_path = ''
AS $$
DECLARE
    v_raw_username TEXT;
    v_username TEXT;
    v_display_name TEXT;
    v_timezone TEXT;
BEGIN
    -- 1. Extract metadata and canonicalize
    v_raw_username := NEW.raw_user_meta_data->>'username';
    v_display_name := pg_catalog.btrim(COALESCE(NEW.raw_user_meta_data->>'display_name', ''));
    v_timezone := pg_catalog.btrim(COALESCE(NEW.raw_user_meta_data->>'timezone', 'UTC'));

    -- Default to UTC if empty
    IF v_timezone = '' THEN
        v_timezone := 'UTC';
    END IF;

    -- Validate and lowercase username
    IF v_raw_username IS NULL OR pg_catalog.btrim(v_raw_username) = '' THEN
        RAISE EXCEPTION 'Username is required';
    END IF;
    v_username := pg_catalog.lower(pg_catalog.btrim(v_raw_username));

    -- Validate display name
    IF v_display_name = '' THEN
        RAISE EXCEPTION 'Display name is required';
    END IF;

    -- 2. Validate timezone against PostgreSQL system catalog
    IF NOT EXISTS (
        SELECT 1 FROM pg_catalog.pg_timezone_names WHERE name = v_timezone
    ) THEN
        RAISE EXCEPTION 'Invalid timezone identifier: %', v_timezone;
    END IF;

    -- 3. Insert into public.profiles
    -- Triggers table constraints (e.g. username regex, display_name length, uniqueness)
    -- Any constraint violation causes an exception and rolls back the auth signup transaction
    INSERT INTO public.profiles (id, username, display_name, timezone)
    VALUES (NEW.id, v_username, v_display_name, v_timezone);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();

-- 4. ROW LEVEL SECURITY (RLS)
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- SELECT: Users can only view their own profile
CREATE POLICY "Users can view their own profile"
ON public.profiles FOR SELECT
TO authenticated
USING ((SELECT auth.uid()) = id);

-- UPDATE: Users can only update their own profile
CREATE POLICY "Users can update their own profile"
ON public.profiles FOR UPDATE
TO authenticated
USING ((SELECT auth.uid()) = id)
WITH CHECK ((SELECT auth.uid()) = id);
