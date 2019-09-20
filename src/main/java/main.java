import util.LogUtil;
import util.MD5;
import model.db.SqliteOpenHelper;
import server.ConnectionManager;
import server.NettySocketServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class main {
    public static final String CONF_NAME = "/opt/dc1.conf";

    public static void main(String[] args) {
        Properties properties = getProperties(CONF_NAME);
        if (properties == null) {
            ConnectionManager.getInstance().token = MD5.getMD5("dc1server");
            LogUtil.notice("token:dc1server");
        } else {
            String property = properties.getProperty("token", "dc1server");
            LogUtil.notice("token:" + property);
            ConnectionManager.getInstance().token = MD5.getMD5(property);
        }
        SqliteOpenHelper.connectSqlite("jdbc:sqlite:/opt/dc1_database.db");
        new NettySocketServer().start();
    }

    private static Properties getProperties(String propertyFile) {
        try {
            InputStream is = new FileInputStream(propertyFile);
            Properties pros = new Properties();
            pros.load(is);
            return pros;
        } catch (IOException e) {
            System.out.println(propertyFile + " is can not read");
        }
        return null;
    }
}
