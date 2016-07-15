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

import com.Ostermiller.util.CSVParser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.fao.fenix.exception.FenixSystemException;

import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

import java.util.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.lang.IllegalArgumentException;

public class CSVBasicValidator {


    private static final Logger LOGGER = Logger.getLogger(CSVBasicValidator.class);

    private CSVParser csvParser;

    private JDBCConnector jdbcConnector;

    public CSVParser getCsvParser() {
        return csvParser;
    }


    public void closeCSVParser() {
       if(getCsvParser()!=null)    {
           try{
               getCsvParser().close();
           }  catch (IOException ioe){
               LOGGER.error("Could not close the CSV Parser.");
               ioe.printStackTrace();
               throw new FenixSystemException("CSVBasicValidator: closeCSVParser(): Could not close the CSV Parser = IOException --- "+ioe.getMessage());
           }
       }

    }

    public void setCsvParser(CSVParser csvParser) {
        this.csvParser = csvParser;
    }



    public Boolean validateCSV(String filePath, String code) throws FenixSystemException {

        StringBuilder errorStringBuilder = new StringBuilder();

        try{
            csvParser = new CSVParser(
                    new InputStreamReader(
                    new FileInputStream(filePath), "UTF8"));

            setCsvParser(csvParser);

            String sql = QueryBuilder.getDescriptorQuery(code);
            List<List<String>> results = jdbcConnector.executeQuerySQL(sql);

            //header, data type
            LinkedHashMap<String, String> descriptors = new LinkedHashMap<String, String>();
            LinkedList<String> expectedHeaders = new LinkedList<String>();

            for(List<String> row: results){
                descriptors.put(row.get(0), row.get(1));
                expectedHeaders.add(row.get(0));
            }

            List<String> csvHeaders = getCSVHeaderLine(expectedHeaders);

            boolean headersIsValidated = validateCSVHeaders(expectedHeaders, csvHeaders,errorStringBuilder);

            List<String> sortedHeaders = getSortedHeaders(expectedHeaders, csvHeaders);

            boolean dateRowIsValidated = validateDateFormatOfFirstRow(sortedHeaders, descriptors, errorStringBuilder);

            if(headersIsValidated && dateRowIsValidated){
               return true;
            }
            else {
                throw new FenixSystemException(errorStringBuilder.toString());
            }

            /** if(validateCSVHeaders(expectedHeaders, csvHeaders,errorStringBuilder)){
                 LOGGER.info("CSV and Expected Headers Match!");

               List<String> sortedHeaders = getSortedHeaders(expectedHeaders, csvHeaders);

                 System.out.println("Validation 1: "+errorStringBuilder.toString());

                if(validateDateFormatOfFirstRow(sortedHeaders, descriptors, errorStringBuilder)){
                    LOGGER.info("CSV Date Format is Validated!");
                    return true;
                }

                 LOGGER.info("Validation 2: "+errorStringBuilder.toString());

                 if(errorStringBuilder.toString().length() > 0){
                     throw new FenixException(errorStringBuilder.toString());
                 }
             }    **/

        }
        catch (FileNotFoundException nfe){
            LOGGER.error("File ("+filePath+") not found. Could not create the CSVParser = FileNotFoundException.");
            nfe.printStackTrace();
            throw new FenixSystemException("CSVBasicValidator: validateCSV(): File ("+filePath+") not found. Could not create the CSVParser = FileNotFoundException. --- "+nfe.getMessage());
          }
        catch (UnsupportedEncodingException uee){
             LOGGER.error("UTF8 Encoding is not supported. Could not create the CSVParser = UnsupportedEncodingException.");
             uee.printStackTrace();
            throw new FenixSystemException("CSVBasicValidator: validateCSV(): UTF8 Encoding is not supported. Could not create the CSVParser = UnsupportedEncodingException. --- "+uee.getMessage());
        }
        catch (IOException ioe){
             LOGGER.error("Could not get the first line of the CSV file (i.e. the headers) = IOException.");
             ioe.printStackTrace();
             throw new FenixSystemException("CSVBasicValidator: validateCSV(): getCSVHeaderLine: Could not get the first line of the CSV file (i.e. the headers) = IOException. --- "+ioe.getMessage());
         }



    }

    public int getLastLineNumber(String filePath) {
        int rows = 0;
        try {
            CSVParser newCsvParser = new CSVParser(
                    new InputStreamReader(
                            new FileInputStream(filePath), "UTF8"));
            rows = newCsvParser.getAllValues().length - 1;
            newCsvParser.close();
        } catch (FileNotFoundException fnfe) {
            LOGGER.error("Could not Create CSVParser, the file (" + filePath + ") could not be found = FileNotFoundException.");
            fnfe.printStackTrace();
            throw new FenixSystemException("CSVBasicValidator: getLastLineNumber(): Could not Create CSVParser, the file (" + filePath + ") could not be found = FileNotFoundException. --- "+fnfe.getMessage());
        } catch (IOException ioe) {
            LOGGER.error("Could not getAllValues from the CSV. Or could not close the CSV Parser = IOException.");
            ioe.printStackTrace();
            throw new FenixSystemException("CSVBasicValidator: getLastLineNumber(): Could not getAllValues from the CSV. Or could not close the CSV Parser = IOException. --- "+ioe.getMessage());
        }

        return rows;
    }

    private Boolean validateCSVHeaders(List<String> expectedHeaders , List<String> csvHeaders, StringBuilder errorStringBuilder) {

        Boolean isValidated = false;

        String expectedHeadersSt = getHeaderString(expectedHeaders);

        String inputHeadersSt = getHeaderString(csvHeaders);


            boolean headersIsEqual = CollectionUtils.isEqualCollection(expectedHeaders, csvHeaders);

            if(headersIsEqual){
                isValidated = true;
            }  else {
                LOGGER.error("The headers in the FTP CSV file do not match what was expected. The CSV contains "+csvHeaders.size()+" headers (i.e. "+inputHeadersSt+"). However, the following "+expectedHeaders.size()+" headers were expected: "+expectedHeadersSt+". Please correct the CSV file. [THE ERROR EMAIL WILL BE SENT TO USER]");
                errorStringBuilder.append("** The headers in the FTP CSV file do not match what was expected. \nThe CSV contains "+csvHeaders.size()+" headers (i.e. "+inputHeadersSt+"). However, the following "+expectedHeaders.size()+" headers were expected: "+expectedHeadersSt+". Please correct the CSV file. \n\n");
                // throw new FenixException("The headers in the FTP CSV file do not match what was expected. The CSV contains "+csvHeaders.size()+" headers (i.e. "+sbh.toString()+"). However, the following "+expectedHeaders.size()+" headers were expected: "+sbd.toString()+". Please correct the CSV file.");
            }

        System.out.println(" validateCSVHeaders "+errorStringBuilder.toString());
           return isValidated;

    }

    private String getHeaderString(List<String> headerList){
        StringBuffer sbh = new StringBuffer("");

        int i = 0;

        for (String inputHeader : headerList) {
            sbh.append(inputHeader);

            if(i < headerList.size()-1)
                sbh.append(", ");

            i++;
        }

        LOGGER.info(sbh);

        return sbh.toString();
    }
    private List<String> getSortedHeaders(List<String> expectedHeaders, List<String> csvHeaders){

        List<String> sorted = new ArrayList<String>();

        for (String inputHeader : csvHeaders) {
            for (String descriptor : expectedHeaders) {
                if (descriptor.equalsIgnoreCase(inputHeader)) {
                    if (!sorted.contains(descriptor)) {
                        sorted.add(descriptor);
                    }
                }
            }
        }

        return sorted;
    }


    private List<String> getCSVHeaderLine(List<String> allDescriptors) throws IOException {
        List<String> inputHeaders = new ArrayList<String>();
            String[] line = getCsvParser().getLine();
            int i = 0;
            while (i < line.length && i < allDescriptors.size()) {
                line[i] = clean(line[i]);
                inputHeaders.add(line[i].trim());
                i++;
            }
        return inputHeaders;
    }

   /* private List<String> validateCSVHeaders(List<String> allDescriptors) throws FenixException {
        List<String> inputHeaders = new ArrayList<String>();
        try {
            String[] line = getCsvParser().getLine();
            int i = 0;
            while (i < line.length && i < allDescriptors.size()) {
                line[i] = clean(line[i]);
                inputHeaders.add(line[i].trim());
                i++;
            }

        } catch (IOException e) {
            LOGGER.error("Could not get the line of the CSV file: ");
            e.printStackTrace();
        }

        StringBuffer sbd = new StringBuffer("Expected headers: ");
        for (String descriptor : allDescriptors) {
            sbd.append(" >").append(descriptor).append("<");
        }
        LOGGER.info(sbd);
        StringBuffer sbh = new StringBuffer("Found: ");
        for (String inputHeader : inputHeaders) {
            sbh.append(" >").append(inputHeader).append("<");
        }
        LOGGER.info(sbh);

        List<String> sorted = new ArrayList<String>();
        for (String inputHeader : inputHeaders) {
            for (String descriptor : allDescriptors) {
                if (descriptor.equalsIgnoreCase(inputHeader)) {
                    if (!sorted.contains(descriptor)) {
                        sorted.add(descriptor);
                    }
                }
            }
        }

        if (sorted.size() < inputHeaders.size()) {
            throw new FenixException("The number of columns in the CSV file ("+sorted.size()+") does not match what is expected (" + sorted.size()+"). "+sbh.toString()+". Please check your .CSV file.");
        } else {
            LOGGER.info("Headers are Validated!");
        }


        return sorted;
    }*/

    public Boolean validateDateFormatOfFirstRow(List<String> sortedHeaders, LinkedHashMap<String, String> descriptorsMap, StringBuilder errorStringBuilder)
            throws FenixSystemException {
       try {
            String[] line = null;

            // testing the first data row
            line = getCsvParser().getLine();

            if (line != null) {
                int i = 0;

                while (i < line.length && i < sortedHeaders.size()) {

                    String token = clean(line[i]);
                    token = token.trim();
                    String descriptor = sortedHeaders.get(i);

                    FenixDataType dataType = FenixDataType.valueOf(descriptorsMap.get(descriptor));

                    if (dataType != null && dataType.equals(FenixDataType.date)) {
                        // check the format
                        if (isValidDate(token, errorStringBuilder)) {
                            return true;
                        }
                        break;
                    }
                    i++;
                }
            }
        } catch (IOException e) {
           //Send Email to Developer
           LOGGER.error("Could not get the first line of the CSV file = IOException");
           e.printStackTrace();
           throw new FenixSystemException("CSVBasicValidator: validateDateFormatOfFirstRow(): Could not get the first line of the CSV file = IOException --- "+e.getMessage());
        }

        return false;
    }

    private boolean isValidDate(String dateString, StringBuilder errorStringBuilder)  {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String errorMessage = "";

        //   LOGGER.info("isValidDate dateString = "+dateString);

        if (dateString != null && !dateString.isEmpty() && dateString.length() == 10) {
            try {
                df.setLenient(false);
                df.parse(dateString);
                return true;
            } catch (ParseException e) {
                LOGGER.error(getErrorMessage(dateString) + " [THE ParseException ERROR EMAIL HAS BEEN SENT TO USER]");
                errorMessage =  getErrorMessage(dateString);
                //throw new FenixException("The FTP CSV file contains an incorrect date format. Found '" + dateString + "' but expected a dd/mm/yyyy format e.g. '21/12/2000'. Please correct the CSV file.");
            } catch (IllegalArgumentException e) {
                LOGGER.error(getErrorMessage(dateString) +" [THE IllegalArgumentException ERROR EMAIL HAS BEEN SENT TO USER]");
                errorMessage =  getErrorMessage(dateString);
                //throw new FenixException("The FTP CSV file contains an incorrect date. Found '" + dateString + "' but expected a dd/mm/yyyy format e.g. '21/12/2000'. Please correct the CSV file.");
             }

        } else {
            LOGGER.error(getErrorMessage(dateString) +" [THE ERROR EMAIL HAS BEEN SENT TO USER]");
            errorMessage =  getErrorMessage(dateString);
            //throw new FenixException("The FTP CSV file contains an incorrect date. Found '" + dateString + "' but expected a dd/mm/yyyy format e.g. '21/12/2000'. Please correct the CSV file.");
        }

        if(errorMessage.length() > 0)   {
          errorStringBuilder.append(errorMessage);
        }

        return false;
    }

    private String clean(String line) {
        line = line.replace("'", " ");
        line = line.replace("\"", " ");
        line = line.replaceAll("\\<.*?\\>", "");
        return line;
    }


    private String getErrorMessage(String dateString){
        return "** The FTP CSV file contains an incorrect date format. Found '" + dateString + "' but expected a dd/mm/yyyy format e.g. '21/12/2000'. Please correct the CSV file.";
    }

    public void setJdbcConnector(JDBCConnector jdbcConnector) {
        this.jdbcConnector = jdbcConnector;
    }

}
