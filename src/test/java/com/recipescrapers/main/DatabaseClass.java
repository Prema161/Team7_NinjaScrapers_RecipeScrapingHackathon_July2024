package com.recipescrapers.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;



public class DatabaseClass {
    private static final String base_url = "jdbc:postgresql://localhost:5432/"; //"jdbc:mysql://localhost:3306/";
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
        		+ "                   recipe_id text PRIMARY KEY,\n"
        		+ "                    recipe_name text NOT NULL,\n"
        		+ "                    recipe_description text,\n"
        		+ "                    recipe_ingredients text,\n"
        		+ "                    preparation_time text,\n"
        		+ "                    cooking_time text,\n"
        		+ "                    preparation_method text,\n"
        		+ "                    servings text,\n"
        		+ "                    cuisine_category text,\n"
        		+ "                    food_category text,\n"
        		+ "                    recipe_category text,\n"
        		+ "                    tags text,\n"
        		+ "                    nutrition_values text,\n"
        		+ "                    recipe_url text\n"
        		+ "             );";
        
        try (Connection conn = this.connect();
                Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
   /*
    * method to insert records into the database table
    */
  
    public void insertData(String tablename,String id, String title, String description, String ingredients, String preparationTime,
            String cookingTime, String preparationMethod, String servings, String cuisineCategory, String foodCategory, String recipeCategory ,String tags,
            String nutrition, String url) {
        String sql = "INSERT INTO "+tablename+"(recipe_id, recipe_name, recipe_description, recipe_ingredients, preparation_time, cooking_time, preparation_method, servings, cuisine_category, food_category, recipe_category,tags, nutrition_values, recipe_url) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

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
            pstmt.setString(9, cuisineCategory);
            pstmt.setString(10, foodCategory);
            pstmt.setString(11, recipeCategory);
            pstmt.setString(12, tags);
            pstmt.setString(13, nutrition);
            pstmt.setString(14, url);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
