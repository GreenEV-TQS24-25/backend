INSERT INTO user_table (id, name, email, password, role) VALUES
                                                             (1, 'admin', 'admin@admin.com', 'password123', 'OPERATOR'),
                                                             (2, 'Jane Operator', 'jane@example.com', 'password123', 'OPERATOR'),
                                                             (3, 'Alice User', 'alice@example.com', 'password123', 'USER'),
                                                             (4, 'Bob User', 'bob@example.com', 'password123', 'USER'),
                                                             (5, 'user', 'user@user.com', 'password123', 'USER');

INSERT INTO vehicle (id, user_id, brand, model, license_plate, connector_type) VALUES
                                                                                   (1, 3, 'Tesla', 'Model S', 'ABC-123', 'SAEJ1772'),
                                                                                   (2, 4, 'Nissan', 'Leaf', 'XYZ-789', 'CHADEMO'),
                                                                                   (3, 5, 'BMW', 'i3', 'DEF-456', 'CCS');

INSERT INTO charging_station (id, name, lat, lon, operator_id, photo_url) VALUES
                                                                              (1, 'Downtown Charger', 40.7128, -74.0060, 1, 'http://example.com/photo1.jpg'),
                                                                              (2, 'Uptown Power', 34.0522, -118.2437, 2, 'http://example.com/photo2.jpg');

INSERT INTO charging_spot (id, station_id, charging_velocity, connector_type, power_kw, price_per_kwh, state) VALUES
                                                                                                                  (1, 1, 'NORMAL', 'SAEJ1772', 7.5, 0.30, 'FREE'),
                                                                                                                  (2, 1, 'FAST', 'CCS', 50.00, 0.40, 'OCCUPIED'),
                                                                                                                  (3, 2, 'FASTPP', 'CHADEMO', 150.00, 0.50, 'FREE'),
                                                                                                                  (4, 2, 'FASTPP', 'MENNEKES', 150.00, 0.50, 'OUT_OF_SERVICE');

INSERT INTO session (id, uuid, vehicle_id, charging_spot_id, start_time, duration, total_cost) VALUES
                                                                                                   (1, '550e8400-e29b-41d4-a716-446655440000', 1, 1, '2024-03-20 08:00:00', 120, 4.50),
                                                                                                   (2, '6ba7b810-9dad-11d1-80b4-00c04fd430c8', 2, 3, '2024-03-20 11:30:00', 45, 56.25),
                                                                                                   (3, '123e4567-e89b-12d3-a456-426614174000', 3, 2, '2024-03-20 09:00:00', 60, 20.00);

INSERT INTO payment (id, session_id, price, method, state, transaction_id, date_hour) VALUES
                                                                                          (1, 1, 4.50, 'Credit Card', 'COMPLETED', 'txn_12345', '2024-03-20 08:05:00'),
                                                                                          (2, 2, 56.25, 'PayPal', 'PENDING', 'txn_67890', '2024-03-20 11:35:00'),
                                                                                          (3, 3, 20.00, 'Debit Card', 'COMPLETED', 'txn_11223', '2024-03-20 09:05:00');