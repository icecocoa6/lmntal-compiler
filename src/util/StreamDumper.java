package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

/*
 *  ���ȥ꡼��������äƥ��󥽡���˽��Ϥ��륯�饹��
 * Runtime.exec()���̥ץ����ˤ��Ƥ��ޤ��ȡ����󥽡���˽Фʤ��ʤäƥǥХå��˺���Τǡ�
 * 
 * @author nakajima
 *
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
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		System.out.println("StreamDumper: now starting dumping the console log of: " + processName);
		
		String input;
		
		while(true){
			try {
				input = readLine();

				if (input == null){
				} else {
					System.out.println(processName + " : " +  input);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//pakuri from mizuno-kun's HybridInputStream
	/**
	 * ���ȥ꡼�फ�顢����ʬ��ʸ����ǡ������ɤ߼��ޤ���
	 * @return �ɤ߼�ä��ǡ��������ȥ꡼��ν�����ã���Ƥ�������null
	 * @throws IOException �����ϥ��顼��ȯ��������硣
	 */
	public synchronized String readLine() throws IOException {
		if (lines == null || nextLine == lineCount) {
			byte[] bytes = readBytes();
			if (bytes == null) {
				return null;
			}
			lines = new String(bytes).split("\n", -1);
			nextLine = 0;
			lineCount = lines.length;
			if (lines[lineCount-1].equals("")) {
				//���Ԥǽ���äƤ����硢�Ǹ�β��Ԥθ��̵��
				lineCount--;
			}
		}
		return lines[nextLine++];
	}
	public synchronized byte[] readBytes() throws IOException {
//		if (lines != null && nextLine < lineCount) {
//			//̤�ɤ߹��ߤ�ʸ����ǡ������ĤäƤ���
//			throw new IOException();
//		}

		ArrayList data = new ArrayList();
		
		byte[] retByteArr;
		
		byte ret;

		while(true){
			ret = (byte)childIn.read();
			if(ret == -1 ) break;
			data.add(new Byte(ret));
		}
		
		if(data.size() == 0){
			return null;
		}
		
		retByteArr = new byte[data.size()];
		
		
		for ( int i = 0; i <  data.size();  i++ ){
			retByteArr[i] = ((Byte)data.get(i)).byteValue();
		}
		
		return retByteArr;
	}
}