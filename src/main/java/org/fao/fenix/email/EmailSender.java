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
package org.fao.fenix.email;

import org.apache.log4j.Logger;
import org.fao.fenix.exception.FenixSystemException;
import org.fao.fenix.exception.FenixExceptionUtils;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class EmailSender {

    private String SMTP_HOST;

    private String USERNAME;

    private String PASSWORD;

    private String sendToIGCContacts;
    private String sendToDevelopers;
    private String sendToFAOContacts;

    private String ccToFAOContacts;


    private static final Logger LOGGER = Logger.getLogger(EmailSender.class);


    public void sendEMailToContacts(String header, String messageBody, boolean isSendToUsers) throws FenixSystemException {

        String [] sendTo = {};
        String [] ccTo = {};
        String [] ccToDevelopers = {};
        String [] ccToFAOUsers = {};
        String [] ccToOtherFAOUsers = {};

        if(isSendToUsers){
            if(getSendToIGCContacts()!=null && getSendToIGCContacts().length() > 0)
                sendTo = getSendToIGCContacts().split(",");

            if(getSendToDevelopers()!=null && getSendToDevelopers().length() > 0)
                ccToDevelopers = getSendToDevelopers().split(",");

            if(getCcToFAOContacts()!=null && getCcToFAOContacts().length() > 0)
                ccToFAOUsers = getCcToFAOContacts().split(",");

            if(getSendToFAOContacts()!=null && getSendToFAOContacts().length() > 0)
                ccToOtherFAOUsers = getSendToFAOContacts().split(",");

            ArrayList<String> temp = new ArrayList<String>();
            temp.addAll(Arrays.asList(ccToDevelopers));
            temp.addAll(Arrays.asList(ccToFAOUsers));
            temp.addAll(Arrays.asList(ccToOtherFAOUsers));

            ccTo = temp.toArray(new String[ccToDevelopers.length+ccToFAOUsers.length+ccToOtherFAOUsers.length]);

        }   else {
            sendTo = getSendToDevelopers().split(",");
        }

        sendEMail(header, messageBody, sendTo, ccTo, isSendToUsers);
    }

    public void sendEMailToFAOContacts(String header, String messageBody) throws FenixSystemException {
        String [] sendTo  = {};
        String [] ccTo = {};
        String [] ccToDevelopers = {};
        String [] ccToFAOUsers = {};

        if(getSendToFAOContacts()!=null && getSendToFAOContacts().length() > 0)
            sendTo = getSendToFAOContacts().split(",");

        if(getSendToFAOContacts()!=null && getSendToFAOContacts().length() > 0)
            sendTo = getSendToFAOContacts().split(",");

        if(getSendToDevelopers()!=null && getSendToDevelopers().length() > 0)
            ccToDevelopers = getSendToDevelopers().split(",");

        if(getCcToFAOContacts()!=null && getCcToFAOContacts().length() > 0)
            ccToFAOUsers = getCcToFAOContacts().split(",");

        ArrayList<String> temp = new ArrayList<String>();
        temp.addAll(Arrays.asList(ccToDevelopers));
        temp.addAll(Arrays.asList(ccToFAOUsers));


        ccTo = temp.toArray(new String[ccToDevelopers.length+ccToFAOUsers.length]);

        sendEMail(header, messageBody, sendTo, ccTo, false);
    }



    public void sendEMailToDevelopers(String header, String messageBody) throws FenixSystemException {

        String [] sendTo = {};
        String [] ccTo = {};

        sendTo = getSendToDevelopers().split(",");

        sendEMail(header, messageBody, sendTo, ccTo, false);
    }


    public void sendEMail(String header, String messageBody, String [] sendTo, String [] ccTo, boolean isSendToUsers) throws FenixSystemException {

        Properties props = buildProperties();
        try {

            Session session = Session.getDefaultInstance(props, null);
            MimeMessage message = new MimeMessage(session);

            InternetAddress[] addressTo = getRecipientsAddresses(sendTo);
            message.setRecipients(Message.RecipientType.TO, addressTo);

            if(ccTo.length > 0){
                InternetAddress[] addressCcTo =  getRecipientsAddresses(ccTo);
                message.setRecipients(Message.RecipientType.CC, addressCcTo);
            }

            message.setFrom(new InternetAddress(USERNAME, "AMIS-FTP Data Uploader"));
            message.setSubject(header);
            messageBody += buildFooter(isSendToUsers);
            message.setText(messageBody);

            Transport transport = session.getTransport("smtp");
            transport.connect(SMTP_HOST, USERNAME, PASSWORD);


            transport.sendMessage(message, message.getAllRecipients());
            transport.close();

        } catch (AddressException e) {
            //e.printStackTrace();
            System.out.println("=== amis-ftp === EmailSender: sendEMail: == ERROR == AddressException");
            System.out.println("=== amis-ftp === EmailSender: sendEMail: == ERROR == AddressException STACK TRACE  " + FenixExceptionUtils.exceptionStackTraceToString(e));

            String  message = "\n\n ==== Stack Trace ==== \n\n " +FenixExceptionUtils.exceptionStackTraceToString(e);

            throw new FenixSystemException("=== amis-ftp === EmailSender: sendEMail: == ERROR == AddressException "+ message);

        } catch (NoSuchProviderException e) {
            //e.printStackTrace();
            System.out.println("=== amis-ftp === EmailSender: sendEMail: == ERROR == NoSuchProviderException");
            System.out.println("=== amis-ftp === EmailSender: sendEMail: == ERROR == NoSuchProviderException STACK TRACE  " + FenixExceptionUtils.exceptionStackTraceToString(e));

            String  message = "\n\n ==== Stack Trace ==== \n\n " +FenixExceptionUtils.exceptionStackTraceToString(e);

            throw new FenixSystemException("=== amis-ftp === EmailSender: sendEMail: == ERROR == NoSuchProviderException "+ message);
        } catch (MessagingException e) {
            //e.printStackTrace();
            System.out.println("=== amis-ftp === EmailSender: sendEMail: == ERROR == NoSuchProviderException");
            System.out.println("=== amis-ftp === EmailSender: sendEMail: == ERROR == NoSuchProviderException STACK TRACE  " + FenixExceptionUtils.exceptionStackTraceToString(e));

            String  message = "\n\n ==== Stack Trace ==== \n\n " +FenixExceptionUtils.exceptionStackTraceToString(e);

            throw new FenixSystemException("=== amis-ftp === EmailSender: sendEMail: == ERROR == NoSuchProviderException "+ message);
    } catch (UnsupportedEncodingException e) {
            //e.printStackTrace();
            System.out.println("=== amis-ftp === EmailSender: sendEMail: == ERROR == UnsupportedEncodingException");
            System.out.println("=== amis-ftp === EmailSender: sendEMail: == ERROR == UnsupportedEncodingException STACK TRACE  " + FenixExceptionUtils.exceptionStackTraceToString(e));

            String  message = "\n\n ==== Stack Trace ==== \n\n " +FenixExceptionUtils.exceptionStackTraceToString(e);

            throw new FenixSystemException("=== amis-ftp === EmailSender: sendEMail: == ERROR == UnsupportedEncodingException "+ message);
    }
    }

    private InternetAddress[] getRecipientsAddresses(String [] toList) throws FenixSystemException {
        InternetAddress[] addressTo = new InternetAddress[toList.length];

        try{
        for (int i = 0; i < toList.length; i++)
        {
            addressTo[i] = new InternetAddress(toList[i]);
        }
        }
        catch (AddressException e) {
            throw new FenixSystemException(e.getMessage());
        }

        return addressTo;
    }

    private String buildFooter(boolean isSendToUsers) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n\n");
        sb.append("============================================================================================================\n");
        sb.append("This e-mail has been automatically generated by the AMIS Statistics FTP Data Uploader, please do not reply. \n");

        if(isSendToUsers) {
            sb.append("If there are questions, please email one of the AMIS contacts who are copied. \n");
        }

        sb.append("AMIS-IGC FTP Address: ftp://ext-ftp.fao.org/ES/Reserved/AMIS/IGC/indices \n");
        sb.append("=============================================================================================================\n");

        return sb.toString();
    }

    private Properties buildProperties() {
        Properties props = System.getProperties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.user", USERNAME);
        props.put("mail.smtp.password", PASSWORD);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        return props;
    }

    public void setSMTP_HOST(String sMTPHOST) {
        SMTP_HOST = sMTPHOST;
    }

    public void setUSERNAME(String uSERNAME) {
        USERNAME = uSERNAME;
    }

    public void setPASSWORD(String pASSWORD) {
        PASSWORD = pASSWORD;
    }

    public String getSendToIGCContacts() {
        return sendToIGCContacts;
    }

    public void setSendToIGCContacts(String sendToIGCContacts) {
        this.sendToIGCContacts = sendToIGCContacts;
    }

    public String getSendToDevelopers() {
        return sendToDevelopers;
    }

    public void setSendToDevelopers(String sendToDevelopers) {
        this.sendToDevelopers = sendToDevelopers;
    }

    public String getSendToFAOContacts() {
        return sendToFAOContacts;
    }

    public void setSendToFAOContacts(String sendToFAOContacts) {
        this.sendToFAOContacts = sendToFAOContacts;
    }

    public String getCcToFAOContacts() {
        return ccToFAOContacts;
    }

    public void setCcToFAOContacts(String ccToFAOContacts) {
        this.ccToFAOContacts = ccToFAOContacts;
    }

}