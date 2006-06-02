package chorus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * ����ե����뤫��Ƽ�������ɤ߹���
 * @author nakano
 *
 */
public class Setting {
	private HashMap settingMap = new HashMap();
	
	final static 
	private String FILE_NAME = "../../chorus.conf";
	
	public Setting(){ 
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
	public String getValue(String key){
		if(!settingMap.containsKey(key)){
			System.err.println("����\"" + key + "\"��¸�ߤ��ޤ���");
			System.exit(0);
		}
		return (String)settingMap.get(key);
	}
	
	public String tryGetValue(String key){
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
	public String getFilePass(String key){
		if(!settingMap.containsKey(key)){
			System.err.println("����\"" + key + "\"��¸�ߤ��ޤ���");
			System.exit(0);
		}
		if(!(new File(((String)settingMap.get(key)).replaceAll("\\\\",""))).exists()){
			System.err.println(((String)settingMap.get(key)).replaceAll("\\\\","")+"����¸�ߤ��ޤ���");
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
			System.err.println(file1+"����¸�ߤ��ޤ���");
			System.exit(0);
		}
		if(!f2.exists()){
			System.err.println(file2+"����¸�ߤ��ޤ���");
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
	
	private void getSetting(FileReader file){
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
					if(!isKey){ System.err.println("����ե����뤬�����Ǥ�"); }
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
}