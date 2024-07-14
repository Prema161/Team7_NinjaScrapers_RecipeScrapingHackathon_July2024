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

    public void createTable() throws SQLException {
        if (conn == null || conn.isClosed()) {
            connect();
        }
        Statement stmt = conn.createStatement();
        String createTableSQL = "CREATE TABLE IF NOT EXISTS recipes (" +
                //"id INT AUTO_INCREMENT PRIMARY KEY," +
                "recipe_id VARCHAR(255) NOT NULL," +
                "name VARCHAR(255) NOT NULL," +
                "prep_time VARCHAR(255)," +
                "cook_time VARCHAR(255)," +
                "ingredients TEXT," +
              //  "ingredient_names TEXT," +
                "cuisine_category VARCHAR(255)," +
                "servings VARCHAR(255)" +
                ")";
        stmt.executeUpdate(createTableSQL);
        stmt.close();
    }

    public void insertRecipeData(String recipeId, String name, String prepTime, String cookTime, String ingredients, String cuisineCategory, String servings) throws SQLException {
        if (conn == null || conn.isClosed()) {
            connect();
        }
        String insertSQL = "INSERT INTO recipes (recipe_id, name, prep_time, cook_time, ingredients, ingredient_names, cuisine_category, servings) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = conn.prepareStatement(insertSQL);
        preparedStatement.setString(1, recipeId);
        preparedStatement.setString(2, name);
        preparedStatement.setString(3, prepTime);
        preparedStatement.setString(4, cookTime);
        preparedStatement.setString(5, ingredients);
       // preparedStatement.setString(6, ingredientNames);
        preparedStatement.setString(7, cuisineCategory);
        preparedStatement.setString(8, servings);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    public void closeConnection() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
}
