package model.db;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import model.DataPool;

import java.sql.SQLException;

public class SqliteOpenHelper {

    public static void connectSqlite(String databaseUrl) {
        JdbcConnectionSource connectionSource;
        try {
            connectionSource = new JdbcConnectionSource(databaseUrl);
            connectionSource.setUsername("root");
            connectionSource.setPassword("toor");
            Dc1Dao.init(connectionSource);
            DataPool.init();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
