package cn.com.smallrecipe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import modle.UpdataUserLogo;
import util.JDBCUtil;
import util.Log;
import util.Util;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.google.gson.Gson;

public class UpDataUserLogo extends HttpServlet {
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
		printWriter=Util.setUnicode(req, resp,"上传头像");
		statement=connectDB();
		Log.d(statement!=null?"上传头像前"+"数据库连接成功":"数据库连接失败");
		if (getParmes(req).getUsernumber()!=null
				&&getParmes(req).getSessionid()!=null
				&& getParmes(req).getUserlogo()!=null) {
			if (authSessionID(getParmes(req))) {
				savaUserLogo(getParmes(req));
			}else {
				printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_LOGIN_STATE_ERROR,
						"用户登陆状态失效", null)));
			}
		}else {
			printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_NULL,
					"头像上传失败,用户账号、sessionid、userlogo均不可为空", null)));
		}
	}
	
	private Statement connectDB() {
		return JDBCUtil.getInstance();
	}
	private void savaUserLogo(UpdataUserLogo parmes) {
		// TODO Auto-generated method stub
		Log.d("收到编码后的图片:"+parmes.getUserlogo());
		Log.d("收到编码后的图片大小:"+parmes.getUserlogo().length());
		
		String select_userlogo_url="select userlogo from registioninfo where usernumber ='"
		+parmes.getUsernumber()+"'";
		try {
			ResultSet resultSet = statement.executeQuery(select_userlogo_url);
			if (resultSet.first()) {
					String url = resultSet.getString("userlogo");
					Log.d("准备存储");
					boolean isSetUserLogoOK=GenerateImage(parmes.getUserlogo(),url);
					if (isSetUserLogoOK) {
						printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_SUCCESS,
								"头像更新成功", null)));
					}else {
						printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_UPDATE_LOGO_ERROR,
								"头像更新失败,文件存储出错", null)));
					}
						
			}else {
				Log.d("找不到该用户的url");
				printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_USERNUMBER_NOTHAVE,
						"用户不存在", null)));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_USERNUMBER_NOTHAVE,
					"用户不存在", null)));
		}
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
	private boolean authSessionID(UpdataUserLogo upLogo) {
		// TODO Auto-generated method stub
		boolean isTrue =false;
		Log.d("开始验证,usernumber="+upLogo.getUsernumber()+",sessionid="+upLogo.getSessionid());
		String sql  ="select sessionid from registioninfo where usernumber ='"+upLogo.getUsernumber()+"'";
		try {
			ResultSet resultSet = statement.executeQuery(sql);
			if (resultSet.first()) {
					String session_service = resultSet.getString("sessionid");
					if (session_service.trim().equals(upLogo.getSessionid().trim())) {
						isTrue=true;
					}else {
						isTrue=false;
					}
			}else {
				isTrue=false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isTrue=false;
		}
		return isTrue;
	}
	private UpdataUserLogo getParmes(HttpServletRequest req) {
		// TODO Auto-generated method stub
		UpdataUserLogo upLogo = new UpdataUserLogo();
		 String usernumber = null;
		 String sessionid = null;
		 String userlogo = null;
		try {
			usernumber=req.getParameter("usernumber");
			sessionid=req.getParameter("sessionid");
			userlogo=req.getParameter("userlogo");
			
			upLogo.setUsernumber(usernumber);
			upLogo.setSessionid(sessionid);
			upLogo.setUserlogo(userlogo);
		} catch (Exception e) {
			// TODO: handle exception
			upLogo=null;
		}
		
		return upLogo;
	}

}
