package io.netty.example.http.route;

public class RouteInfo {

	private String method;
	private String path;
	private String clz;
	
	public RouteInfo(String method,String path,String clz){
		this.method = method;
		this.path = path;
		this.clz = clz;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getClz() {
		return clz;
	}

	public void setClz(String clz) {
		this.clz = clz;
	}
	 
	
}
