-- Drop the 'Category' table if it exists
DROP TABLE IF EXISTS Category;

-- Drop the 'Product' table if it exists
DROP TABLE IF EXISTS Product;

-- Schema for the 'Product' table
CREATE TABLE Product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Schema for the 'Category' table
CREATE TABLE Category (
    id_auto_increment_from_category_service BIGINT AUTO_INCREMENT PRIMARY KEY,
    id BIGINT,  -- This refers to the ID from the category-service
    name VARCHAR(255) NOT NULL,
    product_id BIGINT,  -- Foreign key to Product table
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Establish foreign key relationship to Product
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES Product(id) ON DELETE CASCADE
);
