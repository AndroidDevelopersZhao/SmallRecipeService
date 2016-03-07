package cn.com.smallrecipe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import modle.Mode;
import modle.QQUserInfo;
import modle.RespData;
import modle.ResultToApp;

import util.JDBCUtil;
import util.Log;
import util.Util;

public class RegisterForQQ extends HttpServlet{
	
	private PrintWriter printWriter;
	private Statement statement;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=UTF-8");
		printWriter=Util.setUnicode(req, resp,"使用QQ登陆");
		statement=connectDB();
//		printWriter.print("a");
		Log.d(statement!=null?"QQ注册前"+"数据库连接成功":"数据库连接失败");
		QQUserInfo info=getParms(req);
		if (info.getOpenid()!=null &&!info.getOpenid().equals("")
				&&info.getUsernmae()!=null &&!info.getUsernmae().equals("")
				&&info.getUserlogo()!=null &&!info.getUserlogo().equals("")) {
			//查询当前登陆的qq用户是否已经注册
			boolean isRegisterForQQ = IsRegister(info.getOpenid());
			if (isRegisterForQQ) {
				//已经存在
				login(info.getOpenid());
			}else {
				//不存在
				printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_QQUSER_EXIT,
						"数据不存在，请绑定手机号", null)));
			}
		}else {
			printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_NULL,
					"传入参数为空", null)));
		}
	}
	private void login(String openid) {
		String sql="select * from registioninfo where openid='"+openid+"'";
		try {
			ResultSet resultSet = statement.executeQuery(sql);
			String password = null;
			String sessionid = null;
			String username=null;
			String userID = null;
			String userLogourl =null;
			String usernumber=null;
			if (resultSet.first()) {
				// 进入说明账号存在
				password = resultSet.getString("password");
				sessionid=resultSet.getString("sessionid");
				username=resultSet.getString("username");
				userID=resultSet.getString("userid");
				userLogourl=resultSet.getString("userlogourl");
				usernumber=resultSet.getString("usernumber");
					String sessionId = String.valueOf(new Date().getTime());
					Log.d("用户准备登陆，生成sessionId=" + sessionId);
					if (sessionid==null ||sessionid.equals("")) {
						//用户没有登陆的状态
						String UPDATA_SESSIONID_SQL="update registioninfo set sessionid='"+sessionId+"'where usernumber='"+
						usernumber+"'";
						int isOk=statement.executeUpdate(UPDATA_SESSIONID_SQL);
						if (isOk>=0) {
							Log.d("sessionid 写入成功");
							printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_SUCCESS,
									"登陆成功", new RespData(sessionId,null,usernumber,username,
											userID,userLogourl,null,null,null,null,null,null,null,null,false,false))));
						}else {
							Log.d("sessionid 写入失败");
							printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_WRITE_RELOGINID_ERROR,
									"登陆失败,服务器写入sessionid失败",null)));
						}
						
					}else {
						//用户已经登陆的状态下
						File file_pu = new File(Util.PATH_FILE_AUTHSUCCESSID+usernumber);
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
									"登陆失败,用户已经登陆,如需强制登陆，请使用免验证登陆令牌reLoginId登陆,该令牌将在20秒后失效", new RespData(null, sessionId,usernumber, null, null, null,null,null,null,null,null,null,null,null,false,false))));
							//执行休眠删除操作
							willDeleteFile(Util.PATH_FILE_AUTHSUCCESSID+usernumber);
							
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_DB_ERROR,
									"登陆失败,用户已经登陆,您当前无法登陆，服务器没有准备好",
									new RespData(null,
											null,usernumber, null, null, null,null,null,null,null,null,null,null,null,false,false))));
						} catch (IOException e) {	
							// TODO Auto-generated catch block
							e.printStackTrace();
							printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_DB_ERROR,
									"登陆失败,用户已经登陆,您当前无法登陆，服务器没有准备好", new RespData(null, null, null,null, null, null,null,null,null,null,null,null,null,null,false,false))));
						}
						
						
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
	private boolean IsRegister(String openid) {
		boolean isok = false;
		String sql = "select * from qquser where openid='"+openid+"'";
		try {
			ResultSet set = statement.executeQuery(sql);
			if (set.first()) {
				isok=true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isok;
	}
	private QQUserInfo getParms(HttpServletRequest req) {
		QQUserInfo userInfo  =new QQUserInfo();
		userInfo.setOpenid(req.getParameter("openid"));
		userInfo.setUsernmae("username");
		userInfo.setUserlogo("userlogo");
		return userInfo;
	}
	private Statement connectDB() {
		return JDBCUtil.getInstance();
	}
}
