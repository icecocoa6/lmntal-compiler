package util;
import java.io.*;

/**
 * �Х������ʸ�����ξ���򣱤ĤΥ��ȥ꡼��˽񤭹��ि��Υ��饹��
 * 
 * @author Mizuno
 */
public class HybridOutputStream {
	OutputStream out;

	/**
	 * ���ꤵ�줿���ȥ꡼��˥ǡ�����񤭹��ि��Υ��󥹥��󥹤��������ޤ���
	 * @param out �ǡ�����񤭹���OutputStream
	 * @throws IOException �����ϥ��顼��ȯ��������硣
	 */
	public HybridOutputStream(OutputStream out) throws IOException {
		this.out = out;
	}
	
	/**
	 * ���ȥ꡼��˥��֥������Ȥ�񤭹��ߤޤ���
	 * @param o �񤭹��४�֥�������
	 * @throws IOException �����ϥ��顼��ȯ��������硣
	 */
	public synchronized void writeObject(Object o) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		oout.writeObject(o);
		oout.close();
		writeBytes(bout.toByteArray());
		out.write('\n');
	}

	/**
	 * ���ȥ꡼���ʸ����ǡ�����񤭹��ߤޤ���
	 * @param str �񤭹���ʸ����
	 * @throws IOException �����ϥ��顼��ȯ��������硣
	 */
	public void write(String str) throws IOException {
		writeBytes(str.getBytes());
	}

	/**
	 * ���ȥ꡼���ե�å��夷�ޤ���
	 * @throws IOException �����ϥ��顼��ȯ��������硣
	 */
	public synchronized void flush() throws IOException {
		out.flush();
	}
	/**
	 * ���ȥ꡼����Ĥ��ޤ���
	 * @throws IOException �����ϥ��顼��ȯ��������硣
	 */
	public synchronized void close() throws IOException {
		out.close();
	}
	/**
	 * ���ȥ꡼��˥Х������񤭹��ߤޤ���
	 * @param data �񤭹���Х�����
	 * @throws IOException �����ϥ��顼��ȯ��������硣
	 */
	public synchronized void writeBytes(byte[] data) throws IOException {
		writeInt(data.length);
		out.write(data);
	}
	
	/* writeBytes�Τʤ��ǤΤ߸ƤФ��Τ�syncrhronized�Ϥʤ��Ƥ��ɤ��Ϥ�������ǰ�Τ���*/
	private synchronized void writeInt(int val) throws IOException {
		//out.write()�Ǥϡ����24�ӥåȤ�̵�뤵���
		out.write(val >> 24);
		out.write(val >> 16);
		out.write(val >> 8);
		out.write(val);
	}

	/**
	 * �ƥ����ѥ���ȥ�ݥ���ȡ�
	 * @param args
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		HybridOutputStream writer = new HybridOutputStream(new BufferedOutputStream(bout));

		String data = "12345678901234567890123456789012345678901234567890\n";
		writer.write(data);
		writer.writeObject(data);
		writer.write(data);
		writer.write(data);
		writer.writeObject(data);
		writer.writeObject(data);
		
		data = data + data;
		data = data + data;
		data = data + data;
		data = data + data;
		data = data + data;
		writer.write(data);
		writer.writeObject(data);
		
		writer.close();
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		HybridInputStream reader = new HybridInputStream(new BufferedInputStream(bin));
		Util.println(reader.readLine());
		Util.println(reader.readObject());
		Util.println(reader.readLine());
		Util.println(reader.readLine());
		Util.println(reader.readObject());
		Util.println(reader.readObject());
		for (int i = 0; i < 32; i++) {
			Util.println(reader.readLine());
		}
		Util.println(reader.readObject());

		Util.println(reader.readLine());
		Util.println(reader.readLine());
	}
}
