package modle;

public class Mode {
	public static final int ERRORCODE_SUCCESS=9000;//操作成功 ..
	public static final int ERRORCODE_USER_NULL=-1;//用户名或密码为空 ..
	public static final int ERRORCODE_WRITE_SESSION_ERROR=-2;//sessionid写入失败 ..
	public static final int ERRORCODE_USER_LOGINED=-3;//用户已经登陆 ..
	public static final int ERRORCODE_PASSWORD_ERROR=-4;//密码错误 ..
	public static final int ERRORCODE_USERNUMBER_NOTHAVE=-5;//用户账号不存在..
	public static final int ERRORCODE_RELOGINERROR=-6;//reloginid或用户名为空
	public static final int ERRORCODE_AUTH_RELOGINID_ERROR=-7;//reloginid校验失败
	public static final int ERRORCODE_WRITE_RELOGINID_ERROR=-8;//写入reloginid失败
	public static final int ERRORCODE_USER_NOT_GETED_RELOGINID=-9;//登陆失败,传入的用户名没有获取过ReLoginId
	public static final int ERRORCODE_USER_WAS_SAVED=-10;//用户已存在
	public static final int ERRORCODE_DB_ERROR=-11;//数据库异常
	public static final int ERRORCODE_UNLOGIN_ERROR=-12;//账号、密码、sessionId至少有一项为空
	public static final int ERRORCODE_LOGIN_STATE_ERROR=-13;//登陆状态失效
	public static final int ERRORCODE_CLEAR_SESSIONID_ERROR=-14;//退出时清理sessionId失败
	public static final int ERRORCODE_USER_NOT_LOGIN=-15;//用户未登陆
	public static final int ERRORCODE_NULL=-16;//传入参数为空
	public static final int ERRORCODE_DB_EXQUT=-17;//数据库语句执行异常
	public static final int ERRORCODE_AUTH_SESSIONID_ERROR=-18;//数据库语句执行异常
	public static final int ERRORCODE_UPDATE_LOGO_ERROR=-19;//头像跟新失败
	public static final int ERRORCODE_USERLOGO_NOT_HAVE=-20;//头像不存在
	public static final int ERRORCODE_QQUSER_EXIT=-21;//该QQ用户不存在
	public static final int ERRORCODE_WXUSER_EXIT=-22;//该微信用户不存在
	
	
	
	
	
	
}
