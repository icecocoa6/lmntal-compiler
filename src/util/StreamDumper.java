package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/*
 *  ���ȥ꡼��������äƥ��󥽡���˽��Ϥ��륯�饹��
 * Runtime.exec()���̥ץ����ˤ��Ƥ��ޤ��ȡ����󥽡���˽Фʤ��ʤäƥǥХå��˺���Τǡ�
 * 
 * @author nakajima
 */
public class StreamDumper implements Runnable {
	private InputStream childIn;
	private String processName;
	private String[] lines;
	private int nextLine, lineCount;
	
	public StreamDumper(String processName, InputStream in){
		this.childIn = in;
		this.processName = processName;
	}

	public void run() {
		System.out.println("StreamDumper: now starting dumping the console log of: " + processName);
	
		BufferedReader buff = new BufferedReader(new InputStreamReader(childIn));
		
		String input;
	
		while(true){
			try {
				input = buff.readLine();

				if (input == null){
					System.out.println("StreamDumper of: " + processName + " finished");
					break;
				} else {
					System.out.println(processName + ": " +  input);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}