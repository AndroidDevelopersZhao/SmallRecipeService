package cn.com.smallrecipe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import modle.Mode;
import modle.RespData;
import modle.ResultToApp;
import modle.UserReLoginInfo;
import util.JDBCUtil;
import util.Log;
import util.Util;

import com.google.gson.Gson;

public class ReLogin extends HttpServlet{
	private Statement statement;
	public PrintWriter printWriter = null;
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
	@Override
	synchronized protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		printWriter=Util.setUnicode(req, resp,"强制登陆");
		statement=connectDB();
		Log.d(statement!=null?"强制登陆前"+"数据库连接成功":"数据库连接失败");
			if (getParmes(req).getUserNumber() != null
					&& getParmes(req).getReLoginId() != null) {
				login(getParmes(req));
			}else {
				printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_RELOGINERROR,
						"使用绿色令牌登陆时令牌ID和用户账号不能为空", null)));
			}

	}
	
	private Statement connectDB() {
		return JDBCUtil.getInstance();
	}
	private void login(UserReLoginInfo loginInfo){
		String saveReLoginId=readTxtFile(Util.PATH_FILE_AUTHSUCCESSID+loginInfo.getUserNumber());
		String reLoginId_App = loginInfo.getReLoginId();
		Log.d("用户传入reLoginID="+reLoginId_App);
		Log.d("查询到该用户的saveReLoginId="+saveReLoginId);
		
		if (saveReLoginId==null || saveReLoginId.equals("")) {
			Log.d("传入的用户名没有获取过ReLoginId");
			printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_USER_NOT_GETED_RELOGINID,
					"登陆失败,该用户名没有获取过ReLoginId或已经失效",null)));
			return;
		}
		if (saveReLoginId.equals(reLoginId_App)) {
			Log.d("reloginID校验成功，开始写入新的sessionid到数据库");
			String sessionId = String.valueOf(new Date().getTime());
			String updateSQL = "update registioninfo set sessionid='"+sessionId+"'where usernumber='"+loginInfo.getUserNumber()+"'";
			try {
				int isOk=statement.executeUpdate(updateSQL);
				if (isOk>=0) {
					Log.d("sessionId 写入成功");
					deleteFile(Util.PATH_FILE_AUTHSUCCESSID+loginInfo.getUserNumber());
					
					//TODO
					//查询该用户的信息并返回
					String SELECTSQL = "select * from registioninfo where usernumber='"
						+ loginInfo.getUserNumber() + "'";
					try {
						ResultSet resultSet = statement.executeQuery(SELECTSQL);
						String username=null;
						String userID = null;
						String userLogourl =null;
						if (resultSet.first()) {
							username=resultSet.getString("username");
							userID=resultSet.getString("userid");
							userLogourl=resultSet.getString("userLogourl");
							printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_SUCCESS,
									"登陆成功", 
									new RespData(sessionId,null,loginInfo.getUserNumber(), username, userID, userLogourl,null,null,null,null,null,null,null,null,false,false))));
						}
					} catch (Exception e) {
						// TODO: handle exception
						printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_SUCCESS,
								"登陆成功,用户个人信息获取失败",
								new RespData(sessionId,null, loginInfo.getUserNumber(),null, null, null,null,null,null,null,null,null,null,null,false,false))));
					}
					
				}else {
					Log.d("sessionId 写入失败");
					printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_WRITE_SESSION_ERROR,
							"登陆失败,服务器写入sessionid失败",null)));
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d("relogin更新sessionid失败，抛出异常");
				printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_WRITE_RELOGINID_ERROR,
						"reloginId校验成功，写入时出现错误",null)));
			}
		}else {
			Log.d("reloginID校验失败");
			printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_AUTH_RELOGINID_ERROR,
					"登陆失败,reLoginId校验失败",null)));
		}
	}
	public static void deleteFile(String path){
		File file_pu = new File(path);
		if (file_pu.exists()) {
			file_pu.delete();
		}
	}
	
	 private String readTxtFile(String filePath){
		 String text = null;
	        try {
	                String encoding="UTF-8";
	                File file=new File(filePath);
	                if(file.isFile() && file.exists()){ //判断文件是否存在
	                    InputStreamReader read = new InputStreamReader(
	                    new FileInputStream(file),encoding);//考虑到编码格式
	                    BufferedReader bufferedReader = new BufferedReader(read);
	                    String lineTxt = null;
	                    while((lineTxt = bufferedReader.readLine()) != null){
	                        System.out.println(lineTxt);
	                        text=lineTxt;
	                    }
	                    read.close();
	        }else{
	            System.out.println("找不到指定的文件");
	        }
	        } catch (Exception e) {
	            System.out.println("读取文件内容出错");
	            e.printStackTrace();
	        }
	     return text;
	    }
	/**
	 * 获取app端传过来的参数，此为登陆接口，只会传递用户名、密码
	 * 
	 * @param req
	 * @return UserLoginInfo
	 */
	private UserReLoginInfo getParmes(HttpServletRequest req) {
		// TODO Auto-generated method stub
		UserReLoginInfo loInfo = new UserReLoginInfo();
		String userNumber = null;
		String reLoginId = null;
		try {
			userNumber = req.getParameter("usernumber");
			reLoginId = req.getParameter("reloginid");

			loInfo.setUserNumber(userNumber);
			loInfo.setReLoginId(reLoginId);
		} catch (Exception e) {
			// TODO: handle exception
			loInfo = null;
		}

		return loInfo;
	}
	
	
}
