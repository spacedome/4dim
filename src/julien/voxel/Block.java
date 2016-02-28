package julien.voxel;

/** Minimal class to define blocks. It stores minimal data, as there will be thousands/millions of instances of Block
 * NO LONGER USED
 * @author Julien Brenneck
 * @version December 15, 2013 */
public class Block {
	/** BlockType, essentially just storing color */
	private BlockType blockType;
	/** Does this block exist? */
	private boolean active;
	
	/** Default constructor will create a block of random BLockType */
	@Deprecated
	public Block(){
		blockType = BlockType.random();
		active = true;
	}
	
	/** Create a block of a specific BlockType */
	@Deprecated
	public Block(BlockType bt){
		blockType = bt;
		active = true;
	}
	
	/** Enable or disable a block */
	public void setActive(boolean active){
		this.active = active;
	}
	
	/** Will this block be drawn? */
	public boolean isActive(){
		return active;
	}
	
	/** Returns an array of RGB color values */
	public float[] color(){
		float[] col = {blockType.r, blockType.g, blockType.b};
		return col;
	}

}
