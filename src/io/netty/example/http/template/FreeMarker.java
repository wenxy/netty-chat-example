package io.netty.example.http.template;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
/**
 * 定义FreeMark渲染模板
 * @author fish
 *
 */
public class FreeMarker {

	private static final String charset = "utf-8";
 	
	/**
	 * 定义FreeMarker构造函数
	 * 
	 */
	public FreeMarker(){
		 
	}
	
	/**
	 * 将模板根据对象渲染成字符串返回
	 * @param template
	 * @param object
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public  String renderToString(File template,Object object) throws IOException, TemplateException{
		
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
		cfg.setDirectoryForTemplateLoading(template.getParentFile());
		cfg.setDefaultEncoding(charset);
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
		
		String templateName = template.getName();
		Template temp = cfg.getTemplate(templateName,charset);
		
		StringWriter writer = new StringWriter();
		temp.process(object, writer);
		
		return writer.toString(); 
	}
	
	/**
	 * 将模板根据对象渲染成字节数组返回
	 * @param template
	 * @param object
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public  byte[] renderToByte(File template,Object object) throws IOException, TemplateException{
		String tmp = renderToString(template,object);
		return tmp.getBytes(charset);
	}
	
	/**
	 * 重载
	 * 将模板根据对象渲染成字符串返回 
	 * @param templatePath
	 * @param object
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public  String renderToString(String templatePath,Object object) throws IOException, TemplateException{
		File file = new File(templatePath);
		return renderToString(file,object);
	}
	
	/**
	 * 重载
	 * 将模板根据对象渲染成字节数组返回 
	 * @param templatePath
	 * @param object
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public  byte[] renderToByte(String templatePath,Object object) throws IOException, TemplateException{
		String tmp = renderToString(templatePath,object);
		return tmp.getBytes(charset);
	} 
	
	/*public static void main(String[] args){
		System.out.println(new File("html/result/result.html").getParentFile());
	}*/
}
