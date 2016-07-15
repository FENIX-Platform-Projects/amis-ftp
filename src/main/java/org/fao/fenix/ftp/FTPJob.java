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

import org.fao.fenix.ftp.FTPTask;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;



//Define the method you want to schedule in executeInternal() method, and pass the scheduler task (FTPTask) via setter method
public class FTPJob extends QuartzJobBean {

    private org.fao.fenix.ftp.FTPTask ftpTask;

    public void setFtpTask(FTPTask ftpTask) {
        this.ftpTask = ftpTask;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("FTPJob calling ftpTask "+ftpTask);
        ftpTask.connectToFTP("indices");
    }
}