/*
 * ������: 2003/12/22
 *
 */
package runtime;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * �ʰ��ǥ��饹�������ե����뤫���ɤ߹��ࡣ�ʤ�����ʤ�
 * 
 * @author hara
 */
public class FileClassLoader extends ClassLoader {
	// �ɤ߹���ѥ� : File -> null   ��ʣ��ʤ��������ΤǤ���������
	static Map path = new HashMap();
	static {
		path.put(new File("."), null);
	}
	
	/**
	 * ���饹�ѥ����ɲä���
	 * @param thePath
	 */
	public static void addPath(File thePath) {
		path.put(thePath, null);
	}
	
	/**
	 * ���饹���ɤ߹�����֤�
	 * @param className ���饹̾
	 */
	public Class findClass(String className) {
//		System.out.println("TRY   to load " + className);
		for(Iterator i=path.keySet().iterator();i.hasNext();) {
			File path = (File)i.next();
			try {
				File filename = new File(path + "/" + className + ".class");
//				System.out.println("path "+path);
//				System.out.println("FILE "+filename);
				FileInputStream fi = new FileInputStream( filename );
				byte[] buf = new byte[fi.available()];
				int len = fi.read(buf);
				//Env.p("FileClassLoader : "+buf);
				return defineClass(InlineUnit.className(className), buf, 0, len);
			} catch (Exception e) {
//				e.printStackTrace();
			}
		}
//		System.out.println("FAILED to load " + className);
		return null;
	}
}
