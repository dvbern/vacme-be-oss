ALTER TABLE Registrierung ADD  IF NOT EXISTS schutzstatus  BIT NULL DEFAULT FALSE;
ALTER TABLE Registrierung_AUD ADD IF NOT EXISTS schutzstatus BIT NULL;
