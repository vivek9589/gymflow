-- 📍 Step 1: Add Geofencing coordinate columns to the gyms table
ALTER TABLE gyms
ADD COLUMN latitude DOUBLE PRECISION NULL,
ADD COLUMN longitude DOUBLE PRECISION NULL;

-- 🔐 Step 2: Add the secure passwordless Magic Link token column to the members table
ALTER TABLE members
ADD COLUMN check_in_token VARCHAR(255) NULL;

-- ⚡ Step 3: Populate existing member rows with a fallback random unique token
-- (Ensures older members don't break with a null value on startup)
UPDATE members
SET check_in_token = LOWER(HEX(RANDOM_BYTES(16)))
WHERE check_in_token IS NULL;

-- 🛡️ Step 4: Apply the UNIQUE constraint now that all rows have data
ALTER TABLE members
ADD CONSTRAINT uk_members_check_in_token UNIQUE (check_in_token);

-- 🔑 Step 5: Ensure the column cannot be null for future registrations
ALTER TABLE members
MODIFY COLUMN check_in_token VARCHAR(255) NOT NULL;