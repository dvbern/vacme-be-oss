-- Rename Moderna
UPDATE Impfstoff SET name = 'Spikevax®' WHERE id = 'c5abc3d7-f80d-44fd-be6e-0aba4cf03643'; # Moderna
UPDATE Impfstoff SET name = 'Comirnaty®' WHERE id = '141fca55-ab78-4c0e-a2fd-edf2fe4e9b30'; # Pfizer/BioNTech

/*
--UNDO:
UPDATE Impfstoff SET name = 'COVID-19 vaccine (Moderna)' WHERE id = 'c5abc3d7-f80d-44fd-be6e-0aba4cf03643'; # Moderna
UPDATE Impfstoff SET name = 'Comirnaty' WHERE id = '141fca55-ab78-4c0e-a2fd-edf2fe4e9b30'; # Pfizer/BioNTech
*/