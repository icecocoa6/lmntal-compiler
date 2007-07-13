package toolkit;

import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JFrame;

import runtime.Atom;
import runtime.Dumper;
import runtime.Functor;
import runtime.Membrane;
import runtime.SymbolFunctor;

public class LMNtalWindow extends JFrame {
	
	/////////////////////////////////////////////////////////////////
	// ������Functor
	
	final static
	private Functor NAME_FUNCTOR = new SymbolFunctor("name",1); 

	final static
	private Functor SIZE_FUNCTOR = new SymbolFunctor("size", 2);
	
	final static
	private Functor KILLER_FUNCTOR = new SymbolFunctor("killer", 0);
		
	final static
	private Functor BUTTON_FUNCTOR = new SymbolFunctor("button",0);

	final static
	private Functor TEXTAREA_FUNCTOR = new SymbolFunctor("textarea",0);
	
	final static
	private Functor HTML_FUNCTOR = new SymbolFunctor("html",0);

	final static
	private Functor LABEL_FUNCTOR = new SymbolFunctor("label",0);

	final static
	private Functor GRAPHIC_FUNCTOR = new SymbolFunctor("graphic",0);
	
	final static
	private Functor ID_FUNCTOR = new SymbolFunctor("id", 1);
	
//	final static
//	private Functor IMAGE_FUNCTOR = new SymbolFunctor("image", 0);

	final static
	private Functor TIMER_FUNCTOR = new SymbolFunctor("timer", 0);

	
	/////////////////////////////////////////////////////////////////

	// ������ɥ����Ĥ�����ȡ��ץ�������Ū�˽�λ�����뤫�ɤ����Υե饰
	private boolean killer = false;

//	private String memID;
	private Membrane mymem;
	private String windowName;
	private int sizeX = 0;
	private int sizeY = 0;
	private GridBagLayout layout;

	private boolean sizeUpdate = false;
	
	private Map<String, LMNComponent> componentMap = new HashMap();

	
	///////////////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	public LMNtalWindow(Membrane mem, String name){
		resetMembrane(mem);
		windowName = name;
		makeWindow();
	}
	///////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * ��ξ���򤹤٤Ƽ������������ꤹ�롥
	 * ���������ϥ�����ɥ���Ǥ��뤳���ݾڤ�����Ƥ��뤳�ȡ�
	 * @param mem
	 */
	public void resetMembrane(Membrane mem){
		Iterator atomIte;
		Atom targetAtom;
		
		mymem = mem;
//		// membrane ID
//		memID = mem.getGlobalMemID();
				
		// name atom
		atomIte= mem.atomIteratorOfFunctor(NAME_FUNCTOR);
		if(atomIte.hasNext()){
			targetAtom = (Atom)atomIte.next();
			windowName = ((null != targetAtom) ? targetAtom.nth(0) : "");
		}
			
		// size���ȥࡡ�����
		setSizeAtom(mem);
		
		// killer���ȥ�����
		setKiller(mem);

	}
	
	/**
	 * component��̵ͭ��Ƚ��
	 */
	public void setChildMem(Membrane mem){
	
		String id = getID(mem); // ID�����
		if(id == null) return;
		System.out.println(id);

		//componentMap��key��ID�����ä��鹹��
		if(componentMap.containsKey(id)) {
			LMNComponent component = 
				(LMNComponent)componentMap.get(id); //=button? textarea? label?
			component.resetMembrane(mem);
			return;
		}
		
		if(mem.getAtomCountOfFunctor(BUTTON_FUNCTOR)>0){
			LMNtalButton button = new LMNtalButton(this, mem);
			componentMap.put(id, button);
		}

		if(mem.getAtomCountOfFunctor(TEXTAREA_FUNCTOR)>0){
			LMNtalTextArea textarea = new LMNtalTextArea(this, mem);
			componentMap.put(id, textarea);
		}
		
		if(mem.getAtomCountOfFunctor(HTML_FUNCTOR)>0){
			LMNtalHtml html = new LMNtalHtml(this, mem);
			componentMap.put(id, html);
		}
		
		if(mem.getAtomCountOfFunctor(LABEL_FUNCTOR)>0){
			LMNtalLabel label = new LMNtalLabel(this, mem);
			componentMap.put(id, label);
		}

//		if(mem.getAtomCountOfFunctor(IMAGE_FUNCTOR)>0){
//			LMNtalImage image = new LMNtalImage(this, mem);
//			componentMap.put(key, image);
//		}

		if(mem.getAtomCountOfFunctor(TIMER_FUNCTOR)>0){
			LMNtalTimer timer = new LMNtalTimer(this, mem);
//			componentMap.put(key, timer);
		}

		
		/* LMNtalPanel���������̤ǡ������Ǥ�put���ʤ���
		 * searchGraphicMem�Ǵ������롥
		 * �ޤ���LMNtalPanel�λ��줢�뤫������å����롥
		 */
		LMNtalGraphic graphicPanel = searchGraphicMem(mem);
		if(graphicPanel != null){
			graphicPanel.setChildMem(mem);
		}
		
	}

	public void removeChildMem(String id){
		//componentMap��key��ID(key)�����ä��鹹��
		LMNComponent component = 
			(LMNComponent)componentMap.get(id); //=button? textarea? label?
		if(component == null) {
			return;
		}
		remove(component.getComponent()); // �����Ǻ��
		componentMap.remove(id);
		setVisible(true); // �����Ǻ�����
	}
	
	
	public static String getID(Membrane mem){
		/** ID("id")�����ä��Ȥ���ID��������� */
		String id = mem.getName();
//		Iterator idAtomIte = mem.atomIteratorOfFunctor(ID_FUNCTOR);
//		if(idAtomIte.hasNext()){
//			Atom atom = (Atom)idAtomIte.next();
//			id = atom.nth(0);
//		}
		return id;
	}
	
	/**
	 * ����Υ��ȥ�򸡺�����"size"���ȥ��������롥
	 * ���ȥ�������������������󥯤�⤵�Ȥ��롥
	 * @param mem �����оݤ���
	 */
	private void setSizeAtom(Membrane mem){
		Atom targetAtom;
		Iterator atomIte= mem.atomIteratorOfFunctor(SIZE_FUNCTOR);
		if(atomIte.hasNext()){
			targetAtom = (Atom)atomIte.next();
			try{
				if( (null != targetAtom) &&
					( (sizeX != Integer.parseInt(targetAtom.nth(0))) ||
					  (sizeY != Integer.parseInt(targetAtom.nth(1))) )){
					sizeUpdate = true;
				}
				sizeX = ((null != targetAtom) ? Integer.parseInt(targetAtom.nth(0)) : 0);
				sizeY = ((null != targetAtom) ? Integer.parseInt(targetAtom.nth(1)) : 0);
			}
			catch(NumberFormatException e){}
		}
		
	}

	/**
	 * ����Υ��ȥ�򸡺�����"killer"���ȥ��������롥
	 * "killer"���ȥब¸�ߤ������ϡ�killer�ե饰��Ω�Ƥ�
	 * @param mem �����оݤ���
	 */
	private void setKiller(Membrane mem) {
		// killer
		Iterator atomIte= mem.atomIteratorOfFunctor(KILLER_FUNCTOR);
		if(atomIte.hasNext()){
			killer = true;
		}
	}
	
	
	/** ������ɥ����������� */
	public void makeWindow(){
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		System.out.println("make window");
		setTitle(windowName);
		layout = new GridBagLayout();
		getContentPane().setLayout(layout);
		
		setSize(sizeX, sizeY);
		
		// killer��true�ΤȤ���������ɥ����Ĥ���ȡ��ץ���������λ������
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// �Ĥ���ݤν���
				if(killer){
					String dump = Dumper.dump(mymem.getTask().getRoot()).toString();
					dump = dump.replaceAll("\\}", "\\}\n");
					dump = dump.replaceAll("\\{", "\n\\{");
					dump = dump.substring(1);
					System.out.println(dump);
					System.exit(0);
				}
			}
		});

		setVisible(true);
	}
	
	public GridBagLayout getGridBagLayout(){
		return layout;
	}
	
	///////////////////////////////////////////////////////////////////////////

	
	/**
	 * �����줫�饰��ե��å����õ�����롥
	 * LMNtalGraphic�λ���->LMNtalGraphic���֤���
	 * LMNtalGraphic�λ���Ǥʤ�->null���֤�
	 * @param mem
	 * @return LMNtalGraphic
	 */
	private LMNtalGraphic searchGraphicMem(Membrane mem){
		while(!mem.isRoot()){
			String key = getID(mem); // ID�����
			if(key != null){
				
				// graphic.�Ȥ������ȥ�򤽤���˻��äƤ�����
				if(mem.getAtomCountOfFunctor(GRAPHIC_FUNCTOR)>0){
					
					// ���Ǥ���Ͽ����Ƥ��餳�ä������֤�
					if(componentMap.containsKey(key)){
						return (LMNtalGraphic)componentMap.get(key);
					}
					
					LMNtalGraphic graphicPanel = new LMNtalGraphic(this, mem);
					componentMap.put(key, graphicPanel);
					return graphicPanel; // graphic��¸�ߤ�����(���Τ�)
				}
			}
			mem = mem.getParent();
		}
		return null; // graphic.��¸�ߤ��ޤ���Ǥ�����
	}
	
	
}