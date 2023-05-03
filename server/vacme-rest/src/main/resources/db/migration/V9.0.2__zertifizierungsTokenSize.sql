ALTER TABLE ZertifizierungsToken DROP COLUMN token;
ALTER TABLE ZertifizierungsToken ADD COLUMN token VARCHAR(2000) NOT NULL;

