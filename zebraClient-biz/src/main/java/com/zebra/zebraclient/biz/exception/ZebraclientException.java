package com.zebra.zebraclient.biz.exception;

/**
 * printerp 统一用这异常 自定义
 */
public class ZebraclientException extends RuntimeException {
	/**
     * 
     */
    private static final long serialVersionUID = -2638435419503787392L;
    
    /*** 异常status默认值为－1，普通异常：－1 */
    private int status;
	private String msg;

	public ZebraclientException() {
		this(-1);
	}

	public ZebraclientException(int status) {
		this(status, null);
	}

	public ZebraclientException(String msg){
		this(-1,msg);
	}

	public ZebraclientException(int status, String msg) {
		this.status = status;
		this.msg = msg;
	}

	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}

	public String getMsg() {
		if (msg != null) {
			return msg;
		} else {
			return getMessage();
		}
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
