-- Insert sample data into the product table
INSERT INTO `product` (`name`, `description`, `price`, `quantity`, `created_at`, `updated_at`) VALUES 
('iPhone 14', 'Latest Apple smartphone with A16 chip', 999.99, 50, NOW(), NOW()),
('Samsung Galaxy S23', 'Flagship Android smartphone from Samsung', 799.99, 75, NOW(), NOW()),
('Dell XPS 13', 'Ultrabook with Intel Core i7 and 16GB RAM', 1299.99, 30, NOW(), NOW()),
('Sony WH-1000XM5', 'Noise-canceling wireless headphones', 349.99, 100, NOW(), NOW()),
('Dyson V11 Vacuum Cleaner', 'Cordless vacuum cleaner with powerful suction', 499.99, 20, NOW(), NOW()),
('PlayStation 5', 'Next-gen gaming console from Sony', 499.99, 40, NOW(), NOW()),
('MacBook Pro 16-inch', 'Apple laptop with M2 Max chip and 32GB RAM', 2499.99, 25, NOW(), NOW()),
('Google Pixel 7 Pro', 'Google’s latest flagship Android phone', 899.99, 60, NOW(), NOW()),
('Amazon Echo Dot (5th Gen)', 'Smart speaker with Alexa voice assistant', 49.99, 200, NOW(), NOW()),
('Samsung QLED 55-inch TV', '4K UHD Smart TV with HDR and QLED technology', 1199.99, 15, NOW(), NOW()),
('HP Envy 15', 'Laptop with Intel Core i9, 32GB RAM, and NVIDIA GPU', 1599.99, 35, NOW(), NOW()),
('Bose SoundLink Revolve+', 'Portable Bluetooth speaker with 360-degree sound', 299.99, 150, NOW(), NOW()),
('Xiaomi Mi 11', 'Flagship smartphone with Snapdragon 888 chipset', 699.99, 80, NOW(), NOW()),
('Sony A7 III', 'Full-frame mirrorless camera for professional photography', 1999.99, 10, NOW(), NOW()),
('Apple Watch Series 8', 'Latest Apple smartwatch with fitness tracking', 399.99, 90, NOW(), NOW()),
('Nespresso Vertuo Coffee Maker', 'Coffee machine with capsule brewing system', 249.99, 50, NOW(), NOW()),
('Fitbit Charge 5', 'Advanced fitness tracker with health monitoring', 149.99, 100, NOW(), NOW()),
('Microsoft Surface Pro 9', '2-in-1 tablet with Intel Core i7 and 16GB RAM', 1399.99, 20, NOW(), NOW()),
('GoPro Hero 11', 'Action camera with 5K video recording', 499.99, 50, NOW(), NOW()),
('LG Washing Machine', 'Top load washing machine with smart features', 699.99, 10, NOW(), NOW()),
('JBL Charge 5', 'Portable waterproof Bluetooth speaker', 179.99, 120, NOW(), NOW()),
('Nintendo Switch OLED', 'Hybrid gaming console with OLED display', 349.99, 60, NOW(), NOW()),
('Canon EOS R5', 'Mirrorless camera with 8K video recording', 3899.99, 8, NOW(), NOW()),
('Asus ROG Zephyrus G14', 'Gaming laptop with AMD Ryzen 9 and RTX 3060 GPU', 1899.99, 15, NOW(), NOW()),
('KitchenAid Stand Mixer', 'Iconic mixer with multiple attachments for baking', 399.99, 40, NOW(), NOW()),
('Razer DeathAdder V2', 'High-precision gaming mouse with customizable DPI', 69.99, 200, NOW(), NOW()),
('Nest Learning Thermostat', 'Smart thermostat with energy-saving features', 249.99, 30, NOW(), NOW()),
('Sony PlayStation VR2', 'Virtual reality headset for PlayStation 5', 549.99, 20, NOW(), NOW()),
('Philips Hue Smart Bulb', 'Smart lighting system with color control via app', 49.99, 300, NOW(), NOW()),
('Tesla Model 3 Charger', 'Wall-mounted electric vehicle charger', 499.99, 15, NOW(), NOW());



-- Insert sample data into the category table
INSERT INTO `category` (`category_id`, `product_id`, `created_at`, `updated_at`) VALUES
(1, 1, NOW(), NOW()),   -- iPhone 14 -> Electronics
(2, 1, NOW(), NOW()),   -- iPhone 14 -> Smartphones
(1, 2, NOW(), NOW()),   -- Samsung Galaxy S23 -> Electronics
(2, 2, NOW(), NOW()),   -- Samsung Galaxy S23 -> Smartphones
(1, 3, NOW(), NOW()),   -- Dell XPS 13 -> Electronics
(3, 3, NOW(), NOW()),   -- Dell XPS 13 -> Laptops
(1, 4, NOW(), NOW()),   -- Sony WH-1000XM5 -> Electronics
(4, 4, NOW(), NOW()),   -- Sony WH-1000XM5 -> Audio Devices
(5, 5, NOW(), NOW()),   -- Dyson V11 -> Home Appliances
(6, 6, NOW(), NOW()),   -- PlayStation 5 -> Gaming Consoles
(1, 7, NOW(), NOW()),   -- MacBook Pro -> Electronics
(3, 7, NOW(), NOW()),   -- MacBook Pro -> Laptops
(2, 8, NOW(), NOW()),   -- Google Pixel 7 Pro -> Smartphones
(7, 9, NOW(), NOW()),   -- Amazon Echo Dot -> Smart Home Devices
(1, 10, NOW(), NOW()),  -- Samsung QLED TV -> Electronics
(8, 10, NOW(), NOW()),  -- Samsung QLED TV -> TVs
(1, 11, NOW(), NOW()),  -- HP Envy -> Electronics
(3, 11, NOW(), NOW()),  -- HP Envy -> Laptops
(1, 12, NOW(), NOW()),  -- Bose SoundLink -> Electronics
(4, 12, NOW(), NOW()),  -- Bose SoundLink -> Audio Devices
(2, 13, NOW(), NOW()),  -- Xiaomi Mi 11 -> Smartphones
(9, 14, NOW(), NOW()),  -- Sony A7 III -> Cameras
(10, 15, NOW(), NOW()), -- Apple Watch -> Wearables
(11, 16, NOW(), NOW()), -- Nespresso -> Kitchen Appliances
(12, 17, NOW(), NOW()), -- Fitbit -> Fitness & Health Devices
(13, 18, NOW(), NOW()), -- Microsoft Surface -> Tablets
(9, 19, NOW(), NOW()),  -- GoPro -> Cameras
(5, 20, NOW(), NOW()),  -- LG Washing Machine -> Home Appliances
(4, 21, NOW(), NOW()),  -- JBL Charge -> Audio Devices
(6, 22, NOW(), NOW()),  -- Nintendo Switch -> Gaming Consoles
(9, 23, NOW(), NOW()),  -- Canon EOS R5 -> Cameras
(3, 24, NOW(), NOW()),  -- Asus ROG -> Laptops
(11, 25, NOW(), NOW()), -- KitchenAid -> Kitchen Appliances
(14, 26, NOW(), NOW()), -- Razer DeathAdder -> Computer Accessories
(7, 27, NOW(), NOW()),  -- Nest Thermostat -> Smart Home Devices
(6, 28, NOW(), NOW()),  -- PlayStation VR2 -> Gaming Consoles
(15, 29, NOW(), NOW()), -- Philips Hue -> Smart Home Devices
(16, 30, NOW(), NOW()); -- Tesla Charger -> Automotive Accessories
