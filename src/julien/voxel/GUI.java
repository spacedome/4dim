package julien.voxel;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.jogamp.opengl.util.FPSAnimator;

public class GUI{
	// Define constants for the top-level container
	private static String TITLE = "Voxel";  // window's title
	private static final int CANVAS_WIDTH = 720;  // width of the drawable
	private static final int CANVAS_HEIGHT = 500; // height of the drawable
	private static final int FPS = 200; // animator's target frames per second
	private static Render canvas;
	private static FPSAnimator animator;
	public static int drawDistance = 3;
	public static int mouseSens = 3000;
	public static int chunkSize= 16;
	public static int updates = 2;
	public static boolean lighting = false;
	public static boolean wireframe = false;
	
	/** The entry main() method to setup the top-level container and animator */
	public static void main(String[] args) {
		try{
			String inS = JOptionPane.showInputDialog("Input draw distance (3-6 Recomended)");
			drawDistance = Integer.parseInt(inS);
			if(drawDistance < 1 || chunkSize > 10){
				drawDistance = 3;
			}
		} catch (Exception e) {
			drawDistance = 3;
	    }
		try{
			String inS = JOptionPane.showInputDialog("Mouse sensitivity (higher = less sensative, default is 3000)");
			mouseSens = Integer.parseInt(inS);
		} catch (Exception e) {
			mouseSens = 3000;
	    }
		try{
			String inS = JOptionPane.showInputDialog("Chunk size in voxels (8 - 64 is ok) default is 16");
			chunkSize = Integer.parseInt(inS);
			if(chunkSize < 4 || chunkSize > 128){
				chunkSize = 16;
			}
		} catch (Exception e) {
			chunkSize = 16;
	    }
		try{
			String inS = JOptionPane.showInputDialog("Chunk updates per frame, only use more than 2 - 4 if \n you have a good GPU or very few voxels");
			updates = Integer.parseInt(inS);
			if(updates < 1 || updates > 10){
				updates = 2;
			}
		} catch (Exception e) {
			updates = 2;
	    }
		lighting = (JOptionPane.showConfirmDialog(null, "Enable Lighting?")==0);
		wireframe = (JOptionPane.showConfirmDialog(null, "Enable Wireframe?")==0);
		// Run the GUI codes in the event-dispatching thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Create the OpenGL rendering canvas
				setupCanvas();
				// Create a animator that drives canvas' display() at the specified FPS.
				animator = new FPSAnimator(canvas, FPS);
				
				// Create the top-level container
				final JFrame frame = new JFrame(); // Swing's JFrame or AWT's Frame
				setupFrame(frame, canvas);
	            	
				animator.start(); // start the animation loop
			}
		});
	}
	   
	private static void setupCanvas(){
		canvas = new Render();
		VoxelEventListener listener = new VoxelEventListener(canvas);
		canvas.addGLEventListener(listener);
		canvas.addKeyListener(listener);
		canvas.addMouseListener(listener);
		canvas.addMouseMotionListener(listener);
		canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
	}
	   
	private static void setupFrame(JFrame frame, Canvas canvas){
		frame.getContentPane().add(canvas);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Use a dedicate thread to run the stop() to ensure that the
				// animator stops before program exits.
				new Thread() {
					@Override
					public void run() {
						if (animator.isStarted()) animator.stop();
                        	System.exit(0);
					}
				}.start();
			}
		});
		   
		frame.setTitle(TITLE);
		frame.pack();
		frame.setVisible(true);
	}

}
