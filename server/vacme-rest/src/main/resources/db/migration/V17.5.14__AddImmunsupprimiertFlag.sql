ALTER TABLE Fragebogen ADD IF NOT EXISTS immunsupprimiert  BIT NULL;
ALTER TABLE Fragebogen_AUD ADD IF NOT EXISTS immunsupprimiert BIT NULL;

/*
-- UNDO:
ALTER TABLE Fragebogen DROP IF EXISTS immunsupprimiert;
ALTER TABLE Fragebogen_AUD DROP IF EXISTS immunsupprimiert;
*/