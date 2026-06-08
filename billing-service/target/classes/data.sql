INSERT INTO invoice (invoice_id, patient_id, encounter_id, line_items_json, subtotal, taxes, discounts, total_amount, issued_at, due_date, status) VALUES
(1, 1, 1, '[{"code":"SRV001","amount":500},{"code":"SRV002","amount":350}]', 850.00, 45.00, 50.00, 845.00, '2026-04-10 10:00:00', '2026-04-15 00:00:00', 'PARTIALLY_PAID'),
(2, 2, 2, '[{"code":"SRV001","amount":500}]', 500.00, 25.00, 0.00, 525.00, '2026-04-11 12:00:00', '2026-04-16 00:00:00', 'PAID')
ON DUPLICATE KEY UPDATE invoice_id=invoice_id;

INSERT INTO payment (payment_id, invoice_id, patient_id, amount, method, paid_at, status) VALUES
(1, 1, 1, 400.00, 'CARD', '2026-04-10 10:30:00', 'SUCCESS'),
(2, 2, 2, 525.00, 'UPI', '2026-04-11 12:15:00', 'SUCCESS')
ON DUPLICATE KEY UPDATE payment_id=payment_id;
