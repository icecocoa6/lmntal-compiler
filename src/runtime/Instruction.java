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
     * ��⡧Instruction����ϡ�List�����ArrayList������Ҥˤʤä����äƤ��롣
     * �Ĥޤꡢ[̿��, [����], ̿��, [����], ��]
     *
     */
    public String toString(){
	//��ȴ������ˡ
	//	return "not implemented\n";

	//��ȴ������ˡ����2
	/*	Object hoge;
	 *	hoge = (Object)data;
	*/	return hoge.toString();

	//�ޤȤ����ˡ
	//[̿��, [����], ̿��, [����], ��]������String���Ѵ�
	Object[] hoge; 
	Object[] fuga;
	StringBuffer buffer = new StringBuffer();

	try {
	    hoge = data.toArray();

	    buffer.append("[ ");

	    //̿��Τ���Υ롼��
	    for (int i = 0; i < hoge.length-1; i+=2) {
		buffer.appnend(hoge[i]);
		buffer.append(", ");
		
		buffer.append("[");

		//�����Τ���Υ롼��
		fuga = hoge[i+1].toArray();
		for (int j = 0; j < fuga.length; j++) {
		    buffer.append(fuga[j]);
		}
		buffer.append("]");
	    }

	    buffer.append(" ]");

	} catch (Exception e){
	    //���ꤵ����硧
	    //ArrayList data������̿�᤬���äƤʤ�
	    //ArrayList data[i]����������̵��
	    //̤�ΤΥХ����Ȥꤢ����exception��print

	    //����ʳ����ʤ󤫤��롩

	    System.out.println(e);

	    return "General Protection Fault\n\n";
	}

	return (buffer.toString());
    }


    /**
     * �ǥХå���ɽ���᥽�åɡ�Ϳ����줿List��Object[]���Ѵ��������줾������Ǥ��Ф���toString()��Ƥ��stdout�˿��ή����
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return void
     * @param List
     *
     */
    public static void Dump(List listToBeDumped){
	Object[] hoge = listToBeDumped.toArray();
	Object[] fuga;
	
	for (int i = 0; i < hoge.length-1 ; i+=2){
	    System.out.print("Command: ");
	    System.out.print(hoge[i].toString());

	    System.out.print("\t");
	    System.out.print("Arguments: ");

	    fuga = hoge[i+1].toArray();
	    for (int j = 0; j < fuga.length; j++){
		System.out.print(fuga[j].toString());
		System.out.print(" ");
	    }
	    System.out.println();
	}
	System.out.println();
    }

    /**
     * �ǥХå���ɽ���᥽�åɡ�Instruction
     * ���֥����������List��Object[]���Ѵ�����
     * ���줾������Ǥ��Ф���toString()��Ƥ֡�
     * 
     * deprecated: by NAKAJIMA Motomu on 2003-10-25
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return void
     */
//     public void Dump(){
// 	Object[] tmp = data.toArray();

// 	for (int i = 0; i < tmp.length; i++){
// 	    System.out.print(tmp[i].toString());
// 	    System.out.print(" ");
// 	    System.out.println();
// 	}
//     }

    /**
     * �ǥХå���ɽ���᥽�åɡ�
     * 
     * List����Ƭ��command�ȸ��ʤ���
     * command�μ��ϡ�������ArrayList�ȸ��ʤ���
     * ���μ��Ϥޤ�command�ȸ��ʤ���
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return void
     */
    public void Dump(){
	Object[] hoge = data.toArray();
	Object[] fuga;
	
	for (int i = 0; i < hoge.length-1 ; i+=2){
	    System.out.print("Command: ");
	    System.out.print(hoge[0].toString());

	    System.out.print("\t");
	    System.out.print("Arguments: ");

	    fuga = hoge[i].toArray();
	    for (int j = 1; j < fuga.length; j++){
		System.out.print(fuga[j].toString());
		System.out.print(" ");
	    }
	    System.out.println();
	}
	System.out.println();
    }
}
