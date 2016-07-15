package org.fao.fenix.data;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class BaseJDBC {
    private Connection conn;
    private Statement stmt;
    private String DB_USERNAME;
    private String DB_PASSWORD;
    private String DB_URL;
    private static final Logger LOGGER = Logger.getLogger(BaseJDBC.class);

    public BaseJDBC() {
    }

    public void openConnection() {
        try {
            Class.forName("org.postgresql.Driver").newInstance();
            this.conn = DriverManager.getConnection(this.DB_URL, this.DB_USERNAME, this.DB_PASSWORD);
            this.stmt = this.conn.createStatement();
        } catch (Exception var2) {
            System.err.println("Cannot connect to database server: " + var2.getMessage());
        }

    }

    public void closeConnection() {
        try {
            this.conn.close();
            this.stmt.close();
        } catch (Exception var2) {
            System.err.println("Close error.");
        }

    }

    public void setDB_USERNAME(String dBUSERNAME) {
        this.DB_USERNAME = dBUSERNAME;
    }

    public void setDB_PASSWORD(String dBPASSWORD) {
        this.DB_PASSWORD = dBPASSWORD;
    }

    public void setDB_URL(String dBURL) {
        this.DB_URL = dBURL;
    }

    public Connection getConn() {
        return this.conn;
    }

    public Statement getStmt() {
        return this.stmt;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public void setStmt(Statement stmt) {
        this.stmt = stmt;
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}

