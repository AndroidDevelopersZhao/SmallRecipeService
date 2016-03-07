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

import modle.IndexAllRecipeData;
import modle.Mode;
import modle.RespData;
import modle.ResultToApp;
import modle.StarData;
import util.JDBCUtil;
import util.Log;

public class StarOrUnStar extends HttpServlet {

	private PrintWriter printWriter;
	private Statement statement;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");
		resp.setContentType("text/html;charset=UTF-8");
		Log.d("客户端请求收藏或取消收藏"
				+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new Date()));
		printWriter=resp.getWriter();
		statement=connectDB();
		initData(req,resp);
	}
	
	private int type=-1;
	private void initData(HttpServletRequest req, HttpServletResponse resp) {
		//��ȡ����
				StarData starData = getParms(req, resp);
				if (starData.getUsernumber()!=null
						&& starData.getSessionid()!=null
						&& starData.getCom_id()!=null) {
					boolean isAuthOK=authSessionid(starData.getUsernumber(),starData.getSessionid());
					if (isAuthOK) {
						//���com_id������ݿ�
						IndexAllRecipeData allRecipeData = indexDBByCOM_ID(starData);//���ID������ݿ⣬�����������ܻ᷵��һ��IndexAllRecipeData����
						
						ResultToApp resultToApp=null;
						try {
							resultToApp = getRespData(starData,allRecipeData);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						printWriter.print(new Gson().toJson(resultToApp));
						
					}else {
						printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_LOGIN_STATE_ERROR, "验证sessionid失效", null)));
					}
				}else {
					printWriter.print(new Gson().toJson(new ResultToApp(Mode.ERRORCODE_NULL, "传入参数为空ֵ", null)));
				}
	}
	
	private ResultToApp getRespData(StarData starData,
			IndexAllRecipeData allRecipeData) throws SQLException {
		
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
												
		if ((Integer.valueOf(com_like_number))>0 && likeUser!=null &&!likeUser.equals("")) {
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
			String msg = null;
			
		if (type==1) {
			boolean isjia = false;
			StringBuffer sb=new  StringBuffer();
			try {
				String str=starUser.substring(1, starUser.length());//ȥ����һ�����ź�
				String [] sts = str.split(",");//���е��Ѿ��ղص��û�����Ϣ
				for (int i = 0; i < sts.length; i++) {
					
						if (!sts[i].equals(starData.getUsernumber())) {
							sb.append(","+sts[i]);
						}													//1 2
				}
				sb.append(","+starData.getUsernumber());
			} catch (Exception e) {
				// TODO: handle exception
				sb.append(","+starData.getUsernumber());
			}
		
//			sb.append(","+starData.getUsernumber());
			String sql_startStar = "update cominfos set star_usernumbers='"+sb.toString()+"' where com_id='"+com_id+"'";
			if (statement.executeUpdate(sql_startStar)>-1) {
				int a=Integer.valueOf(com_star_numbers);
					a++;
				String sql_add = "update cominfos set com_star_numbers='"+String.valueOf(a)+"' where com_id='"+com_id+"'";
				if (statement.executeUpdate(sql_add)>-1) {
					msg="收藏成功";
				}else {
					msg="收藏失败";
				}
			}else {
				msg="收藏失败";
			}
		}else if (type==2) {
			//ȡ���ղ�
			StringBuffer stringBuffer = new StringBuffer();
			String starUser_2=starUser.substring(1, starUser.length());
			String[] starsUsers=starUser_2.split(",");
			for (int i = 0; i <starsUsers .length; i++) {
				if (!starsUsers[i].equals(starData.getUsernumber())) {
					stringBuffer.append(","+starsUsers[i]);
				}
			}
			String sq = "update cominfos set star_usernumbers='"+stringBuffer.toString()+"' where com_id='"+com_id+"'";
			if (statement.executeUpdate(sq)>-1) {
				int a=Integer.valueOf(com_star_numbers);
				a--;
				String sql_add = "update cominfos set com_star_numbers='"+String.valueOf(a)+"' where com_id='"+com_id+"'";
				if (statement.executeUpdate(sql_add)>-1) {
					msg="取消成功";
				}else {
					msg="取消成功";
				}
			}else {
				msg="取消失败";
			}
		}
		return new ResultToApp(Mode.ERRORCODE_SUCCESS, msg, null);
	}
	
	/**
	 * ���com_id������ݿ���ݣ�
	 * @param starData app�˴����starData����
	 * @return  �����������ܻ᷵��һ��IndexAllRecipeData����
	 */
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
		
		try {
			ResultSet resultSet = statement.executeQuery(sql);
			if (resultSet.first()) {
				//�����ݴ���
				Log.d("�ò�����ݴ���");
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
				Log.d("�ò�����ݲ�����");
				String insert="insert into cominfos(com_id,com_name)value('"+com_id+"','"+com_name+"')";
				int isInsertOK = statement.executeUpdate(insert);
				Log.d(isInsertOK>=0?"��ݲ���ɹ�":"��ݲ���ʧ��");
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
			//�ô��׳��쳣��ʾ������
//			allRecipeData.setCom_id(null);//����ID
//			allRecipeData.setCom_name(null);//�������
//			allRecipeData.setCom_star_numbers(null);//�ղ�����
//			allRecipeData.setCom_like_number(null);//��������
//			allRecipeData.setCom_comment_number(null);//��������
//			allRecipeData.setStar_usernumbers(null);//�ղص������û��˺�
//			allRecipeData.setLike_usernumbers(null);//���޵������û��˺�
//			allRecipeData.setComment_usernumbers(null);//���۵������û��˺�
		}
		return allRecipeData;
	}
	/**
	 * ��֤��ǰ�û��ĵ�½״̬�Ƿ���Ч
	 * @param usernumber	�û��˺�
	 * @param sessionid		app��sessionid
	 * @return	���boolean
	 */
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
		String usernumber=null;
		String sessionid=null;
		
		com_id=req.getParameter("com_id");
		usernumber=req.getParameter("usernumber");
		sessionid=req.getParameter("sessionid");
		this.type=Integer.valueOf(req.getParameter("type"));
		starData.setCom_id(com_id);
		starData.setUsernumber(usernumber);
		starData.setSessionid(sessionid);
		
		return starData;
	}
	private Statement connectDB() {
		return JDBCUtil.getInstance();
	}	
}	
