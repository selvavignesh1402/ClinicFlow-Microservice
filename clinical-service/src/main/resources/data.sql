INSERT INTO appointments (appt_id, patient_id, clinician_id, department, service_type, start_at, end_at, status, created_by, created_at) VALUES
(1, 1, 1, 'General Medicine', 'Consultation', '2026-04-10 09:00:00', '2026-04-10 09:30:00', 'COMPLETED', 2, '2026-04-09 16:00:00'),
(2, 2, 1, 'General Medicine', 'Follow Up', '2026-04-11 11:00:00', '2026-04-11 11:30:00', 'COMPLETED', 2, '2026-04-10 17:00:00')
ON DUPLICATE KEY UPDATE status=status;

INSERT INTO encounters (encounter_id, patient_id, clinician_id, visit_type, chief_complaint, vitals_json, notes_json, diagnoses_json, orders_json, prescriptions_json, start_at, end_at, status, signed_by, signed_at) VALUES
(1, 1, 1, 'OPD', 'Fever and body pain', '{"temp":"101F","bp":"110/70","pulse":"92"}', '{"soap":"Patient reports fever for 2 days"}', '{"primary":"Viral fever"}', '{"lab":["CBC"]}', '{"planned":["Paracetamol"]}', '2026-04-10 09:00:00', '2026-04-10 09:20:00', 'COMPLETED', 1, '2026-04-10 09:25:00'),
(2, 2, 1, 'FOLLOW_UP', 'Cough and sore throat', '{"temp":"99F","bp":"120/80","pulse":"88"}', '{"soap":"Dry cough for 5 days"}', '{"primary":"Upper respiratory infection"}', '{"lab":["XRay Chest"]}', '{"planned":["Amoxicillin"]}', '2026-04-11 11:00:00', '2026-04-11 11:25:00', 'COMPLETED', 1, '2026-04-11 11:30:00')
ON DUPLICATE KEY UPDATE status=status;

INSERT INTO prescriptions (rx_id, encounter_id, patient_id, clinician_id, med_id, dosage, frequency, duration_days, quantity, repeats, route, notes, status, issued_at) VALUES
(1, 1, 1, 1, 1, '1 tablet', 'TID', 5, 15, 0, 'ORAL', 'After food', 'ISSUED', '2026-04-10 09:15:00'),
(2, 2, 2, 1, 2, '1 capsule', 'BID', 7, 14, 0, 'ORAL', 'Complete the course', 'ISSUED', '2026-04-11 11:15:00')
ON DUPLICATE KEY UPDATE status=status;

INSERT INTO report (report_id, scope, parameters_json, metrics_json, generated_by_fk, generated_at, report_uri) VALUES
(1, 'CLINIC', '{"startDate":"2026-03-01","endDate":"2026-03-15"}', '{"totalEncounters":42}', 2, '2026-03-16 08:00:00', '/reports/clinic-2026-03a.pdf'),
(2, 'FINANCE', '{"startDate":"2026-03-16","endDate":"2026-03-31"}', '{"totalRevenue":125000}', 2, '2026-04-01 08:00:00', '/reports/finance-2026-03b.pdf')
ON DUPLICATE KEY UPDATE scope=scope;

INSERT INTO task (task_id, assigned_to_fk, related_entity_id, description, due_date, priority, created_at, completed_at, status) VALUES
(1, 1, 'patient-1', 'Follow up on lab result for Priya Nair', '2026-04-12 18:00:00', 'HIGH', '2026-04-10 14:05:00', NULL, 'PENDING'),
(2, 2, 'invoice-2', 'Verify insurance claim for Arjun Verma', '2026-04-15 17:00:00', 'MEDIUM', '2026-04-11 12:30:00', '2026-04-12 10:00:00', 'COMPLETED')
ON DUPLICATE KEY UPDATE status=status;
