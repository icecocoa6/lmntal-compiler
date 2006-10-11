package toolkit;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Iterator;

import runtime.Atom;
import runtime.Functor;
import runtime.Membrane;
import runtime.SymbolFunctor;

abstract
public class LMNComponent {
	
	private Component component;
	private Membrane mymem; //���äƤ��������¸���롣
	
	final static
	private Functor POSITION_FUNCTOR = new SymbolFunctor("position", 2);
	
	final static
	private Functor WEIGHT_FUNCTOR = new SymbolFunctor("weight", 2);
	
	final static
	private Functor LABEL_FUNCTOR = new SymbolFunctor("label", 1);
	
	final static //windowSize�ǤϤʤ��ƥ����������
	private Functor SIZE_FUNCTOR = new SymbolFunctor("size", 2);
	
	private GridBagConstraints gbc = new GridBagConstraints(); //����

	private boolean isLabelUpdate = false;
	
	
	public LMNComponent(LMNtalWindow lmnWindow, Membrane mem){
		mymem = mem;
		setPosition(mem);
		setSize(mem);
		setWeight(mem);
		setMembrane(mem);
		gbc.fill = GridBagConstraints.BOTH; //�ǥե���ȤǤҤ��ΤФ�
		component = initComponent();
		GridBagLayout layout = lmnWindow.getGridBagLayout();
		layout.setConstraints(component, gbc);
		lmnWindow.add(component);
		lmnWindow.validate(); // ������ɥ� �ι���
	}
	
	public void setMembrane(Membrane mem){
		//LMNtalButton -- Label���ɲ�
		//LMNtalTextArea -- Text���ɲ�
	}
	
	abstract public Component initComponent();
		
	/** position(X,Y)�Υ��ȥब���ä��Ȥ���gridx��gridy���������(ñ�̤�GridBag)�� */
	public void setPosition (Membrane mem) {
		int positionX = 0;
		int positionY = 0;
		Iterator positionAtomIte = mem.atomIteratorOfFunctor(POSITION_FUNCTOR);
		if(positionAtomIte.hasNext()){
			Atom atom = (Atom)positionAtomIte.next();
			positionX = Integer.parseInt(atom.nth(0));
			positionY = Integer.parseInt(atom.nth(1));
		}
		gbc.gridx = positionX;
		gbc.gridy = positionY;			
	}

	/** size(X,Y)�Υ��ȥब���ä��Ȥ���gridwidth��gridheight���������(ñ�̤�GridBag)�� */
	public void setSize (Membrane mem) {
		int sizeX = 1; //�ǥե���ȤǤϣ�����
		int sizeY = 1; //�ǥե���ȤǤϣ��ι⤵
		Iterator sizeAtomIte = mem.atomIteratorOfFunctor(SIZE_FUNCTOR);
		if(sizeAtomIte.hasNext()){
			Atom atom = (Atom)sizeAtomIte.next();
			sizeX = Integer.parseInt(atom.nth(0));
			sizeY = Integer.parseInt(atom.nth(1));
		}
		gbc.gridwidth = sizeX;
		gbc.gridheight = sizeY;			
	}
		
	/** weight(X,Y)�Υ��ȥब���ä��Ȥ���weightx��weighty���������(ñ�̤�GridBag)��*/
	public void setWeight (Membrane mem) {
		double weightX = 0;
		double weightY = 0;
		Iterator weightAtomIte = mem.atomIteratorOfFunctor(WEIGHT_FUNCTOR);
		if(weightAtomIte.hasNext()){
			Atom atom = (Atom)weightAtomIte.next();
			weightX = Double.parseDouble(atom.nth(0));
			weightY = Double.parseDouble(atom.nth(1));
		}
		System.out.println(weightX);
		gbc.weightx = weightX;
		gbc.weighty = weightY;			
	}
	
	/** label("")�Υ��ȥब���ä��Ȥ���label��Ž�����Ƥ�������� */
	public String getLabel(Membrane mem){
		String label = "object";
		Iterator labelAtomIte = mem.atomIteratorOfFunctor(LABEL_FUNCTOR);
		if(labelAtomIte.hasNext()){
			Atom atom = (Atom)labelAtomIte.next();
			if(label != atom.nth(0))
			{
				label = atom.nth(0);
				isLabelUpdate = true;
			}
		}
		return label;
	}
		
	public void resetMembrane (Membrane mem){
		mymem = mem;
		setMembrane(mem);
	}

	public boolean getLabelUpdate(){
		return isLabelUpdate;
	}
	
	public void setLabelUpdate(boolean update){
		isLabelUpdate = update;
	}
	
	public void addAtom(){}
	
	public Membrane getMymem(){
		return mymem;
	}

}
