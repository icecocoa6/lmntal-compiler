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
	 * õ���ѥ������ꤹ�롣���Ĥ�����
	 * @param path
	 */
	public void setClassPath(String path) {
		this.path = path;
	}
	
	/**
	 * ���ߤΥ������ѥ��ǥ��饹 cname ���ɤ߹���Ȥ�����ɤ�ʥե�����̾�ˤʤ뤫���֤�
	 * @param cname
	 * @return
	 */
	public File filename_of_class(String cname) {
		return new File(path + "/" + cname + ".class");
	}
	
	/**
	 * ���饹���ɤ߹�����֤�
	 */
	public Class findClass(String cname) {
		try {
			File filename = filename_of_class(cname);
			FileInputStream fi = new FileInputStream( filename );
			byte[] buf = new byte[fi.available()];
			int len = fi.read(buf);
			//Env.p("FileClassLoader : "+buf);
			return defineClass(cname, buf, 0, len);
		} catch (Exception e) {
		}
		return null;
	}
}
