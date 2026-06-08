INSERT INTO medication_master (med_id, code, name, formulation, strength, atc_code, controlled_flag, status) VALUES
(1, 'MED001', 'Paracetamol', 'Tablet', '500mg', 'N02BE01', 0, 'ACTIVE'),
(2, 'MED002', 'Amoxicillin', 'Capsule', '250mg', 'J01CA04', 0, 'ACTIVE')
ON DUPLICATE KEY UPDATE med_id=med_id;

INSERT INTO inventory_item (inventory_id, med_id, batch_number, quantity, unit, expiry_date, location, cost_price, status) VALUES
(1, 1, 'BATCH001', 500, 'Tablet', '2028-12-31', 'Pharmacy Rack A', 1.50, 'IN_STOCK'),
(2, 2, 'BATCH002', 200, 'Capsule', '2027-06-30', 'Pharmacy Rack B', 3.00, 'IN_STOCK')
ON DUPLICATE KEY UPDATE inventory_id=inventory_id;

INSERT INTO dispense_record (dispense_id, rx_id, inventory_id, patient_id, dispensed_by_fk, quantity, dispensed_at, notes, status) VALUES
(1, 1, 1, 1, 1, 15, '2026-04-10 10:00:00', 'Dispensed standard dosage', 'DISPENSED'),
(2, 2, 2, 2, 1, 14, '2026-04-11 12:00:00', 'Dispensed complete course', 'DISPENSED')
ON DUPLICATE KEY UPDATE dispense_id=dispense_id;
