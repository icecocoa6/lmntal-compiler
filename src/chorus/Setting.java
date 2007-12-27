package chorus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import util.Util;

/**
 * ����ե����뤫��Ƽ�������ɤ߹���
 * @author nakano
 *
 */
public class Setting {
	private static HashMap settingMap = new HashMap();
	private static HashMap atomSize = new HashMap();
	private static HashMap linkLength = new HashMap();
	private static HashMap atomColor = new HashMap();
	
	final static 
	private String FILE_NAME = "../../chorus.conf";
	
	static{
		try {
			getSetting(new FileReader(FILE_NAME));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * ���ܤ��ͤ��������
	 * @param key ����̾
	 * @return ���ܤ��б�������
	 */
	public static String getValue(String key){
		if(!settingMap.containsKey(key)){
			Util.errPrintln("����\"" + key + "\"��¸�ߤ��ޤ���");
			System.exit(0);
		}
		return (String)settingMap.get(key);
	}
	
	public static String tryGetValue(String key){
		if(!settingMap.containsKey(key)){
			return null;
		}
		return (String)settingMap.get(key);
	}
	
	/**
	 * ���ܤ��ͤ��������
	 * ��������ݤ��ͤ�ѥ��Ȳ�ᤷ�ơ�¸�ߤ���ѥ����ɤ���������å����롥
	 * @param key������̾
	 * @return�����ܤ��б�������
	 */
	public static String getFilePass(String key){
		if(!settingMap.containsKey(key)){
			Util.errPrintln("����\"" + key + "\"��¸�ߤ��ޤ���");
			System.exit(0);
		}
		if(!(new File(((String)settingMap.get(key)).replaceAll("\\\\",""))).exists()){
			Util.errPrintln(((String)settingMap.get(key)).replaceAll("\\\\","")+"����¸�ߤ��ޤ���");
			System.exit(0);
		}
		return (String)settingMap.get(key);
		
	}
	
	/**
	 * file2����ߤ�file1�����Х��ɥ쥹���֤���
	 * �����Υǥ��쥯�ȥ���ڤ��"\"��
	 * ��̤Υǥ��쥯�ȥ�ζ��ڤ��"/"��
	 * @param file1
	 * @param file2
	 * @return
	 */
	public static String getRelativeAddress(String file1, String file2){
		File f1 = new File(file1);
		File f2 = new File(file2 + ".java");
		if(!f1.exists()){
			Util.errPrintln(file1+"����¸�ߤ��ޤ���");
			System.exit(0);
		}
		if(!f2.exists()){
			Util.errPrintln(file2+"����¸�ߤ��ޤ���");
			System.exit(0);
		}
		StringBuffer s1 = new StringBuffer(f1.getAbsolutePath());
		StringBuffer s2 = new StringBuffer(f2.getAbsolutePath());
		StringBuffer result = new StringBuffer();
		
		int end = 0; // ������ʬ�ν����
		for(int i = 0; i < s2.length(); i++){
			if(end == 0 && s1.charAt(i) == s2.charAt(i)){ continue; }
			if(end == 0){ end = i; }
			if(s2.charAt(i) == '\\'){
				result.append("../");
			}
		}
		for(int i = end; i < s1.length(); i++){
			if(s1.charAt(i) != '\\'){
				result.append(s1.charAt(i));
			}else{
				result.append("/");
			}
		}
		return result.toString();
		
	}
	
	private static void getSetting(FileReader file){
		int i;
		StringBuffer s = new StringBuffer();
		String key = null;
		String value = null;
		boolean isKey = true;
		boolean isComment = false;
		try {
			while((i = file.read()) != -1){
				//�������Ƚ���
				if((char)i == '#'){
					isComment = true;
				}
				if(((char)i != '\n' && (char)i != '\r') && isComment){
					continue;
				}
				else if(((char)i == '\n' || (char)i == '\r') && isComment){
					isComment = false;
					continue;
				}
				
				// key�ɤ߹���
				if((char)i == '='){
					if(!isKey){ Util.errPrintln("����ե����뤬�����Ǥ�"); }
					key = s.toString();
					s = new StringBuffer();
					// key�ɤ߹��ߥե饰��λ
					isKey = false;
					continue;
				}
				// value�ɤ߹���
				else if((char)i == '\n' || (char)i == '\r'){
					// key�������Ǥ��Ƥ��ʤ�����˴�
					if(isKey || key == null){
						key = value = null;
						continue;
					}
					value = s.toString();
					s = new StringBuffer();
					isKey = true;
					settingMap.put(key, value);
					key = value = null;
					continue;
				}
				// ʸ������
				s.append((char)i);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getAtomSize(String atom) {
		if(atomSize.containsKey(atom)){
			return ((Float)atomSize.get(atom)).toString();
		}
		else{
			return getValue("ATOM_SIZE");
		}
	}

	public static void setAtomSize(String atom, float size) {
		atomSize.put(atom, new Float(size));
	}

	public static String getLinkLength(String atom1, String atom2) {
		if(linkLength.containsKey(atom1+","+atom2)){
			return ((Float)linkLength.get(atom1+","+atom2)).toString();
		}
		else if(linkLength.containsKey(atom2+","+atom1)){
			return ((Float)linkLength.get(atom2+","+atom1)).toString();
		}
		else{
			return getValue("LINK_LENGTH");
		}
	}

	public static void setLinkLength(String atom1, String atom2,float length) {
		linkLength.put(atom1 + "," + atom2, new Float(length));
	}

	public static String getAtomColor(String atom) {
		if(atomColor.containsKey(atom)){
			return (String)atomColor.get(atom);
		}
		else{
			return getValue("DIFFUSE_COLOR");
		}
	}

	public static void setAtomColor(String atom, float r, float g, float b) {
		atomColor.put(atom, r + "," + g + "," + b);
	}
}