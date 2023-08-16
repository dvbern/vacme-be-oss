# Neue Namen
update Impfstoff set name = 'Spikevax bivalent BA.4-5 (Fertigspritze)' where code = '7680692110015';
update Impfstoff set name = 'Comirnaty bivalent BA.1' where code = '7680690470012';
update Impfstoff set name = 'Spikevax bivalent BA.4-5' where code = '7680691890017';
update Impfstoff set name = 'Spikevax bivalent BA.1 (Fertigspritze)' where code = '7680691230017';
update Impfstoff set name = 'Comirnaty monovalent' where code = '7680682250011';
update Impfstoff set name = 'Nuvaxovid' where code = '00380631000045';
update Impfstoff set name = 'Spikevax monovalent' where code = '30380777700688';
update Impfstoff set name = 'Spikevax bivalent BA.1' where code = '7680690090012';
update Impfstoff set name = 'Comirnaty bivalent BA.4-5' where code = '7680691270017';
update Impfstoff set name = 'Janssen' where code = '05413868120110';
update Impfstoff set name = 'Comirnaty Kinder monovalent' where code = '04260703260118';

# Nicht mehr verwendete Impfstoffe inaktivieren
update Impfstoff set eingestellt = true where code = '7680690470012';
update Impfstoff set eingestellt = true where code = '7680691230017';
update Impfstoff set eingestellt = true where code = '7680682250011';
update Impfstoff set eingestellt = true where code = '30380777700688';
update Impfstoff set eingestellt = true where code = '7680690090012';
update Impfstoff set eingestellt = true where code = '05413868120110';