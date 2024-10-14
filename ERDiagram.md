erDiagram
    CONCERT {
      int id PK
      varchar name
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
      int concert_schedule_id FK
      int number
      int price
      boolean is_reserved
      timestamp created_at
      timestamp updated_at
    }

    WAITING_QUEUE {
      int id PK
      int concert_id FK
      int uuid
      int waiting_number
      varchar token
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

    RESERVATION_HISTORY {
      int id PK
      int uuid FK
      int concert_schedule_id FK
      int seat_id FK
      int price
      timestamp created_at
      timestamp updated_at
    }

    PAYMENT_HISTORY {
      int id PK
      int concert_schedule_id FK
      int uuid FK
      int amount
      timestamp created_at
      timestamp updated_at
    }

    CHARGE_HISTORY {
      int id PK
      int uuid FK
      int amount
      timestamp created_at
      timestamp updated_at
    }

    CONCERT ||--o{ CONCERT_SCHEDULE : has
    CONCERT ||--o{ WAITING_QUEUE : has
    CONCERT_SCHEDULE ||--o{ SEAT : has
    CONCERT_SCHEDULE ||--o{ RESERVATION_HISTORY : has
    CONCERT_SCHEDULE ||--o{ PAYMENT_HISTORY : has
    SEAT ||--o{ RESERVATION_HISTORY : "seat_id ref"
    MEMBER ||--o{ RESERVATION_HISTORY : "uuid ref"
    MEMBER ||--o{ PAYMENT_HISTORY : "uuid ref"
    MEMBER ||--o{ CHARGE_HISTORY : "uuid ref"
