package cn.com.smallrecipe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

import modle.Mode;
import modle.ResultToApp;
import modle.SetUserDefultConfig;
import modle.UserLoginInfo;
import util.JDBCUtil;
import util.Log;
import util.Util;

import com.google.gson.Gson;

public class Register extends HttpServlet {
	private Statement statement;
	private PrintWriter printWriter;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
	
	@Override
	synchronized protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		printWriter=Util.setUnicode(req, resp, "注册");
		statement=connectDB();
		Log.d(statement!=null?"注册"+"前数据库连接成功":"数据库连接失败");
		UserLoginInfo userLoginInfo=getParmes(req);
		if (userLoginInfo.getUserNumber()!=null 
				&& userLoginInfo.getPassword()!=null) {
			register(userLoginInfo);
		}else {
			printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_USER_NULL,
					"注册失败，用户名或密码不能为空", null)));
		}
		
	}
	private Statement connectDB() {
		return JDBCUtil.getInstance();
	}
	private void register(UserLoginInfo userLoginInfo) {
		// TODO Auto-generated method stub
		Log.d("App传入服务器的账号："+userLoginInfo.getUserNumber()+",密码："+userLoginInfo.getPassword());
		String sql = "select usernumber from registioninfo where usernumber = '"+userLoginInfo.getUserNumber()+"'";
		
		String selectSQL = "insert into registioninfo(usernumber,password)values('"+
		userLoginInfo.getUserNumber()+"','"+userLoginInfo.getPassword()+"')";
		try {
			ResultSet set = statement.executeQuery(sql);
			if (set.first()) {
				printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_USER_LOGINED,
						"注册失败，数据已存在", null)));
				return;
			}
			int isInsertOk=statement.executeUpdate(selectSQL);
			if (isInsertOk>-1) {
//				
				//为用户设置默认参数
				SetUserDefultConfig defultConfig =setUserDefultConfit(userLoginInfo);
				if (defultConfig.isOK()) {
					printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_SUCCESS,
							"注册成功", null)));
				}else {
					printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_DB_ERROR,
							"注册成功,用户默认信息设置失败", null)));
				}
				
			}else {
				printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_DB_ERROR,
						"注册失败，数据库异常", null)));
			}
			Log.d("isInsertOK="+isInsertOk);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("数据写入失败");
			printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_USER_WAS_SAVED,
					"用户已存在", null)));
		}
	}

	private SetUserDefultConfig setUserDefultConfit(UserLoginInfo userLoginInfo) {
		SetUserDefultConfig config = new SetUserDefultConfig();
		String logopath=Util.PATH_FILE_USERLOGO+userLoginInfo.getUserNumber()+".jpg";
		String clientGetUserLogoUrl=Util.URL_USERLOGO+userLoginInfo.getUserNumber()+".jpg";
		String userId=readTxtFile(Util.PATH_FILE_USERID);
//		String userId = 
		String sql = "update registioninfo set username='"+"测试账户"+userId+"',userlogo='"+logopath
				+"',userid='"+userId+"',userlogourl='"+clientGetUserLogoUrl+"' where usernumber='"+userLoginInfo.getUserNumber()+"'";
		try {
			int isSetDefultConfigOK=statement.executeUpdate(sql);
			if (isSetDefultConfigOK>-1) {
				int uid=Integer.valueOf(userId);
				Log.d("用户默认信息设置成功，开始对userid子增1");
				uid++;
				File file_pu = new File(Util.PATH_FILE_USERID);
				if (file_pu.exists()) {
					file_pu.delete();
				}
				FileOutputStream out_pu;
				String finalUserId=String.valueOf(uid);
				try {
					out_pu = new FileOutputStream(file_pu);
					out_pu.write(finalUserId.getBytes());
					out_pu.flush();
					out_pu.close();
					config.setOK(true);
					config.setErrorMsg("设置成功");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					config.setOK(false);
					config.setErrorMsg("写入新的UserId出错");
				} catch (IOException e) {	
					// TODO Auto-generated catch block
					e.printStackTrace();
					config.setOK(false);
					config.setErrorMsg("写入新的UserId出错");
				}
			}else {
				config.setOK(false);
				config.setErrorMsg("设置失败");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			config.setOK(false);
			config.setErrorMsg("设置失败，异常");
			
		}
		
		return config;
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
	 * 获取app端传过来的参数，此为登陆/注册接口，只会传递用户名、密码
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
	

}
