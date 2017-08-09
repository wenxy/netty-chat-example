package io.netty.example.http.helloworld;

import java.util.*;

public class Datamodel {

	public int getoperator() {
		Scanner in = new Scanner(System.in); // 使用Scanner类定义对象
		System.out.println("请输入一个操作符:");
		int operator = in.nextInt();
		// in.close();
		return operator;
	}

	public int getoperand() {
		Scanner in = new Scanner(System.in); // 使用Scanner类定义对象
		System.out.println("请输入一个操作数:");
		int operand = in.nextInt();
		// in.close();
		return operand;
	}

}
