package edu.usc.iiw.storage;

import java.io.IOException;
import java.util.HashMap;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

/**
 * Object store wrapper for Berkley DB
 * 
 * @author 
 *
 */

public class DBWrapper {

	String envDirectory = null;
	Environment myEnv;
	public Database myDatabase;
	String dbName;
	
	public Database getDatabase() {
		return myDatabase;
	}
	
	public DBWrapper(Environment e, String dbN) {
		myEnv = e;
		dbName = dbN;

		try {
			// create a configuration for DB
			DatabaseConfig dbConf = new DatabaseConfig();
			// db will be created if not exits

			dbConf.setAllowCreate(true);
			dbConf.setTransactional(true);
			dbConf.setAllowCreate(true);

			// create/open testDB using config
			myDatabase = myEnv.openDatabase(null, dbName, dbConf);

		} catch (DatabaseException dbe) {
			System.out.println("Error :" + dbe.getMessage());
		}
	}

	/**
	 * Insert the data onto database with the specified key
	 * @param key
	 * @param data
	 * @throws Exception
	 */
	public void insert(Object key, Object data) throws Exception {
		Serializer serializer = new Serializer();
		final DatabaseEntry keyEntry = new DatabaseEntry(
				serializer.serialize(key));
		final DatabaseEntry dataEntry = new DatabaseEntry(
				serializer.serialize(data));

		try {
			final Transaction txn = myEnv.beginTransaction(null, null);
			final OperationStatus res = myDatabase
					.put(txn, keyEntry, dataEntry);
			if (res != OperationStatus.SUCCESS) {
				throw new Exception("Error inserting into database");
			}
			txn.commit();
		} catch (DatabaseException DE) {
			System.out.println(DE);
		}
	}

	/**
	 * Retrieve the object with the specified key
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public Object get(Object key) throws Exception {
		Serializer serializer = new Serializer();
		final DatabaseEntry keyEntry = new DatabaseEntry(
				serializer.serialize(key));
		Object result;

		final DatabaseEntry dataEntry = new DatabaseEntry();

		final Transaction txn = myEnv.beginTransaction(null, null);
		final OperationStatus res = myDatabase.get(txn, keyEntry, dataEntry,
				null);
		if (res != OperationStatus.SUCCESS) {
			return null;
		} else {
			result = serializer.deserialize(dataEntry.getData());
		}
		txn.commit();
		return result;
	}
	
	/**
	 * Delete the object with the specified key
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public void delete(Object key) throws Exception {
		Serializer serializer = new Serializer();
		final DatabaseEntry keyEntry = new DatabaseEntry(
				serializer.serialize(key));
		final Transaction txn = myEnv.beginTransaction(null, null);
		final OperationStatus res = myDatabase.delete(txn, keyEntry);
		if (res != OperationStatus.SUCCESS) {
			//throw new Exception("Error deleting from database");
			return;
		} 
		txn.commit();
		return;
	}
	
	
	public HashMap<Object, Object> iterate() throws ClassNotFoundException, IOException {
		HashMap<Object, Object> dataMap = new HashMap<Object, Object>();
		Cursor cursor = null;
		Serializer serializer = new Serializer();
		try {
		    // Database and environment open omitted for brevity
		    
		    // Open the cursor. 
		    cursor = myDatabase.openCursor(null, null);

		    // Cursors need a pair of DatabaseEntry objects to operate. These hold
		    // the key and data found at any given position in the database.
		    DatabaseEntry foundKey = new DatabaseEntry();
		    DatabaseEntry foundData = new DatabaseEntry();

		    // To iterate, just call getNext() until the last database record has been 
		    // read. All cursor operations return an OperationStatus, so just read 
		    // until we no longer see OperationStatus.SUCCESS
		    while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) ==
		        OperationStatus.SUCCESS) {
		        // getData() on the DatabaseEntry objects returns the byte array
		        // held by that object. We use this to get a String value. If the
		        // DatabaseEntry held a byte array representation of some other data
		        // type (such as a complex object) then this operation would look 
		        // considerably different.
		        Object key = serializer.deserialize(foundKey.getData());
		        Object value = serializer.deserialize(foundData.getData());
		        dataMap.put(key, value);
		    }
		} catch (DatabaseException de) {
		    System.err.println("Error accessing database." + de);
		} finally {
		    // Cursors must be closed.
		    cursor.close();
		}
		return dataMap;
	}
	
	/**
	 * Delete the database from the disk
	 * @param databaseName name of the database to be deleted
	 */
	public void removeDatabase() {
        final Transaction txn = myEnv.beginTransaction(null, null);
        close();
        myEnv.removeDatabase(txn, dbName);
        txn.commit();
    }
	
	/**
	 * Close the database connection
	 */
	public void close() {
		myDatabase.close();
	}
}
