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

package org.fao.fenix.ftp;

import org.fao.fenix.data.AMISDataImporter;
import org.fao.fenix.data.JDBCConnector;
import org.quartz.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.*;
import java.util.Date;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

public class FTPNotificationServlet extends HttpServlet {
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException  {

        ServletContext context = getServletContext();
        WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);

        //try{
        Scheduler scheduler = (Scheduler)applicationContext.getBean("quartzScheduler");

        JDBCConnector jdbcConnector = (JDBCConnector)applicationContext.getBean("jdbcConnector");
        AMISDataImporter dataImporter = (AMISDataImporter)applicationContext.getBean("amisDataImporter");

       try{

        List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();
          // System.out.println("Summary:: "+ scheduler.getMetaData().getSummary());
           System.out.println("======================FTPNotificationServlet =================================");

           System.out.println("Number of Jobs Executed "+ scheduler.getMetaData().getNumberOfJobsExecuted());
           System.out.println("Running Since  "+ scheduler.getMetaData().getRunningSince());
           System.out.println("Standby Mode  "+scheduler.getMetaData().isInStandbyMode());
           System.out.println("Scheduler is started  "+scheduler.isStarted());

           System.out.println("==============================================================================");

           String lastModifiedDate = "";
           jdbcConnector.openConnection();

           lastModifiedDate =  dataImporter.getLastModifiedDateFormatted("AMIS_IGC_DAILY_INDICATORS");

           System.out.println("Last Modified Date  "+lastModifiedDate);
           jdbcConnector.closeConnection();


           System.out.println("Executing Jobs = "+executingJobs.size());

           Date nextFireTime = null;
           Date previousFireTime = null;
           String triggerState = "";
           String jobNameSt = "";
           //loop all group
           for (String groupName : scheduler.getJobGroupNames()) {

               //loop all jobs by groupname
               for (String jobName : scheduler.getJobNames(groupName)) {

                   JobDetail jobDetail = scheduler.getJobDetail(jobName, groupName);

                   //get job's trigger
                   Trigger[] triggers = scheduler.getTriggersOfJob(jobName,groupName);
                   nextFireTime = triggers[0].getNextFireTime();
                   previousFireTime = triggers[0].getPreviousFireTime();
                   jobNameSt =   jobName;
                   System.out.println("[jobName] : " + jobName + " [groupName] : "
                           + groupName + " - nextFireTime " + nextFireTime);

                   System.out.println("[triggerName] : " + triggers[0].getName() + " [previousFire] : "
                           + previousFireTime + " trigger state "+ scheduler.getTriggerState(jobName, groupName));

                    /**  if(scheduler.getTriggerState(jobName, groupName) == Trigger.STATE_PAUSED){
                          triggerState = "PAUSED";
                      } else if(){
                          triggerState = "PAUSED";
                      }   else if(){
                          triggerState = "PAUSED";
                      }           **/

               }

           }


           request.setAttribute("scheduler-running-since", scheduler.getMetaData().getRunningSince());
           request.setAttribute("scheduler-state", scheduler.isStarted());
           request.setAttribute("dataset-last-updated", lastModifiedDate);
           request.setAttribute("job-name", jobNameSt);
           request.setAttribute("trigger-previous-fire-time", previousFireTime);
           request.setAttribute("trigger-next-fire-time", nextFireTime);


       }
       catch(SchedulerException e){
           e.printStackTrace();
       }

        getServletContext().getRequestDispatcher("/summary.jsp").forward(request, response);

    }
}
