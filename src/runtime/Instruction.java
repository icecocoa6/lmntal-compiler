/*
 * ������: 2003/10/21
 *
 */
package runtime;
import java.util.*;

/**
 * 1 �Ĥ�̿����ݻ����롣
 * 
 * ʣ���Ǥ⹽��ʤ��Ȥ��⤦��
 * 
 * �ǥХå���ɽ���᥽�åɤ������롣
 *
 * @author pa
 *
 */
public class Instruction {
	public static final int NULL      = 0;
	public static final int MEM       = 1;
	public static final int VAR       = 2;
	public static final int NAME      = 3;
	public static final int AT        = 4;
	public static final int METAVAR   = 5;

    //����̵�����Ƚ�����̤�10(by api���ͽ�)
	public List data = new ArrayList();
	
	public Instruction() {
		// ���Ȥ��� [react, [1, 2, 5]]
		ArrayList sl = new ArrayList();
		sl.add(new Integer(1));
		sl.add(new Integer(2));
		sl.add(new Integer(5));
		data.add("react");
		data.add(sl);
		System.out.println(data);
	}

    /**
     * �ǥХå���ɽ���᥽�åɡ�
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return String
     *
     */
    public String toString(){
	return "not implemented\n";
    }

    /**
     * �ǥХå���ɽ���᥽�åɡ�Ϳ����줿List��Object[]���Ѵ��������줾������Ǥ��Ф���toString()��Ƥ֡�
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return void
     * @param List
     */
    public static void Dump(List listToBeDumped){
	Object[] tmp = listToBeDumped.toArray();

	for (int i = 0; i < tmp.length; i++){
	    System.out.print(tmp[i].toString());
	    System.out.print(" ");
	    System.out.println();
	}
    }
    /**
     * �ǥХå���ɽ���᥽�åɡ�Instruction
     * ���֥����������List��Object[]���Ѵ�����
     * ���줾������Ǥ��Ф���toString()��Ƥ֡�
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return void
     */
    public void Dump(){
	Object[] tmp = data.toArray();

	for (int i = 0; i < tmp.length; i++){
	    System.out.print(tmp[i].toString());
	    System.out.print(" ");
	    System.out.println();
	}
    }


}
