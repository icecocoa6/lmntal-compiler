/*
 * ������: 2003/10/21
 *
 */
package runtime;

import java.util.*;
import java.lang.reflect.Modifier;
import java.lang.reflect.Field;

/*
 * TODO memof ���ѻߤ��������Ǹ�Ƥ���롣
 * TODO ���դ����Ƥ�����������Ƥ��顢��������Ԥ��褦�ˤ��롣���Τ����activatemem̿���Ƥ֡�
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
     * �ɤ�̿��ʤΤ����ݻ�����
     */
    private int id;

    /** ̤�����̿�� */	
    public static final int UNDEF = 0;
	
    // ���Ϥ�����ܥ�����̿�� (1--9)
    
    /** deref [-dstatom, srcatom, srcpos, dstpos]
     * <br><strong><font color="#ff0000">������̿��</font></strong><br>
     * ���ȥ�srcatom����srcpos�����Υ���褬��dstpos��������³���Ƥ��뤳�Ȥ��ǧ�����顢
     * �����Υ��ȥ�ؤλ��Ȥ�dstatom���������롣
     */
    public static final int DEREF = 1;

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
    public static final int LOCKMEM = 2;

    /** anymem [-dstmem, srcmem] 
     * <br>ȿ�������å��������륬����̿��<br>
     * ��srcmem�λ���Τ����ޤ���å���������Ƥ��ʤ�����Ф��Ƽ����ˡ�
     * �Υ�֥�å��󥰤ǤΥ�å��������ߤ롣
     * �����ơ���å����������������ƻ���ؤλ��Ȥ�dstmem���������롣
     * ����������å��ϡ���³��̿���󤬤�������Ф��Ƽ��Ԥ����Ȥ��˲�������롣
     * <p><b>���</b>����å������˼��Ԥ������ȡ������줬¸�ߤ��Ƥ��ʤ��ä����Ȥ϶��̤Ǥ��ʤ���*/
    public static final int ANYMEM = 3;

    /** findatom [-dstatom, srcmem, func]
     * <br>ȿ�����륬����̿��<br>
     * ��srcmem�ˤ��äƥե��󥯥�func����ĥ��ȥ�ؤλ��Ȥ򼡡���dstatom���������롣*/
    public static final int FINDATOM = 4;

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
    //    public static final int LOCK = 20;

    // ���Ϥ�����ܥܥǥ�̿�� (25--29)

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

    // ���Ϥ��ʤ����ܥܥǥ�̿�� (30--44)    

    /** removeatom [srcatom]
     * <br>�ܥǥ�̿��<br>
     * ���ȥ�srcatom�򸽺ߤ��줫����Ф���
     * <p><strike>�¹ԥ����å������äƤ���м¹ԥ����å���������롣</strike>*/
    public static final int REMOVEATOM = 30;

    /** removemem [srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem�򸽺ߤ��줫����Ф���*/
    public static final int REMOVEMEM = 31;

    /** newatom [-dstatom, srcmem, func]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem�˥ե��󥯥�func����Ŀ��������ȥ�����������Ȥ�dstatom���������롣
     * <p>�����ƥ��֥��ȥ�ʤ����srcmem�μ¹ԥ����å����Ѥࡣ*/
    public static final int NEWATOM = 32;

    /** newmem [-dstmem, srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem�˿�����������������dstmem���������롣*/
    public static final int NEWMEM = 33;

    /** newfreelink [-dstfreelinkatom, srcmem]
     * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
     * ��srcmem�˿�������ͳ��󥯽��ϴ������ȥ������������Ȥ�dstfreelinkatom���������롣
     * <p>����̿����ѰƤˤʤä���*/
    public static final int NEWFREELINK = 34;

    /** newroot [-dstmem, srcmem, node]
     * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
     * ��srcmem�λ���˷׻��Ρ���node�Ǽ¹Ԥ���뿷�����롼���������������Ȥ�dstmem���������롣*/
    public static final int NEWROOT = 35;

    /** enqueueatom [srcatom]
     * <br>�ܥǥ�̿��<br>
     * ���ȥ�srcatom���°��μ¹ԥ����å�������롣*/
    public static final int ENQUEUEATOM = 36;

    /** dequeueatom [srcatom]
     * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
     * ���ȥ�srcatom��¹ԥ����å�������Ф���*/
    public static final int DEQUEUEATOM = 36;

    /** dequeuemem [srcmem]
     * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
     * ��srcmem��Ƶ�Ū�˼¹��쥹���å�������Ф���*/
    public static final int DEQUEUEMEM = 37;

    /** activatemem [srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem���������ޥ���μ¹��쥹���å�����srcmem���Ѥࡣ
     * <p>�¹Ը塢srcmem�ؤλ��Ȥ��Ѵ����ʤ���Фʤ�ʤ���
     * @see activatemem */
    public static final int ACTIVATEMEM = 38;

    // �롼�������ܥǥ�̿�� (45--49)
	
    /** loadruleset [dstmem, ruleset]
     * <br>�ܥǥ�̿��<br>
     * ruleset�����Ȥ���롼�륻�åȤ���dstmem�˥��ԡ����롣
     * TODO ����Ǥ���Ф�����Υ��ȥ�򥨥󥭥塼��ľ���ʤ���Фʤ�ʤ��Τ򲿤Ȥ����롣*/
    public static final int LOADRULESET = 45;

    /** copyrules [dstmem, srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem�ˤ������ƤΥ롼�����dstmem�˥��ԡ����롣
     * <p><b>���</b>��Ruby�Ǥ�inheritrules����̾���ѹ����ޤ�����*/
    public static final int COPYRULES = 46;

    /** clearrules [dstmem]
     * <br>�ܥǥ�̿��<br>
     * ��dstmem�ˤ������ƤΥ롼���õ�롣*/
    public static final int CLEARRULES = 47;

    // ��󥯤�����ܥǥ�̿�� (50--55)
	
    /** newlink [atom1, pos1, atom2, pos2]
     * <br>�ܥǥ�̿��<br>
     * ���ȥ�atom1����pos1�����ȡ����ȥ�atom2����pos2�����δ֤�ξ������󥯤�ĥ�롣
     * <p>ŵ��Ū�ˤϡ�atom1��atom2�Ϥ������롼��ܥǥ���¸�ߤ��롣
     * <p><b>���</b>��Ruby�Ǥ���������������ѹ����줿*/
    public static final int NEWLINK = 50;

    /** relink [atom1, pos1, atom2, pos2]
     * <br>�ܥǥ�̿��<br>
     * ���ȥ�atom1����pos1�����ȡ����ȥ�atom2����pos2�����Υ�������³���롣
     * <p>ŵ��Ū�ˤϡ�atom1�ϥ롼��ܥǥ��ˡ�atom2�ϥ롼��إåɤ�¸�ߤ��롣
     * <p>�¹Ը塢atom2[pos2]�����Ƥ�̵���ˤʤ롣*/
    public static final int RELINK = 51;

    /** unify [atom1, pos1, atom2, pos2]
     * <br>�ܥǥ�̿��<br>
     * ���ȥ�atom1����pos1�����Υ����ΰ����ȡ����ȥ�atom2����pos2�����Υ����ΰ�������³���롣
     * <p>ŵ��Ū�ˤϡ�atom1��atom2�Ϥ������롼��إåɤ�¸�ߤ��롣*/
    public static final int UNIFY = 52;

    /** getlink [-link2, atom2, pos2]
     * <br>���Ϥ����Ŭ���ѥܥǥ�̿��<br>
     * ���ȥ�atom2����pos2�����˳�Ǽ���줿��󥯥��֥������Ȥؤλ��Ȥ�link2���������롣
     * <p>ŵ��Ū�ˤϡ�atom2�ϥ롼��إåɤ�¸�ߤ��롣
     * inheritlink���Ȥ߹�碌�ƻ��Ѥ���relink�����Ѥˤ��롣*/
    public static final int GETLINK = 53;

    /** inheritlink [atom1, pos1, link2]
     * <br>��Ŭ���ѥܥǥ�̿��<br>
     * atom1����pos1�����ȡ����link2�Υ�������³���롣
     * ���ȥ�atom1����pos1�����ȡ����link2�Υ�������³���롣
     * <p>ŵ��Ū�ˤϡ�atom1�ϥ롼��ܥǥ���¸�ߤ���link2�ϥ롼��إåɤ�¸�ߤ��롣
     * <p>link2�Ϻ����Ѥ���뤿�ᡢ�¹Ը��link2����Ѥ��ƤϤʤ�ʤ���
     * @see getlink */
    public static final int INHERITLINK = 54;

    // ͽ�� (55-59)
	
    // ��ΰ�ư����Ӽ�ͳ��󥯴������ȥ��ư�������뤿��Υܥǥ�̿�� (60--69)
	
    /** removeproxies [srcmem]
     * <br>�ܥǥ�̿��<br>
     * ���⤷�ʤ���
     * TODO removemem���������srcmem���̤�̵�ط��ʼ�ͳ��󥯴������ȥ��ư�������פ�ʬΥ���롣*/
    public static final int REMOVEPROXIES = 60;

    /** removetoplevelproxies [srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem������ˤ��̲ᤷ�Ƥ���̵�ط��ʼ�ͳ��󥯴������ȥ�����롣*/
    public static final int REMOVETOPLEVELPROXIES = 61;

    /** insertproxies [parentmem,childmem]
     * <br>�ܥǥ�̿��<br>
     * ���ꤵ�줿��֤˼�ͳ��󥯴������ȥ��ư�������롣*/
    public static final int INSERTPROXIES = 62;
	
    /** removetemporaryproxies [srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem������ˤ˻Ĥ��줿"star"���ȥ�����롣*/
    public static final int REMOVETEMPORARYPROXIES = 63;

    /** movecells [dstmem, srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem�ˤ������ƤΥ��ȥ�������dstmem�˰�ư���롣
     * <p>�¹Ը塢��srcmem�Ϥ��Τޤ��Ѵ�����ʤ���Фʤ�ʤ���
     * <p>��srcmem�ˤ��ä����ȥ�ϼ¹ԥ����å��˥��󥭥塼����롣
     * <p><b>���</b>��Ruby�Ǥ�pour����̾���ѹ� */
    public static final int MOVECELLS = 64;

    /** movemem [dstmem, srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem����dstmem�˰�ư���롣*/
    public static final int MOVEMEM = 65;

    /** unlockmem [srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��srcmem�Υ�å���������롣
     * <p>��������������ޥ���μ¹��쥹���å����Ѥޤ�롣
     * <p>�¹Ը塢srcmem�ؤλ��Ȥ��Ѵ����ʤ���Фʤ�ʤ���
     * TODO activatemem ��ʻ�礹�롩 */
    public static final int UNLOCKMEM = 66;

    // �ץ���ʸ̮�򥳥ԡ��ޤ����Ѵ����뤿���̿�� (70--74)
    
    /** recursivelock [srcmem]
     * <br>��ͽ�󤵤줿�˼��Ԥ��ʤ��ʡ��˥�����̿��<br>
     * ��srcmem�����Ƥλ�����Ф��ƺƵ�Ū�˥�å���������롣�֥�å��󥰤ǹԤ���*/
    public static final int RECURSIVELOCK = 70;

    /** recursiveunlock [srcmem]
     * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
     * ��srcmem�����Ƥλ�����Ф��ƺƵ�Ū�˥�å���������롣*/
    public static final int RECURSIVEUNLOCK = 71;

    /** copymem [dstmem, srcmem]
     * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
     * �Ƶ�Ū�˥�å����줿��srcmem�����ƤΥ��ԡ������������dstmem������롣
     * ��������ͳ��󥯴������ȥ����1�����ξ��֤��������ʤ���*/
    public static final int COPYMEM = 72;

    /** dropmem [srcmem]
     * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
     * �Ƶ�Ū�˥�å����줿��srcmem���˴����롣
     * ��������¹�����롼����Ȥ���ޥ���϶�����λ���롣*/
    public static final int DROPMEM = 73;

    // ͽ�� (75--79)
	
    // ����̿�� (80--99)
	
    /** react [ruleid, [atomargs...], [memargs...]]
     * <br>���Ԥ��ʤ�������̿��<br>
     * ruleid�����Ȥ���롼����Ф���ޥå��������������Ȥ�ɽ����*/
    public static final int REACT = 80;

    /** not [[instructions...]]
     * <br>��ͽ�󤵤줿�˥�����̿��<br>
     * ���������ꤹ�롣*/
    public static final int NOT = 81;

    /** stop 
     * <br>��ͽ�󤵤줿�˼��Ԥ��ʤ�������̿��<br>
     * ������˥ޥå��������Ȥ�ɽ����
     */
    public static final int STOP = 82;

    /** inline [[args...] text]
     * <br>��ͽ�󤵤줿�˥ܥǥ�̿��
     * ʸ����text�ǻ��ꤵ�줿Java�����ɤ򥨥ߥåȤ��롣
     * ���������%0������ؤλ��Ȥ��ִ�����롣
     * ���������%1����%n�Ͼܺ٤�̤��ΰ���args�γ����Ǥ�ɽ����󥯤ؤλ��Ȥ��ִ�����롣
     * <p>Java����������Ϥ��ʤ������ϴĶ��Ǥ��㳰��ȯ�����롣*/
    public static final int INLINE = 83;

    /** builtin [...]
     * <br>��ͽ�󤵤줿�˥ܥǥ�̿��
     * ���ꤵ�줿������Java�Υ��饹�᥽�åɤ�ƤӽФ����ܺ٤�̤�ꡣ*/
    public static final int BUILTIN = 84;
	
    /** spec [formals, locals]
     * <br>̵�뤵���<br>
     * �������Ȱ���ѿ��θĿ���������롣*/
    public static final int SPEC = 85;

    /** loop [[instructions...]]
     * <br>��¤��̿��<br>
     * ������̿�����¹Ԥ��뤳�Ȥ�ɽ����
     * �������Ǹ�ޤǼ¹Ԥ�����硢����loop̿��μ¹Ԥ򷫤��֤���*/
    public static final int LOOP = 91;

    /** branch [[instructions...]]
     * <br>��¤��̿��<br>
     * ������̿�����¹Ԥ��뤳�Ȥ�ɽ����
     * �����¹���˼��Ԥ�����硢�����¹���˼���������å����������
     * ŵ��Ū�ˤϤ���branch�μ���̿��˿ʤࡣ
     * �������Ǹ�ޤǼ¹Ԥ�����硢�����ǽ�λ�����TODO �ޤ���halt̿������*/
    public static final int BRANCH = 92;

    /** ̿��ο�����̿��Ϥ����꾮���ʿ����ˤ��뤳�ȡ�*/
    private static final int END_OF_INSTRUCTION = 100;
	
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
	
    /**
     * ���ߡ�̿����������롣
     * ���������ä������᥽�åɤ��ޤ��Ǥ��Ƥʤ�̿��Ϥ����Ȥ�
     * @param s �����Ѥ�ʸ����
     */
    public static Instruction dummy(String s) {
	Instruction i = new Instruction(-1);
	i.add(s);
	return i;
    }
	
    /**
     * react ̿����������롣
     * 
     * @param r ȿ���Ǥ���롼�륪�֥�������
     * @param actual ����
     * @return
     */
    public static Instruction react(Rule r, List actual) {
	Instruction i = new Instruction(REACT);
	i.add(r);
	i.add(actual);
	return i;
    }
	
    /** findatom ̿����������� */
    public static Instruction findatom(int dstatom, List srcmem, Functor func) {
	Instruction i = new Instruction(FINDATOM);
	i.add(dstatom);
	i.add(srcmem);
	i.add(func);
	return i;
    }
	
    /** anymem ̿����������� */
    public static Instruction anymem(int dstmem, int srcmem) {
	Instruction i = new Instruction(ANYMEM);
	i.add(dstmem);
	i.add(srcmem);
	return i;
    }
	
	
    /**
     * newatom ̿����������롣
     * 
     * @param dstatom
     * @param srcmem
     * @param func
     * @return
     */
    public static Instruction newatom(int dstatom, int srcmem, Functor func) {
	Instruction i = new Instruction(NEWATOM);
	i.add(dstatom);
	i.add(srcmem);
	i.add(func);
	return i;
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
	Instruction i = new Instruction(LOADRULESET);
	i.add(mem);
	i.add(rs);
	return i;
    }
	
    /**
     * getmem ̿����������롣
     * @param ret
     * @param atom
     * @return
     */
    public static Instruction getmem(int ret, int atom) {
	Instruction i = new Instruction(GETMEM);
	i.add(ret);
	i.add(atom);
	return i;
    }
	
    /**
     * removeatom ̿����������롣
     * @param atom
     * @param func
     * @return
     */
    public static Instruction removeatom(int atom, Functor func) {
	Instruction i = new Instruction(REMOVEATOM);
	i.add(atom);
	i.add(func);
	return i;
    }
	
	
	
    /**
     * ̿��ΰ������ݻ����롣
     * ̿��ˤ�äư����η�����ޤäƤ��롣
     */
    public List data = new ArrayList();
	
    /**
     * ̵̾̿����롣
     *
     */
    public Instruction() {
    }
	
    /**
     * ���ꤵ�줿̿���Ĥ���
     * @param id
     */
    public Instruction(int id) {
    	this.id = id;

	//deprecated by NAKAJIMA: �Ť��ǡ�������
	//by HARA
	// ���Ȥ��� [react, [1, 2, 5]]
	// 		ArrayList sl = new ArrayList();
	// 		sl.add(new Integer(1));
	// 		sl.add(new Integer(2));
	// 		sl.add(new Integer(5));
	// 		data.add("react");
	// 		data.add(sl);
	// 		System.out.println(data);

	//�������ǡ�������
	/*
	  ArrayList sl = new ArrayList();
	  sl.add(new Integer(1));
	  sl.add(new Integer(2));
	  sl.add(new Integer(5));
	  data.add(new Integer(0)); // 0->deref̿��
	  data.add(sl);
	  System.out.println(data);
	*/
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
    private Instruction(int id, int arg1, int arg2, int arg3) {
	this.id = id;
	add(arg1);
	add(arg2);
	add(arg2);
    }
    private Instruction(int id, int arg1, int arg2, int arg3, int arg4) {
	this.id = id;
	add(arg1);
	add(arg2);
	add(arg3);
	add(arg4);
    }


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

    static String[] hoge = new String[END_OF_INSTRUCTION]; // ̿��μ���ο�	
		
    {
	hoge[DEREF] = "deref";
    }

    /**
     * �ǥХå���ɽ���᥽�åɡ�
     * ̿��ο���(int)��Ϳ����ȡ���������̿���String���֤��Ƥ���롣
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return String
     * 
     */
    public static String getInstructionString(int instrcutionNum){
	String answer = "";

	Hashtable table = new Hashtable();

	try {
	    Instruction hoge = new Instruction();
	    Field[] fields = Class.forName("Instruction").getDeclaredFields();
	    for (int i = 0; i < fields.length; i++) {
		Field f = fields[i];
		if (f.getType().getName().equals("int") && Modifier.isStatic(f.getModifiers())) {
		    table.put(new Integer(f.getInt(hoge)), f.getName().toLowerCase());
		}
	    }			
	}
	catch(java.lang.SecurityException e)
	    {
		
	    }

	answer = (String)table.get(new Integer(instrcutionNum));

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
