import model.db.SqliteOpenHelper;
import server.NettySocketServer;

public class main {
    public static void main(String[] args) {
        SqliteOpenHelper.connectSqlite("jdbc:sqlite:data.db");
        new NettySocketServer(8000).start();
    }
}
