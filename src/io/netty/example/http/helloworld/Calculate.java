package io.netty.example.http.helloworld;

import freemarker.template.*;
import io.netty.example.http.controller.dto.AdditionDto;

import java.util.*;
import java.io.*;

public class Calculate {
	public static void main(String[] args) throws Exception {

		/*
		 * ---------------------------------------------------------------------
		 * ---
		 */
		/* You should do this ONLY ONCE in the whole application life-cycle: */

		/* Create and adjust the configuration singleton */
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
		cfg.setDirectoryForTemplateLoading(new File("html"));
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);

		/*
		 * ---------------------------------------------------------------------
		 * ---
		 */
		/*
		 * You usually do these for MULTIPLE TIMES in the application
		 * life-cycle:
		 */

		/* Create a data-model */

		/*Datamodel instance = new Datamodel();

		int operator = instance.getoperator();
		int num1 = instance.getoperand();
		int num2 = instance.getoperand();

		int result = 0;
		switch (operator) {
		case 0: {
			result = num1 + num2;
			System.out.println(num1 + num2);
			break;
		}
		case 1: {
			result = num1 - num2;
			System.out.println(num1 - num2);
			break;
		}
		case 2: {
			result = num1 * num2;
			System.out.println(num1 * num2);
			break;
		}
		case 3: {
			result = num1 / num2;
			System.out.println(num1 / num2);
			break;
		}
		}*/

		/*Map root = new HashMap();
		root.put("firstoperand", num1);
		root.put("secondoperand", num2);
		root.put("operator", operator);
		root.put("result", result);*/

		/* Get the template (uses cache internally) */
		Map root = new HashMap();
		Template temp = cfg.getTemplate("index.html","UTF-8");
		AdditionDto dto = new AdditionDto();
		dto.setDesc("测试");
		dto.setResult(100);
		
		root.put("dto",dto);
		
		/* Merge data-model with template */
		Writer out = new OutputStreamWriter(System.out);
		temp.process(dto, out);

		// Note: Depending on what `out` is, you may need to call `out.close()`.
		// This is usually the case for file output, but not for servlet output.
	}
}
