package test3d;
/**
 * @author TOKUNAGA Ken-ichi iam@tokunagakenichi.net
 * @version Time-stamp: <03/02/28 03:17:20 tkenichi>
 *
 * ʪ�Τ�ԥå��󥰤��ơ������ޥ�������������ɽ������롣
 */

import com.sun.j3d.utils.picking.*;
import com.sun.j3d.utils.picking.behaviors.*;
import com.sun.j3d.utils.behaviors.mouse.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.media.j3d.*;
import javax.vecmath.*;

public class MouseDragBehavior extends Behavior 
    implements MouseBehaviorCallback{

    Vector3d currV3d = new Vector3d(),diffV3d = new Vector3d();
    Point3d cursorP3d = new Point3d(),viewP3d = new Point3d();
    Vector4d currV4d = new Vector4d();
    private PickingCallback callback = null;
	private LMNTransformGroup targetObj=null;

    private PickCanvas pickCanvas;
    private WakeupOr wakeupCondition;
    private boolean picked = false;
    
    // �ԥå��󥰤�����ɸ���Ǽ����
    private TransformGroup currGrp;
    private Transform3D 
        currT3d = new Transform3D(),       // �ԥå��󥰤�����ɸ
        localT3d = new Transform3D(),      // �ɽ��ɸ
        inverse = new Transform3D(),       // ���Ѵ�
        plate2world = new Transform3D();   // imageplate ���鲾�۶��֤ؤ��Ѵ�

    public MouseDragBehavior(Canvas3D canvas,
                             BranchGroup root,
                             Bounds bounds){
        currGrp = new TransformGroup();
        currGrp.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        currGrp.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        currGrp.setCapability(TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ);
        // Behavior �� Leaf �����餽�ξ�� BranchGroup �ˤĤ���
        root.addChild(currGrp);
        pickCanvas = new PickCanvas(canvas, root);
        pickCanvas.setMode(PickCanvas.BOUNDS);
        setSchedulingBounds(bounds);
    }

    public void setMode(int pickMode) {
        pickCanvas.setMode(pickMode);
    }
    public int getMode() {
        return pickCanvas.getMode();
    }
    public void setTolerance(float tolerance) {
        pickCanvas.setTolerance(tolerance);
    }
    public float getTolerance() {
        return pickCanvas.getTolerance();
    }
    
    public void initialize() {
        // �ޥ����򲡤����Ȥ���Υ�����Ȥ����ɥ�å������Ȥ���
        // ��ư����
        WakeupCriterion[] conditions = new WakeupCriterion[3];
        conditions[0] = new WakeupOnAWTEvent(MouseEvent.MOUSE_DRAGGED);
        conditions[1] = new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED);
        conditions[2] = new WakeupOnAWTEvent(MouseEvent.MOUSE_RELEASED);
        wakeupCondition = new WakeupOr(conditions);
        wakeupOn(wakeupCondition);
    }
    
    public void processStimulus (Enumeration criteria) {
        WakeupCriterion wakeup;
        AWTEvent[] evt = null;
        int xpos = 0, ypos = 0;
        MouseEvent mevent;

        // AWTEvent ����Ф�
        while(criteria.hasMoreElements()) {
            wakeup = (WakeupCriterion)criteria.nextElement();
            if (wakeup instanceof WakeupOnAWTEvent)
                evt = ((WakeupOnAWTEvent)wakeup).getAWTEvent();
        }
    
        // MouseEvent �ξ��
        if (evt[0] instanceof MouseEvent){
            mevent = (MouseEvent) evt[0];
            // �ޥ������������2�����ԥ������ɸ
            xpos = mevent.getPoint().x;
            ypos = mevent.getPoint().y;

            switch(mevent.getID()){
            case MouseEvent.MOUSE_PRESSED:
                // �����줿�Ȥ�
                System.out.println("mouse pressed");
                // �ԥå��󥰤���
                if (!mevent.isAltDown() && mevent.isMetaDown()){
                    pickingTransformGroup(xpos,ypos);
                }
                if (mevent.isAltDown() && mevent.isMetaDown()){
                	altpickingTransformGroup(xpos,ypos);
                }
                break;
            case MouseEvent.MOUSE_RELEASED:
                // Υ�����Ȥ�
                // �ԥå��󥰤����
            	if(targetObj!=null)targetObj.setVisible(true);
                picked = false;
                break;
            case MouseEvent.MOUSE_DRAGGED:
                // �ɥ�å������Ȥ�
                // �ԥå��󥰤��줿 TransformGroup ��ư����
                if (picked && !mevent.isAltDown() && mevent.isMetaDown()){
                    translate(xpos,ypos);
                }
	            if (picked && mevent.isAltDown() && mevent.isMetaDown()){
	                alttranslate(xpos,ypos);
	            }
                break;
            default:
                System.out.println("other event");
            }
        }

        wakeupOn (wakeupCondition);
    }

    // �ԥå��󥰤��� TransformGroup �� currGrp �˳�Ǽ����
    private void pickingTransformGroup(int xpos,int ypos){
        TransformGroup tg = null;
        
        // PickCanvas ��Ĥ��äƥԥå��󥰤��Ƥ���Ρ��ɤ�õ��
        pickCanvas.setShapeLocation(xpos, ypos);
        // LineArray �� Vertex ��ξ�����ŤʤäƤ������
        // Vertex ����Ф�����ˡ�pickAll() �����Ƽ��Ф�
        PickResult pr[] = pickCanvas.pickAll();

        if(pr != null){
            for(int i=0;i<pr.length;i++){
                System.out.println(i + " = " + pr[i].getNode(PickResult.GROUP));
                // �ԥå��󥰤�����̤��ɤ߽񤭲�ǽ�� TransformGroup ����Ф�
                if ((pr[i] != null) &&
                    ((tg = (TransformGroup)pr[i].getNode(PickResult.TRANSFORM_GROUP)) 
                     != null) &&
                    (tg.getCapability(TransformGroup.ALLOW_TRANSFORM_READ)) && 
                    (tg.getCapability(TransformGroup.ALLOW_TRANSFORM_WRITE)) && 
                    (tg.getCapability(TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ))){
                    
                    System.out.println("success to pick " + tg);
                    
                    picked = true;
                    // �ԥå��󥰤�����̤λ��Ȥ� currGrp �˥��åȤ���
                    currGrp = tg;
                    targetObj=(LMNTransformGroup)currGrp;
                    targetObj.setVisible(false);
                    // ʪ�Τ���äƤ����ɸ�ϤȤ��ε��Ѵ�������
                    currGrp.getLocalToVworld(localT3d);
                    //System.out.println(localT3d);
                    inverse.invert(localT3d);
                    System.out.println(inverse);
                    
                    // ImagePlate ���鲾�۶��֤ؤ��Ѵ����������
                    pickCanvas.getCanvas().
                        getImagePlateToVworld(plate2world);
                    // �����ΰ��֤�����
                    pickCanvas.getCanvas().getCenterEyeInImagePlate(viewP3d);
                    plate2world.transform(viewP3d);
                    
                    // �ޥ�����������ΰ��֤�ʪ�Τ��碌��
                    translate(xpos,ypos);
                    //            freePickResult(pr);
                    break;
                }
            }
        }
    }

    // �ԥå��󥰤��� TransformGroup �� currGrp �˳�Ǽ����
    private void altpickingTransformGroup(int xpos,int ypos){
        TransformGroup tg = null;
        
        // PickCanvas ��Ĥ��äƥԥå��󥰤��Ƥ���Ρ��ɤ�õ��
        pickCanvas.setShapeLocation(xpos, ypos);
        // LineArray �� Vertex ��ξ�����ŤʤäƤ������
        // Vertex ����Ф�����ˡ�pickAll() �����Ƽ��Ф�
        PickResult pr[] = pickCanvas.pickAll();

        if(pr != null){
            for(int i=0;i<pr.length;i++){
                System.out.println(i + " = " + pr[i].getNode(PickResult.GROUP));
                // �ԥå��󥰤�����̤��ɤ߽񤭲�ǽ�� TransformGroup ����Ф�
                if ((pr[i] != null) &&
                    ((tg = (TransformGroup)pr[i].getNode(PickResult.TRANSFORM_GROUP)) 
                     != null) &&
                    (tg.getCapability(TransformGroup.ALLOW_TRANSFORM_READ)) && 
                    (tg.getCapability(TransformGroup.ALLOW_TRANSFORM_WRITE)) && 
                    (tg.getCapability(TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ))){
                    
                    System.out.println("success to pick " + tg);
                    
                    picked = true;
                    // �ԥå��󥰤�����̤λ��Ȥ� currGrp �˥��åȤ���
                    currGrp = tg;

                    
                    // ʪ�Τ���äƤ����ɸ�ϤȤ��ε��Ѵ�������
                    currGrp.getLocalToVworld(localT3d);
                    //System.out.println(localT3d);
                    inverse.invert(localT3d);
                    System.out.println(inverse);
                    
                    // ImagePlate ���鲾�۶��֤ؤ��Ѵ����������
                    pickCanvas.getCanvas().
                        getImagePlateToVworld(plate2world);
                    // �����ΰ��֤�����
                    pickCanvas.getCanvas().getCenterEyeInImagePlate(viewP3d);
                    plate2world.transform(viewP3d);
                    
                    // �ޥ�����������ΰ��֤�ʪ�Τ��碌��
                    translate(xpos,ypos);
                    //            freePickResult(pr);
                    break;
                }
            }
        }
    }


    private void translate(int xpos,int ypos){
        // �ޥ������������imageplate��ΰ��֤�����
        pickCanvas.getCanvas().
            getPixelLocationInImagePlate(xpos,ypos,cursorP3d);
        plate2world.transform(cursorP3d);
        // �ԥå��󥰤���ʪ�Τκ�ɸ���̤�
        currGrp.getTransform(currT3d);
        currT3d.get(currV3d);
        currV4d.set(currV3d.x,
                    currV3d.y,
                    currV3d.z,
                    1);
        localT3d.transform(currV4d);
        currV3d.set(currV4d.x,
                    currV4d.y,
                    currV4d.z);
        //System.out.println("cursor = " + cursorP3d);
        //System.out.println("current = " + currV3d);

        // ʪ�Τκ�ɸ��ޥ�����������ΰ��֤ˤ��碌��
        // view �ȿ�ľ������ʿ�̾���ư����
        currV3d.sub(viewP3d);
        cursorP3d.sub(viewP3d);
        double alpha = 
            (currV3d.x * viewP3d.x +
             currV3d.y * viewP3d.y +
             currV3d.z * viewP3d.z) /
            (cursorP3d.x * viewP3d.x +
             cursorP3d.y * viewP3d.y +
             cursorP3d.z * viewP3d.z);
        currV3d.scaleAdd(alpha,cursorP3d,viewP3d);
        // ����ɸ��ɽ��ɸ��ľ��
        currV4d.set(currV3d.x,
                    currV3d.y,
                    currV3d.z,
                    1);
        inverse.transform(currV4d);
        currV3d.set(currV4d.x,
                    currV4d.y,
                    currV4d.z);
        currT3d.setTranslation(currV3d);
        currGrp.setTransform(currT3d);
        transformChanged(MouseBehaviorCallback.TRANSLATE,
                         currT3d);
    }


    private void alttranslate(int xpos,int ypos){
        // �ޥ������������imageplate��ΰ��֤�����
        pickCanvas.getCanvas().
            getPixelLocationInImagePlate(xpos,ypos,cursorP3d);
        plate2world.transform(cursorP3d);
        // �ԥå��󥰤���ʪ�Τκ�ɸ���̤�
        currGrp.getTransform(currT3d);
        currT3d.get(currV3d);
        currV4d.set(currV3d.x,
                    currV3d.y,
                    currV3d.z,
                    1);
        localT3d.transform(currV4d);
        currV3d.set(currV4d.x,
                    currV4d.y,
                    currV4d.z);
        //System.out.println("cursor = " + cursorP3d);
        //System.out.println("current = " + currV3d);

        // ʪ�Τκ�ɸ��ޥ�����������ΰ��֤ˤ��碌��
        // view �ȿ�ľ������ʿ�̾���ư����
        currV3d.sub(viewP3d);
        cursorP3d.sub(viewP3d);
        double alpha = 
            (currV3d.x * viewP3d.x +
             currV3d.y * viewP3d.y +
             currV3d.z * viewP3d.z) /
            (cursorP3d.x * viewP3d.x +
             cursorP3d.y * viewP3d.y +
             cursorP3d.z * viewP3d.z);
        currV3d.scaleAdd(alpha,cursorP3d,viewP3d);
        // ����ɸ��ɽ��ɸ��ľ��
        currV4d.set(currV3d.x,
                    currV3d.y,
                    currV3d.z,
                    1);
        inverse.transform(currV4d);
        currV3d.set(currV4d.x,
                    currV4d.y,
                    currV4d.z);
        currT3d.setTranslation(currV3d);
        currGrp.setTransform(currT3d);
        transformChanged(MouseBehaviorCallback.TRANSLATE,
                         currT3d);
    }


    
    public void setupCallback(PickingCallback callback){
        this.callback = callback;
    }
    
    public void transformChanged(int type,Transform3D transform){
        if(callback != null){
            callback.transformChanged(type,currGrp);
        }
    }
}
