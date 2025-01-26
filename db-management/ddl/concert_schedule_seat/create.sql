CREATE TABLE concert_schedule_seat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    concert_hall_seat_id BIGINT NOT NULL,
    concert_schedule_id BIGINT NOT NULL,
    seat_grade_id BIGINT NOT NULL,
    status ENUM('AVAILABLE', 'PENDING', 'RESERVED') NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);