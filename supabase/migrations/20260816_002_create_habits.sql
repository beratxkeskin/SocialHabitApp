-- ============================================================================
-- SPRINT 2.1 MIGRATION: Habits and Habit Members Foundation
-- ============================================================================

-- 1. Private Security Schema (PostgREST API'ye kapalı izole şema)
CREATE SCHEMA IF NOT EXISTS private;

-- 2. Selected Days Doğrulama Fonksiyonu (cardinality güvenli, 1..7 aralığı, UNIQUE kontrolü)
CREATE OR REPLACE FUNCTION private.is_valid_selected_days(p_days INT[])
RETURNS BOOLEAN
LANGUAGE sql
IMMUTABLE
SET search_path = ''
AS $$
    SELECT p_days IS NOT NULL
       AND pg_catalog.cardinality(p_days) BETWEEN 1 AND 7
       AND p_days <@ ARRAY[1,2,3,4,5,6,7]::INT[]
       AND (
           SELECT pg_catalog.count(DISTINCT elem) 
           FROM pg_catalog.unnest(p_days) AS elem
       ) = pg_catalog.cardinality(p_days);
$$;

-- 3. Habits Tablosu
CREATE TABLE IF NOT EXISTS public.habits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    description TEXT NULL,
    frequency_type TEXT NOT NULL,
    target_days_per_week INT NULL,
    selected_days INT[] NULL,
    created_by UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT pg_catalog.now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT pg_catalog.now(),
    archived_at TIMESTAMPTZ NULL,

    -- Başlık kısıtlaması: Boş olamaz, 1-100 karakter
    CONSTRAINT habit_title_length CHECK (
        pg_catalog.char_length(pg_catalog.btrim(title)) >= 1 
        AND pg_catalog.char_length(pg_catalog.btrim(title)) <= 100
    ),

    -- Frequency Type İzin Verilen Değerler
    CONSTRAINT habit_frequency_type_check CHECK (
        frequency_type IN ('daily', 'selected_days', 'weekly_target')
    ),

    -- Frequency Kuralları ve Redundant Veri Önleme
    CONSTRAINT habit_frequency_rules CHECK (
        (frequency_type = 'daily' AND target_days_per_week IS NULL AND selected_days IS NULL) OR
        (frequency_type = 'selected_days' AND selected_days IS NOT NULL AND target_days_per_week IS NULL) OR
        (frequency_type = 'weekly_target' AND target_days_per_week BETWEEN 1 AND 7 AND selected_days IS NULL)
    ),

    -- Selected Days: 1..7 elemanları, duplicate içermeyen ve boş olmayan dizi
    CONSTRAINT habit_selected_days_check CHECK (
        frequency_type != 'selected_days' OR private.is_valid_selected_days(selected_days)
    )
);

-- 4. Habit Members Tablosu (Aktif Üyelikler ve Sahiplik)
CREATE TABLE IF NOT EXISTS public.habit_members (
    habit_id UUID NOT NULL REFERENCES public.habits(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    role TEXT NOT NULL DEFAULT 'member',
    joined_at TIMESTAMPTZ NOT NULL DEFAULT pg_catalog.now(),

    PRIMARY KEY (habit_id, user_id),
    CONSTRAINT habit_member_role_check CHECK (role IN ('owner', 'member'))
);

-- 5. İndeksler ve Single-Owner Partial Unique Index
CREATE INDEX idx_habit_members_user_id ON public.habit_members(user_id);
CREATE INDEX idx_habits_active_created_by ON public.habits(created_by) WHERE archived_at IS NULL;

-- Her habit için kesinlikle en fazla 1 adet 'owner' olabilir
CREATE UNIQUE INDEX idx_habit_members_single_owner 
ON public.habit_members(habit_id) 
WHERE role = 'owner';

-- 6. Private Helper Fonksiyonlar (Recursion-safe, caller-isolated)
CREATE OR REPLACE FUNCTION private.is_habit_member(p_habit_id UUID)
RETURNS BOOLEAN
LANGUAGE sql
SECURITY DEFINER
SET search_path = ''
STABLE
AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.habit_members
        WHERE habit_id = p_habit_id 
          AND user_id = (SELECT auth.uid())
    );
$$;

CREATE OR REPLACE FUNCTION private.is_habit_owner(p_habit_id UUID)
RETURNS BOOLEAN
LANGUAGE sql
SECURITY DEFINER
SET search_path = ''
STABLE
AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.habit_members
        WHERE habit_id = p_habit_id 
          AND user_id = (SELECT auth.uid()) 
          AND role = 'owner'
    );
$$;

-- 7. Trigger Fonksiyonları

-- A. Creator -> Owner Otomatik Üyelik Trigger'ı
CREATE OR REPLACE FUNCTION public.handle_new_habit()
RETURNS TRIGGER
SECURITY DEFINER
SET search_path = ''
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO public.habit_members (habit_id, user_id, role)
    VALUES (NEW.id, NEW.created_by, 'owner');
    RETURN NEW;
END;
$$;

CREATE OR REPLACE TRIGGER on_habit_created
    AFTER INSERT ON public.habits
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_habit();

-- B. created_by ve id Değiştirilemezlik (Immutability) Trigger'ı
CREATE OR REPLACE FUNCTION public.handle_habit_immutable_fields()
RETURNS TRIGGER
LANGUAGE plpgsql
SET search_path = ''
AS $$
BEGIN
    IF NEW.created_by != OLD.created_by THEN
        RAISE EXCEPTION 'Field created_by is immutable and cannot be changed';
    END IF;
    IF NEW.id != OLD.id THEN
        RAISE EXCEPTION 'Field id is immutable and cannot be changed';
    END IF;
    RETURN NEW;
END;
$$;

CREATE OR REPLACE TRIGGER on_habit_immutable_check
    BEFORE UPDATE ON public.habits
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_habit_immutable_fields();

-- C. updated_at Otomatik Güncelleme Trigger'ı (Mevcut public.handle_updated_at fonksiyonunu kullanır)
CREATE OR REPLACE TRIGGER on_habits_updated_at
    BEFORE UPDATE ON public.habits
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_updated_at();

-- 8. Row Level Security (RLS) Etkinleştirme
ALTER TABLE public.habits ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.habit_members ENABLE ROW LEVEL SECURITY;

-- 9. RLS Politikaları

-- HABITS POLICIES:
-- SELECT: Alışkanlığın üyesi olanlar VEYA oluşturan kullanıcı okuyabilir
-- (created_by kontrolü INSERT ... RETURNING cevabının anında dönmesini garanti eder)
CREATE POLICY "Users can view habits they belong to"
ON public.habits
FOR SELECT
TO authenticated
USING (
    created_by = (SELECT auth.uid()) 
    OR private.is_habit_member(id)
);

-- INSERT: Yalnızca authenticated kullanıcılar kendi adına alışkanlık oluşturabilir
CREATE POLICY "Users can create habits"
ON public.habits
FOR INSERT
TO authenticated
WITH CHECK (created_by = (SELECT auth.uid()));

-- UPDATE: Yalnızca owner rolüne sahip kullanıcılar alışkanlığı güncelleyebilir/arşivleyebilir
CREATE POLICY "Owners can update their habits"
ON public.habits
FOR UPDATE
TO authenticated
USING (private.is_habit_owner(id))
WITH CHECK (private.is_habit_owner(id));

-- DELETE: Client-side DELETE tamamen yasaktır (Arşivleme kullanılır).

-- HABIT_MEMBERS POLICIES:
-- SELECT: Kullanıcı kendi üyeliklerini VEYA üyesi olduğu habit'in diğer üyelerini görebilir
CREATE POLICY "Users can view members of their habits"
ON public.habit_members
FOR SELECT
TO authenticated
USING (
    user_id = (SELECT auth.uid()) 
    OR private.is_habit_member(habit_id)
);

-- INSERT / UPDATE / DELETE: Client-side doğrudan erişim politikası yoktur (Trigger ve davet akışı yönetir).

-- 10. Şema ve Fonksiyon Yetki Yönetimi (Least-Privilege)
GRANT USAGE ON SCHEMA private TO authenticated;

REVOKE EXECUTE ON ALL FUNCTIONS IN SCHEMA private FROM PUBLIC;
GRANT EXECUTE ON FUNCTION private.is_valid_selected_days(INT[]) TO authenticated;
GRANT EXECUTE ON FUNCTION private.is_habit_member(UUID) TO authenticated;
GRANT EXECUTE ON FUNCTION private.is_habit_owner(UUID) TO authenticated;
