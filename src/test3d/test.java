package test3d;

import javax.swing.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsConfiguration;

import com.sun.j3d.utils.picking.behaviors.PickTranslateBehavior;
import com.sun.j3d.utils.picking.behaviors.PickingCallback;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.Sphere;
import javax.vecmath.*;

public class test extends JFrame {
		Canvas3D canvas;
		Sphere sphere;
		Shape3D shape = new Shape3D();
		public BranchGroup createSceneGraph() {
			BranchGroup objRoot = new BranchGroup();
			TransformGroup objKey = new TransformGroup();
			TransformGroup objKey2 = new TransformGroup();
			objRoot.addChild(createLight());
		

			
			BoundingSphere bounds = new BoundingSphere();
			bounds.setRadius( 10.0 );
			
			MouseRotate rotat = new MouseRotate( objKey );
			rotat.setSchedulingBounds( bounds );
			objKey.addChild(rotat);
        	MouseZoom zoom = new MouseZoom( objKey );
        	zoom.setSchedulingBounds( bounds );
        	objKey.addChild(zoom);
        	objKey.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        	objKey.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        	objKey.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
        	objKey2.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        	objKey2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        	objKey2.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
        	PickTranslateBehavior translator = new PickTranslateBehavior( objRoot, canvas, bounds );
        	objRoot.addChild( translator );
        	objRoot.addChild(objKey);
        	objRoot.addChild(objKey2);
        	
        	/* Appearance����� */
        	Appearance ap = new Appearance();
        	Material ma = new Material();
        	ma.setDiffuseColor(0.0f, 1.0f, 1.0f);
        	ap.setMaterial(ma);

        	/* Sphere���饹��Ȥä�ʪ�Τ�������� */
        	sphere = new Sphere( 0.1f, ap);
        	objKey.addChild(sphere);
        	
        	
        	/* ��������*/
    		BranchGroup obj3 = new BranchGroup();


    		Point3d[] vertex = new Point3d[2];
    		
    		vertex[0] = new Point3d(0.0,0.0,0.0);
    		vertex[1] = new Point3d(-0.8,-0.8,-0.8);
    		
    		/* LineArray���饹��Ȥä�ʪ�Τη�������� */
    		LineArray geometry = new LineArray(vertex.length, 
    				GeometryArray.COORDINATES | GeometryArray.COLOR_3);
    		/*LineArray geometry = new LineArray(vertex.length, 
    				GeometryArray.COORDINATES | GeometryArray.COLOR_3);*/
    		geometry.setCoordinates(0, vertex);
    		//eometry.updateData();
    		/* ��ĺ�����Ȥ˿������ */
    		geometry.setColor(0, new Color3f(Color.magenta));
    		geometry.setColor(1, new Color3f(Color.cyan));

    		/* ��������Geometry�򸵤�ʪ�Τ���� */
    		//Shape3D shape = new Shape3D(geometry);
    		shape.setGeometry(geometry);
    		shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
    		shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
    		obj3.addChild(shape);
    		
        	objKey2.addChild(obj3);
        	objRoot.compile();
    		translator.setupCallback(new PickingCallback(){
                Vector3d vect = new Vector3d();   // ��ɸ���Ǽ����
                Transform3D t3d = new Transform3D();
                public void transformChanged(int type,TransformGroup tg){
                    if(type == PickingCallback.TRANSLATE){
                		Point3d[] vertex = new Point3d[2];
                		vertex[0] = new Point3d(0.0,0.0,0.0);
                		vertex[1] = new Point3d(0.8,0.8,0.8);
                		LineArray geometry = new LineArray(vertex.length, 
                				GeometryArray.COORDINATES | GeometryArray.COLOR_3);
                		geometry.setCoordinates(0, vertex);
                		geometry.setColor(0, new Color3f(Color.magenta));
                		geometry.setColor(1, new Color3f(Color.cyan));
                		shape.setGeometry(geometry);

                        
                    }
                }
            });

        	return objRoot;
        }

	/* DirectionalLight��������� */
	private Light createLight(){
		DirectionalLight light = new DirectionalLight( true,
                        new Color3f(0.0f, 1.0f, 1.0f),
                	new Vector3f(0.0f, 0.0f, -1.0f));

		light.setInfluencingBounds(new BoundingSphere(new Point3d(), 100.0));
  
		return light;
	}

        public test() {
                getContentPane().setLayout(new BorderLayout());

                GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();

                canvas = new Canvas3D(config);
                getContentPane().add("Center", canvas);

                BranchGroup scene = createSceneGraph();
                SimpleUniverse universe = new SimpleUniverse(canvas);

                universe.getViewingPlatform().setNominalViewingTransform();

                universe.addBranchGraph(scene);
        }

        public static void main(String[] args) {
		test sample = new test();

		/* ���������� */
		sample.setBounds( 10, 10, 480, 480);

                /* ��λ�������ɲ� */
                sample.addWindowListener(new WindowAdapter(){
                        public void windowClosing(WindowEvent e){System.exit(0);}
                });

                /* �ºݤ�ɽ������ */
                sample.setVisible(true);
        }
}

