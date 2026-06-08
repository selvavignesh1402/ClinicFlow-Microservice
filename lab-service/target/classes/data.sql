INSERT INTO lab_order (lab_order_id, encounter_id, patient_id, ordered_by_fk, tests_json, sample_id, collected_at, status, result_uri) VALUES
(1, 1, 1, 1, '{"tests":["CBC","CRP"]}', 'SMP1001', '2026-04-10 09:35:00', 'COLLECTED', '/lab/orders/1/results'),
(2, 2, 2, 1, '{"tests":["Chest X-Ray"]}', 'SMP1002', '2026-04-11 11:40:00', 'RESULTS_REPORTED', '/lab/orders/2/results')
ON DUPLICATE KEY UPDATE lab_order_id=lab_order_id;

INSERT INTO lab_result (result_id, lab_order_id, test_code, value, units, reference_range_json, flag, reported_at, reported_by) VALUES
(1, 1, 'CBC-WBC', '8600', 'cells/uL', '{"min":"4000","max":"11000"}', 'NORMAL', '2026-04-10 14:00:00', 1),
(2, 2, 'XR-CHEST', 'Mild bronchitic changes', 'TEXT', '{"type":"narrative"}', 'NORMAL', '2026-04-11 16:30:00', 1)
ON DUPLICATE KEY UPDATE result_id=result_id;
