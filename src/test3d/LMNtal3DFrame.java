package test3d;

import java.awt.*;
import javax.swing.*;

import java.awt.event.*;
import java.awt.GraphicsConfiguration;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;

import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import javax.vecmath.*;

import runtime.Env;

import com.sun.j3d.utils.behaviors.mouse.*;
import com.sun.j3d.utils.picking.behaviors.*;








class MyThread extends Thread {
	LMNtal3DFrame f;
	private static int i=0;
	
	MyThread(LMNtal3DFrame ff) {
		f = ff;
		f.LMNtal3DFrame_init();
		//f.lmnPanel.creat(f.objBranch);
		f.lmnPanel.run_Graph3DLayout();
		System.out.println("MyThread");
	}
	
	public void run() {
		while(true) {
			try {
				//System.out.println(i);
				//f.busy = false;
				i++;
				//f.lmnPanel.creat(f.objBranch);
//				if(i<2)
				sleep(1000);
				//f.lmnPanel.getGraph3DLayout().make_atom(f.objKey);
			} catch (Exception e) {
			}
		}
	}
}
public class LMNtal3DFrame extends JFrame  implements KeyListener {
	public static boolean fWindow=false;
	public boolean busy = true;
	public boolean running = true;
	public LMNGraph3DPanel lmnPanel = null;
	private static BranchGroup objRoot;
	public TransformGroup objKey;
	public static BranchGroup objBranch;

   	Thread th;
    public LMNtal3DFrame() {
		running = busy = false;
    	initComponents();
		th = new MyThread(this);
		th.start();
		
    }
	
    private void initComponents(){
    	lmnPanel = new LMNGraph3DPanel(this);
    }
    
    public void creat(){

    }
	public void waitBusy() {
//		lmnPanel.getGraphLayout().calc();
		/*lmnPanel.getGraph3DLayout().setAllowRelax(true);
		busy = true;
		while(busy) {
			try {
				this.wait(10);
			} catch (Exception e) {
			}
		}
		lmnPanel.getGraph3DLayout().setAllowRelax(false);*/
	}
	
    public BranchGroup createSceneGraph(Canvas3D canvas) {
    	objRoot = new BranchGroup();

		/*����������*/
		createLight(objRoot);
		
		/*�ʲ���objKey��Ϣ�����*/
		
		/*�ޥ����ȥ����ˤ��ޤβ�ž����ư�μ���*/
		objKey = new TransformGroup();
		objRoot.addChild(objKey);

		objKey.setCapability( TransformGroup.ALLOW_TRANSFORM_READ );
		objKey.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );
		objKey.setCapability(TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ);
		
/*
 * 		objRoot.setCapability( BranchGroup.ALLOW_PICKABLE_WRITE );
		objRoot.setCapability( BranchGroup.ALLOW_PICKABLE_READ );
*/		
		KeyNavigatorBehavior knb = new KeyNavigatorBehavior(objKey);
		objKey.addChild(knb);
        /*
        //�ޥ����ǻ����Ѵ�
	    MouseTranslate trans = new MouseTranslate( objKey );
	    trans.setSchedulingBounds( bounds );
		objKey.addChild(trans);

        */
		
		BoundingSphere bounds = new BoundingSphere();
		bounds.setRadius( 10.0 );
		knb.setSchedulingBounds( bounds );

		MouseRotate rotat = new MouseRotate( objKey );
	    rotat.setSchedulingBounds( bounds );
		objKey.addChild(rotat);
        MouseZoom zoom = new MouseZoom( objKey );
	    zoom.setSchedulingBounds( bounds );
		objKey.addChild(zoom);
		
		
		/*ʪ�Τΰ�ư��*/
      
		//PickRotateBehavior rotator = new PickRotateBehavior( objRoot, canvas, bounds);
		//PickTranslateBehavior translator = new PickTranslateBehavior( objRoot, canvas, bounds );
		MouseDragBehavior translator = new MouseDragBehavior(canvas, objRoot, bounds);
		//PickZoomBehavior zoomer = new PickZoomBehavior( objRoot, canvas, bounds);
	
		translator.setupCallback(new PickingCallback(){
            Vector3d vect = new Vector3d();   // ��ɸ���Ǽ����
            Transform3D t3d = new Transform3D();
            public void transformChanged(int type,TransformGroup tg){
                if(type == PickingCallback.TRANSLATE){
                    //tg.getTransform(t3d);
                    //t3d.get(vect);
                    //System.out.println(vect.x);
                    // �����Ǻ�ɸ�ν����򤹤롣

                    LMNTransformGroup lmntg = (LMNTransformGroup)tg;
                    lmntg.getTransform(t3d);
                    t3d.get(vect);
                    Double3DPoint p = new Double3DPoint(vect);
                    //System.out.println(lmntg.getEdgeNum());
                    lmntg.getMe().setPosition3d(p);
                    lmntg.setVisible(false);
                    lmnPanel.getGraph3DLayout().edgerelax(lmntg.getMe());
                    lmntg.setVisible(true);
                    
                }
            }
        });

		//objRoot.addChild( rotator );
		objRoot.addChild( translator );
		//objRoot.addChild( zoomer );
		
		
		Background background=new Background(new Color3f(1.0f,1.0f,1.0f));
			
		background.setApplicationBounds(bounds);
		objRoot.addChild(background);
		/*�ʲ���objTrans��Ϣ�����*/
		
		/*objTrans��������objKey��Child����Ͽ*/
		objBranch = new BranchGroup();
		objBranch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		objKey.addChild(objBranch);

		//gl.make_atom(objKey);
		lmnPanel.setBranch(objBranch);
		/*objRoot�Υ���ѥ����ɽ��*/
		objRoot.compile();
		//lmnPanel.creat(objBranch);
		//setBackground(Color.red);
		return objRoot;


    }

  
    
	/* DirectionalLight(�¹Ը���)��������� */
	private void createLight(BranchGroup objRoot){
		DirectionalLight light = new DirectionalLight( true,
        new Color3f(10.0f, 10.0f, 10.0f),
        new Vector3f(0.0f, -10.0f, -1.0f));
		/*̵�¸���*/
		light.setInfluencingBounds(new BoundingSphere(new Point3d(), Double.POSITIVE_INFINITY));
		objRoot.addChild(light);
		
		DirectionalLight light2 = new DirectionalLight( true,
		        new Color3f(10.0f, 10.0f, 10.0f),
		        new Vector3f(0.0f, 10.0f, 1.0f));
		/*̵�¸���*/
		/*
		light2.setInfluencingBounds(new BoundingSphere(new Point3d(), Double.POSITIVE_INFINITY));
		objRoot.addChild(light2);
		*/

	}

    public void LMNtal3DFrame2() {
        getContentPane().setLayout(new BorderLayout());

        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();

        Canvas3D canvas = new Canvas3D(config);
        getContentPane().add("Center", canvas);

        BranchGroup scene = createSceneGraph(canvas);
        SimpleUniverse universe = new SimpleUniverse(canvas);

        universe.getViewingPlatform().setNominalViewingTransform();
        universe.addBranchGraph(scene);
    }
	
	/** @return �롼�륹��åɤμ¹Ԥ��³���Ƥ褤���ɤ��� */
	public boolean onTrace() {
		if(Env.f3D) {
//			lmnPanel.start();
//			Env.gui.lmnPanel.setMembrane((runtime.Membrane)Env.theRuntime.getGlobalRoot());
			Env.threed.waitBusy();
//			lmnPanel.stop();
		}
		return Env.threed.running;
	}

    public void LMNtal3DFrame_init() {
    	LMNtal3DFrame2();
    	/*�����ȥ�����*/
    	setTitle("It's 3D-LMNtal");
    	/*Go ahead�ܥ�����ɲá�*/
		lmnPanel = new LMNGraph3DPanel(this);
    	JButton bt;
		getContentPane().add(bt=new JButton("Go ahead"), BorderLayout.SOUTH);
		bt.addActionListener(new ActionAdapter(this));
		
		/* ���������� */
		setBounds( 10, 10, 580, 580);
		/* ��λ�������ɲ� */
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){System.exit(0);}
		});
		/* �ºݤ�ɽ������ */
		setVisible(true);
    }

	public void keyPressed(KeyEvent e) {
	}
	
	public void keyReleased(KeyEvent e) {
	}
	
	public void keyTyped(KeyEvent e) {
		System.out.println(e.getKeyChar());
	}

}

class ActionAdapter implements ActionListener {
	LMNtal3DFrame frame;
	ActionAdapter(LMNtal3DFrame f) {
		frame = f;
	}
	public void actionPerformed(ActionEvent e) {
//		e.getSource();
		frame.busy = false;
	}
}

class MyKeyAdapter extends KeyAdapter {
	LMNtal3DFrame frame;
	MyKeyAdapter(LMNtal3DFrame f) {
		frame = f;
	}
	public void keyPressed(KeyEvent e) {
		frame.busy = false;
//		super.keyPressed(e);
	}
}