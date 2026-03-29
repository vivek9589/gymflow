ALTER TABLE gyms
ADD COLUMN whatsapp_instance VARCHAR(255) UNIQUE,
ADD COLUMN whatsapp_status VARCHAR(50);