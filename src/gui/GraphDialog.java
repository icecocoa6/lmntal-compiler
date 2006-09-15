package gui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/** graphic�⡼�ɵ�ư��˱�����å������Ȥ��ν��� */
public class GraphDialog extends JDialog implements ActionListener{
	public boolean flags[];
	public String names[];
	private JCheckBox checkbox[];
	private int length;
	
	/** graphic�⡼�ɤ���ư��ΤȤ�����������ɽ�� */
	public GraphDialog(JFrame parent){
		super(parent,"It's LMNtal",true);
		setSize(400,250);
	}
	
	/** �������������褹���Τ����� */
	public void initButtons(){
		Container contentPane = getContentPane();
		JPanel panel = new JPanel();
		checkbox = new JCheckBox[length];
		for(int i = 0;i < length;i++){
			checkbox[i] = new JCheckBox(names[i],flags[i]);
			panel.add(checkbox[i]);
		}
		contentPane.add("Center",panel);
		JPanel panel2 = new JPanel();
		JButton button = new JButton("OK");
		button.addActionListener(this);
		panel2.add(button);
		contentPane.add("South",panel2);
	}
	
	/** Ĺ���μ��� */
	public void setLength(int length){
		this.length = length;
	}
	
	/** �ե饰�μ��� */
	public void setFlags(boolean flags[]){
		this.flags = flags;
	}
	
	/** ̾���μ��� */
	public void setNames(String names[]){
		this.names = names;
	}
	
	/** �����å�����Ƥ��뤫�����Τ��Ĥ���(�����ʤ�����) */	
	public void actionPerformed(ActionEvent e){
		for(int i = 0;i < length;i++){
			flags[i] = checkbox[i].isSelected();
		}
		setVisible(false);
	}
	
	
}