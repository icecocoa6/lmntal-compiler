/*
 * ������: 2003/10/22
 */
package runtime;

import java.io.FileInputStream;
import java.io.SequenceInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.SecurityException;

/**
 * LMNtal �Υᥤ��
 * 
 * <pre>
 * TODO ̾���� FrontEnd �Ǥ������������
 *       �ơ�FrontEnd
 *           ��ľ�� Main
 * </pre>
 * 
 * ������: 2003/10/22
 */
public class FrontEnd {
	/**
	 * ���ƤλϤޤ�
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		FileInputStream fis = null;
		InputStream is = null;
		Reader src = null;
		
		/**
		 * TODO ���ޥ�ɥ饤����������ä���ե��������Ȥ���¹�
		 */
		for(int i = 0; i < args.length;i++){
			// ɬ��length>0, '-'�ʤ饪�ץ����
			if(args[i].charAt(0) == '-'){
				if(args[i].length() < 2){ // '-'�Τߤλ�
					System.out.println("�����ʥ��ץ����:" + args[i]);
					System.exit(-1);					
				}else{ // ���ץ��������
					switch(args[i].charAt(1)){
					case 'd':
						System.out.println("debug mode");
						break;
					case '-': // ʸ���󥪥ץ����
						if(args[i].equals("--help")){
							System.out.println("usage: FrontEnd [-d] filename");
						}else{
							System.out.println("�����ʥ��ץ����:" + args[i]);
							System.exit(-1);
						}
						break;
					default:
						System.out.println("�����ʥ��ץ����:" + args[i]);
						System.exit(-1);						
					}							
				}
			}else{ // '-'�ʳ��ǻϤޤ��Τϥե�����Ȥߤʤ�
				try{
					fis = new FileInputStream(args[i]);
				}catch(FileNotFoundException e){
					System.out.println("�ե����뤬���Ĥ���ޤ���:" + args[i]);
					System.exit(-1);
				}catch(SecurityException e){
					System.out.println("�ե����뤬�����ޤ���:" + args[i]);
					System.exit(-1);
				}
				if(is == null) is = fis;
				else is = new SequenceInputStream(is, fis); // �������ե������Ϣ��
			}
		}
		
		if(is == null){
			/* �����Ǥ���Ȥ����ʤ�������
			src = new REPL();
			 */
			//file���꤬�ʤ���Ф����Ƥ�
			REPL repl = new REPL();
			repl.run();
		}else{
			src = new BufferedReader(new InputStreamReader(is));
		}
		
		// src��ʸ���Ϥ��Ϥ���
		koubun_kaiseki(src); // ���ߡ�
		try{
			src.close();
		}catch(IOException e){
			System.out.println("�ե����뤬�������Ǥ��ޤ���");
			System.exit(-1);
		}
		// �׻��Ρ��ɤˡ�����줿������롼����Ϥ��ƸƤӽФ�
	}
	
	static void koubun_kaiseki(Reader src){
		// �����������פ������
		int i;
		while(true){
			try{
				i = src.read();
				if(i == -1) break;
				System.out.write(i);
			}catch(IOException e){
				System.out.println("file dump error");
				System.exit(-1);
			}
		}
	}
}
