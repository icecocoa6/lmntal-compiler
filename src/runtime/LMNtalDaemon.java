package runtime;

//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.net.Socket;

//TODO ����

/**
 * ʪ��Ū�ʷ׻����ζ����ˤ��äơ�LMNtalRuntime���󥹥��󥹤ȥ�⡼�ȥΡ��ɤ��б�ɽ���ݻ����롣
 * @author nakajima
 *
 */
public class LMNtalDaemon{
	/*
	 * �Ȥꤢ�����ͤ��Ƥߤ��ץ�ȥ���
	 * LMNtalDaemon�����Ȥꤷ�ʤ��㤤���ʤ��ΤϤ��줰�餤���ʤ���
	 * 
	 * ��Ͽ�ط�
	 * HELO �ġʥΎߧ��ߡˤ��Ϥ褦
	 * READY ��ack
	 * REGISTLOCAL runtimeid �ĥ�����ˤ���ޥ�����󥿥������Ͽ
	 * OK �� ����
	 * FAIL �� ����
	 * REGISTREMOTE runtimeid �ļ긵����Ͽ���Ƥ���ޥ�����󥿥������������
	 * REGISTFINISHED �Ĺ�������Ǥ������Ȥ�ޥ�����󥿥��ब����׻�����daemon������
	 * 
	 * �¹Դط�
	 * COPYRULESET �ĥ롼�륻�åȤ���������
	 * COPYRULE �ĥ롼���������
	 * COPYPROCESSCONTEXT ��$p������
	 * COPYFREELINK
	 * COPYATOM
	 * 
	 *
	 * 
	 */
}
/*��n-kato����Υ����ȡ�
 * - REGIST �� REGISTER ����������
 * - REGISTREMOTE �ϡ�runtimegroupid�ʡ�ޥ�����󥿥����(runtime)id�ˤ�����ɬ�פ����롣
 * - REGISTFINISHED �ϡ��ޥ�����󥿥��ब����׻����ǤϤʤ���REGISTREMOTE��ȯ�Ԥ����׻����������֤���
 *   �������äơ�runtimegroupid ������˻���ɬ�פ����롣
 * - TERMINATE runtimegroupid ��ɬ�ס����������鼫ʬ���ΤäƤ������ƤΥ�󥿥����Ʊ����å����������롣
 */