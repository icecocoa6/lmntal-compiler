/*
 * �쐬��: 2003/10/21
 *
 */
package runtime;
import java.util.ArrayList;

/**
 * 1 �̖��߂�ێ�����B
 * 
 * �����ł��\��Ȃ��Ƃ������B
 * 
 * @author pa
 *
 */
public class Instruction {
	public ArrayList data = new ArrayList();
	
	public Instruction() {
		// ���Ƃ��� [react, [1, 2, 5]]
		ArrayList sl = new ArrayList();
		sl.add(new Integer(1));
		sl.add(new Integer(2));
		sl.add(new Integer(5));
		data.add("react");
		data.add(sl);
		System.out.println(data);
	}
}
