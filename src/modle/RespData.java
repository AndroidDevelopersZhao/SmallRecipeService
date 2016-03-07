package modle;

import java.io.Serializable;

public class RespData implements Serializable{
	private String sessionId = null;
	private String reLoginId = null;
	private String username = null;
	private String userid = null;
	private String userlogo = null;
	
	//菜谱信息
	private String com_id = null;
	private String com_name=null;
	private String com_star_numbers=null;
	private String com_like_number=null;
	private String com_comment_number=null;
	private String starUser=null;
	private String likeUser=null;
	private String commentUser=null;
	
	private boolean IsUserLike = false;//用户是否可以点赞
	private boolean IsUserStar = false;//用户是否可以收藏
	private String usernumber=null;
	
	
	public String getUsernumber() {
		return usernumber;
	}
	public void setUsernumber(String usernumber) {
		this.usernumber = usernumber;
	}
	public RespData(String sessionId, String reLoginId, String usernumber,String username,
			String userid, String userlogo, String com_id, String com_name,
			String com_star_numbers, String com_like_number,
			String com_comment_number, String starUser, String likeUser,
			String commentUser, boolean isUserLike, boolean isUserStar) {
		super();
		this.sessionId = sessionId;
		this.reLoginId = reLoginId;
		this.username = username;
		this.userid = userid;
		this.userlogo = userlogo;
		this.com_id = com_id;
		this.com_name = com_name;
		this.com_star_numbers = com_star_numbers;
		this.com_like_number = com_like_number;
		this.com_comment_number = com_comment_number;
		this.starUser = starUser;
		this.likeUser = likeUser;
		this.commentUser = commentUser;
		IsUserLike = isUserLike;
		IsUserStar = isUserStar;
		this.usernumber=usernumber;
	}
	public String getCom_id() {
		return com_id;
	}
	public void setCom_id(String com_id) {
		this.com_id = com_id;
	}
	public String getCom_name() {
		return com_name;
	}
	public void setCom_name(String com_name) {
		this.com_name = com_name;
	}
	public String getCom_star_numbers() {
		return com_star_numbers;
	}
	public void setCom_star_numbers(String com_star_numbers) {
		this.com_star_numbers = com_star_numbers;
	}
	public String getCom_like_number() {
		return com_like_number;
	}
	public void setCom_like_number(String com_like_number) {
		this.com_like_number = com_like_number;
	}
	public String getCom_comment_number() {
		return com_comment_number;
	}
	public void setCom_comment_number(String com_comment_number) {
		this.com_comment_number = com_comment_number;
	}
	public String getStarUser() {
		return starUser;
	}
	public void setStarUser(String starUser) {
		this.starUser = starUser;
	}
	public String getLikeUser() {
		return likeUser;
	}
	public void setLikeUser(String likeUser) {
		this.likeUser = likeUser;
	}
	public String getCommentUser() {
		return commentUser;
	}
	public void setCommentUser(String commentUser) {
		this.commentUser = commentUser;
	}
	public boolean isIsUserLike() {
		return IsUserLike;
	}
	public void setIsUserLike(boolean isUserLike) {
		IsUserLike = isUserLike;
	}
	public boolean isIsUserStar() {
		return IsUserStar;
	}
	public void setIsUserStar(boolean isUserStar) {
		IsUserStar = isUserStar;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getUserlogo() {
		return userlogo;
	}
	public void setUserlogo(String userlogo) {
		this.userlogo = userlogo;
	}
	public String getReLoginId() {
		return reLoginId;
	}
	public void setReLoginId(String reLoginId) {
		this.reLoginId = reLoginId;
	}
	
	
//	public RespData(String sessionId,String reLoginId,String username,String userid,String userlogo) {
//		// TODO Auto-generated constructor stub
//		this.sessionId=sessionId;
//		this.reLoginId=reLoginId;
//		this.username=username;
//		this.userid=userid;
//		this.userlogo=userlogo;
//		
//	}
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
}
