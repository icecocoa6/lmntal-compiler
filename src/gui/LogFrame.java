package gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * �¹����LMNtal�ץ�������ɽ�����뤿���LOG������ɥ�
 * @author nakano
 *
 */
public class LogFrame extends JFrame implements ChangeListener {

	/////////////////////////////////////////////////////////////////
	// ���
	
	final static
	public int WINDOW_WIDTH = LMNtalFrame.WINDOW_WIDTH;
	
	final static
	public int WINDOW_HIEGHT = 200;
	
	final static
	public String TITLE = "Log Panel";
	
	/////////////////////////////////////////////////////////////////
	
	private JTextArea logArea = new JTextArea("log");

	private LMNtalFrame mainFrame;
	
	private JSlider timeSlider_ = new JSlider(JSlider.HORIZONTAL, 0, 0, 0);
	
	private CommonListener commonListener_ = new CommonListener(this);
	
	private boolean timeSliderResizing_ = false;
	
	/////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	public LogFrame(LMNtalFrame f) {
		
		mainFrame = f;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(WINDOW_WIDTH, WINDOW_HIEGHT);
		setLocation(0, LMNtalFrame.WINDOW_HIEGHT);
		
		initComponents();
		
		setVisible(true);
	}
	/////////////////////////////////////////////////////////////////
	
	public void addTime(){
		timeSliderResizing_ = true;
		timeSlider_.setMaximum(timeSlider_.getMaximum() + 1);
		timeSlider_.setValue(timeSlider_.getMaximum());
		timeSlider_.setMajorTickSpacing(1);
		timeSlider_.setPaintTicks(true);
		timeSliderResizing_ = false;
	}
	
	public String getLog(){
		return logArea.getText();
	}
	
	private void initComponents() {
		
		setTitle(TITLE);
		logArea.setEditable(false);
		logArea.setLineWrap(true);
		timeSlider_.setSnapToTicks(true);
		timeSlider_.addChangeListener(this);
		setLayout(new BorderLayout());
		add(timeSlider_, BorderLayout.NORTH);
		add(new JScrollPane(logArea), BorderLayout.CENTER);
	}
	
	public void revokeTime(){
		timeSliderResizing_ = true;
		timeSlider_.setValue(0);
		timeSlider_.setMaximum(0);
		timeSliderResizing_ = false;
	}
	
	public void setLog(String log){
		logArea.setText(log);
	}

	///////////////////////////////////////////////////////////////////////////
	public void stateChanged(ChangeEvent arg0) {
		if(timeSliderResizing_){ return; }
		commonListener_.setState(timeSlider_.getValue());
	}
}