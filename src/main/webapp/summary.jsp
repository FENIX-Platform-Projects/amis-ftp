<%@ page import="java.util.Date" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<%
        Date schedulerRunningSince = (Date)request.getAttribute("scheduler-running-since");
        Boolean schedulerState = (Boolean)request.getAttribute("scheduler-state");
        String datasetLastUpdated = (String)request.getAttribute("dataset-last-updated");
        String jobName = (String)request.getAttribute("job-name");
        Date triggerPreviousFireTime = (Date)request.getAttribute("trigger-previous-fire-time");
        Date triggerNextFireTime = (Date)request.getAttribute("trigger-next-fire-time");


        String previousFireTime =  "";

        if(triggerPreviousFireTime==null){
            previousFireTime = "Not available";
        }  else {
            previousFireTime =   triggerPreviousFireTime.toString();
        }

%>
<head>
    <title>AMIS-IGC FTP Data Uploader</title>
    <link rel="stylesheet" type="text/css" href="css.css">
    <style>

        h2 {
            padding-top:20px;
            color: #333399;
        }

        h3 {
             color: #666699;
        }

        h2, h3, p strong, em {
            font-family: 'Skolar Bold',"Droid Serif","Cambria","Georgia","Times New Roman","Times",serif;
            font-style: normal;
            font-weight: 700;
        }
        body {
            font-family: "Proxima Nova Regular","Calibri","Droid Sans","Helvetica Neue","Helvetica","Arial",sans-serif;
            font-style: normal;
            font-weight: 400;
            padding-left: 20px;
        }

        p {
            padding: 0.5em 0;
        }

        a:link {
        }
        p a {
            padding-bottom: 1px;
        }
        a {
            color: #666699;
            text-decoration: none;
        }
        a {
            background: none repeat scroll 0 0 transparent;
            font-size: 100%;
            margin: 0;
            padding: 0;
            vertical-align: baseline;
        }

        #summary-table {
            border: 1px solid #6699CC;
            border-collapse: collapse;
            font-family: "Lucida Sans Unicode","Lucida Grande",Sans-Serif;
            font-size: 12px;
            margin: 20px;
            text-align: left;
            width: 700px;
        }
        #summary-table th {
            color: #003399;
            font-size: 14px;
            font-weight: normal;
            padding: 15px 10px 10px;
        }
        #summary-table tbody {
            background: none repeat scroll 0 0 #D0DAFD;
        }
        #summary-table td {
            border-top: 1px dashed #FFFFFF;
            color: #333399;
            padding: 10px;
        }
        #summary-table tbody tr:hover td {
            background: none repeat scroll 0 0 #D0DAFD;
            color: #333399;
        }


    </style>
</head>
<body>
<h2>AMIS-IGC FTP Data Uploader</h2>

<div style="width:800px">
    <p>
    Every 3 hours the AMIS FTP share directory is checked for a new CSV data file from IGC.
    If the CSV conforms to the right structure, the IGC GOI and Sub-Indices data is uploaded into the AMIS Statistics database.
    The latest data can be viewed from
    the <a href="http://www.amis-outlook.org/amis-monitoring/indicators/prices/en" target="_blank">AMIS Market Monitor Indicators page</a>.
   </p>
</div>

<h3>FTP Checking Schedule:</h3>
<strong>Last upload into AMIS Statistics Database:</strong> <em><%= datasetLastUpdated %></em>

<table id="summary-table">
    <thead>
    <tr>
        <th scope="col">Last Check</th>
        <th scope="col">Next Check</th>
    </tr>
    </thead>
    <tfoot>
    <tr>
        <td colspan="2">&nbsp;</td>
    </tr>
    </tfoot>
    <tbody>
    <tr>
        <td> <%= previousFireTime %><td> <%= triggerNextFireTime %> </td></tr>
    </tbody>
</table>

   <h3>FTP Checking Process:</h3>

    <!--<em>Current Time:</em> <strong><%= new Date() %></strong><br/> -->
    <strong>Status:</strong> <em><%= schedulerState ? "Running" : "Not Running" %></em> <br/>
    <strong>Started on:</strong> <em><%= schedulerRunningSince %></em>


</body>

</html>