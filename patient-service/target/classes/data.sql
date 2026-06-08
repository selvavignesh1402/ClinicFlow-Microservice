INSERT INTO patients (patient_id, mrn, name, dob, gender, contact_info_json, address_json, primary_contact, insurance_id, status, created_at, updated_at, version) VALUES
(1, 'MRN000001', 'Priya Nair', '1992-06-15', 'FEMALE', '{"phone":"9000000001","email":"priya.nair@email.com"}', '{"line1":"12 Lake View","city":"Bengaluru","state":"KA","zip":"560001"}', 'Anil Nair', 'INS1001', 'ACTIVE', '2026-04-01 10:00:00', '2026-04-01 10:00:00', 0),
(2, 'MRN000002', 'Arjun Verma', '1988-11-20', 'MALE', '{"phone":"9000000002","email":"arjun.verma@email.com"}', '{"line1":"45 Green Park","city":"Hyderabad","state":"TS","zip":"500001"}', 'Sneha Verma', 'INS1002', 'ACTIVE', '2026-04-01 10:05:00', '2026-04-01 10:05:00', 0)
ON DUPLICATE KEY UPDATE mrn=mrn;

INSERT INTO problem_list (problem_id, patient_id, code, description, onset_date, status, created_at) VALUES
(1, 1, 'J11', 'Viral fever', '2026-04-08', 'ACTIVE', '2026-04-10 09:15:00'),
(2, 2, 'J06.9', 'Upper respiratory infection', '2026-04-06', 'ACTIVE', '2026-04-11 11:15:00')
ON DUPLICATE KEY UPDATE code=code;
