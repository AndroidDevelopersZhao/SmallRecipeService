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
import java.io.UnsupportedEncodingException;
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
import modle.QQUserInfo;
import modle.RespData;
import modle.ResultToApp;
import modle.SetUserDefultConfig;
import modle.UserLoginInfo;
import sun.misc.BASE64Decoder;
import util.JDBCUtil;
import util.Log;
import util.Util;

import com.google.gson.Gson;

public class BoindUserForQQ extends HttpServlet{
	private PrintWriter printWriter;
	private Statement statement;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");
		resp.setContentType("text/html;charset=UTF-8");
		Log.d("客户端请求绑定用户"
				+ new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒")
						.format(new Date()));
		printWriter=resp.getWriter();
		statement=connectDB();
//		printWriter.print("a");
		Log.d(statement!=null?"QQ绑定用户号前"+"数据库连接成功":"数据库连接失败");
		QQUserInfo info=getParms(req);
		if (info.getOpenid()!=null &&!info.getOpenid().equals("")
				&&info.getUsernmae()!=null &&!info.getUsernmae().equals("")
				&&info.getUserlogo()!=null &&!info.getUserlogo().equals("")
				&&info.getUsernumber()!=null &&!info.getUsernumber().equals("")) {
			Log.d("QQ用户："+info.getOpenid()+"\n请求绑定用户名："+info.getUsernumber()+
					"\n传入用户昵称信息："+info.getUsernmae()+
					"\n传入用户头像信息(Base64编码后的字符串长度)："+info.getUserlogo().length());
			//插入openid到qquser表
			boolean isInsetQQUserOk = InsetQQUserInfo(info.getOpenid());
			//插入用户信息到注册表
			if (isInsetQQUserOk) {
				Log.d("openid插入成功");
				boolean isInsetUserOK = InsertUserInfo(info.getOpenid(),info.getUsernumber(),info.getUsernmae(),info.getUserlogo());
				if (isInsetUserOK) {
					Log.d("用户绑定成功,开始登陆");
					//登陆成功后返回值，失败也返回值
					login(info.getUsernumber(),"123456");
				}else {
					String sql = "delete from qquser where openid='"+info.getOpenid()+"'";
					try {
						;
						if (statement.executeUpdate(sql)>-1) {
							
						
						printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_DB_ERROR,
								"注册失败，用户名重复", null)));
						}else {
							printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_DB_ERROR,
									"注册失败，数据库异常", null)));
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.d("opeid删除失败");
					}
					
				}
			}else {
				printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_DB_ERROR,
						"注册失败，请稍后再试", null)));
			}
		}else {
			printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_NULL,
					"传入参数为空", null)));
		}
	}
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
				
				if (password.equals(pasword)) {
					String sessionId = String.valueOf(new Date().getTime());
					Log.d("用户使用QQ注册并接着登陆，生成sessionId=" + sessionId);
					if (sessionid==null ||sessionid.equals("")) {
						//用户没有登陆的状态
						String UPDATA_SESSIONID_SQL="update registioninfo set sessionid='"+sessionId+"'where usernumber='"+usernumber+"'";
						int isOk=statement.executeUpdate(UPDATA_SESSIONID_SQL);
						if (isOk>=0) {
							Log.d("sessionid 写入成功");
							printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_SUCCESS,
									"登陆成功", 
									new RespData(sessionId,null,usernumber,username,
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
	private boolean InsertUserInfo(String openid,String usernumber, String usernmae,
			String userlogo) {
		boolean isok = false;
		//根据传入的用户头像（Base64编码后的字符串）
		//生成本地保存的用户头像路劲并将其存储，再生成该存储地址的网络访问路径
		boolean isRegisterOK=register(openid,usernumber,usernmae,userlogo);
			if (isRegisterOK) {
				//将图片写入路径
				boolean isUpdateOK=updataUserLogo(usernumber,userlogo);
				if (isUpdateOK) {
					isok=true;
				}
			}
		return isok;
	}
	
	private boolean updataUserLogo(String usernumber, String userlogo) {
		boolean isok = false;
		String sql = "select * from registioninfo where usernumber='"+usernumber+"'";
		try {
			ResultSet set = statement.executeQuery(sql);
			if (set.first()) {
				String logoPath=set.getString("userlogo");
				boolean isSetUserLogoOK=GenerateImage(userlogo,logoPath);
				if (isSetUserLogoOK) {
					isok=true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isok;
	}

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
            return true;  
        }   
        catch (Exception e)   
        {  
            return false;  
        }  
    }  
	private boolean register(String openid,String usernumber,String usernmae,String userlogo) {
boolean isOK=false;
		String sql = "select usernumber from registioninfo where usernumber = '"+usernumber+"'";
		String defultPassword="123456";
		String selectSQL = "insert into registioninfo(usernumber,password,openid)values('"+
		usernumber+"','"+defultPassword+"','"+openid+"')";
		try {
			ResultSet set = statement.executeQuery(sql);
			if (!set.first()) {
				int isInsertOk=statement.executeUpdate(selectSQL);
				if (isInsertOk>-1) {
					//为用户设置默认参数
					SetUserDefultConfig defultConfig =setUserDefultConfit(usernumber,usernmae);
					if (defultConfig.isOK()) {
						isOK=true;
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isOK;
	}
	private SetUserDefultConfig setUserDefultConfit(String usernumber,String username) {
		SetUserDefultConfig config = new SetUserDefultConfig();
		String logopath=Util.PATH_FILE_USERLOGO+usernumber+".jpg";
		String clientGetUserLogoUrl=Util.URL_USERLOGO+usernumber+".jpg";
		String userId=readTxtFile(Util.PATH_FILE_USERID);
//		String userId = 
		String sql = "update registioninfo set username='"+"qq用户"+username+"',userlogo='"+logopath
				+"',userid='"+userId+"',userlogourl='"+clientGetUserLogoUrl+"' where usernumber='"+
				usernumber+"'";
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
	private boolean InsetQQUserInfo(String openid) {
		boolean isok =false;
		String sql = "insert into qquser(openid) values('"+openid+"')";
		try {
			int isOk=statement.executeUpdate(sql);
			if (isOk>=0) {
				isok=true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isok;
	}

	private QQUserInfo getParms(HttpServletRequest req) throws UnsupportedEncodingException {
		QQUserInfo userInfo  =new QQUserInfo();
		String userName=null;
			userName=req.getParameter("username");
		
		userInfo.setOpenid(req.getParameter("openid"));
		userInfo.setUsernmae(userName);
		userInfo.setUserlogo(req.getParameter("userlogo"));
		userInfo.setUsernumber(req.getParameter("usernumber"));
//		Log.d("QQ用户："+userInfo.getOpenid()+"\n请求绑定用户名："+userInfo.getUsernumber()+
//				"\n传入用户昵称信息："+userInfo.getUsernmae()+
//				"\n传入用户头像信息(Base64编码后的字符串长度)："+userInfo.getUserlogo().length());
		
		return userInfo;
	}
	private Statement connectDB() {
		return JDBCUtil.getInstance();
	}
}
