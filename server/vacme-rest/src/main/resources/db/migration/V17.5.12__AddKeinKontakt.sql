ALTER TABLE Registrierung ADD IF NOT EXISTS keinKontakt  BIT NULL DEFAULT FALSE;
ALTER TABLE Registrierung_AUD ADD IF NOT EXISTS keinKontakt BIT NULL;