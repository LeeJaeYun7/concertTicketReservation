
-- 폴 킴 콘서트(concert_schedule_id는 1,2,3,4 로 교체)
-- 각 콘서트 스케줄마다 3389개의 좌석, 총 3389x4 = 13556개 좌석
-- 좌석 등급은 ALL로 동일

SET @start_seat_id = 4096;
SET @end_seat_id = @start_seat_id + 3389 - 1;

DROP PROCEDURE IF EXISTS InsertConcertScheduleSeats;
DELIMITER //

CREATE PROCEDURE InsertConcertScheduleSeats()
BEGIN
  DECLARE current_seat_id BIGINT DEFAULT @start_seat_id;

  WHILE current_seat_id <= @end_seat_id DO
    INSERT INTO concert_schedule_seat (
        concert_hall_seat_id,
        concert_schedule_id,
        seat_grade_id,
        status,
        created_at,
        updated_at
    )
    VALUES (
        current_seat_id,
        1,
        1,
        'AVAILABLE',
        NOW(),
        NOW()
    );

    SET current_seat_id = current_seat_id + 1;
  END WHILE;
END;
//

DELIMITER ;

CALL InsertConcertScheduleSeats();

DROP PROCEDURE InsertConcertScheduleSeats;

-- 기리보이 콘서트(concert_schedule_id는 5, seat_grade_id는 2)
-- 콘서트 스케줄은 3389개의 좌석, 총 1회이므로 3389x1 = 3389개 좌석
-- 좌석 등급은 RESERVED로 동일

SET @start_seat_id = 4096;
SET @end_seat_id = @start_seat_id + 3389 - 1;

DROP PROCEDURE IF EXISTS InsertConcertScheduleSeats;
DELIMITER //

CREATE PROCEDURE InsertConcertScheduleSeats()
BEGIN
  DECLARE current_seat_id BIGINT DEFAULT @start_seat_id;

  WHILE current_seat_id <= @end_seat_id DO
    INSERT INTO concert_schedule_seat (
        concert_hall_seat_id,
        concert_schedule_id,
        seat_grade_id,
        status,
        created_at,
        updated_at
    )
    VALUES (
        current_seat_id,
        5,
        2,
        'AVAILABLE',
        NOW(),
        NOW()
    );

    SET current_seat_id = current_seat_id + 1;
  END WHILE;
END;
//

DELIMITER ;

CALL InsertConcertScheduleSeats();

DROP PROCEDURE InsertConcertScheduleSeats;

-- KEHLANI(concert_schedule_id는 6, seat_grade_id는 3, 4, 5)
-- 콘서트 스케줄은 3152개의 좌석, 총 1회이므로 3152x1 = 3152개 좌석
-- 3152개 좌석 중 RESERVED_R 100개 좌석, RESERVED_P 100개 좌석, 나머지는 STANDING

SET @start_seat_id = 1;
SET @end_seat_id = @start_seat_id + 3152 - 1;

DROP PROCEDURE IF EXISTS InsertConcertScheduleSeats;
DELIMITER //

CREATE PROCEDURE InsertConcertScheduleSeats()
BEGIN
  DECLARE current_seat_id BIGINT DEFAULT @start_seat_id;

  WHILE current_seat_id <= @end_seat_id DO
    INSERT INTO concert_schedule_seat (
        concert_hall_seat_id,
        concert_schedule_id,
        seat_grade_id,
        status,
        created_at,
        updated_at
    )
    VALUES (
        current_seat_id,
        6,
        CASE
          WHEN current_seat_id BETWEEN 1 AND 100 THEN 4
          WHEN current_seat_id BETWEEN 101 AND 200 THEN 5
          ELSE 3
        END,
        'AVAILABLE',
        NOW(),
        NOW()
    );

    SET current_seat_id = current_seat_id + 1;
  END WHILE;
END;
//

DELIMITER ;

CALL InsertConcertScheduleSeats();

DROP PROCEDURE InsertConcertScheduleSeats;


-- Keshi 콘서트(concert_schedule_id는 7, seat_grade_id는 6~10)
-- 콘서트 스케줄은 3152개의 좌석, 총 1회이므로 3152x1 = 3152개의 좌석
-- 3152개 좌석 중
-- Limbo VIP Merch Package는 50개 좌석, Understand M＆G and Soundcheck Package는 50개 좌석
-- RESERVED_R 100개 좌석, RESERVED_P 100개 좌석, 나머지는 STANDING

SET @start_seat_id = 1;
SET @end_seat_id = @start_seat_id + 3152 - 1;

DROP PROCEDURE IF EXISTS InsertConcertScheduleSeats;
DELIMITER //

CREATE PROCEDURE InsertConcertScheduleSeats()
BEGIN
  DECLARE current_seat_id BIGINT DEFAULT @start_seat_id;

  WHILE current_seat_id <= @end_seat_id DO
    INSERT INTO concert_schedule_seat (
        concert_hall_seat_id,
        concert_schedule_id,
        seat_grade_id,
        status,
        created_at,
        updated_at
    )
    VALUES (
        current_seat_id,
        7,
        CASE
          WHEN current_seat_id BETWEEN 1 AND 50 THEN 9
          WHEN current_seat_id BETWEEN 51 AND 100 THEN 10
          WHEN current_seat_id BETWEEN 101 AND 200 THEN 6
          WHEN current_seat_id BETWEEN 201 AND 300 THEN 7
          ELSE 8
        END,
        'AVAILABLE',
        NOW(),
        NOW()
    );

    SET current_seat_id = current_seat_id + 1;
  END WHILE;
END;
//

DELIMITER ;

CALL InsertConcertScheduleSeats();

DROP PROCEDURE InsertConcertScheduleSeats;


-- 마블 스튜디오 인피니티 사가 콘서트(concert_schedule_id는 8~9, seat_grade_id는 11~13)
-- 콘서트 스케줄은 각 4400개의 좌석, 총 2회이므로 4400x2 = 8800개의 좌석
-- 4440개 좌석 중 VIP 100개 좌석, R 300개 좌석, 나머지는 S석

SET @start_seat_id = 8191;
SET @end_seat_id = @start_seat_id + 4400 - 1;

DROP PROCEDURE IF EXISTS InsertConcertScheduleSeats;
DELIMITER //

CREATE PROCEDURE InsertConcertScheduleSeats()
BEGIN
  DECLARE current_seat_id BIGINT DEFAULT @start_seat_id;

  WHILE current_seat_id <= @end_seat_id DO
    INSERT INTO concert_schedule_seat (
        concert_hall_seat_id,
        concert_schedule_id,
        seat_grade_id,
        status,
        created_at,
        updated_at
    )
    VALUES (
        current_seat_id,
        8,
        CASE
          WHEN current_seat_id BETWEEN 8191 AND 8290 THEN 11
          WHEN current_seat_id BETWEEN 8291 AND 8590 THEN 12
          ELSE 13
        END,
        'AVAILABLE',
        NOW(),
        NOW()
    );

    SET current_seat_id = current_seat_id + 1;
  END WHILE;
END;
//

DELIMITER ;

CALL InsertConcertScheduleSeats();

DROP PROCEDURE InsertConcertScheduleSeats;




