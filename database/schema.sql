CREATE DATABASE IF NOT EXISTS disaster_management;
USE disaster_management;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'RescueTeam', 'Citizen') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rescue_teams (
    team_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    team_name VARCHAR(100) NOT NULL,
    specialty VARCHAR(100) NOT NULL,
    status ENUM('Available', 'Busy') DEFAULT 'Available',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS disasters (
    disaster_id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    location VARCHAR(200) NOT NULL,
    severity ENUM('Low', 'Medium', 'High', 'Critical') NOT NULL,
    status ENUM('Active', 'Resolved') DEFAULT 'Active',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS help_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    citizen_id INT NOT NULL,
    disaster_id INT NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(200) NOT NULL,
    status ENUM('Pending', 'Assigned', 'Resolved') DEFAULT 'Pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (citizen_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (disaster_id) REFERENCES disasters(disaster_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS assignments (
    assignment_id INT AUTO_INCREMENT PRIMARY KEY,
    request_id INT NOT NULL,
    team_id INT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('In Progress', 'Completed') DEFAULT 'In Progress',
    FOREIGN KEY (request_id) REFERENCES help_requests(request_id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES rescue_teams(team_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS resources (
    resource_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category ENUM('Food', 'Water', 'Medical', 'Shelter', 'Other') NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    unit VARCHAR(50) NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_disaster_status ON disasters(status);
CREATE INDEX idx_help_status ON help_requests(status);

-- Sample Data Insert
INSERT INTO users (name, email, password_hash, role) VALUES 
('Admin User', 'admin@disaster.com', 'admin123', 'Admin'),
('Rescue Team Alpha', 'alpha@disaster.com', 'team123', 'RescueTeam'),
('John Doe', 'john@example.com', 'citizen123', 'Citizen');

INSERT INTO rescue_teams (user_id, team_name, specialty, status) VALUES 
(2, 'Alpha Squad', 'Medical & Evacuation', 'Available');

INSERT INTO disasters (type, location, severity, status, description) VALUES 
('Flood', 'Downtown Area', 'High', 'Active', 'Severe flooding due to heavy rain.');

INSERT INTO resources (name, category, quantity, unit) VALUES 
('Bottled Water', 'Water', 5000, 'Liters'),
('First Aid Kits', 'Medical', 200, 'Kits');

ALTER TABLE disasters
ADD image_path VARCHAR(255),
ADD image_path_2 VARCHAR(255);
