package com.sms.database;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class DatabaseConfig {
    private static final String CONFIG_FILE = "config/db.properties";
    
    private String host = "localhost";
    private int port = 3306;
    private String dbName = "student_management_db";
    private String user = "root";
    private String password = "";

    private static DatabaseConfig instance;

    private DatabaseConfig() {
        load();
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public void load() {
        Path path = Paths.get(CONFIG_FILE);
        if (Files.exists(path)) {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(path)) {
                props.load(in);
                this.host = props.getProperty("db.host", "localhost");
                this.port = Integer.parseInt(props.getProperty("db.port", "3306"));
                this.dbName = props.getProperty("db.name", "student_management_db");
                this.user = props.getProperty("db.user", "root");
                this.password = props.getProperty("db.password", "");
            } catch (Exception e) {
                System.err.println("Failed to load db.properties: " + e.getMessage());
            }
        }
    }

    public void save() {
        try {
            Path path = Paths.get(CONFIG_FILE);
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            Properties props = new Properties();
            props.setProperty("db.host", this.host);
            props.setProperty("db.port", String.valueOf(this.port));
            props.setProperty("db.name", this.dbName);
            props.setProperty("db.user", this.user);
            props.setProperty("db.password", this.password != null ? this.password : "");

            try (OutputStream out = Files.newOutputStream(path)) {
                props.store(out, "MySQL Database Configuration for Student Management System");
            }
        } catch (Exception e) {
            System.err.println("Failed to save db.properties: " + e.getMessage());
        }
    }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getDbName() { return dbName; }
    public void setDbName(String dbName) { this.dbName = dbName; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getJdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    }

    public String getServerJdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    }
}
