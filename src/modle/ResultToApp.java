package modle;

public class ResultToApp {
	private int errorCode ;
	private String resultMsg = null;
	private RespData respData=null;
	
	public ResultToApp(int errorCode,String resultMsg,RespData respData) {
		// TODO Auto-generated constructor stub
		this.errorCode=errorCode;
		this.resultMsg=resultMsg;
		this.respData=respData;
		
		
	}
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public String getResultMsg() {
		return resultMsg;
	}
	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}
	public RespData getRespData() {
		return respData;
	}
	public void setRespData(RespData respData) {
		this.respData = respData;
	}
	
}
