package simpledb.buffer;

import simpledb.file.*;

import java.util.ArrayList;

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
   
   /**
    * CS4432-Project1:
    * Modified this constructor to initialize the list of
    * free frames.
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
    * the free frames list.
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
      }
   }
   
   /**
    * Returns the number of available (i.e. unpinned) buffers.
    * @return the number of available buffers
    */
   int available() {
      return freeFrames.size();
   }
   
   private Buffer findExistingBuffer(Block blk) {
      for (Buffer buff : bufferpool) {
         Block b = buff.block();
         if (b != null && b.equals(blk))
            return buff;
      }
      return null;
   }


   /**
    * CS4432-Project1:
    * Modified this function to utilize the list of
    * free frames instead of the inefficient pool scan method.
    * The first buffer element in the free frames list is returned
    * if the list is non-empty.
    * =================================================
    */
   private Buffer chooseUnpinnedBuffer() {

      if(!freeFrames.isEmpty()) {
          return freeFrames.get(0);
      }
      return null;
/*      for (Buffer buff : bufferpool)
         if (!buff.isPinned())
         return buff;
      return null;*/
   }
}
