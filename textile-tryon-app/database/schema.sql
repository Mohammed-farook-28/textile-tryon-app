-- Textile Try-On Application Database Schema
-- MySQL Database Schema

-- Create database
CREATE DATABASE IF NOT EXISTS textile_tryon;
USE textile_tryon;

-- Garments table
CREATE TABLE garments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name_id VARCHAR(100) UNIQUE NOT NULL,
    garment_name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    subcategory VARCHAR(100),
    garment_type VARCHAR(100) NOT NULL,
    color VARCHAR(50) NOT NULL,
    pattern_style VARCHAR(100),
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_color (color),
    INDEX idx_garment_type (garment_type),
    INDEX idx_price (price),
    INDEX idx_name_id (name_id)
);

-- Garment images table
CREATE TABLE garment_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    garment_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (garment_id) REFERENCES garments(id) ON DELETE CASCADE,
    INDEX idx_garment_id (garment_id),
    INDEX idx_is_primary (is_primary)
);

-- User profiles table (no auth, just session-based)
CREATE TABLE user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) UNIQUE NOT NULL,
    profile_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id)
);

-- User saved photos
CREATE TABLE user_photos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_profile_id BIGINT NOT NULL,
    photo_url VARCHAR(500) NOT NULL,
    photo_name VARCHAR(100),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_profile_id) REFERENCES user_profiles(id) ON DELETE CASCADE,
    INDEX idx_user_profile_id (user_profile_id)
);

-- Favorites table
CREATE TABLE favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_profile_id BIGINT NOT NULL,
    garment_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_profile_id) REFERENCES user_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (garment_id) REFERENCES garments(id) ON DELETE CASCADE,
    UNIQUE KEY unique_favorite (user_profile_id, garment_id),
    INDEX idx_user_profile_id (user_profile_id),
    INDEX idx_garment_id (garment_id)
);

-- Try-on results table
CREATE TABLE tryon_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_profile_id BIGINT NOT NULL,
    garment_id BIGINT NOT NULL,
    user_photo_id BIGINT NOT NULL,
    result_image_url VARCHAR(500) NOT NULL,
    ai_model_used VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_profile_id) REFERENCES user_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (garment_id) REFERENCES garments(id) ON DELETE CASCADE,
    FOREIGN KEY (user_photo_id) REFERENCES user_photos(id) ON DELETE CASCADE,
    INDEX idx_user_profile_id (user_profile_id),
    INDEX idx_garment_id (garment_id),
    INDEX idx_user_photo_id (user_photo_id)
);

-- Insert sample data for development
INSERT INTO garments (name_id, garment_name, category, subcategory, garment_type, color, pattern_style, price, stock_quantity) VALUES
('SAR001', 'Elegant Silk Saree', 'Saree', 'Silk', 'Traditional', 'Red', 'Embroidered', 2500.00, 10),
('SAR002', 'Cotton Casual Saree', 'Saree', 'Cotton', 'Casual', 'Blue', 'Printed', 1200.00, 15),
('SHT001', 'Formal White Shirt', 'Shirt', 'Formal', 'Business', 'White', 'Plain', 800.00, 25),
('SHT002', 'Casual Printed Shirt', 'Shirt', 'Casual', 'Casual', 'Multi', 'Printed', 650.00, 20),
('DRS001', 'Evening Gown', 'Dress', 'Evening', 'Formal', 'Black', 'Plain', 3500.00, 8),
('DRS002', 'Summer Floral Dress', 'Dress', 'Summer', 'Casual', 'Yellow', 'Floral', 1800.00, 12);

-- Insert sample garment images
INSERT INTO garment_images (garment_id, image_url, is_primary, display_order) VALUES
(1, 'https://example-bucket.s3.amazonaws.com/garments/SAR001_1.jpg', TRUE, 1),
(1, 'https://example-bucket.s3.amazonaws.com/garments/SAR001_2.jpg', FALSE, 2),
(2, 'https://example-bucket.s3.amazonaws.com/garments/SAR002_1.jpg', TRUE, 1),
(3, 'https://example-bucket.s3.amazonaws.com/garments/SHT001_1.jpg', TRUE, 1),
(4, 'https://example-bucket.s3.amazonaws.com/garments/SHT002_1.jpg', TRUE, 1),
(5, 'https://example-bucket.s3.amazonaws.com/garments/DRS001_1.jpg', TRUE, 1),
(6, 'https://example-bucket.s3.amazonaws.com/garments/DRS002_1.jpg', TRUE, 1);
