package julien.voxel;

import java.util.Random;


/** Enumerator for the discrete types of blocks
 * Main use is to set a specific set of colors used by blocks
 * @author Julien Brenneck
 * @version December 15, 2013 */
public enum BlockType {
	DEFAULT (1.0f, 1.0f, 1.0f), // 0
	WHITE (1.0f, 1.0f, 1.0f), // 1
	PURPLE (1.0f, 0.0f, 1.0f), // 2
	PINK (1.0f, 0.3f, 1.0f), // 3
	REDPINK (1.0f, 0.3f, 0.7f), // 4
	RED (1.0f, 0.0f, 0.0f), // 5
	REDORANGE (1.0f, 0.3f, 0.3f), // 6
	ORANGE (1.0f, 0.6f, 0.3f), // 7
	YELLOW (1.0f, 0.9f, 0.3f), // 8
	YELLOWGREEN (0.7f, 1.0f, 0.3f), // 9
	GREEN (0.3f, 1.0f, 0.3f), //10
	BLUEGREEN(0.3f, 1.0f, 0.7f), // 11
	BLUER(0.3f, 0.1f, 1.0f), //12
	BLUER2(0.4f, 0.5f, 1.0f), //13
	BLUE (0.5f, 0.2f, 1.0f);
	
	/** red component of RGB color value */
	protected float r;
	/** green component of RGB color value */
	protected float g;
	/** blue component of RGB color value */
	protected float b;
	/** List of all BlockTypes, for generating random BlockType */
	private static final BlockType[] values = values();
	/** Random used to generate random BlockTypes */
	private static final Random random = new Random();
	
	/** Enumerated constructor, takes red green blue float values */
	BlockType(float r, float g, float b){
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	/** Returns a random BlockType */
	public static BlockType random(){
		return values[random.nextInt(values.length)];
	}

	/** Will this block be drawn? */
	public static boolean isActive(byte block){
		return (block > 0);
	}
	
	/** With blocks stored as bytes, this returns the block's color */
	public static float[] color(byte block){
		if(block < 0) block += 127;
		float[] colors =  {values[block].r, values[block].g, values[block].b};
		return colors;
	}
	

}
