CREATE TYPE ROLE AS ENUM ( 'USER', 'OPERATOR');
CREATE TYPE CONNECTOR_TYPE AS ENUM ( 'TYPE_1', 'TYPE_2', 'TYPE_3', 'TYPE_4', 'TYPE_5');
CREATE TYPE SPOT_STATE AS ENUM ( 'OCCUPIED', 'FREE', 'OUT_OF_SERVICE');
CREATE TYPE PAYMENT_STATE AS ENUM ( 'PENDING', 'COMPLETED', 'FAILED');
CREATE TYPE SONIC AS ENUM ( 'NORMAL', 'FAST', 'FASTPP'); -- speed charging

CREATE TABLE user_table
(
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(255)        NOT NULL,
    email    VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255)        NOT NULL,
    role     ROLE                NOT NULL
);

CREATE TABLE vehicle
(
    id             SERIAL PRIMARY KEY,
    user_id        INTEGER REFERENCES user_table (id),
    brand          VARCHAR(50)        NOT NULL,
    model          VARCHAR(50)        NOT NULL,
    license_plate  VARCHAR(20) UNIQUE NOT NULL,
    connector_type CONNECTOR_TYPE     NOT NULL
);


CREATE TABLE charging_station
(
    id               SERIAL PRIMARY KEY,
    name             VARCHAR(100)  NOT NULL,
    lat              DECIMAL(9, 6) NOT NULL,
    lon              DECIMAL(9, 6) NOT NULL,
    operator_id      INTEGER REFERENCES user_table (id),
    last_maintenance DATE,
    photo_url        VARCHAR(255)
);

CREATE TABLE price
(
    id                  SERIAL PRIMARY KEY,
    connector_type      CONNECTOR_TYPE NOT NULL,
    charging_velocity   SONIC          NOT NULL,
    price_per_kwh       DECIMAL(8, 2)  NOT NULL,
    charging_station_id INTEGER REFERENCES charging_station (id)
);

CREATE TABLE charging_spot
(
    id                SERIAL PRIMARY KEY,
    station_id        INTEGER REFERENCES charging_station (id),
    charging_velocity SONIC          NOT NULL,
    connector_type    CONNECTOR_TYPE NOT NULL,
    energy_kwh        DECIMAL(7, 2)  NOT NULL,
    state             SPOT_STATE     NOT NULL DEFAULT 'FREE'
);

CREATE TABLE reservation
(
    id               SERIAL PRIMARY KEY,
    user_id          INTEGER REFERENCES user_table (id),
    vehicle_id       INTEGER REFERENCES vehicle (id),
    charging_spot_id INTEGER REFERENCES charging_spot (id),
    start_time       TIMESTAMP NOT NULL,
    end_time         TIMESTAMP NOT NULL,
    uuid             UUID      NOT NULL DEFAULT gen_random_uuid()
);

CREATE TABLE session
(
    id             SERIAL PRIMARY KEY,
    reservation_id INTEGER REFERENCES reservation (id),
    start_time     TIMESTAMP NOT NULL,
    end_time       TIMESTAMP,
    energy_kwh     DECIMAL(7, 3),
    total_cost     DECIMAL(8, 2) DEFAULT 0.00
);

CREATE TABLE payment
(
    id             SERIAL PRIMARY KEY,
    session_id     INTEGER REFERENCES session (id),
    value          DECIMAL(8, 2) NOT NULL,
    method         VARCHAR(30)   NOT NULL,
    state          PAYMENT_STATE NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(100),
    date_hour      TIMESTAMP              DEFAULT CURRENT_TIMESTAMP
);