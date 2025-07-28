SET SQL_SAFE_UPDATES = 0;

-- Use the techstore database
USE techstore;

-- Delete existing products to replace them with new prices
DELETE FROM products;
-- Reset auto-increment counter for products table (optional, but good for clean IDs)
ALTER TABLE products AUTO_INCREMENT = 1;

-- Insert sample product data with more reasonable prices and categories
INSERT INTO products (name, price, description, image, stock, category) VALUES
('Dell XPS 15 Laptop', 14999.00, 'Powerful laptop with 11th Gen Intel Core i7, 16GB RAM, 512GB SSD, and NVIDIA RTX 3050 Ti.', 'https://i.dell.com/is/image/DellContent/content/dam/ss2/product-images/dell-client-products/notebooks/xps-notebooks/xps-15-9520/media-gallery/black/notebook-xps-15-9520-gallery-4.psd?fmt=png-alpha&pscan=auto&scl=1&hei=402&wid=649&qlt=100,1&resMode=sharp2&size=649,402', 15, 'Laptops'),
('Apple MacBook Air M2', 11990.00, 'Next-generation performance with the Apple M2 chip, 8GB RAM, 256GB SSD, and a stunning Liquid Retina display.', 'https://store.storeimages.cdn-apple.com/4668/as-images.apple.com/is/macbook-air-midnight-select-20220606?wid=904&hei=840&fmt=jpeg&qlt=90&.v=1653084303665', 20, 'Laptops'),
('Samsung Galaxy S23 Ultra', 12499.00, 'The ultimate smartphone with a pro-grade camera, massive battery, and an embedded S Pen.', 'https://images.samsung.com/in/smartphones/galaxy-s23-ultra/images/galaxy-s23-ultra-highlights-kv.jpg', 30, 'Mobiles'),
('Apple iPhone 14 Pro', 12990.00, 'Featuring the Dynamic Island, a 48MP main camera, and an Always-On display.', 'https://store.storeimages.cdn-apple.com/4668/as-images.apple.com/is/iphone-14-pro-finish-select-202209-6-7inch-deeppurple?wid=5120&hei=2880&fmt=p-jpg&qlt=80&.v=166370384', 25, 'Mobiles'),
('Sony WH-1000XM5 Headphones', 2999.00, 'Industry-leading noise canceling headphones with exceptional sound quality and a comfortable design.', 'https://www.sony.co.in/image/5d02da5df552836db894cead8a68f5f3?fmt=pjpeg&wid=330&bgcolor=FFFFFF&bgc=FFFFFF', 50, 'Headphones'),
('Bose QuietComfort 45', 2790.00, 'Acoustic Noise Cancelling headphones with high-fidelity audio and lightweight comfort.', 'https://assets.bose.com/content/dam/Bose_DAM/Web/consumer_electronics/global/products/headphones/quietcomfort_headphones_45/product_silo_images/QC45_PDP_Ecom_Gallery_01_RGB.psd/image.psd/width_1000.jpg', 40, 'Headphones'),
('Logitech MX Master 3S Mouse', 1099.00, 'An iconic mouse, remastered for ultimate precision and performance with quiet clicks.', 'https://resource.logitech.com/content/dam/logitech/en/products/mice/mx-master-3s/gallery/mx-master-3s-mouse-gallery-1-graphite.png', 60, 'Accessories'),
('Samsung Odyssey G7 Monitor', 4550.00, 'A 32-inch curved QLED gaming monitor with a 240Hz refresh rate and 1ms response time.', 'https://images.samsung.com/is/image/samsung/p6pim/in/ls32bg700ewxxl/gallery/in-odyssey-g7-g70b-ls32bg700ewxxl-533221332?$650_519_PNG$', 10, 'Accessories');


-- Use the techstore database
USE techstore;

UPDATE products SET image = '/techstore-ecommerce/images/dell_xps.png' WHERE name = 'Dell XPS 15 Laptop';
UPDATE products SET image = '/techstore-ecommerce/images/macbook_air.png' WHERE name = 'Apple MacBook Air M2';
UPDATE products SET image = '/techstore-ecommerce/images/galaxy_s23.jpg' WHERE name = 'Samsung Galaxy S23 Ultra';
UPDATE products SET image = '/techstore-ecommerce/images/iphone_14.jpg' WHERE name = 'Apple iPhone 14 Pro';
UPDATE products SET image = '/techstore-ecommerce/images/sony_xm5.jpg' WHERE name = 'Sony WH-1000XM5 Headphones';
UPDATE products SET image = '/techstore-ecommerce/images/bose_qc45.jpg' WHERE name = 'Bose QuietComfort 45';
UPDATE products SET image = '/techstore-ecommerce/images/logitech_mouse.png' WHERE name = 'Logitech MX Master 3S Mouse';
UPDATE products SET image = '/techstore-ecommerce/images/samsung_monitor.png' WHERE name = 'Samsung Odyssey G7 Monitor';



USE techstore;

-- Execute these ONE BY ONE, or comment out if DESCRIBE shows they don't exist
ALTER TABLE orders DROP COLUMN shipping_address_line1;
ALTER TABLE orders DROP COLUMN shipping_address_line2;
ALTER TABLE orders DROP COLUMN shipping_city;
ALTER TABLE orders DROP COLUMN shipping_state;
ALTER TABLE orders DROP COLUMN shipping_zip_code;
ALTER TABLE orders DROP COLUMN shipping_country;
-- ALTER TABLE orders ADD COLUMN delivery_address TEXT;

USE techstore;
ALTER TABLE orders ADD COLUMN delivery_address TEXT;

-- Use the techstore database
USE techstore;

-- Drop existing tables (this will also drop order_items due to foreign key constraints, which is fine)
DROP TABLE IF EXISTS order_items; -- Drop child table first
DROP TABLE IF EXISTS orders;     -- Then drop parent table

-- Re-create the orders table with the correct delivery_address column
CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    total DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'completed',
    delivery_address TEXT, -- Correct column for single address string
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Re-create the order_items table (as it was dropped)
CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    product_name VARCHAR(255),
    product_image TEXT, -- Ensure product_image is TEXT here too
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);

-- You might need to re-insert your sample products and users if you dropped them earlier
-- (The full schema script from our previous conversation includes product/user inserts)