-- =============================================================
-- Migration: Create `user_accounts` table for extended profile info
-- Database: PostgreSQL (Supabase-compatible)
-- Date: 2025-07-02
-- =============================================================

-- 1. Create the user_accounts table
CREATE TABLE IF NOT EXISTS public.user_accounts (
  id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  full_name TEXT,
  email TEXT UNIQUE,
  institution TEXT,
  department TEXT,
  position TEXT,
  bio TEXT,
  profile_image_url TEXT,
  profile_image_filename TEXT,
  website_url TEXT,
  google_scholar_url TEXT,
  linkedin_url TEXT,
  github_url TEXT,
  facebook_url TEXT,
  orcid_id TEXT,
  address_line_1 TEXT,
  address_line_2 TEXT,
  city TEXT,
  state_province_region TEXT,
  postal_code TEXT,
  country TEXT,
  language_preference TEXT DEFAULT 'en',
  timezone TEXT DEFAULT 'UTC',
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Comments for documentation
COMMENT ON TABLE public.user_accounts IS 'User profile information for ScholarAI.';
COMMENT ON COLUMN public.user_accounts.id IS 'Links to the corresponding user in auth.users.';

-- 3. Enable Row Level Security
ALTER TABLE public.user_accounts ENABLE ROW LEVEL SECURITY;

-- 4. Row-Level Security Policies
-- 4.1 Allow the user to SELECT their own data
CREATE POLICY user_account_select_own
  ON public.user_accounts
  FOR SELECT
  USING (auth.uid() = id);

-- 4.2 Allow the user to UPDATE their own data
CREATE POLICY user_account_update_own
  ON public.user_accounts
  FOR UPDATE
  USING (auth.uid() = id);

-- 5. Trigger function to create user_accounts on new user sign-up
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  INSERT INTO public.user_accounts (id, email, full_name)
  VALUES (NEW.id, NEW.email, NEW.raw_user_meta_data->>'full_name');
  RETURN NEW;
END;
$$;

-- 6. Trigger binding the function to auth.users
CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW
  EXECUTE FUNCTION public.handle_new_user();
