package Interfaces;

import java.sql.SQLException;

/**
 * INTERFACE CONTRACT forming basis for all persistent models in our application.
 * each model implementing this requires a mechanism to commit state [WRITE]
 * to the database and retrieve state [READ] back into memory.
 */
public interface IDatabaseModel {

    /**
     * Create table for current clas if it isn't already present in db.
     */
    public void CreateTable() throws SQLException;

    /**
     * Persists the current object state to the database.
     * Operations should ensure data is [SAVED] to the storage layer.
     */
    public void WriteToDatabase() throws SQLException;

    /**
     * Populates the object state from the database.
     * Operations should ensure data is [FETCHED] from the storage layer.
     */
    public void ReadFromDatabase();
}