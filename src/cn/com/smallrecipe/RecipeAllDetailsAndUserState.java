package cn.com.smallrecipe;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import modle.IndexAllRecipeData;
import modle.Mode;
import modle.RespData;
import modle.ResultToApp;
import modle.StarData;
import util.JDBCUtil;
import util.Log;
import util.Util;

import com.google.gson.Gson;

public class RecipeAllDetailsAndUserState extends HttpServlet{

	private PrintWriter printWriter;
	private Statement statement;
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");
		resp.setContentType("text/html;charset=UTF-8");
		Log.d("客户端请求获取所有菜谱信息"
				+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new Date()));
		printWriter=resp.getWriter();
		statement=connectDB();
		initData(req,resp);
		
	}
	private void initData(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException{
		StarData starData = getParms(req, resp);
		if (starData.getUsernumber()!=null
				&& starData.getSessionid()!=null
				&& starData.getCom_id()!=null
				&& starData.getCom_name()!=null) {
			boolean isAuthOK=authSessionid(starData.getUsernumber(),starData.getSessionid());
			if (isAuthOK) {
				IndexAllRecipeData allRecipeData = indexDBByCOM_ID(starData);//���ID������ݿ⣬�����������ܻ᷵��һ��IndexAllRecipeData����
				ResultToApp resultToApp = getRespData(starData,allRecipeData);
				printWriter.print(new Gson().toJson(resultToApp));
				
			}else {
				printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_LOGIN_STATE_ERROR, "登陆状态失效", null)));
			}
		}else {
			printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_NULL, "传入参数不能为空", null)));
		}
	}
	private ResultToApp getRespData(StarData starData,
			IndexAllRecipeData allRecipeData) {
		
		String com_id = allRecipeData.getCom_id();
		String com_name=allRecipeData.getCom_name();
		String com_star_numbers=allRecipeData.getCom_star_numbers();
		String com_like_number=allRecipeData.getCom_like_number();
		String com_comment_number=allRecipeData.getCom_comment_number();
		String starUser=allRecipeData.getStar_usernumbers();
		String likeUser=allRecipeData.getLike_usernumbers();
		String commentUser=allRecipeData.getComment_usernumbers();
		
		boolean IsUserLike = true;//�û��Ƿ���Ե���
		boolean IsUserStar = true;//�û��Ƿ�����ղ�
												
		if ((Integer.valueOf(com_like_number))>0 && likeUser!=null&& !likeUser.equals("")) {
			String likeUser_2=likeUser.substring(1, likeUser.length());
			String[] likesUser=likeUser_2.split(",");
			for (int i = 0; i <likesUser.length; i++) {
				IsUserLike=!(likesUser[i].equals(starData.getUsernumber()));
			}
		}
		if ((Integer.valueOf(com_star_numbers))>0 && starUser!=null&&!starUser.equals("")) {
			String starUser_2=starUser.substring(1, starUser.length());
			String[] starsUsers=starUser_2.split(",");
			for (int i = 0; i <starsUsers.length; i++) {
				IsUserStar=!(starsUsers[i].equals(starData.getUsernumber()));
				
			}
		}
		return new ResultToApp(Mode.ERRORCODE_SUCCESS, "获取成功", new RespData(null,
				null,starData.getUsernumber(), starData.getUsernumber(), null, null, 
				com_id, com_name, com_star_numbers, com_like_number, com_comment_number, null, null, null, IsUserLike, IsUserStar));
	}
	private IndexAllRecipeData indexDBByCOM_ID(StarData starData) {
		IndexAllRecipeData allRecipeData = new IndexAllRecipeData();//���巵�ص�IndexAllRecipeData����
		
		String com_id = starData.getCom_id();
		String com_name = starData.getCom_name();
		
		allRecipeData.setCom_id(null);//����ID
		allRecipeData.setCom_name(null);//�������
		allRecipeData.setCom_star_numbers(null);//�ղ�����
		allRecipeData.setCom_like_number(null);//��������
		allRecipeData.setCom_comment_number(null);//��������
		allRecipeData.setStar_usernumbers(null);//�ղص������û��˺�
		allRecipeData.setLike_usernumbers(null);//���޵������û��˺�
		allRecipeData.setComment_usernumbers(null);//���۵������û��˺�
		//���ID����
		String sql = "select * from cominfos where com_id='"+com_id+"'";
		Log.d("com_name："+com_name);
		try {
			ResultSet resultSet = statement.executeQuery(sql);
			if (resultSet.first()) {
				//�����ݴ���
				
				// TODO ��Ŀ���------��������׻�û�б�¼��ʱ�Զ�¼�룬Ŀǰͣ�ڱ��������ϣ�����˽��յ�����������
				if (resultSet.first()) {
					allRecipeData.setCom_id(resultSet.getString("com_id"));//����ID
					allRecipeData.setCom_name(resultSet.getString("com_name"));//�������
					allRecipeData.setCom_star_numbers(resultSet.getString("com_star_numbers"));//�ղ�����
					allRecipeData.setCom_like_number(resultSet.getString("com_like_number"));//��������
					allRecipeData.setCom_comment_number(resultSet.getString("com_comment_number"));//��������
					allRecipeData.setStar_usernumbers(resultSet.getString("star_usernumbers"));//�ղص������û��˺�
					allRecipeData.setLike_usernumbers(resultSet.getString("like_usernumbers"));//���޵������û��˺�
					allRecipeData.setComment_usernumbers(resultSet.getString("comment_usernumbers"));//���۵������û��˺�
				}
			}else {
				//��ݲ�����
				
				String insert="insert into cominfos(com_id,com_name,com_star_numbers,com_like_number,com_comment_number)value('"+
				com_id+"','"+com_name+"','"+String.valueOf(Math.abs(new Random().nextInt() % 15000))+"','"+
						String.valueOf(Math.abs(new Random().nextInt() % 15000))+"','"+
				String.valueOf(Math.abs(new Random().nextInt() % 15000))+"')";
				int isInsertOK = statement.executeUpdate(insert);
				Log.d(isInsertOK>=0?"插入成功":"插入失败");
				if (isInsertOK>=0) {
					//����ɹ����������в�����Ϣ
					String sql_get_all_info = "select * from cominfos where com_id='"+com_id+"'";
					ResultSet set = statement.executeQuery(sql_get_all_info);
					if (set.first()) {
						allRecipeData.setCom_id(set.getString("com_id"));//����ID
						allRecipeData.setCom_name(set.getString("com_name"));//�������
						allRecipeData.setCom_star_numbers(set.getString("com_star_numbers"));//�ղ�����
						allRecipeData.setCom_like_number(set.getString("com_like_number"));//��������
						allRecipeData.setCom_comment_number(set.getString("com_comment_number"));//��������
						allRecipeData.setStar_usernumbers(set.getString("star_usernumbers"));//�ղص������û��˺�
						allRecipeData.setLike_usernumbers(set.getString("like_usernumbers"));//���޵������û��˺�
						allRecipeData.setComment_usernumbers(set.getString("comment_usernumbers"));//���۵������û��˺�
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return allRecipeData;
	}
	private boolean authSessionid(String usernumber, String sessionid) {
		boolean isAuthOK=false;
				//��ѯ��ݿ�
		String sql="select * from registioninfo where usernumber='"+usernumber+"'";
				try {
					ResultSet resultSet=statement.executeQuery(sql);
						if (resultSet.first()) {
							String sessionid_db=resultSet.getString("sessionid");
							if (sessionid_db!=null) {
								isAuthOK=sessionid.equals(sessionid_db);
							}
						}
						//Ĭ�Ϸ���false
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					//�쳣Ĭ�Ϸ���false
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
