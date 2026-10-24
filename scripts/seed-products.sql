-- Adds :n generated products straight into the catalog's database, to measure search with a large catalog.
-- Demo/test tooling, not part of the service. Run through scripts/seed-million.sh.
-- The products look realistic: brands, colors, types, materials and attributes are mixed, so common
-- words ("red", "shoe", "waterproof") match tens of thousands of products, like in a real big catalog.

\set ON_ERROR_STOP on
\timing on

-- building the search index once at the end is much faster than updating it for every row
SET maintenance_work_mem = '512MB';
DROP INDEX IF EXISTS products_search_vector_idx;

INSERT INTO products (sku, name, description, brand, category, attributes)
SELECT
    'GEN-' || lpad(i::text, 7, '0'),
    brand || ' ' || initcap(adjective) || ' ' || initcap(type),
    'A ' || adjective || ' ' || color || ' ' || type || ' made of ' || material || ', by ' || brand || '.',
    brand,
    category,
    jsonb_build_object('color', color, 'material', material, 'size', size, 'waterproof', waterproof)
FROM generate_series(1, :n) AS i
CROSS JOIN LATERAL (
    SELECT
        (ARRAY['Nordwind','Alpina','Velora','Kestrel','Brightline','Oakridge','Summit','Lumen','Corvo','Tidewater',
               'Ironleaf','Peakform','Solace','Arbor','Vanta','Fjord','Halcyon','Redstone','Bluefin','Everhart'])[1 + i % 20] AS brand,
        (ARRAY['red','blue','black','white','green','grey','navy','orange','yellow','purple','pink','brown'])[1 + i % 13 % 12] AS color,
        (ARRAY['lightweight','waterproof','wireless','compact','premium','classic','ergonomic','insulated','foldable','durable'])[1 + i % 17 % 10] AS adjective,
        (ARRAY['cotton','leather','aluminium','nylon','wool','steel','recycled polyester','bamboo'])[1 + i % 11 % 8] AS material,
        (ARRAY['running shoe','hiking boot','sneaker','sandal','trail shoe',
               'rain jacket','hoodie','t-shirt','fleece','down jacket',
               'headphones','speaker','laptop','smartwatch','keyboard',
               'desk lamp','coffee maker','kettle','blender','office chair',
               'backpack','tent','sleeping bag','water bottle','headlamp'])[1 + i % 25] AS type,
        (ARRAY['Footwear','Clothing','Electronics','Home','Outdoor'])[1 + i % 25 / 5] AS category,
        (ARRAY['S','M','L','XL','38','40','42','44'])[1 + i % 7 % 8] AS size,
        i % 3 = 0 AS waterproof
) AS p
ON CONFLICT (sku) DO NOTHING;

CREATE INDEX products_search_vector_idx ON products USING GIN (search_vector);
ANALYZE products;

SELECT count(*) AS products_in_catalog FROM products;
