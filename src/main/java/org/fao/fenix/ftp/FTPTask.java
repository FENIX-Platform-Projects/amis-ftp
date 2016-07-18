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

// This is the class that you want to schedule in Quartz
package org.fao.fenix.ftp;


import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.fao.fenix.data.AMISDataImporter;
import org.fao.fenix.data.CSVBasicValidator;
import org.fao.fenix.data.JDBCConnector;
import org.fao.fenix.email.EmailSender;
import org.fao.fenix.exception.FenixSystemException;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.FileOutputStream;

import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;

public class FTPTask {

    private String path;
    private String serverAddress;
    private String remoteDirectory;
    private String username;
    private String password;


    private AMISDataImporter amisDataImporter;
    private CSVBasicValidator csvBasicValidator;

    private JDBCConnector jdbcConnector;

    private FTPClient ftpClient;

    private EmailSender emailSender;

    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(FTPTask.class);

    private static String emailSubject = "AMIS-FTP Automatic Data Uploader";
    private static String emailErrorSubject = emailSubject+" Problems";


    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    public FTPTask(Resource resource) throws FenixSystemException {
        try {
            System.out.println("=== amis-ftp === FTPTask: == INFO == resource.getFile().getPath() ..."+resource.getFile().getPath());
            System.out.println(resource.getFile().getPath());

            this.setPath(resource.getFile().getPath());
        } catch (IOException e) {
            throw new FenixSystemException(e.getMessage());
        }
    }

    public void connectToFTP(String datasetDirectoryName) {
        FTPClient newFtpClient = new FTPClient();
        setFtpClient(newFtpClient);
        checkFTPDirectory(datasetDirectoryName);
    }

    private void checkFTPDirectory(String datasetDirectoryName) {

        try{
             if(ftpConnect()){
                System.out.println("=== amis-ftp === FTPTask: checkFTPDirectory: == INFO == Task executing ...");
                //enter passive mode
                ftpClient.enterLocalPassiveMode();

                //get system name
                System.out.println("=== amis-ftp === FTPTask: checkFTPDirectory: == INFO == Remote system is " + ftpClient.getSystemType());


                //change current directory
                ftpClient.changeWorkingDirectory(remoteDirectory+ File.separator +datasetDirectoryName);
                System.out.println("=== amis-ftp === FTPTask: checkFTPDirectory: == INFO == Current directory is " + ftpClient.printWorkingDirectory());


                //get list of filenames
                FTPFile[] ftpFiles = ftpClient.listFiles();

                // Check the directory
                if (ftpFiles != null && ftpFiles.length > 0) {
                    ArrayList csvFiles = new ArrayList();

                    for (FTPFile file : ftpFiles) {
                        if (file.isFile() && file.getName().endsWith(".csv")) {
                            csvFiles.add(file);
                        }
                    }

                    FTPFile[] csvFTPFiles = (FTPFile[])csvFiles.toArray(new FTPFile[csvFiles.size()]);

                    System.out.println("=== amis-ftp === FTPTask: checkFTPDirectory: == INFO ==  Current directory contains  " + csvFTPFiles.length + " CSV File(s)");

                    if(csvFTPFiles.length == 1){
                        validateAndImportFile(csvFTPFiles, datasetDirectoryName);
                    }
                    else   {
                        System.out.println("=== amis-ftp === FTPTask: checkFTPDirectory: == ERROR ==  The ftp://ext-ftp.fao.org"+ftpClient.printWorkingDirectory()+" directory contains "+ftpFiles.length+" files, instead of the expected 1. Please remove the redundant files. [THE ERROR EMAIL HAS BEEN SENT TO USER]");
                        //throw new FenixException("** The ftp://ext-ftp.fao.org"+ftpClient.printWorkingDirectory()+" directory contains "+ftpFiles.length+" files, instead of the expected 1. Please remove the redundant files.");
                    }
                }
                else {
                    System.out.println("=== amis-ftp === FTPTask: checkFTPDirectory: == WARNING ==  The ftp://ext-ftp.fao.org"+ftpClient.printWorkingDirectory()+" directory contains no files. No action taken until 1 CSV file is there.");
                }

                ftpClient.logout();
                ftpClient.disconnect();

            }  else {
                ftpClient.disconnect();
            }
        } catch(IOException ex){
            ex.printStackTrace();
        }
        //Email to Developers
        catch (FenixSystemException fse) {
            System.out.println("=== amis-ftp === FTPTask: checkFTPDirectory: == ERROR ==  EMAIL SENT TO AMIS DEVELOPERS FenixSystemException: ");
            emailSender.sendEMailToDevelopers(emailErrorSubject, fse.getMessage());
           //emailSender.sendEMailToContacts(emailErrorSubject, fse.getMessage(), false);
        }
        finally{
            try{
                if(jdbcConnector.getConn()!=null)
                    jdbcConnector.getConn().close();

                if(ftpClient.isConnected())
                    ftpClient.disconnect();

            }catch(SQLException se){
                se.printStackTrace();
            }
            catch(IOException se){
                se.printStackTrace();
            }
        }
    }

    public boolean ftpConnect(){
        try {

            if(getFtpClient()!=null){
                //try to connect
                getFtpClient().connect(serverAddress);
                //login to server
                if(!getFtpClient().login(username, password))
                //if(!getFtpClient().login(user, ""))
                {
                    getFtpClient().logout();
                    return false;
                }  else {
                    System.out.println("=== amis-ftp === FTPTask: ftpConnect: == INFO == Logged in ! for "+username + " to  "+serverAddress);
                }
                int reply = getFtpClient().getReplyCode();

                //FTPReply stores a set of constants for FTP reply codes.
                if (!FTPReply.isPositiveCompletion(reply))
                {
                    getFtpClient().disconnect();
                    return false;
                }  else {
                    System.out.println("=== amis-ftp === FTPTask: ftpConnect: == INFO ==  Positive Completion!");
                    return true;
                }
            } else {
                System.out.println("=== amis-ftp === FTPTask: ftpConnect: == ERROR ==  ftpConnectFTP Client is Null ");
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            return false;
        }


        return true;
    }

    private void  validateAndImportFile(FTPFile[] ftpFiles, String datasetDirectoryName)  {

        String datasetCode = "";
        String tempTableName = "";

        if(datasetDirectoryName.equalsIgnoreCase("indices")){
            datasetCode = "AMIS_IGC_DAILY_INDICATORS";
            tempTableName = "igc_daily_indicators_temp";
        }

        jdbcConnector.openConnection();
        // Check the last modified date of the database
        String lastModifiedDate = amisDataImporter.getLastModifiedDate(datasetCode);

        jdbcConnector.closeConnection();

        System.out.println("=== amis-ftp === FTPTask: validateAndImportFile: == INFO == lastModifiedDate "+ lastModifiedDate);

                for (FTPFile file : ftpFiles) {

                    Date fileDate = file.getTimestamp().getTime();
                    String formattedFileDate = df.format(fileDate);
                    Date today = new Date();
                    String formattedTodayDate = df.format(today);

                    System.out.println("=== amis-ftp === FTPTask: validateAndImportFile: == INFO == formattedFileDate "+ formattedFileDate);

                    Date lmd = null;

                      try{
                         lmd =   df.parse(lastModifiedDate);
                      } catch (ParseException ex){
                          System.out.println("=== amis-ftp === FTPTask: validateAndImportFile: == ERROR == cannot parse last modified date "+ lastModifiedDate);
                      }

                    if (formattedFileDate.equals(formattedTodayDate) ||  fileDate.after(lmd)) {

                        if(!formattedFileDate.equals(lastModifiedDate)){

                            jdbcConnector.openConnection();

                            //Retrieve the file from the FTP and place in download directory
                            retrieveFileFromFTP(formattedTodayDate, file);

                            // connect to DB and get metadata for the dataset in order validate the csv
                            Boolean isValidCSV = csvBasicValidator.validateCSV(getPath()+File.separator+formattedTodayDate+"_"+file.getName(), datasetCode);

                            if(isValidCSV){
                                System.out.println("=== amis-ftp === FTPTask: validateAndImportFile: == INFO == CSV is Validated ... proceed to import");

                                int expectedRows = csvBasicValidator.getLastLineNumber(getPath()+File.separator+formattedTodayDate+"_"+file.getName());

                                //set date format
                                amisDataImporter.setPostgresDateFormat();

                                int rowsInsertedIntoTemp = amisDataImporter.importDataFromCSVIntoTempTable(formattedTodayDate+"_"+file.getName(), expectedRows, tempTableName);

                                if(rowsInsertedIntoTemp == expectedRows){


                                    //import the data into the final data table
                                    int rowsInserted = amisDataImporter.importDataFromCSVIntoDataTable(formattedTodayDate+"_"+file.getName(), datasetCode, expectedRows);
                                    System.out.println("=== amis-ftp === FTPTask: validateAndImportFile: == INFO == Data Table: rowsInserted into "+datasetCode+" = "+rowsInserted+" (expected =  "+expectedRows+")");


                                    //reset date format
                                    amisDataImporter.resetPostgresDateFormat();


                                    int lastModifiedDateUpdatedRows = amisDataImporter.updateLastModifiedDate(datasetCode);
                                    System.out.println("=== amis-ftp === FTPTask: validateAndImportFile: == INFO == CustomDataset Table: lastModifiedDateUpdated Executed affected " +lastModifiedDateUpdatedRows + " rows");



                                    // Empty the download folder
                                    boolean isUploadedFileDeleted = emptyDownloadDirectory();
                                    System.out.println("=== amis-ftp === FTPTask: validateAndImportFile: == INFO == Is the uploaded file deleted from the 'download' directory: "+isUploadedFileDeleted);



                                    //Send Email once successfully updated!
                                    sendOnSuccessEmail(datasetCode);

                                } else {

                                    //reset date format
                                    amisDataImporter.resetPostgresDateFormat();

                                    System.out.println("=== amis-ftp === FTPTask: validateAndImportFile: == ERROR == The number of rows in the CSV ("+expectedRows+") do not match how many rows where inserted into the igc_daily_indicators_temp (temporary) database table ("+rowsInsertedIntoTemp+ ")... so the import into the actual database table DID NOT happen ");
                                    String message = "FTPTask: validateAndImportFile(): The number of rows in the CSV ("+expectedRows+") do not match how many rows where inserted into the igc_daily_indicators_temp (temporary) database table ("+rowsInsertedIntoTemp+")... so the import into the actual database table DID NOT happen ";
                                    emailSender.sendEMailToDevelopers(emailErrorSubject, message);
                                    //emailSender.sendEMailToContacts(emailErrorSubject, message, false);
                                 }
                            }

                            csvBasicValidator.closeCSVParser();

                            jdbcConnector.closeConnection();

                        }else{
                            System.out.println("=== amis-ftp === FTPTask: validateAndImportFile: == WARNING == The dataset was last modified on "+lastModifiedDate+", which matches the date of the FTP file (i.e."+formattedFileDate+") - so there is no need to re-upload");
                        }

                    }
                    else {
                        System.out.println("=== amis-ftp === FTPTask: validateAndImportFile: == WARNING == No new files in the FTP for "+formattedTodayDate);
                    }
         }
    }

    private boolean emptyDownloadDirectory() {
     File downloadFolder = new File(getPath());
        String[] uploadedFiles;
        if(downloadFolder.isDirectory()){
            uploadedFiles = downloadFolder.list();
            for (int i=0; i<uploadedFiles.length; i++) {
                File myFile = new File(downloadFolder, uploadedFiles[i]);
                return myFile.delete();
            }
        }
        return false;
    }

    private void sendOnSuccessEmail(String datasetCode){
        String lastUpdatedDate = amisDataImporter.getLastModifiedDateFormatted(datasetCode);
        String message = "The AMIS database was successfully updated on "+lastUpdatedDate+" with the IGC GOI and sub-Indices data.";
        emailSender.sendEMailToFAOContacts(emailErrorSubject, message);
    }

    private void retrieveFileFromFTP(String formattedTodayDate, FTPFile ftpFile) throws FenixSystemException {
        try{
            OutputStream output = new FileOutputStream(getPath()+File.separator+formattedTodayDate+"_"+ftpFile.getName());
            //get the file from the remote system
            ftpClient.retrieveFile(ftpFile.getName(), output);

            System.out.println("=== amis-ftp === FTPTask: retrieveFileFromFTP: == INFO == File Name "+ftpFile.getName());

            System.out.println("=== amis-ftp === FTPTask: retrieveFileFromFTP: == INFO == File downloaded  "+getPath()+File.separator+formattedTodayDate+"_"+ftpFile.getName());

            output.close();

        } catch(FileNotFoundException fnfe){
            System.out.println("=== amis-ftp === FTPTask: retrieveFileFromFTP: == ERROR == Could not create file output stream ("+getPath()+File.separator+formattedTodayDate+"_"+ftpFile.getName()+") = FileNotFoundException.");
            fnfe.printStackTrace();
            throw new FenixSystemException("FTPTask: retrieveFileFromFTP(): Could not create file output stream ("+getPath()+File.separator+formattedTodayDate+"_"+ftpFile.getName()+") = FileNotFoundException. --- "+fnfe.getMessage());

        }  catch(IOException ioe){
            System.out.println("=== amis-ftp === FTPTask: retrieveFileFromFTP: == ERROR == Could not retrieve FTP File and place in file output stream ("+getPath()+File.separator+formattedTodayDate+"_"+ftpFile.getName()+")  = IOException.");
            ioe.printStackTrace();
            throw new FenixSystemException("=== amis-ftp === FTPTask: Could not retrieve FTP File and place in file output stream ("+getPath()+File.separator+formattedTodayDate+"_"+ftpFile.getName()+")  = IOException. --- "+ioe.getMessage());
        }

    }
    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setAmisDataImporter(AMISDataImporter amisDataImporter) {
        this.amisDataImporter = amisDataImporter;
    }

    public void setCsvBasicValidator(CSVBasicValidator csvBasicValidator) {
        this.csvBasicValidator = csvBasicValidator;
    }

    public void setRemoteDirectory(String remoteDirectory) {
        this.remoteDirectory = remoteDirectory;
    }

    public void setJdbcConnector(JDBCConnector jdbcConnector) {
        this.jdbcConnector = jdbcConnector;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    public void setEmailSender(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

}