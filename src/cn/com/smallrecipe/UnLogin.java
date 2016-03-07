package cn.com.smallrecipe;

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
import modle.UnLoginInfo;
import util.JDBCUtil;
import util.Log;
import util.Util;

public class UnLogin extends HttpServlet{
	private PrintWriter printWriter;
	private Statement statement;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		printWriter=Util.setUnicode(req, resp,"退出登陆");
		statement=connectDB();
		Log.d(statement!=null?"退出登陆前"+"数据库连接成功":"数据库连接失败");
		if (getParmers(req).getUsernumber()!=null
			&&getParmers(req).getSessionId()!=null) {
			unLogin(getParmers(req));
		}else {
			printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_UNLOGIN_ERROR,
					"退出失败，账号、sessionId至少有一项为空", null)));
		}
	}
	
	private Statement connectDB() {
		return JDBCUtil.getInstance();
	}
	private void unLogin(UnLoginInfo parmers) {
		// TODO Auto-generated method stub
		Log.d("将要退出的账号:"+parmers.getUsernumber()+",sessionId:"+parmers.getSessionId());
		String selectUserSessionId_SQL="select sessionid from registioninfo where usernumber = '"+parmers.getUsernumber()+"'";
		try {
			ResultSet resultSet = statement.executeQuery(selectUserSessionId_SQL);
			if (resultSet.first()) {
				String session_service = resultSet.getString("sessionid");
				Log.d("查询到服务器存储的该用户的SessionId："+session_service);
				if (session_service!=null&&!session_service.equals("")
						&&parmers.getSessionId().equals(session_service)) {
					String SQL_update="update registioninfo set sessionid='' where usernumber ='"+parmers.getUsernumber()+"'";
					int isUpdateOK=statement.executeUpdate(SQL_update);
					Log.d("isOk"+isUpdateOK);
					if (isUpdateOK>-1) {
						printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_SUCCESS,
								"app退出成功",null)));
					}else {
						printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_CLEAR_SESSIONID_ERROR,
								"未知错误",null)));
					}
				}else {
					if (session_service==null ||session_service.equals("")) {
						printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_USER_NOT_LOGIN,
								"用户未登陆", null)));
					}else {
						printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_LOGIN_STATE_ERROR,
								"登陆状态已失效，请重新登陆", null)));
					}
				}
			}else {
				Log.d("账号不存在");
				printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_USERNUMBER_NOTHAVE,
						"账号不存在", null)));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_DB_ERROR,
					"数据库异常", null)));
			Log.d("用户退出时查询sessionid失败,数据库抛出异常");
		}
	}
	private UnLoginInfo getParmers(HttpServletRequest req) {
		UnLoginInfo unLoginInfo = new  UnLoginInfo();
		String userNumber = null;
		String sessionId = null;
		try {
			userNumber=req.getParameter("usernumber");
			sessionId=req.getParameter("sessionid");
			
			unLoginInfo.setUsernumber(userNumber);
			unLoginInfo.setSessionId(sessionId);
		} catch (Exception e) {
			// TODO: handle exception
			unLoginInfo=null;
		}
		return unLoginInfo;
	}
	

}
