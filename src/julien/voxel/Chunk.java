package julien.voxel;

import java.nio.FloatBuffer;
import com.jogamp.common.nio.Buffers;

import javax.media.opengl.*;

/** Class for an individual chunk. Each chunk holds a 3x3 set of Blocks, and builds a mesh/renders them.
 * Default size of a chunk is 16 blocks across, but it can be easily changed.
 * Most optimizations are within Chunk's createMesh/update methods (not drawing unseen faces).
 * Chunk renders blocks using Vertex Buffer Objects, which must be updated when a chunk is changed.
 * While a chunk is mostly self-contained, it should be managed by a chunk manager.
 * @author Julien Brenneck
 * @version December 15, 2013 */
public class Chunk {
	/** Is this chunk active */
	private boolean active;
	/** Stores all the blocks in the chunk */
	private byte[][][] blocks = new byte[chunkSize][chunkHeight][chunkSize];
	/** Holds Buffer IDs assigned by openGL */
	private int buffer[] = new int[3];
	/** Number of vertices in the chunk */
	private int vertexCount;
	/** Size of each block */
	private final float len = ChunkManager.len;
	/** Buffer for vertices */
	private FloatBuffer vertexData;
	/** Buffer for normals */
	private FloatBuffer normalData;
	/** Buffer for colors */
	private FloatBuffer colorsData;
	/** Number of blocks across a chunk is */
	public static final int chunkSize = ChunkManager.chunkSize;
	/** Number of blocks across a chunk is */
	public static final int chunkHeight = ChunkManager.chunkHeight;
	/** Chunk position: Assigned by chunk manager */
	public int[] pos = new int[3];
	
	/** Default constructor, makes a random chunk */
	public Chunk(int xp, int yp, int zp){
		pos[0] = xp;
		pos[1] = yp;
		pos[2] = zp;
		active = true;
	}
	
	/** Updates the Vertex Buffer when a chunk is changed */
	public void update(GL2 gl){
		// Not implemented, chunks are immutable
	}
	
	/** Creates a mesh from cube data, and stores it to a newly allocated Vertex Buffer */
	public void createMesh(GL2 gl){
		for(int x = 0; x < chunkSize; x++){
			for(int y = 0; y < chunkHeight; y++){
				for(int z = 0; z < chunkSize; z++){
					if(y + pos[1]*chunkHeight <= ChunkManager.map2D(x + pos[0]*chunkSize, z + pos[2]*chunkSize)){
						blocks[x][y][z] = (byte) (2.7*(y+pos[1]*chunkHeight)/13+1); // Initialize each block
					} else {
						blocks[x][y][z] = 0;
					}
				}
			}
		}
		vertexCount = 0;
		// Allocate the buffers (Change to one buffer, or use short/byte buffer for normals and color)
		vertexData = Buffers.newDirectFloatBuffer(chunkSize*chunkSize*chunkSize*18*3);
		normalData = Buffers.newDirectFloatBuffer(chunkSize*chunkHeight*chunkSize*18*3);
		colorsData = Buffers.newDirectFloatBuffer(chunkSize*chunkSize*chunkSize*18*3);
		for(int x = 0; x < chunkSize; x++){
			for(int y = 0; y < chunkHeight; y++){
				for(int z = 0; z < chunkSize; z++){
					if(BlockType.isActive(blocks[x][y][z])){
						createCube(x, y, z); // If the cube is active, add it to the Buffer
					}
				}
			}
		}
		// correct the buffer size, and rewind
		normalData.flip();
		vertexData.flip();
		colorsData.flip();
		gl.glGenBuffers(3, buffer, 0); // allocate the buffers and get IDs
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buffer[0]);
		gl.glBufferData(GL.GL_ARRAY_BUFFER, vertexCount*3*4, vertexData, GL.GL_DYNAMIC_DRAW);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buffer[1]);
		gl.glBufferData(GL.GL_ARRAY_BUFFER, vertexCount*3*4, normalData, GL.GL_DYNAMIC_DRAW);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buffer[2]);
		gl.glBufferData(GL.GL_ARRAY_BUFFER, vertexCount*3*4, colorsData, GL.GL_DYNAMIC_DRAW);
		// set buffer as null now that it's done being used, hope it will be garbage collected
		vertexData = null;
		normalData = null;
		colorsData = null;		
	}
	
	/** Calls finalize, hope it actually gets deleted. Chunks can quickly build up in memory. */
	public void delete(){
		blocks = null;
		try {
			finalize();
		} catch (Throwable e) {	}
	}
	
	/** Renders the previously created mesh. Ideally the Vertex Buffer is stored on the graphics memory */
	public void render(GL2 gl){
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY); // enable vertex array
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY); // enable color array
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY); // enable normal array
		
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, buffer[0]);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);   // Set The Vertex Pointer To The Vertex Buffer 
        
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buffer[1]);
        gl.glNormalPointer(GL.GL_FLOAT, 0, 0); // Set the normal pointer to the normal buffer
        
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buffer[2]);
        gl.glColorPointer(3, GL.GL_FLOAT, 0, 0); // set the color pointer to the color buffer
        
		gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertexCount); // draw the buffer
		
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);  
		gl.glDisableClientState(GL2.GL_COLOR_ARRAY); 
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY); 
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0); // reset bound buffer
	}
	
	@Override
	public boolean equals(Object comp){
		try{
		Chunk other = (Chunk) comp;
		return (pos[0]==other.pos[0]&&pos[1]==other.pos[1]&&pos[2]==other.pos[2]);
		} catch (Exception e){
			return false;
		}
	}
	
	@Override
	public int hashCode() {
        return (pos[0]*7919+pos[1]*977+pos[0]*7);
    }
	
	/** If true, draw side */
	private boolean[] checkCubeSides(int x, int y, int z){
		boolean[] sideCheck = new boolean[6];
		if ((!(y>=chunkHeight-1))&&BlockType.isActive(blocks[x][y+1][z])) {
			sideCheck[0] = false;
		} else {
			sideCheck[0] = true;
		}
		
		if ((!(y<=0))&&BlockType.isActive(blocks[x][y-1][z])) {
			sideCheck[1] = false;
		} else {
			sideCheck[1] = true;
		}
		
		if ((!(z<=0))&&BlockType.isActive(blocks[x][y][z-1])) {
			sideCheck[2] = false;
		} else {
			sideCheck[2] = true;
		}
		
		if ((!(z>=chunkSize-1))&&BlockType.isActive(blocks[x][y][z+1])) {
			sideCheck[3] = false;
		} else {
			sideCheck[3] = true;
		}
		
		if ((!(x<=0))&&BlockType.isActive(blocks[x-1][y][z])) {
			sideCheck[4] = false;
		} else {
			sideCheck[4] = true;
		}
		
		if ((!(x>=chunkSize-1))&&BlockType.isActive(blocks[x+1][y][z])) {
			sideCheck[5] = false;
		} else {
			sideCheck[5] = true;
		}
		

		
		return sideCheck;
	}
	
	/** If a cube should be draw, createCube adds its vertex, normal, and color data to the Arrays that will become buffers */
	private void createCube(float x, float y, float z){
		boolean[] sides = checkCubeSides((int)x,(int)y,(int)z);
		float[] color = BlockType.color(blocks[(int)x][(int)y][(int)z]);
		
//		  gl.glNormal3f(0.0f, 1.0f, 0.0f);
		if(sides[0]){
			vertexData.put((len)*(x+1.0f));
			vertexData.put((len)*(y+1.0f));
			vertexData.put(len*z);
			
			vertexData.put(len*x);
			vertexData.put((len)*(y+1.0f));
			vertexData.put(len*z);
			
			vertexData.put(len*x);
			vertexData.put((len)*(y+1.0f));
			vertexData.put((len)*(z+1.0f));
			
			
			vertexData.put(len*x);
			vertexData.put((len)*(y+1.0f));
			vertexData.put((len)*(z+1.0f));
			
			vertexData.put((len)*(x+1.0f));
			vertexData.put((len)*(y+1.0f));
			vertexData.put((len)*(z+1.0f));
			
			vertexData.put((len)*(x+1.0f));
			vertexData.put((len)*(y+1.0f));
			vertexData.put(len*z);
			
			vertexCount += 6;
			for(int i = 0; i < 6; i++){
				normalData.put(0f);
				normalData.put(1f);
				normalData.put(0f);
				colorsData.put(color[0]);
				colorsData.put(color[1]);
				colorsData.put(color[2]);
			}
		}
	 
//	      // Bottom-face
//	      gl.glNormal3f(0.0f, -1.0f, 0.0f);
		
		if(sides[1]){
			vertexData.put(len*x);
			vertexData.put(len*y);
			vertexData.put((len)*(z+1.0f));
			
			vertexData.put(len*x);
			vertexData.put(len*y);
			vertexData.put(len*z);
			
			vertexData.put((len)*(x+1.0f));
			vertexData.put(len*y);
			vertexData.put(len*z);
			
			vertexData.put((len)*(x+1.0f));
			vertexData.put(len*y);
			vertexData.put((len)*(z+1.0f));
			
			vertexData.put(len*x);
			vertexData.put(len*y);
			vertexData.put((len)*(z+1.0f));
			
			vertexData.put((len)*(x+1.0f));
			vertexData.put(len*y);
			vertexData.put(len*z);
			
			vertexCount += 6;
			for(int i = 0; i < 6; i++){
				normalData.put(0f);
				normalData.put(-1f);
				normalData.put(0f);
				colorsData.put(color[0]);
				colorsData.put(color[1]);
				colorsData.put(color[2]);
			}
		}
//	      // Back-face
//	      gl.glNormal3f(0.0f, 0.0f, -1.0f);
		if(sides[2]){
			vertexData.put((len)*(x+1.0f));
			vertexData.put(len*y);
			vertexData.put(len*z);
			
			vertexData.put(len*x);
			vertexData.put(len*y);
			vertexData.put(len*z);
			
			vertexData.put(len*x);
			vertexData.put((len)*(y+1.0f));
			vertexData.put(len*z);
			
			vertexData.put(len*x);
			vertexData.put((len)*(y+1.0f));
			vertexData.put(len*z);
			
			vertexData.put((len)*(x+1.0f));
			vertexData.put((len)*(y+1.0f));
			vertexData.put(len*z);
			
			vertexData.put((len)*(x+1.0f));
			vertexData.put(len*y);
			vertexData.put(len*z);
			
			vertexCount += 6;
			for(int i = 0; i < 6; i++){
				normalData.put(0f);
				normalData.put(0f);
				normalData.put(-1f);
				colorsData.put(color[0]);
				colorsData.put(color[1]);
				colorsData.put(color[2]);
			}
		}
//	      // Front-face
//	      gl.glNormal3f(0.0f, 0.0f, 1.0f);
		if(sides[3]){
			vertexData.put((len)*(x+1.0f));
			vertexData.put((len)*(y+1.0f));
			vertexData.put((len)*(z+1.0f));
			
			vertexData.put(len*x);
			vertexData.put((len)*(y+1.0f));
			vertexData.put((len)*(z+1.0f));
			
			vertexData.put(len*x);
			vertexData.put(len*y);
			vertexData.put((len)*(z+1.0f));
			
			vertexData.put((len)*(x+1.0f));
			vertexData.put(len*y);
			vertexData.put((len)*(z+1.0f));
			
			vertexData.put((len)*(x+1.0f));
			vertexData.put((len)*(y+1.0f));
			vertexData.put((len)*(z+1.0f));
			
			vertexData.put(len*x);
			vertexData.put(len*y);
			vertexData.put((len)*(z+1.0f));
			
			vertexCount += 6;
			for(int i = 0; i < 6; i++){
				normalData.put(0f);
				normalData.put(0f);
				normalData.put(1f);
				colorsData.put(color[0]);
				colorsData.put(color[1]);
				colorsData.put(color[2]);
			}
		}

//	 
//	      // Left-face
//	      gl.glNormal3f(-1.0f, 0.0f, 0.0f);
		if(sides[4]){
			vertexData.put(len*x);
			vertexData.put((len)*(y+1.0f));
			vertexData.put((len)*(z+1.0f));
			
			vertexData.put(len*x);
			vertexData.put((len)*(y+1.0f));
			vertexData.put(len*z);
			
			vertexData.put(len*x);
			vertexData.put(len*y);
			vertexData.put(len*z);
			
			vertexData.put(len*x);
			vertexData.put(len*y);
			vertexData.put((len)*(z+1.0f));
			
			vertexData.put(len*x);
			vertexData.put((len)*(y+1.0f));
			vertexData.put((len)*(z+1.0f));
			
			vertexData.put(len*x);
			vertexData.put(len*y);
			vertexData.put(len*z);
			
			vertexCount += 6;
			for(int i = 0; i < 6; i++){
				normalData.put(-1f);
				normalData.put(0f);
				normalData.put(0f);
				colorsData.put(color[0]);
				colorsData.put(color[1]);
				colorsData.put(color[2]);
			}
		}

//	      // Right-face
//	      gl.glNormal3f(1.0f, 0.0f, 0.0f);
		if(sides[5]){
			vertexData.put((len)*(x+1.0f));
			vertexData.put((len)*(y+1.0f));
			vertexData.put(len*z);
			
			vertexData.put((len)*(x+1.0f));
			vertexData.put((len)*(y+1.0f));
			vertexData.put((len)*(z+1.0f));
			
			vertexData.put((len)*(x+1.0f));
			vertexData.put(len*y);
			vertexData.put((len)*(z+1.0f));
			
			vertexData.put((len)*(x+1.0f));
			vertexData.put(len*y);
			vertexData.put(len*z);
			
			vertexData.put((len)*(x+1.0f));
			vertexData.put((len)*(y+1.0f));
			vertexData.put(len*z);
			
			vertexData.put((len)*(x+1.0f));
			vertexData.put(len*y);
			vertexData.put((len)*(z+1.0f));
			
			vertexCount += 6;
			for(int i = 0; i < 6; i++){
				normalData.put(1f);
				normalData.put(0f);
				normalData.put(0f);
				colorsData.put(color[0]);
				colorsData.put(color[1]);
				colorsData.put(color[2]);
			}
		}
	}

}
