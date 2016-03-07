package cn.com.smallrecipe;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import modle.AuthUserSessionID;
import modle.Mode;
import modle.ResultToApp;
import util.JDBCUtil;
import util.Log;
import util.Util;

import com.google.gson.Gson;

public class AuthSessionID extends HttpServlet {
	private PrintWriter printWriter;
	private Statement statement;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	synchronized protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		printWriter=Util.setUnicode(req, resp, "验证sessionId");
		statement=connectDB();
		Log.d(statement!=null?"验证sessionId"+"前数据库连接成功":"数据库连接失败");
		if (getParmes(req).getUsernumber()!=null
				&&getParmes(req).getSessionId()!=null) {
			auth(getParmes(req).getUsernumber(),getParmes(req).getSessionId());
		}else {
			printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_USER_NULL, "用户账号或sessionid为空", null)));
		}
	}
	
	private Statement connectDB() {
		return JDBCUtil.getInstance();
	}
	/**
	 * 验证
	 * @param usernumber
	 * @param sessionId
	 */
	synchronized private void auth(String usernumber, String sessionId) {
		// TODO Auto-generated method stub
		Log.d("开始验证,usernumber="+usernumber+",sessionid="+sessionId);
		String sql  ="select sessionid from registioninfo where usernumber ='"+usernumber+"'";
		try {
			ResultSet resultSet = statement.executeQuery(sql);
			if (resultSet.first()) {
					String session_service = resultSet.getString("sessionid");
					if (session_service.trim().equals(sessionId.trim())) {
						printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_SUCCESS, "状态有效", null)));
					}else {
						printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_LOGIN_STATE_ERROR, "登陆失效，sessionid校验失败", null)));
					}
			}else {
				printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_USERNUMBER_NOTHAVE, "账号不存在", null)));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_USERNUMBER_NOTHAVE, "账号不存在", null)));
		}
	}

	private AuthUserSessionID getParmes(HttpServletRequest req) {
		// TODO Auto-generated method stub
		AuthUserSessionID auth = new AuthUserSessionID();
		String usernumber = null;
		String sessionId = null;
		try {
			usernumber = req.getParameter("usernumber");
			sessionId = req.getParameter("sessionid");
			auth.setUsernumber(usernumber);
			auth.setSessionId(sessionId);
		} catch (Exception e) {
			// TODO: handle exception
			auth = null;
		}

		return auth;
	}



}
