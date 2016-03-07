package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCUtil {
	
	private static Statement stat = null;
	private static final String forname="com.mysql.jdbc.Driver";
	private static final String url="jdbc:mysql://127.0.0.1:3306/smallrecipedb?characterEncoding=utf-8";
	private static final String serviceUserName="root";
	private static final String servicePassWord="root";
	
	private JDBCUtil() {
		super();
	}
	public static Statement getInstance() {
		
        return jdbc(forname,url,serviceUserName,servicePassWord);
    }
	protected static Statement jdbc(String forName,String url,String serviceUserName,String servicePassWord){
		
		try {
			Class.forName(forName);
			Connection conn=DriverManager.getConnection(url, serviceUserName, servicePassWord);
			stat=conn.createStatement();
			System.out.println("smallrecipe-db连接成功");
		} catch (ClassNotFoundException e) {
			System.out.println("加载驱动失败！");
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println("数据库连接失败，请检查url，数据库用户名和密码！");
			e.printStackTrace();
		}
		return stat;
	}
}
