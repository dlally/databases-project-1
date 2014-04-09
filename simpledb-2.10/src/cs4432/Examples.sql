Douglas Lally - dlally@wpi.edu
Nathaniel Miller - nwmiller@wpi.edu
CS4432 - Project 1
-----------------------------------
-- The following two tables were created:
CREATE TABLE Maps(MID int, MName varchar(50), MSize int);
CREATE TABLE Vehicles(VID int, VName varchar(30), VCapacity int, VMaxSpeed int);

-- The tables were populated with example data using insert into statements.
INSERT INTO Maps(MID, MName, MSize) VALUES (1, 'Siege of Shanghai', 64)
INSERT INTO Maps(MID, MName, MSize) VALUES (2, 'Paracel Storm', 64)
INSERT INTO Maps(MID, MName, MSize) VALUES (3, 'Operation Locker', 32)
INSERT INTO Maps(MID, MName, MSize) VALUES (4, 'Flood Zone', 64)
INSERT INTO Maps(MID, MName, MSize) VALUES (5, 'Golmud Railway', 64)
INSERT INTO Maps(MID, MName, MSize) VALUES (6, 'Dawnbreaker', 64)
INSERT INTO Maps(MID, MName, MSize) VALUES (7, 'Hainan Resort', 32)
INSERT INTO Maps(MID, MName, MSize) VALUES (8, 'Lancang Dam', 32)
INSERT INTO Maps(MID, MName, MSize) VALUES (9, 'Rouge Transmission', 64)
INSERT INTO Maps(MID, MName, MSize) VALUES (10, 'Zavod 311', 32)

INSERT INTO Vehicles(VID, VName, VCapacity, VMaxSpeed) VALUES (1, 'M1 Abrams', 3, 50)
INSERT INTO Vehicles(VID, VName, VCapacity, VMaxSpeed) VALUES (2, 'T-90', 2, 60)
INSERT INTO Vehicles(VID, VName, VCapacity, VMaxSpeed) VALUES (3, 'LAV-25', 8, 70)
INSERT INTO Vehicles(VID, VName, VCapacity, VMaxSpeed) VALUES (4, 'BTR-90', 8, 75)
INSERT INTO Vehicles(VID, VName, VCapacity, VMaxSpeed) VALUES (5, 'Humvee', 4, 90)
INSERT INTO Vehicles(VID, VName, VCapacity, VMaxSpeed) VALUES (6, 'Vodnik', 4, 100)

-- The populated tables were then queried:
SELECT MID, MName, MSize FROM Maps WHERE MSize = 64;
SELECT VName, VCapacity, VMaxSpeed FROM Vehicles WHERE VMaxSpeed = 70


-- The following is output from those queries
Table Maps created.
Table Vehicles created.
Map records inserted into table Maps.
Vehicle records inserted into table Vehicles.

Table Maps Example Query Results:

Map #1: Siege of Shanghai supports 64 players.
Map #2: Paracel Storm supports 64 players.
Map #4: Flood Zone supports 64 players.
Map #5: Golmud Railway supports 64 players.
Map #6: Dawnbreaker supports 64 players.
Map #9: Rouge Transmission supports 64 players.

Table Vehicles Example Query Results:

Vehicle LAV-25 carries 8 players at a top speed of 70 km/h."

-- Then the records in the tables are deleted from the tables to ensure
-- of records works and to clean up the tables for subsequent program
-- executions
DELETE FROM Maps WHERE MID = 0
DELETE FROM Maps WHERE MID = 1
DELETE FROM Maps WHERE MID = 2
DELETE FROM Maps WHERE MID = 3
DELETE FROM Maps WHERE MID = 4
DELETE FROM Maps WHERE MID = 5
DELETE FROM Maps WHERE MID = 6
DELETE FROM Maps WHERE MID = 7
DELETE FROM Maps WHERE MID = 8
DELETE FROM Maps WHERE MID = 9

DELETE FROM Maps WHERE VID = 0
DELETE FROM Maps WHERE MID = 1
DELETE FROM Maps WHERE MID = 2
DELETE FROM Maps WHERE MID = 3
DELETE FROM Maps WHERE MID = 4
DELETE FROM Maps WHERE MID = 5