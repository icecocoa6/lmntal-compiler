package util;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * �Х������ʸ�����ξ���򣱤ĤΥ��ȥ꡼�फ���ɤ߼�뤿��Υ��饹��
 * 
 * @author Mizuno
 */
public class HybridInputStream {
	private InputStream in;
	private String[] lines;
	private int nextLine, lineCount;

	/**
	 * ���ꤵ�줿���ȥ꡼�फ��ǡ������ɤ߹��ि��Υ��󥹥��󥹤��������ޤ���
	 * @param in �ǡ������ɤ߹���InputStream
	 * @throws IOException �����ϥ��顼��ȯ��������硣
	 */
	public HybridInputStream(InputStream in) throws IOException {
		this.in = in;
	}

	/**
	 * ���ȥ꡼�फ�饪�֥������Ȥ��ɤ߼��ޤ���
	 * @return �ɤ߼�ä����֥�������
	 * @throws IOException �����ϥ��顼��ȯ��������硣�ɤ߼�ä��ǡ�����ʸ������ä�����ޤࡣ
	 * @throws ClassNotFoundException �ɤ߼�ä����֥������ȤΥ��饹�����Ĥ���ʤ��ä���硣
	 */
	public synchronized Object readObject() throws IOException, ClassNotFoundException {
		if (lines != null && nextLine < lineCount) {
			//̤�ɤ߹��ߤ�ʸ����ǡ������ĤäƤ���
			throw new IOException();
		}
		byte[] bytes = readBytes();
		if (bytes == null) {
			return null;
		}
		if (in.read() != '\n') {
			throw new IOException("\\n is expected after Object data");
		}
		ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
		ObjectInputStream oin = new ObjectInputStream(bin);
		return oin.readObject();
	}

	/**
	 * ���ȥ꡼����Ĥ��ޤ���
	 * @throws IOException �����ϥ��顼��ȯ��������硣
	 */
	public synchronized void close() throws IOException {
		System.out.println("HybridInputStream.close() entered");
		in.close();
	}

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

	/**
	 * ���ȥ꡼�फ�顢���ĤΥǡ�����ɽ���Х�������ɤ߹��ߤޤ���
	 * �֣��ĤΥǡ����פȤϡ�HybridOutputStream���饹��write, writeObject, writeBytes�Τ����줫�Υ᥽�åɤ��Ѥ��ƣ���ǽ񤭹�����ǡ����Ǥ���
	 * @return �ɤ߼�ä��Х�����
	 * @throws IOException �����ϥ��顼��ȯ��������硣
	 */
	public synchronized byte[] readBytes() throws IOException {
		if (lines != null && nextLine < lineCount) {
			//̤�ɤ߹��ߤ�ʸ����ǡ������ĤäƤ���
			throw new IOException();
		}
		int size = readInt();
		if (size == -1) {
			return null;
		}
		byte[] data = new byte[size];
		//in.read���ɤ߹��ߤ��֥�å�������������äƤ��Ƥ��ޤ��Τǡ�
		//�Ǹ�ޤ��ɤि��˥롼�פ�ޤ魯ɬ�פ����롣
		int index = 0;
		while (size != 0) {
			int count = in.read(data, index, size);
			size -= count;
			index += count;
		}
		return data;
	}
	/* readBytes����ǤΤ߸Ƥ֤Τ�syncronized�Ǥʤ��Ƥ��ɤ��Ϥ�������ǰ�Τ���*/
	private synchronized int readInt() throws IOException {
		int a1 = in.read();
		if (a1 == -1) {
			//���Υ᥽�åɤ��ɤ�ǡ����ϥХ��ȿ���ɽ���ͤʤΤǡ���ˤʤ���Ϥʤ�
			return -1;
		}
		int a2 = in.read();
		int a3 = in.read();
		int a4 = in.read();
		return (a1 << 24) + (a2 << 16) + (a3 << 8) + a4;
	}
}
