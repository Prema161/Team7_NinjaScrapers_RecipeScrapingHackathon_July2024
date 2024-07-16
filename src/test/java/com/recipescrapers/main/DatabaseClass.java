package com.recipescrapers.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;



public class DatabaseClass {
    private static final String base_url = "jdbc:postgresql://localhost:5432/";//"jdbc:mysql://localhost:3306/";
    private static final String DB_name = "recipes_scarping";
    private static final String username = "postgres";
    private static final String password = "root";
    private Connection conn;

   
	
    public Connection connect() throws SQLException {
        conn = DriverManager.getConnection(base_url + DB_name, username, password);
        return conn;
    }

    public void createDatabase() throws SQLException {
        Connection tempConn = DriverManager.getConnection(base_url, username, password);
        Statement stmt = tempConn.createStatement();
        String dropDbSQL = "DROP DATABASE IF EXISTS " + DB_name;
        stmt.executeUpdate(dropDbSQL);
        String createDbSQL = "CREATE DATABASE " + DB_name;
        stmt.executeUpdate(createDbSQL);
        stmt.close();
        tempConn.close();
    }

    public void createTable(String tablename) throws SQLException {
       
        String createTableSQL = "CREATE TABLE IF NOT EXISTS "+tablename+" (\n"
        		+ "                   id text PRIMARY KEY,\n"
        		+ "                    title text NOT NULL,\n"
        		+ "                    description text,\n"
        		+ "                    ingredients text,\n"
        		+ "                    preparation_time text,\n"
        		+ "                    cooking_time text,\n"
        		+ "                    preparation_method text,\n"
        		+ "                    servings text,\n"
        		+ "                    cuisine text,\n"
        		+ "                    category text,\n"
        		+ "                    tags text,\n"
        		+ "                    nutrition text,\n"
        		+ "                    url text\n"
        		+ "             );";
        
        try (Connection conn = this.connect();
                Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
   
  
    public void insertData(String tablename,String id, String title, String description, String ingredients, String preparationTime,
            String cookingTime, String preparationMethod, String servings, String cuisine, String category, String tags,
            String nutrition, String url) {
        String sql = "INSERT INTO "+tablename+"(id, title, description, ingredients, preparation_time, cooking_time, preparation_method, servings, cuisine, category, tags, nutrition, url) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setString(4, ingredients);
            pstmt.setString(5, preparationTime);
            pstmt.setString(6, cookingTime);
            pstmt.setString(7, preparationMethod);
            pstmt.setString(8, servings);
            pstmt.setString(9, cuisine);
            pstmt.setString(10, category);
            pstmt.setString(11, tags);
            pstmt.setString(12, nutrition);
            pstmt.setString(13, url);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    
 
    }

}
