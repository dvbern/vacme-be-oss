-- Comirnaty Kinder: orange statt rosa
update Impfstoff set name = 'Comirnaty® Kinderformulierung' where id = '4ebb48c1-cc96-4a8e-9832-77092bb968db';
update Impfstoff set hexFarbe = '#FE9A2E' where id = '4ebb48c1-cc96-4a8e-9832-77092bb968db';
-- Bharat Biotech Covaxin: rosa statt orange
update Impfstoff set hexFarbe = '#ff8dd6' where id = '54513128-c6ed-4fd6-b61d-c23e6a73b20c';