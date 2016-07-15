/**
 *
 * FENIX (Food security and Early warning Network and Information Exchange)
 *
 * Copyright (c) 2008, by FAO of UN under the EC-FAO Food Security
 Information for Action Programme
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */


package org.fao.fenix.data;

import org.apache.log4j.Logger;
import org.fao.fenix.exception.FenixSystemException;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;


public class AMISDataImporter {

    private String path;


    private JDBCConnector jdbcConnector;

    private static final Logger LOGGER = Logger.getLogger(AMISDataImporter.class);


    public AMISDataImporter(Resource resource) throws FenixSystemException {
        try {
            this.setPath(resource.getFile().getPath());
        } catch (IOException e) {
            throw new FenixSystemException(e.getMessage());
        }
    }

    public int importDataFromCSVIntoTempTable(String importedCSVFileName, int expectedRows, String tempTableName){
        System.out.println("=== amis-ftp === AMISDataImporter: importDataFromCSVIntoTempTable: == INFO == Importing data into temp table ("+tempTableName+")...");
       return importDataFromCSV(importedCSVFileName, tempTableName ,expectedRows);
    }

    public void setPostgresDateFormat(){
        String sql = QueryBuilder.setDateFormat();
        jdbcConnector.executeUpdateSQL(sql);
    }

    public void resetPostgresDateFormat(){
        String sql = QueryBuilder.resetDateFormat();
        jdbcConnector.executeUpdateSQL(sql);
    }


    public int importDataFromCSVIntoDataTable(String importedCSVFileName,  String datasetCode, int expectedRows) {
       String tableName = tableNameQuery(datasetCode);
       System.out.println("=== amis-ftp === AMISDataImporter: importDataFromCSVIntoDataTable: == INFO == Importing data into table ("+tableName+")...");
       return importDataFromCSV(importedCSVFileName, tableName, expectedRows);
    }


     public int importDataFromCSV(String importedCSVFileName, String tableName, int expectedRows) {
         int rowsInserted = 0;
         String pathToCsv = path + File.separator + importedCSVFileName;

         System.out.println("=== amis-ftp === AMISDataImporter: importDataFromCSV: == INFO == "+ pathToCsv);

         File file = new File(pathToCsv);
         if(file.exists() &&  file.canRead()){

               if(tableName!=null){
                     // First clear data table
                     int intCount = deleteTableContentsQuery(tableName);

                     if(intCount==0)  {
                         //import the file
                         rowsInserted = importCSVIntoTable(tableName, pathToCsv);
                         System.out.println("=== amis-ftp === AMISDataImporter: importDataFromCSV: == INFO == Row Count of  = " + tableName + " = "+rowsInserted + " [expected "+expectedRows+"]");

                         return  rowsInserted;

                     }
                 }

         } else {
             System.out.println("=== amis-ftp === AMISDataImporter: importDataFromCSV: == ERROR == "+ pathToCsv + " does not exist or cannot be read ");
             throw new FenixSystemException("AMISDataImporter: importDataFromCSV(): "+ pathToCsv + " does not exist or cannot be read . ");
         }

         return  rowsInserted;

     }

    public String tableNameQuery(String datasetCode) throws FenixSystemException {
        String sql = QueryBuilder.getTableNameQuery(datasetCode);
        return jdbcConnector.executeSingleStringResultQuerySQL(sql);
    }

    public String getLastModifiedDate(String datasetCode) throws FenixSystemException {
        String sql = QueryBuilder.getLastModifiedDateQuery(datasetCode);
        return jdbcConnector.executeSingleStringResultQuerySQL(sql);
    }

    public String getLastModifiedDateFormatted(String datasetCode) throws FenixSystemException {
        String sql = QueryBuilder.getLastModifiedDateFormattedQuery(datasetCode);
        return jdbcConnector.executeSingleStringResultQuerySQL(sql);
    }


    public int deleteTableContentsQuery(String tablename) throws FenixSystemException {
        // First clear data table
        String sql = QueryBuilder.getDeleteTableContentsQuery(tablename);

        int rowsUpdated = jdbcConnector.executeUpdateSQL(sql);
        LOGGER.info("Delete contents of  = " + tablename + " (rows removed = "+rowsUpdated+")");

        //Check table is empty (count = 0)
        sql = QueryBuilder.getCountQuery(tablename);

       return jdbcConnector.executeSingleIntResultQuerySQL(sql);
  }

    public int updateLastModifiedDate(String datasetCode) throws FenixSystemException {
        String sql = QueryBuilder.getUpdateDateLastUpdateQuery(datasetCode);
        return jdbcConnector.executeUpdateSQL(sql);
    }

    public int importCSVIntoTable(String tablename, String pathToCsv) {
        String sql = QueryBuilder.getImportCSVQuery(tablename, pathToCsv);
        int rowsUpdated = jdbcConnector.executeUpdateSQL(sql);

        //Check table is not empty
        sql = QueryBuilder.getCountQuery(tablename);

        return jdbcConnector.executeSingleIntResultQuerySQL(sql);
    }


    public void setJdbcConnector(JDBCConnector jdbcConnector) {
        this.jdbcConnector = jdbcConnector;
    }


     public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }
}
