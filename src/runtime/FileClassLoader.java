/*
 * ������: 2003/12/22
 *
 */
package runtime;

import java.io.*;

/**
 * �ʰ��ǥ��饹�������ե����뤫���ɤ߹��ࡣ�ʤ�����ʤ�
 * 
 * @author hara
 */
public class FileClassLoader extends ClassLoader {
	String path;
	
	/**
	 * ���饹���ɤ߹�����֤�
	 */
	public Class findClass(String uname) {
		try {
			File filename = InlineUnit.classFile(uname);
			FileInputStream fi = new FileInputStream( filename );
			byte[] buf = new byte[fi.available()];
			int len = fi.read(buf);
			//Env.p("FileClassLoader : "+buf);
			return defineClass(InlineUnit.className(uname), buf, 0, len);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
