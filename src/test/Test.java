/*
 * ������: 2004/01/26
 *
 */
package test;


import runtime.Env;

/**
 * �狼�ä����Ȣ� path.separator = (linux => : windows => ;)
 * 
 * @author hara
 *
 */
public class Test {

	public static void main(String[] args) {
		java.util.Enumeration e = System.getProperties().keys();
		while(e.hasMoreElements()) {
			String o = (String)e.nextElement(); 
			Env.p(o+"  =>  "+System.getProperty(o));
		}
	}
}
