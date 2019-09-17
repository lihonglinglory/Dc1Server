import model.db.SqliteOpenHelper;
import server.NettySocketServer;

public class main {
    public static void main(String[] args) {
        SqliteOpenHelper.connectSqlite("jdbc:sqlite:/opt/dc1_database.db");
        new NettySocketServer().start();
    }
}
