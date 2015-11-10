CREATE  TABLE users (
  username VARCHAR(45) NOT NULL ,
  password VARCHAR(45) NOT NULL ,
  enabled TINYINT NOT NULL DEFAULT 1 ,
  is_verified TINYINT NOT NULL DEFAULT 0 ,
  is_admin TINYINT NOT NULL DEFAULT 0 ,
  clinic_id VARCHAR(60),
  token VARCHAR(60),
  otp_token VARCHAR(10),
  PRIMARY KEY (username));

CREATE TABLE authorities (
  user_role_id INT(11) NOT NULL AUTO_INCREMENT,
  username VARCHAR(45) NOT NULL,
  authority VARCHAR(45) NOT NULL,
  PRIMARY KEY (user_role_id),
  UNIQUE KEY uni_username_role (authority,username),
  KEY fk_username_idx (username),
  CONSTRAINT fk_username FOREIGN KEY (username) REFERENCES users (username));

INSERT INTO users(username,password,enabled)VALUES ('admin','admin', TRUE);

INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_USER');
INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');
