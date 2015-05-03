-- DROP TABLE IF EXISTS mail_account;
-- DROP INDEX IF EXISTS mail_account_unique;
-- DROP TABLE IF EXISTS mail_message;

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
