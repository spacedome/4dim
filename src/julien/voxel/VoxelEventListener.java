package julien.voxel;

import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;


/**
 * GL event listener, as well as all other listeners for the GLCanvas.
 * Render calls (i.e init and draw) are sent to the Render object.
 * @author Julien Brenneck
 * @version December 18, 2013
 */
public class VoxelEventListener implements GLEventListener, KeyListener, MouseListener, MouseMotionListener {
    /** keep pointer to associated canvas so we can refresh the screen (equivalent to glutPostRedisplay()) */     
    private Render canvas;
    /** Mouse position */
    private float[] mPos = {0,0, 0,0};
    /** Robot overrides mouse control */
    private Robot robo;
    /** Has the user pressed escape */
    private boolean escape = true;
    /** Invisible cursor */
    private Cursor blankCursor;

    /** Default constructor */
    public VoxelEventListener(Render canvas) {
        this.canvas = canvas;
        try {
			robo = new Robot();
		} catch (AWTException e) {
			System.err.println("AWT functions could not be loaded : Robot");
		}
    }

	@Override
	public void init(GLAutoDrawable drawable) {
		canvas.setup(drawable);
		
		// Transparent 16 x 16 pixel cursor image.
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		// Create a new blank cursor.
		blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {	}

	@Override
	public void display(GLAutoDrawable drawable) {
		if(!escape){
			//force mouse to center of GL component. allows for better mouse movement tracking
			robo.mouseMove(canvas.getLocationOnScreen().x+canvas.getWidth()/2,
					canvas.getLocationOnScreen().y + canvas.getHeight()/2);
			
		}
		canvas.draw(drawable);	
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		canvas.resize(drawable, x, y, width, height);	
		mPos[0] = canvas.getWidth();
		mPos[1] = canvas.getHeight();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(escape){
			robo.mouseMove(canvas.getWidth()/2, canvas.getHeight()/2);
			canvas.setCursor(blankCursor);
			escape = false;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(!escape){
		canvas.rChange[0] += (2.0f*e.getX() - mPos[0])/GUI.mouseSens;
		canvas.rChange[1] -= (2.0f*e.getY() - mPos[1])/GUI.mouseSens;
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// up-arrow or w
		if(e.getKeyCode()==38||e.getKeyCode()==87){
			canvas.lChange[0] = 0.15f;
		}
		// down-arrow
		if(e.getKeyCode()==40||e.getKeyCode()==83){
			canvas.lChange[0] = -0.15f;
		}
		// right-arrow
		if(e.getKeyCode()==39||e.getKeyCode()==68){
			canvas.lChange[2] = 0.15f;
		}
		// left-arrow
		if(e.getKeyCode()==37||e.getKeyCode()==65){
			canvas.lChange[2] = -0.15f;
		}
		// space
		if(e.getKeyCode()==32){
			canvas.lChange[1] = 0.15f;
		}
		// shift
		if(e.getKeyCode()==16){
			canvas.lChange[1] = -0.15f;
		}
		// escape
		if(e.getKeyCode()==27){
			escape = true;
			canvas.setCursor(Cursor.getDefaultCursor());
		}
		// q - force update
		if(e.getKeyCode()==81){
			canvas.chMan.updateUnload(canvas.location[0], canvas.location[2]);
		}
		// e - force update
		if(e.getKeyCode()==69){
			canvas.chMan.updateLoad(canvas.location[0], canvas.location[2]);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// up-arrow or w
				if(e.getKeyCode()==38||e.getKeyCode()==87){
					canvas.lChange[0] = 0.0f;
				}
				// down-arrow
				if(e.getKeyCode()==40||e.getKeyCode()==83){
					canvas.lChange[0] = 0.0f;
				}
				// right-arrow
				if(e.getKeyCode()==39||e.getKeyCode()==68){
					canvas.lChange[2] = 0.0f;
				}
				// left-arrow
				if(e.getKeyCode()==37||e.getKeyCode()==65){
					canvas.lChange[2] = 0.0f;
				}
				// space
				if(e.getKeyCode()==32){
					canvas.lChange[1] = 0.0f;
				}
				// control
				if(e.getKeyCode()==16){
					canvas.lChange[1] = 0.0f;
				}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}
}