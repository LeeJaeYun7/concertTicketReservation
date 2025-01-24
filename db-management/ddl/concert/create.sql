CREATE TABLE concert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    concert_hall_id BIGINT NOT NULL,
    genre VARCHAR(255) NOT NULL,
    performance_time BIGINT NOT NULL,
    age_restriction VARCHAR(255) NOT NULL,
    start_at DATE NOT NULL,
    end_at DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);