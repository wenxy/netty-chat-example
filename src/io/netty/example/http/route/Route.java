package io.netty.example.http.route;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 路由管理-单例
 * @author fish
 *
 */
public class Route {
	
	 private Map<String,RouteInfo> routes = new HashMap<String,RouteInfo>();
	 
	 private static class RouteHolder { 
		 private static final Route instance = new Route();
	 }
	 
	 private Route(){
		 BufferedReader br = null;
		 try {
			br = new BufferedReader(new FileReader("conf/route"));
			String line = null;
			while((line =  br.readLine()) != null){
				String[] oneRouteItems = line.split("\\s+");
				if(oneRouteItems == null || oneRouteItems.length !=3 ) continue;
				RouteInfo ri = new RouteInfo(oneRouteItems[0],oneRouteItems[1],oneRouteItems[2]);
				routes.put(oneRouteItems[1], ri);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			 try {
				if(br != null)br.close();
			} catch (IOException e) { 
				e.printStackTrace();
			}
		}
	 }
	 
	 public static Route getInstance() {
	     return RouteHolder.instance;
	 }
	 
	 public RouteInfo getByUri(String path){
		 return routes.get(path);
	 }
}
