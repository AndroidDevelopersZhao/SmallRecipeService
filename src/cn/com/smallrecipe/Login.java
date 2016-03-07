package cn.com.smallrecipe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import modle.Mode;
import modle.RespData;
import modle.ResultToApp;
import modle.UserLoginInfo;
import util.JDBCUtil;
import util.Log;
import util.Util;

public class Login extends HttpServlet {
	private PrintWriter printWriter = null;
	private Statement statement = null;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	//
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Log.d(req.getParameter("com_name"));
		printWriter=Util.setUnicode(req, resp,"登陆");
		statement=connectDB();
		Log.d(statement!=null?"登陆前"+"数据库连接成功":"数据库连接失败");
		if (getParmes(req).getUserNumber() != null
				&& getParmes(req).getPassword() != null) {
			login(getParmes(req));
		}else {
			printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_USER_NULL,
					"用户名或密码不能为空", null)));
		}

	}

	private void login(UserLoginInfo loInfo) {
		Log.d("u:" + loInfo.getUserNumber() + ",p:" + loInfo.getPassword());
		// TODO Auto-generated method stub
		final String SELECTSQL = "select * from registioninfo where usernumber='"
				+ loInfo.getUserNumber() + "'";// 查询该用户是否存在语句
		try {
			ResultSet resultSet = statement.executeQuery(SELECTSQL);
			String password = null;
			String sessionid = null;
			String username=null;
			String userID = null;
			String userLogourl =null;
			if (resultSet.first()) {
				// 进入说明账号存在
				password = resultSet.getString("password");
				sessionid=resultSet.getString("sessionid");
				username=resultSet.getString("username");
				userID=resultSet.getString("userid");
				userLogourl=resultSet.getString("userlogourl");
				
				if (password.equals(loInfo.getPassword())) {
					String sessionId = String.valueOf(new Date().getTime());
					Log.d("用户准备登陆，生成sessionId=" + sessionId);
					if (sessionid==null ||sessionid.equals("")) {
						//用户没有登陆的状态
						String UPDATA_SESSIONID_SQL="update registioninfo set sessionid='"+sessionId+"'where usernumber='"+loInfo.getUserNumber()+"'";
						int isOk=statement.executeUpdate(UPDATA_SESSIONID_SQL);
						if (isOk>=0) {
							Log.d("sessionid 写入成功");
							
							printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_SUCCESS,
									"登陆成功", new RespData(sessionId,null,loInfo.getUserNumber(),username,
											userID,userLogourl,null,null,null,null,null,null,null,null,false,false))));
						}else {
							Log.d("sessionid 写入失败");
							printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_WRITE_RELOGINID_ERROR,
									"登陆失败,服务器写入sessionid失败",null)));
						}
						
					}else {
						//用户已经登陆的状态下
						File file_pu = new File(Util.PATH_FILE_AUTHSUCCESSID+loInfo.getUserNumber());
						if (file_pu.exists()) {
							file_pu.delete();
						}
						FileOutputStream out_pu;
						try {
							out_pu = new FileOutputStream(file_pu);
							out_pu.write(sessionId.getBytes());
							out_pu.flush();
							out_pu.close();
							Log.d("用户已登陆，登陆失败，已生成绿色登陆令牌存入本地");
							printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_USER_LOGINED,
									"登陆失败,用户已经登陆,如需强制登陆，请使用免验证登陆令牌reLoginId登陆,该令牌将在20秒后失效",
									new RespData(null, sessionId, loInfo.getUserNumber(),null, null, null,null,null,null,null,null,null,null,null,false,false))));
							//执行休眠删除操作
							willDeleteFile(Util.PATH_FILE_AUTHSUCCESSID+loInfo.getUserNumber());
							
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_DB_ERROR,
									"登陆失败,用户已经登陆,您当前无法登陆，服务器没有准备好", 
									new RespData(null,
											null,loInfo.getUserNumber(), null, null, null,null,null,null,null,null,null,null,null,false,false))));
						} catch (IOException e) {	
							// TODO Auto-generated catch block
							e.printStackTrace();
							printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_DB_ERROR,
									"登陆失败,用户已经登陆,您当前无法登陆，服务器没有准备好", 
									new RespData(null, null, loInfo.getUserNumber(),null, null, null,null,null,null,null,null,null,null,null,false,false))));
						}
						
						
					}
					
					
					
				} else {
					printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_PASSWORD_ERROR,
							"密码不正确", null)));
				}
			} else {
				printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_USERNUMBER_NOTHAVE,
						"账号不存在", null)));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("执行查询语句错误，抛出异常");
			printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_DB_ERROR,
					"服务器没有准备好", null)));
		}
	}

	private void willDeleteFile(final String string) {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(20000);
					ReLogin.deleteFile(string);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * 获取app端传过来的参数，此为登陆接口，只会传递用户名、密码
	 * 
	 * @param req
	 * @return UserLoginInfo
	 */
	private UserLoginInfo getParmes(HttpServletRequest req) {
		// TODO Auto-generated method stub
		UserLoginInfo loInfo = new UserLoginInfo();
		String userNumber = null;
		String password = null;
		try {
			userNumber = req.getParameter("usernumber");
			password = req.getParameter("password");

			loInfo.setUserNumber(userNumber);
			loInfo.setPassword(password);
		} catch (Exception e) {
			// TODO: handle exception
			loInfo = null;
		}

		return loInfo;
	}

	private Statement connectDB() {
		return JDBCUtil.getInstance();
	}

}
