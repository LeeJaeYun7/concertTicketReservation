erDiagram
    CONCERT_HALL {
      int id PK
      varchar name
      varchar address
      varchar phone_number
      timestamp created_at
      timestamp updated_at
    }

    CONCERT {
      int id PK
      int concert_hall_id FK
      varchar name
      varchar genre
      int performance_time
      varchar age_restriction
      timestamp start_at
      timestamp end_at
      timestamp created_at
      timestamp updated_at
    }

    CONCERT_SCHEDULE {
      int id PK
      varchar concert_id FK 
      timestamp dateTime
      timestamp created_at
      timestamp updated_at
    }

    SEAT {
      int id PK
      int concert_hall_id FK
      int number
      timestamp created_at
      timestamp updated_at
    }

    SEAT_INFO {
      int id PK
      int seat_id FK
      int concert_schedule_id FK
      varchar seat_grade 
      varchar status
      timestamp created_at
      timestamp updated_at
    }

    MEMBER {
      int id PK
      varchar uuid
      varchar name
      int balance
      timestamp created_at
      timestamp updated_at
    }

    RESERVATION {
      int id PK
      int uuid FK
      int concert_schedule_id FK
      int seat_info_id FK
      int price
      timestamp created_at
      timestamp updated_at
    }

    PAYMENT {
      int id PK
      int concert_schedule_id FK
      int uuid FK
      int amount
      timestamp created_at
      timestamp updated_at
    }

    CHARGE {
      int id PK
      int uuid FK
      int amount
      timestamp created_at
      timestamp updated_at
    }

    CONCERT_HALL ||--o{ CONCERT : has
    CONCERT_HALL ||--o{ SEAT : has
    CONCERT ||--o{ CONCERT_SCHEDULE : has
    CONCERT_SCHEDULE ||--o{ SEAT : has
    CONCERT_SCHEDULE ||--o{ RESERVATION : has
    CONCERT_SCHEDULE ||--o{ PAYMENT : has
    SEAT ||--o{ SEAT_INFO : has
    MEMBER ||--o{ RESERVATION : "uuid ref"
    MEMBER ||--o{ PAYMENT : "uuid ref"
    MEMBER ||--o{ CHARGE : "uuid ref"
    SEAT_INFO ||--|| CONCERT_SCHEDULE : "has one" 
