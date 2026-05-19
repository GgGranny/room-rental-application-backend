
-- Create User table
INSERT INTO users (
    id,
    created_at,
    updated_at,
    is_deleted,
    email,
    phone_number,
    password,
    role,
    first_name,
    last_name,
    date_of_birth,
    profile_picture_url,
    provider,
    provider_id,
    is_verified,
    is_active
) VALUES (
    '1beecb10-5543-48a8-a481-480e12345678',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    false,
    'dishcovery28@gmail.com',
    '9860972650',
    '$2a$12$ZOh9D3yBohmZxE.rXgIEzefsmQ2dEGCIXm0lirIT3Uxlx3WBBsSFq',
    'ROLE_USER',
    'Ram',
    'Rai',
    '1999-11-01',
    'uploads/profile/ram.jpg',
    'LOCAL',
    NULL,
    true,
    true
);
