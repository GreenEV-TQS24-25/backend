INSERT INTO user_table (name, email, password, role) VALUES
                                                             ('admin', 'admin@admin.com', '$2b$12$oRjaIuC3w9Q/.5QED/42yu0nlRBxBCEXrB2abXwq5FPk858REL7XO', 'OPERATOR'),
                                                             ('Jane Operator', 'jane@example.com', '$2b$12$oRjaIuC3w9Q/.5QED/42yu0nlRBxBCEXrB2abXwq5FPk858REL7XO', 'OPERATOR'),
                                                             ('Alice User', 'alice@example.com', '$2b$12$oRjaIuC3w9Q/.5QED/42yu0nlRBxBCEXrB2abXwq5FPk858REL7XO', 'USER'),
                                                             ('Bob User', 'bob@example.com', '$2b$12$oRjaIuC3w9Q/.5QED/42yu0nlRBxBCEXrB2abXwq5FPk858REL7XO', 'USER'),
                                                             ('user', 'user@user.com', '$2b$12$oRjaIuC3w9Q/.5QED/42yu0nlRBxBCEXrB2abXwq5FPk858REL7XO', 'USER');

INSERT INTO vehicle (user_id, brand, model, license_plate, connector_type) VALUES
                                                                                   (3, 'Tesla', 'Model S', 'ABC-123', 'SAEJ1772'),
                                                                                   (4, 'Nissan', 'Leaf', 'XYZ-789', 'CHADEMO'),
                                                                                   (5, 'BMW', 'i3', 'DEF-456', 'CCS');

INSERT INTO charging_station (name, lat, lon, operator_id, photo_url) VALUES
                                                                              ('Downtown Charger', 40.7128, -74.0060, 1, 'http://example.com/photo1.jpg'),
                                                                              ('Uptown Power', 34.0522, -118.2437, 2, 'http://example.com/photo2.jpg');

INSERT INTO charging_spot (station_id, charging_velocity, connector_type, power_kw, price_per_kwh, state) VALUES
                                                                                                                  (1, 'NORMAL', 'SAEJ1772', 7.5, 0.30, 'FREE'),
                                                                                                                  (2, 'FAST', 'CCS', 50.00, 0.40, 'OCCUPIED'),
                                                                                                                  (2, 'FASTPP', 'CHADEMO', 150.00, 0.50, 'FREE'),
                                                                                                                  (2, 'FASTPP', 'MENNEKES', 150.00, 0.50, 'OUT_OF_SERVICE');

INSERT INTO session (uuid, vehicle_id, charging_spot_id, start_time, duration, total_cost) VALUES
                                                                                                   ('550e8400-e29b-41d4-a716-446655440000', 1, 1, '2024-03-20 08:00:00', 120, 4.50),
                                                                                                   ('6ba7b810-9dad-11d1-80b4-00c04fd430c8', 2, 3, '2024-03-20 11:30:00', 45, 56.25),
                                                                                                   ('123e4567-e89b-12d3-a456-426614174000', 3, 2, '2024-03-20 09:00:00', 60, 20.00);

INSERT INTO payment (session_id, price, method, state, transaction_id, date_hour) VALUES
                                                                                          (1, 4.50, 'Credit Card', 'COMPLETED', 'txn_12345', '2024-03-20 08:05:00'),
                                                                                          (2, 56.25, 'PayPal', 'PENDING', 'txn_67890', '2024-03-20 11:35:00'),
                                                                                          (3, 20.00, 'Debit Card', 'COMPLETED', 'txn_11223', '2024-03-20 09:05:00');