package de.shop.util.impl;

import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.dbunit.operation.DatabaseOperation.CLEAN_INSERT;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.oracle.OracleConnection;

import de.shop.util.DbReload;


/**
 */
public class DbReloadImpl implements DbReload {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private boolean dbReloaded = false;
	
	private static final String DATASET_XML = "src/test/resources/datasets/db.xml";
	private static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
	
	private String dbUrl;
	private String user;
	private String password;
	private IDataSet dataset;
	
	public DbReloadImpl() throws ClassNotFoundException, MalformedURLException, DataSetException {
		dbUrl = System.getProperty("url");
		
		boolean caseSensitive = false;
		if (dbUrl.contains("oracle")) {
			Class.forName(ORACLE_DRIVER);
		}
		else {
			throw new IllegalStateException("Die Datenbank-URL " + dbUrl + " wird nicht unterstuetzt");
		}
		
		user = System.getProperty("user");
		password = System.getProperty("password");

		final URL datasetUrl = Paths.get(DATASET_XML).toUri().toURL();
		final FlatXmlDataSetBuilder flatXmlDataSetBuilder = new FlatXmlDataSetBuilder();
		dataset = flatXmlDataSetBuilder.setCaseSensitiveTableNames(caseSensitive)
				                       .build(datasetUrl);
	}
	
	@Override
	public void run() {
		if (dbReloaded) {
			LOGGER.log(FINEST, "Die Datenbank {0} ist bereits neu geladen.", dbUrl);
			return;
		}
		
		LOGGER.log(INFO, "Die Datenbank {0} wird neu geladen...", dbUrl);
		
		Connection jdbcConn = null;
		IDatabaseConnection dbunitConn = null;
		try {			
			try {
				jdbcConn = DriverManager.getConnection(dbUrl, user, password);
	
				if (dbUrl.contains("oracle")) {
					dbunitConn = new OracleConnection(jdbcConn, user.toUpperCase());
				}
				else {
					throw new IllegalStateException("Der Datenbank-URL " + dbUrl + " wird nicht unterstuetzt");
				}
				
				CLEAN_INSERT.execute(dbunitConn, dataset);
			}
			catch (SQLException | DatabaseUnitException e) {
				throw new RuntimeException("Fehler beim Laden der Datenbank", e);
			}
			
			dbReloaded = true;
			dbUrl = null;
			user = null;
			password = null;
			dataset = null;
			
			LOGGER.info("... die Datenbank wurde neu geladen");
		}
		finally {
			try {
				if (dbunitConn != null) {
					dbunitConn.close();
					dbunitConn = null;
				}
				if (jdbcConn != null) {
					jdbcConn.close();
					jdbcConn = null;
				}
			}
			catch (SQLException e) {
				LOGGER.log(WARNING, "Fehler beim Schliessen der Datenbank", e);
			}
		}
	}
}
