package gui2;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * ����̾����������Listener
 * @author nakano
 *
 */
public class SliderMouseListener implements MouseWheelListener {
	static
	private SubFrame subFrame;
	
	public SliderMouseListener(SubFrame f) {
		if(f == null){ return; }
		subFrame = f;
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		if(subFrame == null){ return; }
		
		subFrame.setSliderValue(subFrame.getSliderValue() - e.getWheelRotation());
	}

}
