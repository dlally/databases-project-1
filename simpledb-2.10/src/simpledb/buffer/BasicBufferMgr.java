package simpledb.buffer;

import simpledb.file.Block;
import simpledb.file.FileMgr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CS4432-Project1:
 * @author Nathaniel Miller
 * @author Douglas Lally
 * =================================================
 * Manages the pinning and unpinning of buffers to blocks.
 * @author Edward Sciore
 *
 */
class BasicBufferMgr {
    private static final int maxClockCycles = 10; // Maximum number of full rotations the clock pointer will make before giving up
    private Buffer[] bufferpool;
    private ArrayList<Buffer> freeFrames;
    private ConcurrentHashMap<Block, Buffer> blocksToBuffers;
    private final BufferReplacementPolicy replacementPolicy = BufferReplacementPolicy.LRU; // Specifies replacement policy
    private int framePtr; // clock replacement policy frame pointer

    /**
     * CS4432-Project1:
     * Modified this constructor to initialize the list of
     * free frames. Initializes a ConcurrentHashMap to keep track
     * of the association between blocks and buffers
     * =================================================
     * Creates a buffer manager having the specified number
     * of buffer slots.
     * This constructor depends on both the {@link FileMgr} and
     * {@link simpledb.log.LogMgr LogMgr} objects
     * that it gets from the class
     * {@link simpledb.server.SimpleDB}.
     * Those objects are created during system initialization.
     * Thus this constructor cannot be called until
     * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or
     * is called first.
     * @param numbuffs the number of buffer slots to allocate
     */
    BasicBufferMgr(int numbuffs) {
        bufferpool = new Buffer[numbuffs];
        freeFrames = new ArrayList<Buffer>(numbuffs);
        blocksToBuffers = new ConcurrentHashMap<Block, Buffer>();
        framePtr = 0;
        for (int i=0; i<numbuffs; i++) {
            bufferpool[i] = new Buffer(i);
            freeFrames.add(bufferpool[i]);
        }

    }

    /**
     * Flushes the dirty buffers modified by the specified transaction.
     * @param txnum the transaction's id number
     */
    synchronized void flushAll(int txnum) {
        for (Buffer buff : bufferpool)
            if (buff.isModifiedBy(txnum))
                buff.flush();
    }


    /**
     * CS4432-Project1:
     * Modified this function to properly update the
     * list of free frames when a page is pinned.
     * Once a page is pinned the buffer is removed from
     * the free frames list. The buffer is recorded as
     * containing the specified block.
     * =================================================
     * Pins a buffer to the specified block.
     * If there is already a buffer assigned to that block
     * then that buffer is used;
     * otherwise, an unpinned buffer from the pool is chosen.
     * Returns a null value if there are no available buffers.
     * @param blk a reference to a disk block
     * @return the pinned buffer
     */
    synchronized Buffer pin(Block blk) {
        Buffer buff = findExistingBuffer(blk);
        if (buff == null) {
            buff = chooseUnpinnedBuffer();
            if (buff == null)
                return null;
            buff.assignToBlock(blk);
        }
        buff.pin();

        // Remove the pinned frame from the free frames list
        if(freeFrames.contains(buff)) {
            freeFrames.remove(buff);
        }

        // Associate the block with the buffer
        blocksToBuffers.put(blk, buff);

        return buff;
    }

    /**
     * CS4432-Project1:
     * Modified this function to properly update the
     * list of free frames when a page is pinned.
     * Once a page is pinned the buffer is removed from
     * the free frames list.
     * =================================================
     * Allocates a new block in the specified file, and
     * pins a buffer to it.
     * Returns null (without allocating the block) if
     * there are no available buffers.
     * @param filename the name of the file
     * @param fmtr a pageformatter object, used to format the new block
     * @return the pinned buffer
     */
    synchronized Buffer pinNew(String filename, PageFormatter fmtr) {
        Buffer buff = chooseUnpinnedBuffer();
        if (buff == null)
            return null;
        buff.assignToNew(filename, fmtr);
        buff.pin();

        // Remove the pinned frame from the free frames list
        if(freeFrames.contains(buff)) {
            freeFrames.remove(buff);
        }
        //System.out.println("===New buffer assigned!===\nBuffer Manager State:\n" + toString());


        return buff;
    }

    /**
     * CS4432-Project1:
     * Modified this function to properly update
     * the list of free frames when a frame is unpinned.
     * An unpinned frame is free to be used and is therefore
     * added to the list of free frames.
     * =================================================
     * Unpins the specified buffer.
     * @param buff the buffer to be unpinned
     */
    synchronized void unpin(Buffer buff) {
        buff.unpin();
        if (!buff.isPinned()) {
            // Add the unpinned frame from the free frames list
            if(!freeFrames.contains(buff)) {
                freeFrames.add(buff);
            }
            // Remove the block associated with this frame
            blocksToBuffers.remove(buff.block());
        }
    }

    /**
     * Returns the number of available (i.e. unpinned) buffers.
     * @return the number of available buffers
     */
    int available() {
        return freeFrames.size();
    }

    /**
     * CS4432-Project1: Searches for a buffer already containing a given block
     *
     * @param blk the block that the buffer must contain
     * @return the Buffer that contains blk, or null if no buffers contain that Block
     */
    private Buffer findExistingBuffer(Block blk) {
        return blocksToBuffers.get(blk);
    }


    /**
     * CS4432-Project1:
     * Returns a frame based on the selected replacement policy
     */
    private synchronized Buffer chooseUnpinnedBuffer() {
        Buffer b = null;
        if(replacementPolicy.equals(BufferReplacementPolicy.LRU)){
            b = chooseLRU();
        }
        else if(replacementPolicy.equals(BufferReplacementPolicy.CLOCK)){
            b = chooseClock();
        }
        if(b != null){
            b.setSecondChance(true);
            System.out.println("Based on replacement policy " + replacementPolicy + " buffer ID " + b.getID() + " was selected");
        }
        return b;
    }

    /**
     * CS4432-Project1: Returns an unpinned buffer to use based on LRU
     * If there are no free buffers, it returns null instead
     * @return a free buffer to use, or null if no free buffers exist
     */
    private Buffer chooseLRU(){
        Buffer oldest = null;
        for(Buffer b : bufferpool){
            if(!b.isPinned()){
                // Special case if we haven't found an unpinned buffer yet
                if(oldest == null){
                    System.out.println("Oldest bufferID found so far: " + b.getID());
                    oldest = b;
                }
                // Compare access time, replace if needed
                else if(oldest.getLastAccessTime() > b.getLastAccessTime()){
                    System.out.println("Oldest bufferID found so far: " + b.getID());
                    oldest = b;
                }
            }
        }
        return oldest;
    }

    /**
     * CS4432-Project1: Returns an unpinned buffer to use based on clock replacement policy.
     * Once an candidate frame is found for eviction it is returned.
     * If no frame can be found within 10 clock cycles, null is returned instead
     * @return a free buffer to use
     */
    private Buffer chooseClock(){
        for(int i = 0; i < bufferpool.length * maxClockCycles; i++){
            // Reset the frame pointer to the beginning if needed
            if(framePtr == bufferpool.length){
                framePtr = 0;
            }
            Buffer b = bufferpool[framePtr];
            if(!b.isPinned() && b.hasSecondChance()) {
                // frame has second chance so skip
                System.out.println("BufferID " + b.getID() + " had a second chance");
                b.setSecondChance(false);
            }
            else if(!b.isPinned() && !b.hasSecondChance()) {
                // frame is free for use so return it
                framePtr++;
                return b;
            }
            framePtr++;
        }
        System.err.println("All buffers were pinned, unable to select replacement");
        return null;
    }

    /** CS4432-Project1: Get an array of dirty buffers in this manager
     * Dirty buffers will be written back to disk before being replaced
     *
     * @return Array of dirty buffers.
     */
    Buffer[] dirtyBuffers(){
        ArrayList<Buffer> dirtyBuffs = new ArrayList<Buffer>();
        for(Buffer b : bufferpool){
            if(b.isDirty()){
                dirtyBuffs.add(b);
            }
        }
        return dirtyBuffs.toArray(new Buffer[0]);
    }

    /** CS4432-Project1: Get an array of pinned buffers
     *
     * @return Array of pinned buffers
     */
    Buffer[] pinnedBuffers(){
        ArrayList<Buffer> pinnedBuffs = new ArrayList<Buffer>();
        for(Buffer b : bufferpool){
            if(b.isPinned()){
                pinnedBuffs.add(b);
            }
        }
        return pinnedBuffs.toArray(new Buffer[0]);
    }

    /**
     * CS4432-Project1: Custom toString method to provide details about this buffermgr
     * @return String representation of this BasicBufferMgr
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BasicBufferMgr{");
        sb.append("bufferpool=\n").append(Arrays.toString(bufferpool)).append('\n');
        sb.append(", freeFrames=\n").append(Arrays.toString(freeFrames.toArray())).append('\n');
        sb.append(", replacementPolicy=").append(replacementPolicy);
        if(replacementPolicy.equals(BufferReplacementPolicy.CLOCK)) {
            sb.append(",\nframePtr=").append(framePtr);
        }
        sb.append('}');
        return sb.toString();
    }
}
