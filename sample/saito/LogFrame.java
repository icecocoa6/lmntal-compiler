package gui;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JPanel;
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
public class LogFrame extends JPanel{

	/////////////////////////////////////////////////////////////////
	// ���
	
	final static
	public int WINDOW_WIDTH = LMNtalFrame.WINDOW_WIDTH-SubFrame.WINDOW_WIDTH;
	
	final static
	public int WINDOW_HEIGHT = 200;
	
	final static
	public String TITLE = "Log Panel";
	
	/////////////////////////////////////////////////////////////////
	
	static
	private JTextArea logArea = new JTextArea("log");

	static
	private LMNtalFrame mainFrame;
	
	static
	private JSlider timeSlider_ = new JSlider(JSlider.HORIZONTAL, 0, 0, 0);
	
	static
	private Commons commonListener_;
	
	static
	private boolean timeSliderResizing_ = false;
	
	/////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	public LogFrame(LMNtalFrame f) {
		
		mainFrame = f;
		
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setLocation(0, LMNtalFrame.WINDOW_HEIGHT);
		commonListener_ = new Commons(this);
		
		initComponents(this);
//		setTitle(TITLE);
		
		setVisible(true);
	}
	/////////////////////////////////////////////////////////////////
	
	static
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
	
	static
	public void initComponents(Container container) {
		
		logArea.setEditable(false);
		logArea.setLineWrap(true);
		timeSlider_.setSnapToTicks(true);
		timeSlider_.addChangeListener(new LogChangeListener());
		container.setLayout(new BorderLayout());
		container.add(timeSlider_, BorderLayout.NORTH);
		container.add(new JScrollPane(logArea), BorderLayout.CENTER);
	}
	
	static
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
	
	static
	private class LogChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent arg0) {
			if(timeSliderResizing_){ return; }
			commonListener_.loadState(timeSlider_.getValue());
		}
	}
}