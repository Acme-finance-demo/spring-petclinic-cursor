INSERT INTO vets VALUES (default, 'James', 'Carter');
INSERT INTO vets VALUES (default, 'Helen', 'Leary');
INSERT INTO vets VALUES (default, 'Linda', 'Douglas');
INSERT INTO vets VALUES (default, 'Rafael', 'Ortega');
INSERT INTO vets VALUES (default, 'Henry', 'Stevens');
INSERT INTO vets VALUES (default, 'Sharon', 'Jenkins');

INSERT INTO specialties VALUES (default, 'radiology');
INSERT INTO specialties VALUES (default, 'surgery');
INSERT INTO specialties VALUES (default, 'dentistry');

INSERT INTO vet_specialties VALUES (2, 1);
INSERT INTO vet_specialties VALUES (3, 2);
INSERT INTO vet_specialties VALUES (3, 3);
INSERT INTO vet_specialties VALUES (4, 2);
INSERT INTO vet_specialties VALUES (5, 1);

INSERT INTO types VALUES (default, 'cat');
INSERT INTO types VALUES (default, 'dog');
INSERT INTO types VALUES (default, 'lizard');
INSERT INTO types VALUES (default, 'snake');
INSERT INTO types VALUES (default, 'bird');
INSERT INTO types VALUES (default, 'hamster');

INSERT INTO owners VALUES (default, 'George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023');
INSERT INTO owners VALUES (default, 'Betty', 'Davis', '638 Cardinal Ave.', 'Sun Prairie', '6085551749');
INSERT INTO owners VALUES (default, 'Eduardo', 'Rodriquez', '2693 Commerce St.', 'McFarland', '6085558763');
INSERT INTO owners VALUES (default, 'Harold', 'Davis', '563 Friendly St.', 'Windsor', '6085553198');
INSERT INTO owners VALUES (default, 'Peter', 'McTavish', '2387 S. Fair Way', 'Madison', '6085552765');
INSERT INTO owners VALUES (default, 'Jean', 'Coleman', '105 N. Lake St.', 'Monona', '6085552654');
INSERT INTO owners VALUES (default, 'Jeff', 'Black', '1450 Oak Blvd.', 'Monona', '6085555387');
INSERT INTO owners VALUES (default, 'Maria', 'Escobito', '345 Maple St.', 'Madison', '6085557683');
INSERT INTO owners VALUES (default, 'David', 'Schroeder', '2749 Blackhawk Trail', 'Madison', '6085559435');
INSERT INTO owners VALUES (default, 'Carlos', 'Estaban', '2335 Independence La.', 'Waunakee', '6085555487');

INSERT INTO pets VALUES (default, 'Leo', '2010-09-07', 1, 1);
INSERT INTO pets VALUES (default, 'Basil', '2012-08-06', 6, 2);
INSERT INTO pets VALUES (default, 'Rosy', '2011-04-17', 2, 3);
INSERT INTO pets VALUES (default, 'Jewel', '2010-03-07', 2, 3);
INSERT INTO pets VALUES (default, 'Iggy', '2010-11-30', 3, 4);
INSERT INTO pets VALUES (default, 'George', '2010-01-20', 4, 5);
INSERT INTO pets VALUES (default, 'Samantha', '2012-09-04', 1, 6);
INSERT INTO pets VALUES (default, 'Max', '2012-09-04', 1, 6);
INSERT INTO pets VALUES (default, 'Lucky', '2011-08-06', 5, 7);
INSERT INTO pets VALUES (default, 'Mulligan', '2007-02-24', 2, 8);
INSERT INTO pets VALUES (default, 'Freddy', '2010-03-09', 5, 9);
INSERT INTO pets VALUES (default, 'Lucky', '2010-06-24', 2, 10);
INSERT INTO pets VALUES (default, 'Sly', '2012-06-08', 1, 10);

INSERT INTO visits VALUES (default, 7, '2013-01-01', 'rabies shot');
INSERT INTO visits VALUES (default, 8, '2013-01-02', 'rabies shot');
INSERT INTO visits VALUES (default, 8, '2013-01-03', 'neutered');
INSERT INTO visits VALUES (default, 7, '2013-01-04', 'spayed');

-- Sample vet schedules (Monday=1, Tuesday=2, ..., Sunday=0)
INSERT INTO vet_schedules VALUES (default, 1, 1, '09:00', '17:00', true);  -- James Carter, Monday
INSERT INTO vet_schedules VALUES (default, 1, 2, '09:00', '17:00', true);  -- James Carter, Tuesday
INSERT INTO vet_schedules VALUES (default, 1, 3, '09:00', '17:00', true);  -- James Carter, Wednesday
INSERT INTO vet_schedules VALUES (default, 1, 4, '09:00', '17:00', true);  -- James Carter, Thursday
INSERT INTO vet_schedules VALUES (default, 1, 5, '09:00', '17:00', true);  -- James Carter, Friday
INSERT INTO vet_schedules VALUES (default, 1, 6, '09:00', '14:00', true);  -- James Carter, Saturday

INSERT INTO vet_schedules VALUES (default, 2, 1, '08:00', '16:00', true);  -- Helen Leary, Monday
INSERT INTO vet_schedules VALUES (default, 2, 2, '08:00', '16:00', true);  -- Helen Leary, Tuesday
INSERT INTO vet_schedules VALUES (default, 2, 3, '08:00', '16:00', true);  -- Helen Leary, Wednesday
INSERT INTO vet_schedules VALUES (default, 2, 4, '08:00', '16:00', true);  -- Helen Leary, Thursday
INSERT INTO vet_schedules VALUES (default, 2, 5, '08:00', '16:00', true);  -- Helen Leary, Friday

INSERT INTO vet_schedules VALUES (default, 3, 2, '10:00', '18:00', true);  -- Linda Douglas, Tuesday
INSERT INTO vet_schedules VALUES (default, 3, 3, '10:00', '18:00', true);  -- Linda Douglas, Wednesday
INSERT INTO vet_schedules VALUES (default, 3, 4, '10:00', '18:00', true);  -- Linda Douglas, Thursday
INSERT INTO vet_schedules VALUES (default, 3, 5, '10:00', '18:00', true);  -- Linda Douglas, Friday
INSERT INTO vet_schedules VALUES (default, 3, 6, '09:00', '15:00', true);  -- Linda Douglas, Saturday

-- Sample vet holidays
INSERT INTO vet_holidays VALUES (default, 1, '2024-12-25', 'Christmas Day');
INSERT INTO vet_holidays VALUES (default, 1, '2024-01-01', 'New Year Day');
INSERT INTO vet_holidays VALUES (default, 2, '2024-02-15', 'Personal leave');

-- Sample appointments
INSERT INTO appointments VALUES (default, 1, 1, '2024-01-15', '10:00', 30, 'Regular checkup', 'Pet seems healthy but owner wants routine examination', 'CONFIRMED', 'First appointment for this pet', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO appointments VALUES (default, 2, 2, '2024-01-15', '14:30', 45, 'Vaccination', 'Annual vaccination due', 'PENDING', 'Reminder sent to owner', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO appointments VALUES (default, 3, 1, '2024-01-16', '11:00', 30, 'Follow-up', 'Check healing progress after surgery', 'COMPLETED', 'Recovery going well', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO appointments VALUES (default, 4, 3, '2024-01-17', '09:30', 60, 'Dental cleaning', 'Routine dental care', 'CONFIRMED', 'Fasting required before procedure', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
