/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htmlserver_012;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
//import java.util.Base64;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * @code {
 *    // создаем  запись  raw2.put("имя поля", "хзначение");
 * Hashtable< String, Object> raw2 = new Hashtable< String, Object>(10, (float) 0.5);
 * raw2.put("Pole1", 1);
 * raw2.put("a_2", 22.22);
 * raw2.put("Pole_3", (float)33.999933);
 * raw2.put("Pole_4", "RTe1");
 * raw2.put("Pole_5", "RTEXTY2");
 * raw2.put("Pole_6", "22222");
 * Date dat = new Date(44444444);
 * raw2.put("Pole_7",  dat);
 * Time tim = new Time(44444);
 * raw2.put("Pole_8",  tim);
 *
 * SQLiteProxy sqlite = new SQLiteProxy();
 * sqlite.createTab("D:\\testdb.db", "tab2", raw2, true);
 * sqlite.addRaw("D:\\testdb.db", "tab2", raw2);
 * Hashtable< String, Object> raw3 = sqlite.getRaw("D:\\testdb.db", "tab2", 1);
 * Hashtable< Object, Hashtable< String, Object>> raw4 = sqlite.getRawList("D:\\testdb.db", "tab2", "");
 * System.out.println("Table created successfully");
 * }
 * @author myasnikov
 */
public class SQLiteProxy {

    private Connection con = null;

    /**
     * процедура подключения к SQLite Базе данных
     *
     * @param DataBaseFile
     * @return
     */
    private Connection ConnecrDb(String DataBaseFile) {
        try {
            //String dir = System.getProperty("user.dir");
            Class.forName("org.sqlite.JDBC");
            Connection con = DriverManager.getConnection("jdbc:sqlite:" + DataBaseFile);
            //         Connection con = DriverManager.getConnection("jdbc:sqlite:D:\\testdb.db");
            return con;
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(null, "Problem with connection of database");
            return null;
        }
    }

    //  private Hashtable<String, String> Tab = new Hashtable<String, String>(10, (float) 0.5);
    /**
     * создать таблицу в БД
     *
     * @param DataBaseFile - имя файла
     * @param TabName - имя таблицы
     * @param Tab - поля в таблицу
     * @param dropOldTab - флаг для удаления старой таблицы
     */
    public void createTab(String DataBaseFile, String TabName, Hashtable<String, Object> Tab, boolean dropOldTab) {
        // SELECT count(*) FROM sqlite_master WHERE type='table' AND name='table_name';
        try {
            con = ConnecrDb(DataBaseFile);
            Statement stmt = null;
            stmt = con.createStatement();
            if (dropOldTab == true) {
                // удаление таблицы
                stmt.executeUpdate("drop table if exists " + TabName);
            }
            stmt.executeUpdate(createSqlTab(TabName, Tab));
            stmt.close();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(SQLiteProxy.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * создать строковое представление SQL запроса для создания таблицы
     *
     * @param TabName -имя таблицы
     * @param Tab - список полей
     * @return
     */
    private String createSqlTab(String TabName, Hashtable<String, Object> Tab) {
        final StringBuffer sb = new StringBuffer();
        int numPole = 0;
        sb.append("create table if not exists " + TabName + " (");
        sb.append(" id integer primary key autoincrement,");
        for (Enumeration Eraw = Tab.keys(); Eraw.hasMoreElements();) {
            numPole++;
            Object key = Eraw.nextElement();
            String typ = Tab.get(key).getClass().getSimpleName();
            if (typ.equals("Byte[]")) {
                typ = "Blob";
            }
            if (numPole == 1) {
                sb.append(" " + key + "  " + typ + " ");
            } else {
                sb.append(" , " + key + "  " + typ + " ");
            }
        }
        sb.append(" );");
        //  System.out.println("SQL \r\n" + sb.toString());
        return sb.toString();
    }

    private Hashtable<String, Object> getHeshFromSqlRes(java.sql.ResultSet rs2, Hashtable<Integer, Vector> poleTyp) {
        Hashtable<String, Object> rawRes = new Hashtable<String, Object>(10, (float) 0.5);
        try {
            ResultSetMetaData rsmd = rs2.getMetaData();
            int colnum = rsmd.getColumnCount();
            while (rs2.next()) {
                for (int i = 1; i <= colnum; i++) {
                    if (poleTyp.get(i) != null) {
                        if (poleTyp.get(i).get(1).equals("STRING")) {
                            rawRes.put((String) poleTyp.get(i).get(0), rs2.getString(i));
                        }
                        if (poleTyp.get(i).get(1).equals("DOUBLE")) {
                            rawRes.put((String) poleTyp.get(i).get(0), rs2.getDouble(i));
                        }
                        if (poleTyp.get(i).get(1).equals("INTEGER")) {
                            rawRes.put((String) poleTyp.get(i).get(0), rs2.getInt(i));
                        }
                        if (poleTyp.get(i).get(1).equals("FLOAT")) {
                            rawRes.put((String) poleTyp.get(i).get(0), rs2.getInt(i));
                        }
                        if (poleTyp.get(i).get(1).equals("DATE")) {
                            rawRes.put((String) poleTyp.get(i).get(0), rs2.getDate(i));
                        }
                        if (poleTyp.get(i).get(1).equals("TIME")) {
                            rawRes.put((String) poleTyp.get(i).get(0), rs2.getDate(i));
                        }
                        if (poleTyp.get(i).get(1).equals("Byte[]")) {
                            rawRes.put((String) poleTyp.get(i).get(0), rs2.getByte(i));
                        }

                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLiteProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rawRes;
    }

    /**
     * Метод для получения записи по ID
     *
     * @param DataBaseFile - "D:\\testdb.db"
     * @param TabName -"tab2"
     * @param id id
     * @return Hashtable<String, String> rawRes = new
     * Hashtable<String, String>(10, (float) 0.5);
     */
    public Hashtable<String, Object> getRaw(String DataBaseFile, String TabName, int id) {
        Hashtable<String, Object> rawRes = new Hashtable<String, Object>(10, (float) 0.5);
        try {
            con = ConnecrDb(DataBaseFile);
            Statement stmt = null;
            stmt = con.createStatement();
            // получаем список полей
            //получить список полей (0) - Имя поля (1)- тип поля
            Hashtable<Integer, Vector> poleTyp = getColumName(DataBaseFile, TabName);
            // делаем запрос
            String sqlPole = "select  * from " + TabName + " where id=" + id;
            java.sql.ResultSet rs2 = stmt.executeQuery(sqlPole);
            rawRes = getHeshFromSqlRes(rs2, poleTyp);

        } catch (SQLException ex) {
            Logger.getLogger(SQLiteProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rawRes;
    }

    public Hashtable<String, Object> getRawFirst(String DataBaseFile, String TabName, boolean onDelete) {
        Hashtable<String, Object> rawRes = new Hashtable<String, Object>(10, (float) 0.5);
        try {
            con = ConnecrDb(DataBaseFile);
            Statement stmt = con.createStatement();
            // получаем список полей
            //получить список полей (0) - Имя поля (1)- тип поля
            Hashtable<Integer, Vector> poleTyp = getColumName(DataBaseFile, TabName);
            // делаем запрос
            String sqlPole = "select  * from " + TabName + " LIMIT 1";
            java.sql.ResultSet rs2 = stmt.executeQuery(sqlPole);
            rawRes = getHeshFromSqlRes(rs2, poleTyp);
            if (onDelete == true) {
                // создаем таблицу если она отсутствует
            //    Statement stmt2 = con.createStatement();
             //   String deleteQuery = "DELETE FROM  "+TabName+" where id="+ (String)rawRes.get("id") +" ";
              //  System.out.println(""+deleteQuery);
                //stmt2.executeUpdate(deleteQuery);
            }
            stmt.close();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(SQLiteProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rawRes;
    }

    /**
     * Получить список полей
     *
     * @param DataBaseFile - имя Базы данных
     * @param TabName - имя таблицы
     * @return Vector.get(0)- имя поля , Vector.get(1)- тип поля
     */
    private Hashtable<Integer, Vector> getColumName(String DataBaseFile, String TabName) {
        Hashtable<Integer, Vector> poleTyp = new Hashtable<Integer, Vector>(10, (float) 0.5);
        try {
            con = ConnecrDb(DataBaseFile);
            Statement stmt = null;
            stmt = con.createStatement();
            // получаем список полей
            String sqlPole = "select  * from " + TabName + " LIMIT 1";
            java.sql.ResultSet rs = stmt.executeQuery(sqlPole);
            ResultSetMetaData rsmd = rs.getMetaData();
            int colnum = rsmd.getColumnCount();
            for (int ind = 1; ind <= colnum; ind++) {
                String name = rsmd.getColumnName(ind);
                String nameTyp = rsmd.getColumnTypeName(ind);
                Vector pole = new Vector();
                pole.add(name);
                pole.add(nameTyp);
                poleTyp.put(ind, pole);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLiteProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
        return poleTyp;
    }

    /**
     * Метод для получения записи по ID
     *
     * @param DataBaseFile - "D:\\testdb.db"
     * @param TabName -"tab2"
     * @param Where - Условие выборки строк из таблицы
     * @return Hashtable<String, Hashtable<String, String>> listRes = new
     * Hashtable<String, Hashtable<String, String>>(10, (float) 0.5);
     */
    public Hashtable<Object, Hashtable<String, Object>> getRawList(String DataBaseFile, String TabName, String Where) {
        Hashtable<Object, Hashtable<String, Object>> listRes = new Hashtable<Object, Hashtable<String, Object>>(10, (float) 0.5);
        try {
            con = ConnecrDb(DataBaseFile);
            Statement stmt = null;
            stmt = con.createStatement();
            // получаем список полей
            String sqlPole = "select  * from " + TabName + " LIMIT 1";
            java.sql.ResultSet rs = stmt.executeQuery(sqlPole);
            ResultSetMetaData rsmd = rs.getMetaData();
            Hashtable<Integer, String> poleName = new Hashtable<Integer, String>(10, (float) 0.5);
            Hashtable<Integer, String> poleTyp = new Hashtable<Integer, String>(10, (float) 0.5);
            int colnum = rsmd.getColumnCount();
            for (int ind = 1; ind <= colnum; ind++) {
                String name = rsmd.getColumnName(ind);
                String nameTyp = rsmd.getColumnTypeName(ind);
                poleName.put(ind, name);
                poleTyp.put(ind, nameTyp);
                // System.out.println(name+"  "+nameTyp);
            }
            // делаем запрос
            if (Where.length() > 0) {
                sqlPole = "select  * from " + TabName + " where " + Where;
            } else {
                sqlPole = "select  * from " + TabName;
            }
            java.sql.ResultSet rs2 = stmt.executeQuery(sqlPole);
            while (rs2.next()) {
                Hashtable<String, Object> rawRes = new Hashtable<String, Object>(10, (float) 0.5);
                for (int i = 1; i <= colnum; i++) {
                    if (poleName.get(i) != null) {
                        if (poleTyp.get(i).equals("STRING")) {
                            rawRes.put(poleName.get(i), rs2.getString(i));
                        }
                        if (poleTyp.get(i).equals("DOUBLE")) {
                            rawRes.put(poleName.get(i), rs2.getDouble(i));
                        }
                        if (poleTyp.get(i).equals("INTEGER")) {
                            rawRes.put(poleName.get(i), rs2.getInt(i));
                        }
                        if (poleTyp.get(i).equals("FLOAT")) {
                            rawRes.put(poleName.get(i), rs2.getInt(i));
                        }
                        if (poleTyp.get(i).equals("DATE")) {
                            rawRes.put(poleName.get(i), rs2.getDate(i));
                        }
                        if (poleTyp.get(i).equals("TIME")) {
                            rawRes.put(poleName.get(i), rs2.getDate(i));
                        }
                        if (poleTyp.get(i).equals("Byte[]")) {
                            rawRes.put(poleName.get(i), rs2.getByte(i));
                        }

                    }
                }
                listRes.put(rs2.getString(1), rawRes);
            }

        } catch (SQLException ex) {
            Logger.getLogger(SQLiteProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listRes;
    }

    /**
     * Добавить группу строк в таблицу
     *
     * @param DataBaseFile
     * @param TabName
     * @param Tab
     */
    public void addRawList(String DataBaseFile, String TabName, Hashtable<Object, Hashtable<String, Object>> Tab) {
        for (Enumeration Eraw = Tab.keys(); Eraw.hasMoreElements();) {
            Object key = Eraw.nextElement();
            String typ = Tab.get(key).getClass().getSimpleName();
            Hashtable<String, Object> raw = Tab.get(key);
            addRaw(DataBaseFile, TabName, raw);
        }
    }

    /**
     * Удаление таблицы из базы данных
     *
     * @param DataBaseFile
     * @param TabName
     */
    public void dropTable(String DataBaseFile, String TabName) {
        try {
            con = ConnecrDb(DataBaseFile);
            Statement stmt = null;
            stmt = con.createStatement();
            // создаем таблицу если она отсутствует
            String sql="drop table if exists " + TabName;
            System.out.println(sql);
           // stmt.executeUpdate("drop table if exists " + TabName);
            stmt.close();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(SQLiteProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    

    public void delRaw(String DataBaseFile, String TabName,Object ID) {
        try {
            con = ConnecrDb(DataBaseFile);
            Statement stmt = null;
            stmt = con.createStatement();
            // создаем таблицу если она отсутствует
            String s="DELETE FROM  "+TabName+" where id="+ ID +" " ;
            System.out.println(s);
        //    stmt.execute(s );
            stmt.close();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(SQLiteProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String getInservSQL(String TabName, Hashtable<String, Object> Tab) {
        // Вставляем запись
        StringBuffer sb = new StringBuffer();
        int numPole = 0;
        StringBuffer pole = new StringBuffer();
        StringBuffer value2 = new StringBuffer();
        for (Enumeration Eraw = Tab.keys(); Eraw.hasMoreElements();) {
            numPole++;
            Object key = Eraw.nextElement();
            String typ = Tab.get(key).getClass().getSimpleName();
            // System.out.println(key + " typ:" + typ);
            if (numPole == 1) {
                pole.append(" " + key + " ");
            } else {
                pole.append(" , " + key + " ");
            }

            if (numPole == 1) {
                value2.append(" ? ");
            } else {
                value2.append(" , ? ");
            }
        }
        sb.append("INSERT INTO " + TabName + " ( " + pole.toString() + " ) VALUES (" + value2.toString() + " );");
        return sb.toString();
    }

    /**
     * добавить одну строчку в таблицу
     *
     * @param DataBaseFile
     * @param TabName
     * @param Tab
     */
    public boolean addRaw(String DataBaseFile, String TabName, Hashtable<String, Object> Tab) {
        boolean ret = false;
        // SELECT count(*) FROM sqlite_master WHERE type='table' AND name='table_name';
        try {
            con = ConnecrDb(DataBaseFile);
            Statement stmt = null;
            stmt = con.createStatement();
            // создаем таблицу если она отсутствует
            stmt.executeUpdate(createSqlTab(TabName, Tab));
            stmt.close();
            PreparedStatement creerFilm = con.prepareStatement(getInservSQL(TabName, Tab));
            int numPole = 0;
            for (Enumeration Eraw = Tab.keys(); Eraw.hasMoreElements();) {
                numPole++;
                Object key = Eraw.nextElement();
                String typ = Tab.get(key).getClass().getSimpleName();
                if (typ.equals("String")) {
                    creerFilm.setString(numPole, (String) Tab.get(key));
                }
                if (typ.equals("Float")) {
                    creerFilm.setFloat(numPole, (float) Tab.get(key));
                }
                if (typ.equals("Integer")) {
                    creerFilm.setInt(numPole, (int) Tab.get(key));
                }
                if (typ.equals("Double")) {
                    creerFilm.setDouble(numPole, (Double) Tab.get(key));
                }
                if (typ.equals("Time")) {
                    creerFilm.setTime(numPole, (Time) Tab.get(key));
                }
                if (typ.equals("Date")) {
                    creerFilm.setDate(numPole, (Date) Tab.get(key));
                }
                if (typ.equals("Byte[]")) {
                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    ObjectOutputStream o = new ObjectOutputStream(b);
                    o.writeObject(Tab.get(key));
                    InputStream is = new ByteArrayInputStream(b.toByteArray());
                    creerFilm.setBlob(numPole, is);
                }
            }
            creerFilm.executeUpdate();
            creerFilm.close();
            con.close();
            ret = true;
        } catch (SQLException ex) {
            ret = false;
            Logger.getLogger(SQLiteProxy.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SQLiteProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

}
