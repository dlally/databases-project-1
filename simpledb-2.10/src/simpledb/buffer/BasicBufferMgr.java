package simpledb.buffer;

import simpledb.file.Block;
import simpledb.file.FileMgr;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CS4432-Project1:
 * @author Nathaniel Miller
 * =================================================
 * Manages the pinning and unpinning of buffers to blocks.
 * @author Edward Sciore
 *
 */
class BasicBufferMgr {
    private Buffer[] bufferpool;
    private ArrayList<Buffer> freeFrames;
    private ConcurrentHashMap<Block, Buffer> blocksToBuffers;
    private final BufferReplacementPolicy replacementPolicy = BufferReplacementPolicy.LRU; // Specifies replacement policy


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
        for (int i=0; i<numbuffs; i++) {
            bufferpool[i] = new Buffer();
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
            for(Block b : blocksToBuffers.keySet()){
                if(blocksToBuffers.get(b).equals(buff)){
                    blocksToBuffers.remove(b);
                    // There will only be one buffer for each block, no need to keep searching
                    break;
                }
            }
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
     * =================================================
     */
    private Buffer chooseUnpinnedBuffer() {
        if(replacementPolicy.equals(BufferReplacementPolicy.LRU)){
            return chooseLRU();
        }
        else if(replacementPolicy.equals(BufferReplacementPolicy.CLOCK)){
            return chooseClock();
        }

        else{
            return null;
        }
    }

    /**
     * CS4432-Project1: Returns an unpinned buffer to use based on LRU
     * If there are no free buffers, it returns null instead
     * @return a free buffer to use, or null if no free buffers exist
     */
    private Buffer chooseLRU(){
        Buffer oldest = null;
        for(Buffer b : freeFrames){
            if(oldest == null){
                oldest = b;
            }
            if(oldest.getLastAccessTime() > b.getLastAccessTime()){
                oldest = b;
            }
        }
        return oldest;
    }

    /**
     * CS4432-Project1: Returns an unpinned buffer to use based on clock replacement
     * If there are no free buffers, it returns null instead
     * @return a free buffer to use, or null if no free buffers exist
     */
    private Buffer chooseClock(){
        // TODO
        return null;
    }
}
