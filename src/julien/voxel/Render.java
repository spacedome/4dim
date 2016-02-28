package julien.voxel;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import static javax.media.opengl.GL.*;  // GL constants
import static javax.media.opengl.GL2.*; // GL2 constants
 
/**
 * Creates the GLCanvas to be drawn onto. Creates scene and controls chunk render calls
 * @author Julien Brenneck
 * @version December 15, 2013
 */
public class Render extends GLCanvas {
   // Setup OpenGL Graphics Renderer
   private GLU glu;  // for the GL Utility
   /** Location in absolute coordinates */
   public float[] location = {0.0f, 32.0f, 0.0f};
   /** How much to change location by */
   public float[] lChange = {0.0f, 0.0f, 0.0f};
   /** How much to rotate */
   public float[] rChange = {1.0f, 0.0f};
   /** Current screen aspect ratio */
   private float aspect;
   /** Chunk Manager */
   protected ChunkManager chMan;
   /** Keep track of time, doesn't do much. Supposed to help smooth user input, but doesn't really. */
   private long time;
   /** Frame counter */
   private int tickCount = 0;
   
   /** Default constructor */
   public Render() {
      
   }
 
   /** Sets up the scene */
   public void setup(GLAutoDrawable drawable) {
      GL2 gl = drawable.getGL().getGL2();      // get the GL Context
      glu = new GLU();                         // get GL Utilities
      gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // background color
      gl.glClearDepth(1.0f); // background depth
      gl.glEnable(GL_DEPTH_TEST); // enable depth testing
      gl.glDepthFunc(GL_LEQUAL); 
      gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
      if(GUI.lighting){
    	  gl.glEnable(GL_LIGHTING); // enable lighting
      }
      gl.glEnable(GL_LIGHT0);
      gl.glShadeModel(GL_SMOOTH); // Smooth lighting
      gl.glEnable(GL_CULL_FACE); // cull the back of faces
      gl.glEnable(GL2.GL_COLOR_MATERIAL); // enable color
      
      if(GUI.wireframe){
    	  gl.glPolygonMode( GL_FRONT_AND_BACK, GL_LINE ); // DEBUG: ENABLE FOR WIREFRAME
      } else {
    	  gl.glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
      }
      time = System.currentTimeMillis(); // start time
      chMan = new ChunkManager(gl); // setup chunk manager
      double m = (System.currentTimeMillis() - (double) time)/1000;
      System.out.println("Created in: " + m);
   }
 
   /** Called when window is resized, and first time window becomes visible. gets proper aspect ratio */
   public void resize(GLAutoDrawable drawable, int x, int y, int width, int height) {
      GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
      if (height == 0) height = 1;   // prevent divide by zero
      aspect = (float)width / height; 
      gl.glViewport(0, 0, width, height); // Set the view port (display area) to cover the entire window
 
   }
 
   /** Called to render each frame */
   public void draw(GLAutoDrawable drawable) {
	   // Limit vertical view. Prevents looking over and around yourself, also prevents gimbal locking
	   if(rChange[1]>=Math.PI/2){
		   rChange[1] = (float) (Math.PI/2 - 0.001);
	   } else if (rChange[1]<=-Math.PI/2){
		   rChange[1] = (float) (-Math.PI/2 + 0.001);
	   }
	   GL2 gl = drawable.getGL().getGL2(); // Get GL2 Context: Required for several features used by the program, not supported fully by OpenGL 1.5
	   gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color and depth buffers 
	  
	   gl.glMatrixMode(GL_PROJECTION);
	   gl.glLoadIdentity();             
	   glu.gluPerspective(45.0, aspect, 0.1, 1000.0); // Setup perspective (relative to current aspect ratio)
	   
	   
       gl.glMatrixMode(GL_MODELVIEW); 
       gl.glLoadIdentity();
       // I didn't feel like writing a matrix class or using a library, so gluLookAt is handling position (shitty coding)
	   glu.gluLookAt(location[0], location[1], location[2],
			   		 location[0] + Math.cos(rChange[0])*Math.cos(rChange[1]),
			   		 location[1] + Math.sin(rChange[1]),
			   		 location[2] + Math.sin(rChange[0])*Math.cos(rChange[1]),
			   		 0.0f, 1.0f, 0.0f);
 
      
       chMan.render(gl);
       double tchange =  (System.currentTimeMillis() - (double) time)/15;
       // Update the viewpoint location
       location[0] += lChange[0]*Math.cos(rChange[0])*tchange + lChange[2]*Math.cos(rChange[0]+Math.PI/2)*tchange;
       location[1] += lChange[1]*tchange;
       location[2] += lChange[0]*Math.sin(rChange[0])*tchange + lChange[2]*Math.sin(rChange[0]+Math.PI/2)*tchange;
       
	   if(tickCount%10==0){ 
		   chMan.updateUnload(location[0], location[2]); // update location in chunk manager every 10 ticks
	   } else if(tickCount%10==5){
		   chMan.updateLoad(location[0], location[2]);
	   }
	   chMan.loadNext(gl, GUI.updates);
	   chMan.unloadNext(3*GUI.updates);
	   time = System.currentTimeMillis();
	   tickCount++;
   } 
    

}