CREATE TABLE cinema_records (
 id VARCHAR(128) PRIMARY KEY, name VARCHAR(255) NOT NULL, address VARCHAR(500) NOT NULL
) ${tableOptions};
CREATE TABLE hall_records (
 id VARCHAR(128) PRIMARY KEY, cinema_id VARCHAR(128) NOT NULL, name VARCHAR(255) NOT NULL,
 CONSTRAINT uq_hall_name UNIQUE (cinema_id, name),
 CONSTRAINT fk_hall_cinema FOREIGN KEY (cinema_id) REFERENCES cinema_records(id)
) ${tableOptions};
CREATE TABLE seat_records (
 id VARCHAR(128) PRIMARY KEY, hall_id VARCHAR(128) NOT NULL, seat_row VARCHAR(32) NOT NULL,
 seat_number INT NOT NULL, seat_type VARCHAR(32) NOT NULL,
 CONSTRAINT uq_seat_position UNIQUE (hall_id, seat_row, seat_number),
 CONSTRAINT fk_seat_hall FOREIGN KEY (hall_id) REFERENCES hall_records(id),
 CHECK (seat_number > 0), CHECK (seat_type IN ('NORMAL','VIP','COUPLE'))
) ${tableOptions};
CREATE TABLE movie_records (
 id VARCHAR(128) PRIMARY KEY, title VARCHAR(255) NOT NULL, description VARCHAR(4000),
 duration_minutes INT NOT NULL, age_rating VARCHAR(32), status VARCHAR(32) NOT NULL,
 CHECK (duration_minutes > 0)
) ${tableOptions};
CREATE TABLE screening_records (
 id VARCHAR(128) PRIMARY KEY, movie_id VARCHAR(128) NOT NULL, hall_id VARCHAR(128) NOT NULL,
 start_time DATETIME(6) NOT NULL, end_time DATETIME(6) NOT NULL,
 amount DECIMAL(38,12) NOT NULL, currency VARCHAR(3) NOT NULL, status VARCHAR(32) NOT NULL,
 CONSTRAINT fk_screening_movie FOREIGN KEY (movie_id) REFERENCES movie_records(id),
 CONSTRAINT fk_screening_hall FOREIGN KEY (hall_id) REFERENCES hall_records(id),
 CHECK (start_time < end_time), CHECK (amount >= 0),
 CHECK (status IN ('SCHEDULED','CANCELLED','COMPLETED'))
) ${tableOptions};
CREATE INDEX ix_screening_overlap ON screening_records(hall_id, start_time, end_time);
CREATE TABLE user_records (
 id VARCHAR(128) PRIMARY KEY, email VARCHAR(255) NOT NULL, password_hash VARCHAR(255) NOT NULL,
 CONSTRAINT uq_user_email UNIQUE (email)
) ${tableOptions};
CREATE TABLE customer_records (
 id VARCHAR(128) PRIMARY KEY, user_id VARCHAR(128) NOT NULL, display_name VARCHAR(255) NOT NULL,
 CONSTRAINT uq_customer_user UNIQUE (user_id)
) ${tableOptions};
CREATE TABLE cart_records (
 id VARCHAR(128) PRIMARY KEY, customer_id VARCHAR(128) NOT NULL,
 CONSTRAINT uq_cart_customer UNIQUE (customer_id),
 CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES customer_records(id)
) ${tableOptions};
CREATE TABLE cart_items (
 cart_id VARCHAR(128) NOT NULL, item_index INT NOT NULL, screening_id VARCHAR(128) NOT NULL,
 seat_id VARCHAR(128) NOT NULL, amount DECIMAL(38,12) NOT NULL, currency VARCHAR(3) NOT NULL,
 PRIMARY KEY (cart_id, item_index), CONSTRAINT uq_cart_seat UNIQUE (cart_id, screening_id, seat_id),
 CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES cart_records(id),
 CONSTRAINT fk_cart_item_screening FOREIGN KEY (screening_id) REFERENCES screening_records(id),
 CONSTRAINT fk_cart_item_seat FOREIGN KEY (seat_id) REFERENCES seat_records(id), CHECK (amount >= 0)
) ${tableOptions};
CREATE TABLE booking_records (
 id VARCHAR(128) PRIMARY KEY, customer_id VARCHAR(128) NOT NULL, screening_id VARCHAR(128) NOT NULL,
 amount DECIMAL(38,12) NOT NULL, currency VARCHAR(3) NOT NULL, status VARCHAR(32) NOT NULL, created_at DATETIME(6) NOT NULL,
 CONSTRAINT uq_booking_screening UNIQUE (id, screening_id),
 CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES customer_records(id),
 CONSTRAINT fk_booking_screening FOREIGN KEY (screening_id) REFERENCES screening_records(id),
 CHECK (amount >= 0), CHECK (status IN ('PENDING','CONFIRMED','CANCELLED'))
) ${tableOptions};
CREATE TABLE booking_items (
 booking_id VARCHAR(128) NOT NULL, item_index INT NOT NULL, seat_id VARCHAR(128) NOT NULL,
 amount DECIMAL(38,12) NOT NULL, currency VARCHAR(3) NOT NULL,
 PRIMARY KEY (booking_id, item_index), CONSTRAINT uq_booking_seat UNIQUE (booking_id, seat_id),
 CONSTRAINT fk_booking_item_booking FOREIGN KEY (booking_id) REFERENCES booking_records(id),
 CONSTRAINT fk_booking_item_seat FOREIGN KEY (seat_id) REFERENCES seat_records(id), CHECK (amount >= 0)
) ${tableOptions};
CREATE TABLE payment_records (
 id VARCHAR(128) PRIMARY KEY, booking_id VARCHAR(128) NOT NULL, amount DECIMAL(38,12) NOT NULL,
 currency VARCHAR(3) NOT NULL, status VARCHAR(32) NOT NULL, provider_reference VARCHAR(255), successful_booking_id VARCHAR(128),
 CONSTRAINT uq_successful_payment UNIQUE (successful_booking_id),
 CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES booking_records(id),
 CHECK (amount >= 0), CHECK (status IN ('PENDING','SUCCESS','FAILED','REFUNDED')),
 CHECK ((status = 'SUCCESS' AND successful_booking_id IS NOT NULL AND successful_booking_id = booking_id)
     OR (status <> 'SUCCESS' AND successful_booking_id IS NULL))
) ${tableOptions};
CREATE TABLE seat_reservation_records (
 id VARCHAR(128) PRIMARY KEY, screening_id VARCHAR(128) NOT NULL, seat_id VARCHAR(128) NOT NULL, booking_id VARCHAR(128) NOT NULL,
 CONSTRAINT uq_reserved_seat UNIQUE (screening_id, seat_id),
 CONSTRAINT fk_reservation_screening FOREIGN KEY (screening_id) REFERENCES screening_records(id),
 CONSTRAINT fk_reservation_seat FOREIGN KEY (seat_id) REFERENCES seat_records(id),
 CONSTRAINT fk_reservation_booking FOREIGN KEY (booking_id, screening_id) REFERENCES booking_records(id, screening_id),
 CONSTRAINT fk_reservation_item FOREIGN KEY (booking_id, seat_id) REFERENCES booking_items(booking_id, seat_id)
) ${tableOptions};
CREATE TABLE ticket_records (
 id VARCHAR(128) PRIMARY KEY, booking_id VARCHAR(128) NOT NULL, screening_id VARCHAR(128) NOT NULL,
 seat_id VARCHAR(128) NOT NULL, ticket_code VARCHAR(192) NOT NULL, status VARCHAR(32) NOT NULL,
 CONSTRAINT uq_ticket_code UNIQUE (ticket_code), CONSTRAINT uq_ticket_booking_seat UNIQUE (booking_id, seat_id),
 CONSTRAINT fk_ticket_booking FOREIGN KEY (booking_id, screening_id) REFERENCES booking_records(id, screening_id),
 CONSTRAINT fk_ticket_screening FOREIGN KEY (screening_id) REFERENCES screening_records(id),
 CONSTRAINT fk_ticket_seat FOREIGN KEY (seat_id) REFERENCES seat_records(id),
 CONSTRAINT fk_ticket_item FOREIGN KEY (booking_id, seat_id) REFERENCES booking_items(booking_id, seat_id),
 CHECK (status IN ('VALID','USED','CANCELLED'))
) ${tableOptions};
CREATE TABLE seat_hold_records (
 id VARCHAR(128) PRIMARY KEY, screening_id VARCHAR(128) NOT NULL, seat_id VARCHAR(128) NOT NULL, customer_id VARCHAR(128) NOT NULL,
 CONSTRAINT uq_held_seat UNIQUE (screening_id, seat_id),
 CONSTRAINT fk_hold_screening FOREIGN KEY (screening_id) REFERENCES screening_records(id),
 CONSTRAINT fk_hold_seat FOREIGN KEY (seat_id) REFERENCES seat_records(id),
 CONSTRAINT fk_hold_customer FOREIGN KEY (customer_id) REFERENCES customer_records(id)
) ${tableOptions};
