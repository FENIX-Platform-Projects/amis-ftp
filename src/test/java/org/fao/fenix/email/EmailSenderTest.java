package org.fao.fenix.email;

import java.util.ArrayList;

import junit.framework.TestCase;
import org.fao.fenix.test.BaseDaoTest;

public class EmailSenderTest extends BaseDaoTest {

    private EmailSender emailSender;

    public void testConfiguration() {
        assertNotNull(emailSender);
    }




	
	public void _testEmailUsers() {
	//	es = new EmailSender();
	//	es.setPASSWORD("Liverpool21");
	//	es.setUSERNAME("FAODOMAIN/Mohammad");
	//	es.setSMTP_HOST("faohqmail.fao.org");
        emailSender.sendEMailToContacts("Test", "This is a users test", true);
	}


    public void _testEmailDevelopers() {
       // EmailSender = new EmailSender();
        //	es = new EmailSender();
        //	es.setPASSWORD("Liverpool21");
        //	es.setUSERNAME("FAODOMAIN/Mohammad");
        //	es.setSMTP_HOST("faohqmail.fao.org");
        emailSender.sendEMailToContacts("Test Developers", "This is a developers test", false);
    }


    public void setEmailSender(EmailSender emailSender) {
        this.emailSender = emailSender;
    }
	
}