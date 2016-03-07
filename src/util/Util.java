package util;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Util {
	//三个文件夹是写死的
	public static final String PATH_FILE_USERID="c:/userid/userid.txt";//本地存储的用户ID自增1的文件夹
	public static final String PATH_FILE_AUTHSUCCESSID="c:/userauthsuccessid/";//本地存储用户生成的登陆令牌的目录
	public static final String PATH_FILE_USERLOGO="c:/Users/Administrator/Desktop/apache-tomcat-6.0.44/webapps/SmallRecipeService/userlogo/";//本地存储用户头像的目录
	public static final String URL_USERLOGO="http://221.228.88.249:8080/SmallRecipeService/userlogo/";
	
//	public static final String PATH_FILE_USERID="c:/userid/userid.txt";//本地存储的用户ID自增1的文件夹
//	public static final String PATH_FILE_AUTHSUCCESSID="c:/userauthsuccessid/";//本地存储用户生成的登陆令牌的目录
//	public static final String PATH_FILE_USERLOGO="D:/androidInfo/apache-tomcat-6.0.29/webapps/SmallRecipeService/userlogo/";//本地存储用户头像的目录
//	
//	
//	public static final String URL_USERLOGO="http://192.168.12.106:8080/SmallRecipeService/userlogo/";
//	
	/**
	 * 
	 * @param req
	 * @param resp
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public static PrintWriter setUnicode(HttpServletRequest req, HttpServletResponse resp,String type)throws ServletException, IOException  {
		req.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=UTF-8");
		Log.d("客户端请求"+type+"，时间："
				+ new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒")
						.format(new Date()));
		return resp.getWriter();
	}
}






















