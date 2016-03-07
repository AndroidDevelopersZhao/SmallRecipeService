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

import modle.IndexAllRecipeData;
import modle.Mode;
import modle.ResultToApp;
import modle.StarData;
import util.JDBCUtil;
import util.Log;
import util.Util;

import com.google.gson.Gson;

public class Star extends HttpServlet{

	private PrintWriter printWriter;
	private Statement statement;
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Log.d(req.getParameter("com_name"));
		initData(req,resp);
		/**
		 * 数据库测试块
		 */
//		{
//		Statement statement = connectDB();
//				try {
//					Log.d("1");
//					StringBuffer sb = new StringBuffer();
//					for (int i = 0; i < 68536; i++) {
//						sb.append(",15221340931");
//						Log.d("追加第"+i+"个");
//					}
//					
//					statement.executeUpdate("update cominfos set star_usernumbers='15221340931"+sb.toString()+"' where com_id=1");
//					Log.d("2");
//					ResultSet set = statement.executeQuery("select * from cominfos where com_id=1");
//					if (set.first()) {
//						String ss = set.getString("star_usernumbers");
//						String [] a=ss.split(",");
//						Log.d("a的大小"+a.length);
//					}
//					
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//		}
		
		
		
		
		
//		printWriter.print("----需要传入菜谱ID（聚合返回）、菜谱名称、用户账号（usernumber）、-----");
//		巨灵---
//		接收到参数后，首先验证该用户的登陆状态是否有效，
//		验证成功后，根据传入菜谱ID去cominfos表查询该条目下的所有内容（当
//		菜谱ID不存在时执行sql插入语句，插入成功后再次执行ID索引，索引成功后
//		根据resultset拿到所有收藏该菜谱的用户，做append，最后update该表里面
//		的所有收藏该菜谱的用户字段，update执行成功后再对该商品的收藏数量++，最后返回收藏结果）；
		
	}
	private void initData(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException{
		printWriter=Util.setUnicode(req, resp,"收藏商品");
		statement=connectDB();
		Log.d(statement!=null?"收藏商品前"+"数据库连接成功":"数据库连接失败");
		//获取参数
		StarData starData = getParms(req, resp);
		Log.d("传入的用户信息：\ncom_id="+starData.getCom_id()+"\ncom_name="+starData.getCom_name()+
				"\nusernumber="+starData.getUsernumber()+"\nsessionid="+starData.getSessionid());
		if (starData.getUsernumber()!=null
				&& starData.getSessionid()!=null
				&& starData.getCom_id()!=null
				&& starData.getCom_name()!=null) {
			//验证sessionid是否有效
			boolean isAuthOK=authSessionid(starData.getUsernumber(),starData.getSessionid());
			if (isAuthOK) {
				/*
				 * 根据菜谱ID索引该ID对应的所有收藏的所有用户和该ID对应的所有收藏数量，索引
				 * 异常时插入数据，成功时将所有用户信息做append，对应的收藏数量做++，然后更新数据库表，更新成功后索引该ID对应的数量返回app
				 * 
				 */
				//根据com_id索引数据库
				IndexAllRecipeData allRecipeData = indexDBByCOM_ID(starData);//根据ID索引数据库，不管索引结果，总会返回一个IndexAllRecipeData对象
				
			}else {
				printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_LOGIN_STATE_ERROR, "登陆状态失效，请重新登陆", null)));
			}
		}else {
			printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_NULL, "参入参数有空值", null)));
		}
	}
	/**
	 * 根据com_id索引数据库数据，
	 * @param starData app端传入的starData对象
	 * @return  不管索引结果，总会返回一个StarData对象
	 */
	private IndexAllRecipeData indexDBByCOM_ID(StarData starData) {
		IndexAllRecipeData allRecipeData = new IndexAllRecipeData();//定义返回的IndexAllRecipeData对象
		
		String com_id = starData.getCom_id();
		String com_name = starData.getCom_name();
		String usernumber=starData.getCom_name();
		
		//根据ID索引
		String sql = "select * from cominfos where com_id='"+com_id+"'";
		
		try {
			ResultSet resultSet = statement.executeQuery(sql);
			if (resultSet.first()) {
				//如果数据存在
				Log.d("该菜谱数据存在");
				// TODO 项目进度------当检测待菜谱还没有被录入时自动录入，目前停在编码问题上，服务端接收到的中文乱码
			}else {
				//数据不存在
				Log.d("该菜谱数据不存在");
				String insert="insert into cominfos(com_id,com_name)value('"+com_id+"','"+com_name+"')";
				int isInsertOK = statement.executeUpdate(insert);
				Log.d(isInsertOK>=0?"数据插入成功":"数据插入失败");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			//该处抛出异常表示语句错误
			allRecipeData.setCom_id(null);//菜谱ID
			allRecipeData.setCom_name(null);//菜谱名称
			allRecipeData.setCom_star_numbers(null);//收藏人数
			allRecipeData.setCom_like_number(null);//点赞人数
			allRecipeData.setCom_comment_number(null);//评论人数
			allRecipeData.setStar_usernumbers(null);//收藏的所有用户账号
			allRecipeData.setLike_usernumbers(null);//点赞的所有用户账号
			allRecipeData.setComment_usernumbers(null);//评论的所有用户账号
		}
		return allRecipeData;
	}
	/**
	 * 验证当前用户的登陆状态是否有效
	 * @param usernumber	用户账号
	 * @param sessionid		app端sessionid
	 * @return	结果boolean
	 */
	private boolean authSessionid(String usernumber, String sessionid) {
		boolean isAuthOK=false;
				//查询数据库
		String sql="select * from registioninfo where usernumber='"+usernumber+"'";
				try {
					ResultSet resultSet=statement.executeQuery(sql);
						if (resultSet.first()) {
							String sessionid_db=resultSet.getString("sessionid");
							if (sessionid_db!=null) {
								isAuthOK=sessionid.equals(sessionid_db);
							}
						}
						//默认返回false
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					//异常默认返回false
				}
		return isAuthOK;
	}
	private StarData getParms(HttpServletRequest req, HttpServletResponse resp) {
		StarData starData = new StarData();
		String com_id=null;
		String com_name=null;
		String usernumber=null;
		String sessionid=null;
		
		com_id=req.getParameter("com_id");
		com_name=req.getParameter("com_name");
		usernumber=req.getParameter("usernumber");
		sessionid=req.getParameter("sessionid");
		
		starData.setCom_id(com_id);
		starData.setCom_name(com_name);
		starData.setUsernumber(usernumber);
		starData.setSessionid(sessionid);
		
		return starData;
	}
	private Statement connectDB() {
		return JDBCUtil.getInstance();
	}
	
}
