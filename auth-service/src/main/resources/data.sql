-- Default password for seeded users: password
INSERT INTO users (user_id, name, email, password, phone, role, status, created_at, updated_at) VALUES
(1, 'Dr. Asha Mehta', 'asha.mehta@clinicflow.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.Hm8z5G0siJmq9hw3rroWAt4EvsC0Bp', '9876500001', 'CLINICIAN', 'ACTIVE', '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(2, 'Rahul Sharma', 'rahul.sharma@clinicflow.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.Hm8z5G0siJmq9hw3rroWAt4EvsC0Bp', '9876500002', 'ADMIN', 'ACTIVE', '2026-04-01 09:15:00', '2026-04-01 09:15:00')
ON DUPLICATE KEY UPDATE email=email;
