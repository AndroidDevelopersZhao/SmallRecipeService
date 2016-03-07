package cn.com.smallrecipe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import modle.RespData;
import modle.ResultToApp;
import modle.WXBoindInfo;

import sun.misc.BASE64Decoder;
import util.JDBCUtil;
import util.Log;
import util.Util;

public class BoindUserForWX extends HttpServlet{
	private PrintWriter printWriter;
	private Statement statement;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=UTF-8");
		printWriter=resp.getWriter();
		statement=connectDB();
		Log.d("客户端请求使用微信注册");
		//获取参数
		WXBoindInfo wxBoindInfo = getParms(req);
		if (wxBoindInfo.getUsernumber()!=null
				&&!wxBoindInfo.getUsernumber().equals("")
				&&wxBoindInfo.getUsername()!=null
				&&!wxBoindInfo.getUsername().equals("")
				&&wxBoindInfo.getUserlogo()!=null
				&&!wxBoindInfo.getUserlogo().equals("")
				&&wxBoindInfo.getOpenid()!=null
				&&!wxBoindInfo.getOpenid().equals("")) {
			//查询该用户是否已经存在注册表（usernumber）
			boolean userExist=IsUserExist(wxBoindInfo);
			if (userExist) {
				//已经存在
				printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_USER_WAS_SAVED, "注册失败，该账号已经存在", null)));
			}else {
				//不存在
				//插入是否成功
				boolean isInsertOK = InsertUser(wxBoindInfo);
				if (isInsertOK) {
					//将openid写入到wxuser表
					Log.d("数据插入成功，开始将openid插入到微信用户表.........");
					boolean isInsertOpenidOK=insertOpenIdToWXUser(wxBoindInfo.getOpenid());
					if (isInsertOpenidOK) {
						//数据插入成功，做登陆操作，返回登陆后的用户所有数据
						Log.d("将openid插入到微信用户表成功，开始登陆");
						login(wxBoindInfo.getUsernumber(), "123456");
					}
				}
			}
		}else {
			printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_NULL, "参数不能为空", null)));
		}
	}
	
	/**
	 * 插入openid到wxuser表
	 * @param openid
	 * @return
	 */
	private boolean insertOpenIdToWXUser(String openid) {
		Log.d("11111111111111");
		boolean isok = false;
		Log.d("22222222222222222");
		String sql ="insert into wxuser(openid)values('"+openid+"')";
		Log.d("333333333333333333");
		try {
			Log.d("4444444444444444444444");
			int set = statement.executeUpdate(sql);
			Log.d("555555555555555555555555");
			Log.d(set+"");
			Log.d("666666666666666666666666");
			if (set>-1) {
				Log.d("7777777777777777777777777");
				isok=true;
				Log.d("888888888888888888888888888888888");
			}
			Log.d("999999999999999999999999");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("xxxxxxxxxxxxxxxxxxxxxxx");
			Log.d(""+e.getMessage());
		}
		Log.d("qqqqqqqqqqqqqqqqqqqqqqqqqqq");
		return isok;
	}


	/**
	 * 用户使用用户账号和密码登陆
	 * @param usernumber
	 * @param pasword
	 */
	private void login(String usernumber,String pasword) {
		final String SELECTSQL = "select * from registioninfo where usernumber='"
				+ usernumber + "'";// 查询该用户是否存在语句
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
					String sessionId = String.valueOf(new Date().getTime());
					Log.d("用户使用微信注册并登陆，生成sessionId=" + sessionId);
					if (sessionid==null ||sessionid.equals("")) {
						//用户没有登陆的状态
						String UPDATA_SESSIONID_SQL="update registioninfo set sessionid='"+
						sessionId+"'where usernumber='"+usernumber+"'";
						int isOk=statement.executeUpdate(UPDATA_SESSIONID_SQL);
						if (isOk>=0) {
							Log.d("sessionid 写入成功");
							printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_SUCCESS,
									"登陆成功", 
									new RespData(sessionId,null,usernumber,username,
											userID,userLogourl,null,null,null,null,null,
											null,null,null,false,false))));
						}else {
							Log.d("sessionid 写入失败");
							printWriter.print(new Gson().toJson(new ResultToApp(
									Mode.ERRORCODE_WRITE_RELOGINID_ERROR,
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
									"登陆失败,用户已经登陆,如需强制登陆，请使用免验证登陆令牌reLoginId登陆,该令牌将在20秒后失效", new RespData(null, sessionId, usernumber,null, null, null,null,null,null,null,null,null,null,null,false,false))));
							//执行休眠删除操作
							willDeleteFile(Util.PATH_FILE_AUTHSUCCESSID+usernumber);
							
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_DB_ERROR,
									"登陆失败,用户已经登陆,您当前无法登陆，服务器没有准备好", 
									new RespData(null,
											null, usernumber,null, null, null,null,null,null,null,null,null,null,null,false,false))));
						} catch (IOException e) {	
							// TODO Auto-generated catch block
							e.printStackTrace();
							printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_DB_ERROR,
									"登陆失败,用户已经登陆,您当前无法登陆，服务器没有准备好", new RespData(null, null,null, null, null, null,null,null,null,null,null,null,null,null,false,false))));
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
	
	
	/**
	 * 删除本地保存的reloginid
	 * @param string
	 */
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
	 * 插入新的微信用户
	 * @param wxBoindInfo
	 * @return	默认返回false失败
	 */
	private boolean InsertUser(WXBoindInfo wxBoindInfo) {
		boolean isInsertOK=false;
		String defaultPassword="123456";
		String userlogoFilePath=Util.PATH_FILE_USERLOGO+wxBoindInfo.getUsernumber()+".jpg";//本地存储用户头像的路径
		String userlogoBackToClientUrl=Util.URL_USERLOGO+wxBoindInfo.getUsernumber()+".jpg";//数据库里保存在客户端请求头像的url
		String userId=readTxtFile(Util.PATH_FILE_USERID);
		String sql ="insert into registioninfo(usernumber,username,password,userid,userlogo,userlogourl,openid_wx)values(" +
				"'"+wxBoindInfo.getUsernumber()+"','微信用户-"+wxBoindInfo.getUsername()+"'," +
						"'"+defaultPassword+"','"+userId+"','"+userlogoFilePath+"','"+userlogoBackToClientUrl+"','"+wxBoindInfo.getOpenid()+"')";
		try {
			int isok=statement.executeUpdate(sql);
			if (isok>-1) {
				//数据写入数据库后，对userid自增一并写入文件
				boolean isUserIdAdd1Succ=userIdjiajia(userId);
				if (isUserIdAdd1Succ) {
					//自增一成功后根据头像path保存用户头像
					Log.d("开始保存客户端传入的图片（长度）"+wxBoindInfo.getUserlogo().length()+"至目录："+userlogoFilePath);
					boolean isSetUserLogoOK=GenerateImage(wxBoindInfo.getUserlogo(),userlogoFilePath);
					if (isSetUserLogoOK) {
						isInsertOK=true;
					}
				}
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isInsertOK;
	}
	
	/**
	 * 将Base64转码后的图片字符串保存到本地
	 * @param imgStr
	 * @param path
	 * @return
	 */
	 //base64字符串转化成图片  
    public static boolean GenerateImage(String imgStr,String path)  
    {   //对字节数组字符串进行Base64解码并生成图片  
    	File file = new File(path);
    	if (file.exists()) {
			file.delete();
		}
        if (imgStr == null) //图像数据为空  
            return false;  
        BASE64Decoder decoder = new BASE64Decoder();  
        try   
        {  
            //Base64解码  
            byte[] b = decoder.decodeBuffer(imgStr);  
            for(int i=0;i<b.length;++i)  
            {  
                if(b[i]<0)  
                {//调整异常数据  
                    b[i]+=256;  
                }  
            }  
            //生成jpeg图片  
            String imgFilePath =path;//新生成的图片  
            OutputStream out = new FileOutputStream(imgFilePath);      
            out.write(b);  
            out.flush();  
            out.close();  
            Log.d("图片存储成功");
            return true;  
        }   
        catch (Exception e)   
        {  
        	Log.d(e.getMessage());
            return false;  
        }  
    }  
	/**
	 * userid自增一
	 * @param userid
	 * @return
	 */
	private boolean userIdjiajia(String userid) {
			boolean isok = false;
			int uid=Integer.valueOf(userid);
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
				isok=true;
				Log.d("UserID自增一并保存成功");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {	
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return isok;
	}
	/**
	 * 读取本地存储的用户id，每注册一位用户，id自增1
	 * @param filePath
	 * @return
	 */
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
	 * 查询该用户是否已经存在注册表（usernumber）
	 * @param wxBoindInfo
	 * @return 默認為false不存在
	 */
		private boolean IsUserExist(WXBoindInfo wxBoindInfo) {
			boolean userExist=false;
			String sql  = "select usernumber from registioninfo where usernumber='"+wxBoindInfo.getUsernumber()+"'";
			try {
				ResultSet set = statement.executeQuery(sql);
				if (set.first()) {
					userExist=true;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return userExist;
	}
		/**
		 * 获取参数
		 * @param req
		 * @return
		 */
	private WXBoindInfo getParms(HttpServletRequest req) {
		WXBoindInfo wxBoindInfo=new WXBoindInfo();
		wxBoindInfo.setUsernumber(req.getParameter("usernumber"));
		wxBoindInfo.setUsername(req.getParameter("username"));
		wxBoindInfo.setUserlogo(req.getParameter("userlogo"));
		wxBoindInfo.setOpenid(req.getParameter("openid"));
		return wxBoindInfo;
	}

	private Statement connectDB() {
		return JDBCUtil.getInstance();
	}
}
