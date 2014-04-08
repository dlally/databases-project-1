package cs4432;

import java.sql.*;
import simpledb.remote.SimpleDriver;
import javax.xml.transform.Result;

/**
 * Class TestDB is a test program for exercising the creation of,
 * population of, and querying of tables using SimpleDB.
 *
 * @author Nathaniel Miller
 * @version 4/8/2014
 */
public class TestDB {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            // Initialize the db driver and connect to the SimpleDB local database.
            Driver driver = new SimpleDriver();
            conn = driver.connect("jdbc:simpledb://localhost", null);

            // Create statements and SQL strings for table creation
            Statement stmt1 = conn.createStatement();
            String tbl1 = "CREATE TABLE Maps(MID int, MName varchar(50), MSize int)";
            Statement stmt2 = conn.createStatement();
            String tbl2 = "CREATE TABLE Vehicles(VID int, VName varchar(30), VCapacity int, VMaxSpeed int)";

            // Create the two test tables
            stmt1.executeUpdate(tbl1);
            System.out.println("Table Maps created.");
            stmt2.executeUpdate(tbl2);
            System.out.println("Table Vehicles created.");

            // Populate the Maps test table with example data
            String popTbl1 = "INSERT INTO Maps(MID, MName, MSize) VALUES ";
            String[] mapData = {"(1, 'Siege of Shanghai', 64)",
                                "(2, 'Paracel Storm', 64)",
                                "(3, 'Operation Locker', 32)",
                                "(4, 'Flood Zone', 64)",
                                "(5, 'Golmud Railway', 64)",
                                "(6, 'Dawnbreaker', 64)",
                                "(7, 'Hainan Resort', 32)",
                                "(8, 'Lancang Dam', 32)",
                                "(9, 'Rouge Transmission', 64)",
                                "(10, 'Zavod 311', 32)"};

            // Insert map records into Maps table
            for (int i=0; i<mapData.length; i++) {
                stmt1.executeUpdate(popTbl1 + mapData[i]);
            }
            System.out.println("Map records inserted into table Maps.");

            // Populate the Vehicles test table with example data
            String popTbl2 = "INSERT INTO Vehicles(VID, VName, VCapacity, VMaxSpeed) VALUES ";
            String[] vehicleData = {"(1, 'M1 Abrams', 3, 50)",
                    "(2, 'T-90', 2, 60)",
                    "(3, 'LAV-25', 8, 70)",
                    "(4, 'BTR-90', 8, 75)",
                    "(5, 'Humvee', 4, 90)",
                    "(6, 'Vodnik', 4, 100)",};

            // Insert vehicle records into Vehicles table
            for(int i = 0; i < vehicleData.length; i++) {
                stmt1.executeUpdate(popTbl2 + vehicleData[i]);
            }
            System.out.println("Vehicle records inserted into table Vehicles.");

            // Query the Maps table
            String queryTbl1 = "SELECT MID, MName, MSize FROM Maps WHERE MSize = 64";
            ResultSet res1 = stmt1.executeQuery(queryTbl1);

            // Print out the result set
            System.out.println("\nTable Maps Example Query Results:\n");
            while(res1.next()) {
                int mapID = res1.getInt("MID");
                String mapName = res1.getString("MName");
                int mapSize = res1.getInt("MSize");
                System.out.println("Map #" + mapID + ": " + mapName + " supports " + mapSize + " players.");
            }
            res1.close();

            // Query the Vehicles table
            String queryTbl2 = "SELECT VName, VCapacity, VMaxSpeed FROM Vehicles WHERE VMaxSpeed = 70";
            ResultSet res2 = stmt2.executeQuery(queryTbl2);


            // Print out the result set
            System.out.println("\nTable Vehicles Example Query Results:\n");
            while(res2.next()) {
                String vehicName = res2.getString("VName");
                int vehicCap = res2.getInt("VCapacity");
                int vehicMaxSpeed = res2.getInt("VMaxSpeed");
                System.out.println("Vehicle " + vehicName + " carries " + vehicCap + " players at a top speed of " + vehicMaxSpeed + " km/h.");

            }
            res2.close();


            // Clean up and delete records from test tables
            String deleteData = "DELETE FROM Maps WHERE MID = ";
            for(int i = 1; i < mapData.length; i++) {
                stmt1.executeUpdate(deleteData + Integer.toString(i));
            }
            deleteData = "DELETE FROM Vehicles WHERE VID = ";
            for(int i = 1; i < vehicleData.length; i++) {
                stmt2.executeUpdate(deleteData + Integer.toString(i));
            }

        }
        catch(SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (conn != null)
                    conn.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
