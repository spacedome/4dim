package julien.voxel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import javax.media.opengl.GL2;

/**
 * Manages the chunks. Creates new chunks, removes old chunks, renders active chunks.
 * Chunk updating isn't currently implemented.
 * @author Julien Brenneck
 * @version December 15, 2013
 */
public class ChunkManager {
	/** Number of blocks across a chunk is */
	public static final int chunkSize = GUI.chunkSize;
	/** Number of blocks across a chunk is */
	public static final int chunkHeight = 16;
	/** Size of each block */
	public static final float len = 1.0f;
	/** Number of chunks to draw in each direction */
	private int draw = GUI.drawDistance;
	/** List of active, drawn chunks */
	private HashSet<Chunk> chunkList;
	/** List of chunks to be loaded */
	private ArrayList<Chunk> chunkLoad;
	/** List of chunks to be unloaded */
	private ArrayList<Chunk> chunkUnload;
	/** set of all active chunk locations */
	private HashSet<Integer[]> loc = new HashSet<Integer[]>();
	/** Last chunk position */
	private int[] lastpos = {0, 0};
	/** Is the chunk manager ready to check for new chunks to load */
	private boolean ready = false;
	/** Random for debugging */
	Random ran = new Random();
	
	/** Constructor builds initial chunks and initializes stuff */
	public ChunkManager(GL2 gl){
		chunkList = new HashSet<Chunk>();
		chunkLoad = new ArrayList<Chunk>();
		chunkUnload = new ArrayList<Chunk>();
		for(int x = 0; x < draw*2; x++){
			for(int y = 0; y < 4; y++){
				for(int z = 0; z < draw*2; z++){
					chunkLoad.add(new Chunk(x-draw, y, z-draw));
				}
			}
		}
		while(!chunkLoad.isEmpty()){
			loadNext(gl, 1);
		}
	}

	/** Calls the VBO render calls for all active chunks */
	public void render(GL2 gl){
		for(Chunk ch : chunkList){
			gl.glPushMatrix();
			gl.glTranslatef(ch.pos[0]*chunkSize, ch.pos[1]*chunkHeight, ch.pos[2]*chunkSize);
			ch.render(gl);
			gl.glPopMatrix();
		}
	}
	
	/** Load the next n chunks from the list of chunks ready to be loaded. Slow, has to build each chunk. */
	public void loadNext(GL2 gl, int n){
		while(!chunkLoad.isEmpty()&&n>0){
			Chunk next =chunkLoad.remove(0);
			next.createMesh(gl);
			chunkList.add(next);
			n--;
		}
	}
	
	/** Unload the next n queued chunks. This is very fast, do as many as you want. */
	public void unloadNext(int n){
		while(!chunkUnload.isEmpty()&&n>0){
			Chunk c = chunkUnload.remove(0);
			chunkList.remove(c);
			c.delete(); // call finalize and hope it actually gets deleted
			n--;
		}
	}
	
	/** Check to see if any chunks need to be unloaded. Must be called before updateLoad. */
	public void updateUnload(double x, double z){
		int chunkX = (int) (x)/chunkSize;
		int chunkZ = (int) (z)/chunkSize;
		if(Math.abs(chunkX-lastpos[0])>=1||Math.abs(chunkZ-lastpos[1])>=1){
			for(Chunk c : chunkList){
				if(Math.abs(chunkX-c.pos[0])>draw||Math.abs(chunkZ-c.pos[2])>draw){
					chunkUnload.add(c);
				} else {
					Integer[] temp = {c.pos[0], c.pos[1], c.pos[2]};
					loc.add(temp);
				}
			}
			ready = true;
			lastpos[0] = chunkX;
			lastpos[1] = chunkZ;
		}
	}
	
	/** Checks to see if any chunks need to be loaded, and add them to the loading queue */
	public void updateLoad(double x, double z){
		if(ready){
		int chunkX = (int) (x)/chunkSize;
		int chunkZ = (int) (z)/chunkSize;
		HashSet<Integer[]> check = new HashSet<Integer[]>();
		if(!loc.isEmpty()){
			for(int i = -draw; i <= draw; i++){
				for(int y = 0; y < 4; y++){
					for(int j = -draw; j <= draw; j++){
						Integer[] temp = {chunkX+i,y,chunkZ+j};
						check.add(temp);
					}
				}
			}
			for(Chunk c : chunkLoad){
				Integer[] temp = {c.pos[0], c.pos[1], c.pos[2]};
				loc.add(temp);
			}
			for(Chunk c : chunkUnload){
				Integer[] temp = {c.pos[0], c.pos[1], c.pos[2]};
				loc.add(temp);
			}
			check.removeAll(loc);
			for(Integer[] i : check){
				chunkLoad.add(new Chunk(i[0], i[1], i[2]));
			}
			loc = new HashSet<Integer[]>();
		}
		ready = false;
		}
	}

	/** Layers simplex noise to give better terrain map */
	public static double map2D(double x, double y){
		double temp = 10*SimplexNoise.noise(((float) x)/30, ((float) y)/30);
		temp += 25*SimplexNoise.noise(((float) y+100)/120, ((float) x-100)/100)+36;
		return temp;
	}
}
