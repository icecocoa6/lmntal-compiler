/*
 * ������: 2003/10/21
 *
 */
package runtime;

import java.util.*;
import java.lang.reflect.Modifier;
import java.lang.reflect.Field;

/*
 * ���緯�ء�Eclipse�Υǥե���Ȥ˹�碌�ƥ�����4���Խ����Ʋ�������
 * 
 * TODO ��Ŭ���Ѥˡ���¹�˥롼�������Ĥ��Ȥ��Ǥ��ʤ��ʼ¹Ի����顼��Ф��ˡ֥ǡ�����ץ��饹���롣
 * TODO memof ���ѻߤ��������Ǹ�Ƥ���롣
 * TODO �롼��¹����5�Ĥ�����Ϥ��줾��0����ͤ�ƻ��Ѥ���Τ����롣
 * TODO �롼��¹����5�Ĥ������3�ġ����뤤��1�Ĥ�ʻ�礷�������褤��
 * 
 * TODO �¹��쥹���å����Ѥॿ���ߥ󥰤�ͤ��롣
 *  - ���դ����Ƥ�����������Ƥ��顢��������Ԥ��褦�ˤ��롣���Τ����activatemem̿���Ƥ֡�
 *  - ��Τۤ����Ѥ�μ¸��ˤ�Stack�η���Ȥ������ߥåȤΥ����ߥ󥰤Ƿ�礹��
 * �������Ǥϡ��롼���줬������ȡ����Υ롼����ޤǤ���ʬ���ư��Ǽ¹Ԥ���褦�ˤ��Ƥ��롣
 */

/**
 * 1 �Ĥ�̿����ݻ����롣�̾�ϡ�Instruction��ArrayList�Ȥ����ݻ����롩
 * 
 * �ǥХå���ɽ���᥽�åɤ������롣
 *
 * @author hara, nakajima, n-kato
 */
public class Instruction {
	
    /**
     * ̿��μ�����ݻ����롣<strike>�롼��ID</strike> */	
    private int id;

	/**
	 * ̿��ΰ������ݻ����롣
	 * ̿��μ���ˤ�äư����η�����ޤäƤ��롣
	 */
	public List data = new ArrayList();
	
	//////////
	// ���

	/** �оݤ��줬������η׻��Ρ��ɤ�¸�ߤ��뤳�Ȥ��ݾڤ��뽤���� */
	public static final int LOCAL = 100;
	/** ���ߡ���̿�� */	
	public static final int DUMMY = -1;
    /** ̤�����̿�� */	
    public static final int UNDEF = 0;
	/** ̿��κ������������Ƥ�̿��μ����ɽ���ͤϤ����꾮���ʿ����ˤ��뤳�ȡ�*/
	private static final int END_OF_INSTRUCTION = 256;	

    // ���Ϥ�����ܥ�����̿�� (1--9)
    
    /** deref [-dstatom, srcatom, srcpos, dstpos]
     * <br><strong><font color="#ff0000">������̿��</font></strong><br>
     * ���ȥ�srcatom����srcpos�����Υ���褬��dstpos��������³���Ƥ��뤳�Ȥ��ǧ�����顢
     * �����Υ��ȥ�ؤλ��Ȥ�dstatom���������롣
     */
	public static final int DEREF = 1;

	/** findatom [-dstatom, srcmem, func]
	 * <br>ȿ�����륬����̿��<br>
	 * ��srcmem�ˤ��äƥե��󥯥�func����ĥ��ȥ�ؤλ��Ȥ򼡡���dstatom���������롣*/
	public static final int FINDATOM = 2;

    /** lockmem [-dstmem, srcfreelinkatom]
     * <br>��å��������륬����̿��<br>
     * ��ͳ��󥯽��ϴ������ȥ�freelinkatom����°��������Ф��ơ�
     * �Υ�֥�å��󥰤ǤΥ�å���������ߤ롣
     * �����ƥ�å���������������������ؤλ��Ȥ�dstmem���������롣
     * ����������å��ϡ���³��̿���󤬤�������Ф��Ƽ��Ԥ����Ȥ��˲�������롣
     * <p>
     * ��å���������������С�������Ϥޤ����Ȥ�ʡ��å���˼������Ƥ��ʤ��ä���Ǥ���
     * �ʤ��θ�����Ruby�ǤǤ�nmemeq̿��ǹԤäƤ����ˡ�
     * <p>��γ�����Υ�󥯤ǽ������ꤵ�줿��ؤλ��Ȥ�������뤿��˻��Ѥ���롣
     * @see testmem
     * @see getmem */
    public static final int LOCKMEM = 3;
    
    /** locallockmem [-dstmem, srcfreelinkatom]
     * <br>��å����������Ŭ���ѥ�����̿��<br>
     * lockmem��Ʊ����������srcfreelinkatom�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣
     * @see lockmem */
	public static final int LOCALLOCKMEM = LOCAL + LOCKMEM;

    /** anymem [-dstmem, srcmem] 
     * <br>ȿ�������å��������륬����̿��<br>
     * ��srcmem�λ���Τ����ޤ���å���������Ƥ��ʤ�����Ф��Ƽ����ˡ�
     * �Υ�֥�å��󥰤ǤΥ�å��������ߤ롣
     * �����ơ���å����������������ƻ���ؤλ��Ȥ�dstmem���������롣
     * ����������å��ϡ���³��̿���󤬤�������Ф��Ƽ��Ԥ����Ȥ��˲�������롣
     * <p><b>���</b>����å������˼��Ԥ������ȡ������줬¸�ߤ��Ƥ��ʤ��ä����Ȥ϶��̤Ǥ��ʤ���*/
	public static final int ANYMEM = 4;
	
	/** localanymem [-dstmem, srcmem]
     * <br>ȿ�������å����������Ŭ���ѥ�����̿��<br>
	 * anymem��Ʊ����������srcmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣dstmem�ˤĤ��Ƥϲ��Ⲿ�ꤷ�ʤ���
	 * @see anymem */
	public static final int LOCALANYMEM = LOCAL + ANYMEM;

    // ���Ϥ��ʤ����ܥ�����̿�� (10--24)

    /** testmem [dstmem, srcfreelinkatom]
     * <br>������̿��<br>
     * ��ͳ��󥯽��ϴ������ȥ�freelinkatom���ʥ�å����줿����dstmem�˽�°���뤳�Ȥ��ǧ���롣
     * <p><b>���</b>��Ruby�ǤǤ�getmem�ǻ��Ȥ�����������eqmem��ԤäƤ�����
     * @see lockmem */
    public static final int TESTMEM = 10;

    /** func [srcatom, func]
     * <br>������̿��<br>
     * ���ȥ�srcatom���ե��󥯥�func����Ĥ��Ȥ��ǧ���롣*/
    public static final int FUNC = 11;

    /** norules [srcmem] 
     * <br>������̿��<br>
     * ��srcmem�˥롼�뤬¸�ߤ��ʤ����Ȥ��ǧ���롣*/
    public static final int NORULES = 12;

    /** natoms [srcmem, count]
     * <br>������̿��<br>
     * ��srcmem�μ�ͳ��󥯴������ȥ�ʳ��Υ��ȥ����count�Ǥ��뤳�Ȥ��ǧ���롣*/
    public static final int NATOMS = 13;

    /** nfreelinks [srcmem, count]
     * <br>������̿��<br>
     * ��srcmem�μ�ͳ��󥯿���count�Ǥ��뤳�Ȥ��ǧ���롣*/
    public static final int NFREELINKS = 14;

    /** nmems [srcmem, count]
     * <br>������̿��<br>
     * ��srcmem�λ���ο���count�Ǥ��뤳�Ȥ��ǧ���롣*/
    public static final int NMEMS = 15;

    /** eqatom [atom1, atom2]
     * <br>������̿��<br>
     * atom1��atom2��Ʊ��Υ��ȥ�򻲾Ȥ��Ƥ��뤳�Ȥ��ǧ���롣
     * <p><b>���</b> Ruby�Ǥ�eq����ʬΥ */
    public static final int EQATOM = 16;

    /** eqmem [mem1, mem2]
     * <br>������̿��<br>
     * mem1��mem2��Ʊ�����򻲾Ȥ��Ƥ��뤳�Ȥ��ǧ���롣
     * <p><b>���</b> Ruby�Ǥ�eq����ʬΥ */
    public static final int EQMEM = 17;

    /** neqatom [atom1, atom2]
     * <br>������̿��<br>
     * atom1��atom2���ۤʤ륢�ȥ�򻲾Ȥ��Ƥ��뤳�Ȥ��ǧ���롣
     * <p><b>���</b> Ruby�Ǥ�neq����ʬΥ */
    public static final int NEQATOM = 18;
	
    /** neqmem [mem1, mem2]
     * <br>������̿��<br>
     * mem1��mem2���ۤʤ���򻲾Ȥ��Ƥ��뤳�Ȥ��ǧ���롣
     * <p><b>���</b> Ruby�Ǥ�neq����ʬΥ
     * <P>TODO ����̿������פ����Τ�ʤ� */
    public static final int NEQMEM = 19;

//    /** lock [srcmem]
//     * <br>���ѻߤ��줿�˥�����̿��<br>
//     * ��srcmem���Ф���Υ�֥�å��󥰤ǤΥ�å��������ߤ롣
//     * ��å���������������С�������Ϥޤ����Ȥ�ʡ��å���˼������Ƥ��ʤ��ä���Ǥ���
//     * <p>srcmem��memof��ˡ���ѻߤ��줿���ᡢ��å���lockmem�ǹԤ����������ä�lock���ѻߤ��줿��*/
//    public static final int LOCK = err;

    // �إå�̿�ᤫ��ܴɤ��줿�ܥǥ�̿�� (25--29)

    /** getmem [-dstmem, srcatom]
     * <br>�ܥǥ�̿��<br>
     * ���ȥ�srcatom�ν�°��ؤλ��Ȥ�dstmem���������롣
     * <p><b>���</b>��������̿��Ȥ��Ƥ��ѻߤ��줿��
     * @see lockmem */
    public static final int GETMEM = 25;

    /** getparent [-dstmem, srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem�ο���ؤλ��Ȥ�dstmem���������롣
     * <p><b>���</b>��������̿��Ȥ��Ƥ��ѻߤ��줿��*/
    public static final int GETPARENT = 26;

    // ���ܥܥǥ�̿�� (30--44)    

    /** removeatom [srcatom]
     * <br>�ܥǥ�̿��<br>
     * ���ȥ�srcatom�򸽺ߤ��줫����Ф����¹ԥ����å������ʤ���
     * @see dequeueatom */
	public static final int REMOVEATOM = 30;
	public static final int LOCALREMOVEATOM = LOCAL + REMOVEATOM;

    /** removemem [srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem�򸽺ߤ��줫����Ф���
     * ��srcmem�ϥ�å����˼¹��쥹���å���������Ƥ��뤿�ᡢ�¹��쥹���å������ʤ���*/
	public static final int REMOVEMEM = 31;
	/** localremovemem [srcmem]
     * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * removemem��Ʊ����������srcmem�ο���Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALREMOVEMEM = LOCAL + REMOVEMEM;

    /** newatom [-dstatom, srcmem, func]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem�˥ե��󥯥�func����Ŀ��������ȥ�����������Ȥ�dstatom���������롣
     * ���ȥ�Ϥޤ��¹ԥ����å��ˤ��Ѥޤ�ʤ���
     * @see enqueueatom */
    public static final int NEWATOM = 32;
    
    /** localnewatom [-dstatom, srcmem, func]
     * <br>��Ŭ���ѥܥǥ�̿��<br>
     * newatom��Ʊ����������srcmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALNEWATOM = LOCAL + NEWATOM;

    /** newmem [-dstmem, srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem�˿�����������������dstmem������������å������ޤ޼¹��쥹���å����Ѥࡣ
     * <p><b>���</b>����ˡ4��ʸ����Ф��ơ��֥�å������ޤ޼¹��쥹���å����Ѥ�פȤ������
     * ����Ӥ��η�̤Ρ֥�å������ޤ޼¹��쥹���å��ˤ��Ѥޤ줿�׾��֤��ɲä���ޤ�����
     * <ul>
     * <li>���������ξ�硢����¦����ºݤ˼¹��쥹���å����Ѥߤޤ���
     * ����Υ�å����������ȡ����ȥߥå��˥�å����������줿���֤ˤʤ�ޤ���
     * <li>��⡼�Ȥ���ξ�硢���Ū�ʼ¹ԥ����å����ꡢ�����˿���¦�����Ѥ�Ǥ����ޤ���
     * ��⡼�ȤΥ롼����Υ�å������������ȡ��¹��쥹���å�����Ƭ�˴ݤ��Ȱ�ư����ޤ���
     * ����ˤ�äƥ��ȥߥå��˥�å����������줿���֤ˤʤ�ޤ���
     * TODO �������äơ�movemem��newroot�ξ��ϡ��롼��¹Խ�λ���˥�å���������ʤ���Фʤ�ޤ���
     * </ul>
     * @see enqueuemem */
	public static final int NEWMEM = 33;

	/** localnewmem [-dstmem, srcmem]
	* <br>��Ŭ���ѥܥǥ�̿��<br>
	* newmem��Ʊ����������srcmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALNEWMEM = LOCAL + NEWMEM;

    /** newroot [-dstmem, srcmem, node]
     * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
     * ��srcmem�λ����ʸ����node�ǻ��ꤵ�줿�׻��Ρ��ɤǼ¹Ԥ���뿷������å����줿�롼������������
     * ���Ȥ�dstmem������������å������ޤޡʲ��Ρ˼¹��쥹���å����Ѥࡣ*/
    public static final int NEWROOT = 35;

//	/** localnewroot [-dstmem, srcmem, node]
//	 * <br>��ͽ�󤵤줿�˺�Ŭ���ѥܥǥ�̿��<br>
//	 * newroot��Ʊ����������srcmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣
//	 * ����̿��ˤϺ�Ŭ���θ��̤��ۤȤ��̵�����ᡢ�Ѱơ�*/
//	public static final int LOCALNEWROOT = LOCAL + NEWROOT;

    /** enqueueatom [srcatom]
     * <br>�ܥǥ�̿��<br>
     * ���ȥ�srcatom���°��μ¹ԥ����å����Ѥࡣ
     * <p>���Ǥ˼¹ԥ����å����Ѥޤ�Ƥ�������ư���̤����Ȥ��롣
     * TODO �����[��] srcatom�������ƥ��֤��ɤ�����Ƚ�ꤷ�ʤ����ͤˤ��٤��Ǥ��͡� */
    public static final int ENQUEUEATOM = 36;
    
	/** localenqueueatom [srcatom]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * enqueueatom��Ʊ����������srcatom�ν�°��Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALENQUEUEATOM = LOCAL + ENQUEUEATOM;

    /** dequeueatom [srcatom]
     * <br>��Ŭ���ѥܥǥ�̿��<br>
     * ���ȥ�srcatom�����η׻��Ρ��ɤˤ���¹ԥ����å������äƤ���С��¹ԥ����å�������Ф���
     * <p><b>���</b>������̿��ϡ���������̤Υ�������︺���뤿���Ǥ�դ˻��Ѥ��뤳�Ȥ��Ǥ��롣
     * ���ȥ������Ѥ���Ȥ��ϡ����̴ط�����դ��뤳�ȡ�
     * �ʤ���¾�η׻��Ρ��ɤˤ���¹ԥ����å������Ƥ����/�ѹ�����̿���¸�ߤ��ʤ���*/
    public static final int DEQUEUEATOM = 37;

    /** localdequeueatom [srcatom]
     * <br>��Ŭ���ѥܥǥ�̿��<br>
     * dequeueatom��Ʊ����������srcatom�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALDEQUEUEATOM = LOCAL + DEQUEUEATOM;

//    /** dequeuemem [srcmem]
//     * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
//     * ��srcmem��Ƶ�Ū�˼¹��쥹���å�������Ф���
//     * �Ƶ�Ū�˥�å���������إå�̿�᤬���Ф��Ƥ��뤿�����ס�����̿����ѻߤ��롣
//     */
//    public static final int DEQUEUEMEM = err;

	/** enqueuemem [srcmem]
	 * <br>�ܥǥ�̿��<br>
	 * ��srcmem��������륿�����μ¹��쥹���å�����srcmem���Ѥࡣ
	 * <p><strike>�¹Ը塢srcmem�ؤλ��Ȥ��Ѵ����ʤ���Фʤ�ʤ���</strike>
	 * <p>ŵ��Ū�ˤϡ���å������������Ѥ��뤿��˰�ư����ľ��Υ����ߥ󥰤ǸƤФ�롣
	 * @see newmem
	 * @see activatemem */
	public static final int ENQUEUEMEM = 38;

	/** localenqueuemem [srcmem]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * enqueuemem��Ʊ����������srcmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALENQUEUEMEM = LOCAL + ENQUEUEMEM;

//	�����ޤǳ�ǧ����

    // �롼�������ܥǥ�̿�� (45--49)
	
    /** loadruleset [dstmem, ruleset]
     * <br>�ܥǥ�̿��<br>
     * ruleset�����Ȥ���롼�륻�åȤ���dstmem�˥��ԡ����롣
     * TODO ����Ǥ���Ф�����Υ��ȥ�򥨥󥭥塼��ľ���ʤ���Фʤ�ʤ���*/
    public static final int LOADRULESET = 45;

	/** localloadruleset [dstmem, ruleset]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * loadruleset��Ʊ����������dstmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALLOADRULESET = LOCAL + LOADRULESET;

    /** copyrules [dstmem, srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem�ˤ������ƤΥ롼�����dstmem�˥��ԡ����롣
     * <p><b>���</b>��Ruby�Ǥ�inheritrules����̾���ѹ� */
    public static final int COPYRULES = 46;

	/** localcopyrules [dstmem, srcmem]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * copyrules��Ʊ����������dstmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALCOPYRULES = LOCAL + COPYRULES;

    /** clearrules [dstmem]
     * <br>�ܥǥ�̿��<br>
     * ��dstmem�ˤ������ƤΥ롼���õ�롣*/
	public static final int CLEARRULES = 47;
	
	/** localclearrules [dstmem]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * clearrules��Ʊ����������dstmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALCLEARRULES = LOCAL + CLEARRULES;

    // ��󥯤�����ܥǥ�̿�� (50--55)
	
    /** newlink [atom1, pos1, atom2, pos2]
     * <br>�ܥǥ�̿��<br>
     * ���ȥ�atom1����pos1�����ȡ����ȥ�atom2����pos2�����δ֤�ξ������󥯤�ĥ�롣
     * <p>ŵ��Ū�ˤϡ�atom1��atom2�Ϥ������롼��ܥǥ���¸�ߤ��롣
     * <p><b>���</b>��Ruby�Ǥ���������������ѹ����줿 */
    public static final int NEWLINK = 50;

	/** localnewlink [atom1, pos1, atom2, pos2]
	 * <br>�ܥǥ�̿��<br>
	 * newlink��Ʊ����������atom1�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALNEWLINK = LOCAL + NEWLINK;

    /** relink [atom1, pos1, atom2, pos2]
     * <br>�ܥǥ�̿��<br>
     * ���ȥ�atom1����pos1�����ȡ����ȥ�atom2����pos2�����Υ�������³���롣
     * <p>ŵ��Ū�ˤϡ�atom1�ϥ롼��ܥǥ��ˡ�atom2�ϥ롼��إåɤ�¸�ߤ��롣
     * <p>�¹Ը塢atom2[pos2]�����Ƥ�̵���ˤʤ롣*/
    public static final int RELINK = 51;

	/** localrelink [atom1, pos1, atom2, pos2]
	 * <br>�ܥǥ�̿��<br>
	 * relink��Ʊ����������atom1�����atom2�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALRELINK = LOCAL + RELINK;

    /** unify [atom1, pos1, atom2, pos2]
     * <br>�ܥǥ�̿��<br>
     * ���ȥ�atom1����pos1�����Υ����ΰ����ȡ����ȥ�atom2����pos2�����Υ����ΰ�������³���롣
     * <p>ŵ��Ū�ˤϡ�atom1��atom2�Ϥ������롼��إåɤ�¸�ߤ��롣*/
    public static final int UNIFY = 52;

	/** localunify [atom1, pos1, atom2, pos2]
     * <br>�ܥǥ�̿��<br>
	 * unify��Ʊ����������atom1�����atom2�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALUNIFY = LOCAL + UNIFY;

    /** getlink [-link2, atom2, pos2]
     * <br>���Ϥ����Ŭ���ѥܥǥ�̿��<br>
     * ���ȥ�atom2����pos2�����˳�Ǽ���줿��󥯥��֥������Ȥؤλ��Ȥ�link2���������롣
     * <p>ŵ��Ū�ˤϡ�atom2�ϥ롼��إåɤ�¸�ߤ��롣
     * inheritlink���Ȥ߹�碌�ƻ��Ѥ���relink�����Ѥˤ��롣*/
    public static final int GETLINK = 53;

	/** localgetlink [-link2, atom2, pos2]
	 * <br>�ܥǥ�̿��<br>
	 * getlink��Ʊ����������atom2�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
    public static final int LOCALGETLINK = LOCAL + GETLINK;

    /** inheritlink [atom1, pos1, link2]
     * <br>��Ŭ���ѥܥǥ�̿��<br>
     * atom1����pos1�����ȡ����link2�Υ�������³���롣
     * ���ȥ�atom1����pos1�����ȡ����link2�Υ�������³���롣
     * <p>ŵ��Ū�ˤϡ�atom1�ϥ롼��ܥǥ���¸�ߤ���link2�ϥ롼��إåɤ�¸�ߤ��롣
     * <p>link2�Ϻ����Ѥ���뤿�ᡢ�¹Ը��link2���Ѵ����ʤ���Фʤ�ʤ���
     * @see getlink */
    public static final int INHERITLINK = 54;

	/** localinheritlink [atom1, pos1, link2]
	 * <br>�ܥǥ�̿��<br>
	 * inheritlink��Ʊ����������atom1�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
    public static final int LOCALINHERITLINK = LOCAL + INHERITLINK;

    // ͽ�� (55-59)
	
    // ��ΰ�ư����Ӥ����ȼ����ͳ��󥯴������ȥ༫ư�����Τ���Υܥǥ�̿�� (60--69)
	
    /** removeproxies [srcmem]
     * <br>�ܥǥ�̿��<br>
     * ���⤷�ʤ���
     * <p>removemem��ľ���Ʊ������Ф��ƸƤФ�롣
     * TODO removemem���������srcmem���̤�̵�ط��ʼ�ͳ��󥯴������ȥ��ư�������פ�ʬΥ���롣*/
    public static final int REMOVEPROXIES = 60;

    /** removetoplevelproxies [srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem������ˤ��̲ᤷ�Ƥ���̵�ط��ʼ�ͳ��󥯴������ȥ�����롣
	 * <p>���Ƥ�removeproxies�θ�ǸƤФ�롣*/
    public static final int REMOVETOPLEVELPROXIES = 61;

    /** insertproxies [parentmem,childmem]
     * <br>�ܥǥ�̿��<br>
     * ���ꤵ�줿��֤˼�ͳ��󥯴������ȥ��ư�������롣
     * <p>���Ƥ�movemem�θ�ǸƤФ�롣*/
    public static final int INSERTPROXIES = 62;
	
    /** removetemporaryproxies [srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem������ˤ˻Ĥ��줿"star"���ȥ�����롣
     * <p>���Ƥ�insertproxies�θ�ǸƤФ�롣*/
    public static final int REMOVETEMPORARYPROXIES = 63;

    /** movecells [dstmem, srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem�ˤ������ƤΥ��ȥ�������dstmem�˰�ư���롣
     * <p>�¹Ը塢��srcmem�Ϥ��Τޤ��Ѵ�����ʤ���Фʤ�ʤ���
     * <p><strike>��srcmem�ˤ��ä������ƥ��֥��ȥ�ϼ¹ԥ����å����Ѥޤ�롣</strike>
     * <p><b>���</b>��Ruby�Ǥ�pour����̾���ѹ� */
    public static final int MOVECELLS = 64;

	/** enqueueallatoms [srcmem]
	 * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
	 * ��srcmem�ˤ������ƤΥ����ƥ��֥��ȥ�򤳤���μ¹ԥ����å����Ѥࡣ
	 */
	public static final int ENQUEUEALLATOMS = 65;
	// LOCALENQUEUEALLATOMS �Ϻ�Ŭ���θ��̤����ʤ��Ȼפ��뤿�ᡢ�Ѱơ�

    /** movemem [dstmem, srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem����dstmem�˰�ư���롣
     * <p>��srcmem������Ѥ��뤿��˻��Ѥ���롣
     * @see unlockmem */
    public static final int MOVEMEM = 66;

    /** unlockmem [srcmem]
     * <br>�ܥǥ�̿��<br>
     * �ʳ�������������srcmem�Υ�å���������롣
     * <p>�����Ѥ��줿����Ф��ƸƤФ�롣
     * <p>�¹Ը塢srcmem�ؤλ��Ȥ��Ѵ����ʤ���Фʤ�ʤ���
     * @see activatemem */
    public static final int UNLOCKMEM = 67;

	/** localunlockmem [srcmem]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * unlockmem��Ʊ����������srcmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALUNLOCKMEM = LOCAL + UNLOCKMEM;

    // �ץ���ʸ̮�򥳥ԡ��ޤ����Ѵ����뤿���̿�� (70--74)
    
    /** recursivelock [srcmem]
     * <br>��ͽ�󤵤줿�˥�����̿��<br>
     * ��srcmem�����Ƥλ�����Ф��ƺƵ�Ū�˥�å���������롣
     * <p>���դǤνи���1��Ǥʤ��ץ���ʸ̮���񤫤줿���դ�����Ф��ƻ��Ѥ���롣
     * TODO �ǥåɥ�å���������ʤ����Ȥ��ݾڤǤ���С�����̿��ϥ֥�å��󥰤ǹԤ��٤��Ǥ��롣*/
    public static final int RECURSIVELOCK = 70;

    /** recursiveunlock [srcmem]
     * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
     * ��srcmem�����Ƥλ�����Ф��ƺƵ�Ū�˥�å���������롣
     * ��Ϥ����������륿�����μ¹��쥹���å��˺Ƶ�Ū���Ѥޤ�롣
     * @see unlockmem
     * @see activatemem */
    public static final int RECURSIVEUNLOCK = 71;
	
    /** copymem [-dstmem, srcmem]
     * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
     * �Ƶ�Ū�˥�å����줿��srcmem�����ƤΥ��ԡ������������dstmem������롣
     * ��������ͳ��󥯴������ȥ����1�����ξ��֤��������ʤ���*/
    public static final int COPYMEM = 72;

    /** dropmem [srcmem]
     * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
     * �Ƶ�Ū�˥�å����줿��srcmem���˴����롣
     * ��������¹�����롼����Ȥ��륿�����϶�����λ���롣*/
    public static final int DROPMEM = 73;

    // ͽ�� (75--79)
	
    // ����̿�� (200--219)
	
    /** react [ruleid, [atomargs...], [memargs...]]
     * <br>���Ԥ��ʤ�������̿��<br>
     * ruleid�����Ȥ���롼����Ф���ޥå��������������Ȥ�ɽ����*/
    public static final int REACT = 200;

    /** not [[instructions...]]
     * <br>��ͽ�󤵤줿�˥�����̿��<br>
     * ���������ꤹ�롣
     * @see branch */
    public static final int NOT = 201;

    /** stop 
     * <br>��ͽ�󤵤줿�˼��Ԥ��ʤ�������̿��<br>
     * ������˥ޥå��������Ȥ�ɽ����
     */
    public static final int STOP = 202;

    /** spec [formals, locals]
     * <br>̵�뤵���<br>
     * �������Ȱ���ѿ��θĿ���������롣*/
    public static final int SPEC = 203;

    /** loop [[instructions...]]
     * <br>��¤��̿��<br>
     * ������̿�����¹Ԥ��뤳�Ȥ�ɽ����
     * �������Ǹ�ޤǼ¹Ԥ�����硢����loop̿��μ¹Ԥ򷫤��֤���*/
    public static final int LOOP = 204;

    /** branch [[instructions...]]
     * <br>��¤��̿��<br>
     * ������̿�����¹Ԥ��뤳�Ȥ�ɽ����
     * �����¹���˼��Ԥ�����硢�����¹���˼���������å����������
     * ŵ��Ū�ˤϤ���branch�μ���̿��˿ʤࡣ
     * �������Ǹ�ޤǼ¹Ԥ�����硢�����ǽ�λ����
     * TODO �ޤ���halt̿�����
     * @see not */
    public static final int BRANCH = 205;

	// �Ȥ߹��ߵ�ǽ�˴ؤ���̿�� (210--229)

	/** inline [[args...] text]
	 * <br>��ͽ�󤵤줿�˥ܥǥ�̿��
	 * ʸ����text�ǻ��ꤵ�줿Java�����ɤ򥨥ߥåȤ��롣
	 * ���������%0������ؤλ��Ȥ��ִ�����롣
	 * ���������%1����%n�Ͼܺ٤�̤��ΰ���args�γ����Ǥ�ɽ����󥯤ؤλ��Ȥ��ִ�����롣
	 * <p>Java����������Ϥ��ʤ������ϴĶ��Ǥ��㳰��ȯ�����롣*/
	public static final int INLINE = 210;

	/** builtin [class, method, [args...]]
	 * <br>��ͽ�󤵤줿�˥ܥǥ�̿��
	 * ���ꤵ�줿������Java�Υ��饹�᥽�åɤ�ƤӽФ���
	 * ���󥿥ץ꥿ư���Ȥ����Ȥ߹��ߵ�ǽ���󶡤��뤿��˻��Ѥ��롣�ܺ٤�̤�ꡣ*/
	public static final int BUILTIN = 211;
	

    ////////////////////////////////////////////////////////////////
    /**
     * ID���������
     * @return int
     * TODO �����緯[��]����[��] id��kind��̾���ѹ����ޤ�����ǧ�����鲿���񤤤Ʋ�������
     */
    public int getID() {
		return id;
	}
	public int getIntArg1() {
		return ((Integer)data.get(0)).intValue();
	}
	public int getIntArg2() {
		return ((Integer)data.get(1)).intValue();
	}
	public int getIntArg3() {
		return ((Integer)data.get(2)).intValue();
	}
	public int getIntArg4() {
		return ((Integer)data.get(3)).intValue();
	}
	public Object getArg1() {
		return data.get(0);
	}
	public Object getArg2() {
		return data.get(1);
	}
	public Object getArg3() {
		return data.get(2);
	}
	public Object getArg4() {
		return data.get(3);
	}

    ////////////////////////////////////////////////////////////////

    /**
     * �������ɲä���ޥ���
     * @param o ���֥������ȷ��ΰ���
     */
    private final void add(Object o) { data.add(o); }
	
    /**
     * �������ɲä���ޥ���
     * @param n int ���ΰ���
     */
    private final void add(int n) { data.add(new Integer(n)); }
	
	////////////////////////////////////////////////////////////////

    /**
     * ���ߡ�̿����������롣
     * ���������ä������᥽�åɤ��ޤ��Ǥ��Ƥʤ�̿��Ϥ����Ȥ�
     * @param s �����Ѥ�ʸ����
     */
    public static Instruction dummy(String s) {
		Instruction i = new Instruction(DUMMY);
		i.add(s);
		return i;
    }
	
    /**
     * react ̿����������롣
     * @param r ȿ���Ǥ���롼�륪�֥�������
     * @param actual �°���
     * TODO react�ΰ����λ��ͤ����
     */
    public static Instruction react(Rule r, List actual) {
		Instruction i = new Instruction(REACT);
		i.add(r);
		i.add(actual);
		return i;
    }	
    /** @deprecated */
    public static Instruction findatom(int dstatom, List srcmem, Functor func) {
		Instruction i = new Instruction(FINDATOM);
		i.add(dstatom);
		i.add(srcmem);
		i.add(func);
		return i;
    }	
    
    //
    
	/** findatom ̿����������� */
	public static Instruction findatom(int dstatom, int srcmem, Functor func) {
		return new Instruction(FINDATOM,dstatom,srcmem,func);
	}	
    /** anymem ̿����������� */
    public static Instruction anymem(int dstmem, int srcmem) {
		return new Instruction(ANYMEM,dstmem,srcmem);
    }
    /** newatom ̿����������� */
    public static Instruction newatom(int dstatom, int srcmem, Functor func) {
		return new Instruction(NEWATOM,dstatom,srcmem,func);
    }	
    /** spec ̿����������� */
    public static Instruction spec(int formals, int locals) {
		Instruction i = new Instruction(SPEC);
		i.add(formals);
		i.add(locals);
		return i;
    }
    /** newmem ̿����������� */
    public static Instruction newmem(int ret, int srcmem) {
		return new Instruction(NEWMEM,ret,srcmem);
    }
    /** newlink ̿����������� */
    public static Instruction newlink(int atom1, int pos1, int atom2, int pos2) {
		return new Instruction(NEWLINK,atom1,pos1,atom2,pos2);
    }
    /** loadruleset ̿����������� */
    public static Instruction loadruleset(int mem, Ruleset rs) {
		return new Instruction(LOADRULESET,mem,rs);
    }
    /** getmem ̿����������� */
    public static Instruction getmem(int ret, int atom) {
		return new Instruction(GETMEM,ret,atom);
    }	
    /** removeatom ̿����������� */
	public static Instruction removeatom(int atom) {
		return new Instruction(REMOVEATOM,atom);
	}	
	/** @deprecated */
	public static Instruction removeatom(int atom, Functor func) {
		return new Instruction(REMOVEATOM,atom,func);
	}	
    
	// ���󥹥ȥ饯��
	
    /** ̵̾̿����롣*/
    public Instruction() {
    }
	
    /**
     * ���ꤵ�줿̿���Ĥ���
     * @param id
     */
    public Instruction(int id) {
    	this.id = id;
    }
    private Instruction(int id, int arg1) {
		this.id = id;
		add(arg1);
    }
	private Instruction(int id, int arg1, int arg2) {
		this.id = id;
		add(arg1);
		add(arg2);
	}
	private Instruction(int id, int arg1, Object arg2) {
		this.id = id;
		add(arg1);
		add(arg2);
	}
	private Instruction(int id, int arg1, int arg2, int arg3) {
		this.id = id;
		add(arg1);
		add(arg2);
		add(arg3);
	}
	private Instruction(int id, int arg1, int arg2, Object arg3) {
		this.id = id;
		add(arg1);
		add(arg2);
		add(arg3);
	}
    private Instruction(int id, int arg1, int arg2, int arg3, int arg4) {
		this.id = id;
		add(arg1);
		add(arg2);
		add(arg3);
		add(arg4);
    }
    
	//////////////////////////////////
	//
	// �ǥХå���ɽ���᥽�å�
	//


    /**
     * �ǥХå���ɽ���᥽�åɡ�
     * ̿���ʸ����(String)��Ϳ����ȡ���������̿���int���֤��Ƥ����
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return int
     * 
     */
//     public static int getInstructionInteger(String instructionString){
// 	int answer = -1;
// 	Object tmp;

// 	try {
// 	    answer = ((Integer)table.get(instructionString.toUpperCase())).intValue();
// 	} catch (NullPointerException e){
// 	    System.out.println(e);
// 	    System.exit(1);
// 	}
// 	return answer;
//     }

	/** Integer�ǥ�åפ��줿̿���ֹ椫��̿��̾�ؤΥϥå��塣
	 * �����ϳ�ȯ����«�������ˡ���äȸ�Ψ�Τ褤�̤ι�¤���֤������Ƥ�褤�� */
	static Hashtable instructionTable = new Hashtable();
    {
		try {
			Instruction inst = new Instruction();
			Field[] fields = inst.getClass().getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field f = fields[i];
				if (f.getType().getName().equals("int") && Modifier.isStatic(f.getModifiers())) {
					Integer idobj = new Integer(f.getInt(inst));
					if (instructionTable.containsKey(idobj)) {
						System.out.println("WARNING: ID collision detected on instruction ID = " 
							+ idobj.intValue());
					}
					instructionTable.put(idobj, f.getName().toLowerCase());
				}
			}
		}
		catch(java.lang.SecurityException e)		{ e.printStackTrace(); }
		catch(java.lang.IllegalAccessException e)	{ e.printStackTrace(); }
    }


	/**
	 * �ǥХå���ɽ���᥽�åɡ�
	 * ̿���id (int)��Ϳ����ȡ���������̿���String���֤��Ƥ���롣
	 *
	 * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
	 * @return String
	 * 
	 */
	public static String getInstructionString(int id){
		String answer = "";
		answer = (String)instructionTable.get(new Integer(id));
		return answer;
		

	/* 
	   try {
	   answer = hoge[instrcutionNum];
	   } catch (ArrayIndexOutOfBoundsException e){
	   //	     answer = "�� �ʡ��� \n���ʡ����ϡ��ˡ㡡�̤�� \n\n";
	   answer = "\n1 ̾�������ͽ�̵������ 03/09/21 00:23\n�� �ʡ��� \n���ʡ����ϡ��ˡ㡡�̤�� \n\n2 ̾�������ͽ�̵������ ��03/09/21 00:24\n������������������ \n�� �ʡ����ϡ��ˡ�����|��|�����ގ�\n���ȡ��������ˡ� �� |��| \n���� �١�/�Ρ����� �� \n������ /���ˡ� �� < ��>__���� \n�� ��/��'������. �֡�������/\n���ʡ����ġ��������� ����/ \n\n";
	   answer = "dummy";
	   } catch (Exception e){
	   //�����˥���������
	   System.out.println(e);
	   System.exit(1);
	   }
	   return answer;
	*/
    }

    /**
     * �ǥХå���ɽ���᥽�åɡ�
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return String
     *
     * ��⡧Instruction����ϡ�List�����ArrayList������Ҥˤʤä����äƤ��롣
     * �Ĥޤꡢ��int����name��̿�� [����1, ����2, �� , ����n] 
     * ����Ĥ�Instruction���󥹥��󥹤ˤϡ�1�Ĥ���̿�᤬�ʤ�
     * ������̿���Integer����������Object��
     *
     * ��⡧���Ϥλ����⡼�ɤ��ݤ��ѿ��򥤥�ǥ�Ȥ��롣
     *
     */
    public String toString() {
		return getInstructionString(id)+" "+data.toString();

	//	StringBuffer buffer = new StringBuffer("");
	//
	//	if(data.isEmpty()){
	//	    buffer.append(" No Instructions! ");
	//	} else {
	//	    for (int i = 0; i < data.size()-1; i+=2){
	//		buffer.append("[");
	//
	//		buffer.append("Command: ");
	//		buffer.append( getInstructionString(((Integer)data.get(i)).intValue()));
	//		buffer.append(" Arguments: ");
	//		buffer.append(data.get(i+1));
	//		
	//		buffer.append("]");
	//	    }
	//	}
	//
	//	return buffer.toString();
    }

    /**
     *
     * Deprecated: 2003-10-28 �ǡ��������ѹ��ˤʤä�����
     *
     * �ǥХå���ɽ���᥽�åɡ�
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return String
     *
     * ��⡧Instruction����ϡ�List�����ArrayList������Ҥˤʤä����äƤ��롣
     * �Ĥޤꡢ[̿��, [����], ̿��, [����], ��]
     *
     */
    //     public String toString(){
    // 	//��ȴ������ˡ
    // 	//	return "not implemented\n";

    // 	//��ȴ������ˡ����2
    // 	/*	Object hoge;
    // 	 *	hoge = (Object)data;
    // 	 *	return hoge.toString();     
    // 	*/

    // 	//�ޤȤ����ˡ
    // 	//[̿��, [����], ̿��, [����], ��]������String���Ѵ�
    // 	Object[] hoge; 
    // 	Object[] fuga;
    // 	StringBuffer buffer = new StringBuffer();

    // 	try {
    // 	    hoge = data.toArray();

    // 	    buffer.append("[ ");

    // 	    //̿��Τ���Υ롼��
    // 	    for (int i = 0; i < hoge.length-1; i+=2) {
    // 		buffer.append(hoge[i]);
    // 		buffer.append(", ");
		
    // 		buffer.append("[");

    // 		//�����Τ���Υ롼��
    // 		fuga = hoge[i+1].toArray();
    // 		for (int j = 0; j < fuga.length; j++) {
    // 		    buffer.append(fuga[j]);
    // 		}
    // 		buffer.append("]");
    // 	    }

    // 	    buffer.append(" ]");

    // 	} catch (Exception e){
    // 	    //���ꤵ����硧
    // 	    //ArrayList data������̿�᤬���äƤʤ�
    // 	    //ArrayList data[i]����������̵��
    // 	    //̤�ΤΥХ����Ȥꤢ����exception��print

    // 	    //����ʳ����ʤ󤫤��롩

    // 	    //�㡧ArrayStoreException - a �μ¹Ի��η����ꥹ�����
    // 	    //    �����Ǥμ¹Ի��η��Υ����ѡ����åȤǤʤ����
    // 	    //    (by API���ͽ��ArrayList���饹toArray�᥽�åɤβ���)

    // 	    System.out.println(e);

    // 	    return "General Protection Fault\n\n";
    // 	}

    // 	return (buffer.toString());
    //     }




    /**
     * �ǥХå���ɽ���᥽�åɡ�Ϳ����줿List��Object[]���Ѵ��������줾������Ǥ��Ф���toString()��Ƥ��stdout�˿��ή����
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return void
     * @param List
     *
     * NAKJAIMA:2003-10-26:����ʤ��ä��ʡ�
     *
     */
    //     public static void Dump(List listToBeDumped){
    // 	Object[] hoge = listToBeDumped.toArray();
    // 	Object[] fuga;
	
    // 	for (int i = 0; i < hoge.length-1 ; i+=2){
    // 	    System.out.print("Command: ");
    // 	    System.out.print(hoge[i].toString());

    // 	    System.out.print("\t");
    // 	    System.out.print("Arguments: ");

    // 	    fuga = hoge[i+1].toArray();
    // 	    for (int j = 0; j < fuga.length; j++){
    // 		System.out.print(fuga[j].toString());
    // 		System.out.print(" ");
    // 	    }
    // 	    System.out.println();
    // 	}
    // 	System.out.println();
    //     }

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
     * [̿��, [����], ̿��, [����], ��]������print���롣
     * 
     * List����Ƭ��command�ȸ��ʤ���
     * command�μ��ϡ�������ArrayList�ȸ��ʤ���
     * ���μ��Ϥޤ�command�ȸ��ʤ���
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return void
     */
    //     public void Dump(){
    // 	//[̿��, [����], ̿��, [����], ��]������print����
    // 	Object[] hoge; 
    // 	Object[] fuga;

    // 	try {
    // 	    hoge = data.toArray();

    // 	    System.out.print("[ ");

    // 	    //̿��Τ���Υ롼��
    // 	    for (int i = 0; i < hoge.length-1; i+=2) {
    // 		System.out.print("Command: ");
    // 		System.out.print(hoge[i]);

    // 		System.out.print("\t");
    // 		System.out.print("Arguments: ");

    // 		//�����Τ���Υ롼��
    // 		fuga = hoge[i+1].toArray();
    // 		for (int j = 0; j < fuga.length; j++) {
    // 		    System.out.print(fuga[j]);
    // 		    System.out.print(" ");
    // 		}
    // 	    }

    // 	    System.out.println(" ]");

    // 	} catch (Exception e){
    // 	    //���ꤵ����硧
    // 	    //ArrayList data������̿�᤬���äƤʤ�
    // 	    //ArrayList data[i]����������̵��
    // 	    //̤�ΤΥХ����Ȥꤢ����exception��print

    // 	    //����ʳ����ʤ󤫤��롩

    // 	    //�㡧ArrayStoreException - a �μ¹Ի��η����ꥹ�����
    // 	    //    �����Ǥμ¹Ի��η��Υ����ѡ����åȤǤʤ����
    // 	    //    (by API���ͽ��ArrayList���饹toArray�᥽�åɤβ���)

    // 	    System.out.println(e);
    // 	}
    //     }
}
