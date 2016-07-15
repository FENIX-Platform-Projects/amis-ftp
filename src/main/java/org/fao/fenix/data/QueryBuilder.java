package org.fao.fenix.data;

import java.util.Date;

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

public class QueryBuilder {


    public static String getDeleteTableContentsQuery(String tablename) {
        StringBuffer sb = new StringBuffer();
        sb.append("DELETE FROM "+tablename);
        return sb.toString();
    }


    public static String setDateFormat() {
        StringBuffer sb = new StringBuffer();
        sb.append("SET datestyle='ISO, DMY'");
        System.out.println("**** setDateFormat *** "+ sb.toString());

        return sb.toString();
    }

    public static String resetDateFormat() {
        StringBuffer sb = new StringBuffer();
        sb.append("SET datestyle=default");

        System.out.println("**** resetDateFormat *** "+ sb.toString());

        return sb.toString();
    }

    public static String getImportCSVQuery(String tablename, String pathToCsv) {
        StringBuffer sb = new StringBuffer();
        sb.append("COPY "+tablename+" ");
        sb.append("FROM  '"+pathToCsv + "' ");
        sb.append("WITH DELIMITER ',' ");
        sb.append("CSV HEADER ");

        System.out.println("**** getImportCSVQuery *** "+ sb.toString());

        return sb.toString();
    }

       public static String getTableNameQuery(String code){
        String sql = "SELECT tablename FROM customdataset WHERE code='"+code+"'";
        return sql;
    }


    public static String getLastModifiedDateFormattedQuery(String code){
        String sql = "SELECT to_char(datelastupdate::date, 'Dy Mon DD YYYY') FROM customdataset WHERE code='"+code+"'";

        return sql;
    }

    public static String getLastModifiedDateQuery(String code){
        String sql = "SELECT datelastupdate::date FROM customdataset WHERE code='"+code+"'";

        return sql;
    }


    public static String getUpdateDateLastUpdateQuery(String code){
        String sql = "UPDATE customdataset set datelastupdate=now()::timestamp WHERE code='"+code+"'";
        return sql;
    }

    public static String getDescriptorQuery(String code){
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT header, contentdescriptor FROM descriptor ");
        sb.append("WHERE  datasettype_id IN (");
        sb.append("SELECT datasettype_id FROM customdataset WHERE code='"+code + "'");
        sb.append(") ");
        return sb.toString();
    }

    public static String getCountQuery(String tableName){
        String sql = "SELECT COUNT(*) FROM "+tableName+"";
        return sql;
    }

}
