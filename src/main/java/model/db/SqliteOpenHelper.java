package model.db;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import model.DataPool;
import model.PlanPool;

import java.sql.SQLException;

public class SqliteOpenHelper {

    public static void connectSqlite(String databaseUrl) {
        JdbcConnectionSource connectionSource;
        try {
            connectionSource = new JdbcConnectionSource(databaseUrl);
            connectionSource.setUsername("root");
            connectionSource.setPassword("toor");
            Dc1Dao.init(connectionSource);
            PlanDao.init(connectionSource);
            DataPool.init();
            PlanPool.getInstance().init();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
