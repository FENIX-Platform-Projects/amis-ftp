package org.fao.fenix.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.fao.fenix.test.BaseDaoTest;

import java.io.IOException;

public class FTPTaskTest extends BaseDaoTest {

    FTPTask ftpTask;

	public void testConfiguration() {
        assertNotNull(ftpTask);
        ftpTask.setFtpClient(new FTPClient());
    }
	
	
	public void _testingFTPConnection() throws IOException {
        ftpTask.setFtpClient(new FTPClient());
        if(ftpTask.ftpConnect()){
            System.out.println("testingFTPConnection: Connection is TRUE ");
            assertTrue(true);
        }   else {
            ftpTask.getFtpClient().disconnect();
            fail("Could not connect to FTP");
        }

        ftpTask.getFtpClient().logout();
        ftpTask.getFtpClient().disconnect();

    }



    public void setFtpTask(FTPTask ftpTask) {
        this.ftpTask = ftpTask;
    }
}
