-- all searchable words of a product, filled by postgres itself on every insert and update
-- weights: A = sku, name, brand / B = category / C = description, attributes
ALTER TABLE products
    ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (
        setweight(to_tsvector('english', sku), 'A')
        || setweight(to_tsvector('english', name), 'A')
        || setweight(to_tsvector('english', coalesce(brand, '')), 'A')
        || setweight(to_tsvector('english', coalesce(category, '')), 'B')
        || setweight(to_tsvector('english', coalesce(description, '')), 'C')
        || setweight(jsonb_to_tsvector('english', attributes, '["string", "numeric", "key"]'), 'C')
    ) STORED;

-- index that lists, per word, which products contain it, so a search doesn't have to check every product
CREATE INDEX products_search_vector_idx ON products USING GIN (search_vector);
