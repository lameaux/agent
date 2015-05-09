-- DROP TABLE IF EXISTS mail_account;
-- DROP INDEX IF EXISTS mail_account_unique;
-- DROP TABLE IF EXISTS mail_message;
-- DROP TABLE IF EXISTS twitter_account;
-- DROP TABLE IF EXISTS twitter_message;
-- DROP TABLE IF EXISTS download_file;
-- DROP INDEX IF EXISTS download_file_unique;
-- DROP INDEX IF EXISTS download_file_location;

CREATE TABLE IF NOT EXISTS mail_account (
	id INT auto_increment PRIMARY KEY, 
	login VARCHAR(255),
	domain VARCHAR(255),
	active INT DEFAULT 1
);
CREATE UNIQUE INDEX IF NOT EXISTS mail_account_unique 
ON mail_account(login, domain);

CREATE TABLE IF NOT EXISTS mail_message (
	id INT auto_increment PRIMARY KEY, 
	account_id INT,
	sender VARCHAR(255),
	size INT,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS twitter_account (
	id VARCHAR(20) PRIMARY KEY, 
	screen_name VARCHAR(255),
	tags VARCHAR(255),
	access_token VARCHAR(255),
	access_token_secret VARCHAR(255)	
);

CREATE TABLE IF NOT EXISTS twitter_message (
	id INT auto_increment PRIMARY KEY,
	account_id VARCHAR(20), 
	message_text VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS download_file (
	id INT auto_increment PRIMARY KEY,
	url VARCHAR(1000), 
	file_location VARCHAR(1000),
	no_proxy INT DEFAULT 0	
);
CREATE UNIQUE INDEX IF NOT EXISTS download_file_unique 
ON download_file(url);
CREATE INDEX IF NOT EXISTS download_file_location 
ON download_file(file_location);
