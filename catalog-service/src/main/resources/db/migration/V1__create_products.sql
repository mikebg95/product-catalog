CREATE TABLE products (
    sku              varchar(64)    PRIMARY KEY,
    name             text           NOT NULL,
    description      text,
    brand            text,
    category         text,
    attributes       jsonb          NOT NULL DEFAULT '{}',
    price_amount     numeric(12, 2),
    price_updated_at timestamptz,
    -- amount and time are both filled in, or both empty (no price yet)
    CONSTRAINT price_complete CHECK ((price_amount IS NULL) = (price_updated_at IS NULL))
);
