-- shedlock: makes sure only one instance runs a scheduled job at a time
CREATE TABLE shedlock (
    name       varchar(64)  PRIMARY KEY,
    lock_until timestamp    NOT NULL,
    locked_at  timestamp    NOT NULL,
    locked_by  varchar(255) NOT NULL
);
