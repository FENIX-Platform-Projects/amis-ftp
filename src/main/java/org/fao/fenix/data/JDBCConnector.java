/**
 *
 * FENIX (Food security and Early warning Network and Information Exchange)
 *
 * Copyright (c) 2011, by FAO of UN under the EC-FAO Food Security
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
import org.fao.fenix.exception.FenixExceptionUtils;

import java.sql.*;
import java.util.*;


public class JDBCConnector extends BaseJDBC {

    private final static Logger LOGGER = Logger.getLogger(JDBCConnector.class);

    public int executeUpdateSQL(String sql) throws FenixSystemException {
        int value = 0;
        try {
            this.setStmt(this.getConn().createStatement());
            value = this.getStmt().executeUpdate(sql);
            this.getStmt().close();
        } catch (SQLException e) {
            System.out.println("=== amis-ftp === JDBCConnector: executeUpdateSQL: == ERROR == SQL Exception for " + sql);
            System.out.println("=== amis-ftp === JDBCConnector: executeUpdateSQL: == ERROR == SQL Exception STACK TRACE  " + FenixExceptionUtils.exceptionStackTraceToString(e));

            // e.printStackTrace();
            String  message = " \n\n ==== SQL ==== \n\n "+ sql + "\n\n ==== Stack Trace ==== \n\n " +FenixExceptionUtils.exceptionStackTraceToString(e);

            throw new FenixSystemException("=== amis-ftp === JDBCConnector: executeUpdateSQL(): SQLException "+ message);
         }
        finally{
            try{
                if(this.getStmt()!=null)
                    this.getStmt().close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }

        return value;
    }



    public int executeSingleIntResultQuerySQL(String sql) throws FenixSystemException {
        int value = 0;
        ResultSet rs = null;
        try {
            this.setStmt(this.getConn().createStatement());
            this.getStmt().executeQuery(sql);
            rs = this.getStmt().getResultSet();
            rs.next(); // exactly one result so allowed
            value = rs.getInt(1);
            rs.close();
            this.getStmt().close();
        } catch (SQLException e) {
          //  LOGGER.error("SQLException");
           // e.printStackTrace();
            System.out.println("=== amis-ftp === JDBCConnector: executeSingleIntResultQuerySQL: == ERROR == SQL Exception for " + sql);
            System.out.println("=== amis-ftp === JDBCConnector: executeSingleIntResultQuerySQL: == ERROR == SQL Exception STACK TRACE  " + FenixExceptionUtils.exceptionStackTraceToString(e));

            // e.printStackTrace();
            String  message = " \n\n ==== SQL ==== \n\n "+ sql + "\n\n ==== Stack Trace ==== \n\n " +FenixExceptionUtils.exceptionStackTraceToString(e);

            throw new FenixSystemException("=== amis-ftp === JDBCConnector: executeSingleIntResultQuerySQL(): SQLException "+ message);

        } finally{
            try{
             if(rs!=null)
                    rs.close();
              if(this.getStmt()!=null)
                    this.getStmt().close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }

        return value;
    }

    public String executeSingleStringResultQuerySQL(String sql) throws FenixSystemException {
        String value = null;
        ResultSet rs = null;
        try {
            this.setStmt(this.getConn().createStatement());
            this.getStmt().executeQuery(sql);
            rs = this.getStmt().getResultSet();
            rs.next(); // exactly one result so allowed
            value = rs.getString(1);
            rs.close();
            this.getStmt().close();
        } catch (SQLException e) {
            //LOGGER.error("SQLException");
            //e.printStackTrace();
            System.out.println("=== amis-ftp === JDBCConnector: executeSingleStringResultQuerySQL: == ERROR == SQL Exception for " + sql);
            System.out.println("=== amis-ftp === JDBCConnector: executeSingleStringResultQuerySQL: == ERROR == SQL Exception STACK TRACE  " + FenixExceptionUtils.exceptionStackTraceToString(e));

            // e.printStackTrace();
            String  message = " \n\n ==== SQL ==== \n\n "+ sql + "\n\n ==== Stack Trace ==== \n\n " +FenixExceptionUtils.exceptionStackTraceToString(e);

            throw new FenixSystemException("=== amis-ftp === JDBCConnector: executeSingleStringResultQuerySQL(): SQLException "+ message);

        }   finally{
            try{
                if(rs!=null)
                    rs.close();
                if(this.getStmt()!=null)
                    this.getStmt().close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return value;
    }

    public List<List<String>> executeQuerySQL(String sql) throws FenixSystemException {
        List<List<String>> table = new ArrayList<List<String>>();
        ResultSet rs = null;
         try {
            this.setStmt(this.getConn().createStatement());
            this.getStmt().executeQuery(sql);
            rs = this.getStmt().getResultSet();
             while (rs.next()) {
                 List<String> row = new ArrayList<String>();
                 for (int i = 1 ; i < Integer.MAX_VALUE ; i++) {
                     try {
                         row.add(rs.getString(i).trim());
                     } catch (SQLException e) {
                         break;
                     } catch (NullPointerException e) {
                         row.add("");
                     }
                 }
                 table.add(row);
             }

             rs.close();
             this.getStmt().close();
        } catch (SQLException e) {
             //LOGGER.error("SQLException");
             //e.printStackTrace();
             System.out.println("=== amis-ftp === JDBCConnector: executeQuerySQL: == ERROR == SQL Exception for " + sql);
             System.out.println("=== amis-ftp === JDBCConnector: executeQuerySQL: == ERROR == SQL Exception STACK TRACE  " + FenixExceptionUtils.exceptionStackTraceToString(e));

             // e.printStackTrace();
             String  message = " \n\n ==== SQL ==== \n\n "+ sql + "\n\n ==== Stack Trace ==== \n\n " +FenixExceptionUtils.exceptionStackTraceToString(e);

             throw new FenixSystemException("=== amis-ftp === JDBCConnector: executeQuerySQL(): SQLException "+ message);


         }  finally{
             try{
                 if(rs!=null)
                     rs.close();
                 if(this.getStmt()!=null)
                     this.getStmt().close();
             }catch(SQLException se){
                 se.printStackTrace();
             }
         }

        return table;
    }

}