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
 *
 */
public class FileClassLoader extends ClassLoader {
	public Class findClass(String name) {
		try {
			FileInputStream fi = new FileInputStream( name + ".class" );
			byte[] buf = new byte[fi.available()];
			int len = fi.read(buf);
			//Env.p("FileClassLoader : "+buf);
			return defineClass(name, buf, 0, len);
		} catch (Exception e) {
		}
		return null;
	}
}
