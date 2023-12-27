-- Disable foreign key constraints
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM PUBLIC.owners;
DELETE FROM PUBLIC.pets;
DELETE FROM PUBLIC.illnesss;
DELETE FROM PUBLIC.pet_types;
DELETE FROM PUBLIC.vets;
DELETE FROM PUBLIC.visits;
DELETE FROM PUBLIC.specialties;
DELETE FROM PUBLIC.usr;

-- Re-enable foreign key constraints
SET FOREIGN_KEY_CHECKS = 1;