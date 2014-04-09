package simpledb.buffer;

import simpledb.file.Block;
import simpledb.file.Page;
import simpledb.server.SimpleDB;

/**
 * An individual buffer.
 * A buffer wraps a page and stores information about its status,
 * such as the disk block associated with the page,
 * the number of times the block has been pinned,
 * whether the contents of the page have been modified,
 * and if so, the id of the modifying transaction and
 * the LSN of the corresponding log record.
 * @author Edward Sciore, Douglas Lally, Nathaniel Miller
 */
public class Buffer {
    private Page contents = new Page();
    private Block blk = null;
    private int ID;
    private int pins = 0;
    private int modifiedBy = -1;  // negative means not modified
    private int logSequenceNumber = -1; // negative means no corresponding log record



    private long lastAccessTime = 0; // The last time the Buffer was accessed
    private boolean refBit = true; // Second chance bit for clock replacement policy

    /**
     * CS4432-Project1: Initialize the last access time to the time this buffer was created
     * Assign an ID number to this buffer. This ID is just used for identification purposes,
     * it is not required for buffer functionality
     *
     * Creates a new buffer, wrapping a new
     * {@link simpledb.file.Page page}.
     * This constructor is called exclusively by the
     * class {@link BasicBufferMgr}.
     * It depends on  the
     * {@link simpledb.log.LogMgr LogMgr} object
     * that it gets from the class
     * {@link simpledb.server.SimpleDB}.
     * That object is created during system initialization.
     * Thus this constructor cannot be called until
     * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or
     * is called first.
     */
    public Buffer(int ID) {
        this.ID = ID;
        lastAccessTime = System.nanoTime();
    }

    /**
     * CS4432-Project1: Update the last access time for this buffer
     *
     * Returns the integer value at the specified offset of the
     * buffer's page.
     * If an integer was not stored at that location,
     * the behavior of the method is unpredictable.
     * @param offset the byte offset of the page
     * @return the integer value at that offset
     */
    public int getInt(int offset) {
        lastAccessTime = System.nanoTime();
        return contents.getInt(offset);
    }

    /**
     * CS4432-Project1: Update the last access time for this buffer
     *
     * Returns the string value at the specified offset of the
     * buffer's page.
     * If a string was not stored at that location,
     * the behavior of the method is unpredictable.
     * @param offset the byte offset of the page
     * @return the string value at that offset
     */
    public String getString(int offset) {
        lastAccessTime = System.nanoTime();
        return contents.getString(offset);
    }

    /**
     * CS4432-Project1: Update the last access time for this buffer
     *
     * Writes an integer to the specified offset of the
     * buffer's page.
     * This method assumes that the transaction has already
     * written an appropriate log record.
     * The buffer saves the id of the transaction
     * and the LSN of the log record.
     * A negative lsn value indicates that a log record
     * was not necessary.
     * @param offset the byte offset within the page
     * @param val the new integer value to be written
     * @param txnum the id of the transaction performing the modification
     * @param lsn the LSN of the corresponding log record
     */
    public void setInt(int offset, int val, int txnum, int lsn) {
        lastAccessTime = System.nanoTime();
        modifiedBy = txnum;
        if (lsn >= 0)
            logSequenceNumber = lsn;
        contents.setInt(offset, val);
    }

    /**
     * CS4432-Project1: Update the last access time for this buffer
     *
     * Writes a string to the specified offset of the
     * buffer's page.
     * This method assumes that the transaction has already
     * written an appropriate log record.
     * A negative lsn value indicates that a log record
     * was not necessary.
     * The buffer saves the id of the transaction
     * and the LSN of the log record.
     * @param offset the byte offset within the page
     * @param val the new string value to be written
     * @param txnum the id of the transaction performing the modification
     * @param lsn the LSN of the corresponding log record
     */
    public void setString(int offset, String val, int txnum, int lsn) {
        lastAccessTime = System.nanoTime();
        modifiedBy = txnum;
        if (lsn >= 0)
            logSequenceNumber = lsn;
        contents.setString(offset, val);
    }

    /**
     * CS4432-Project1: Update the last access time for this buffer (we assume accessing the block reference
     * counts as access to the buffer as a whole)
     *
     * Returns a reference to the disk block
     * that the buffer is pinned to.
     * @return a reference to a disk block
     */
    public Block block() {
        lastAccessTime = System.nanoTime();
        return blk;
    }

    /**
     * CS4432-Project1: Update the last access time for this buffer,
     * only write to disk if this block is dirty
     *
     * Writes the page to its disk block if the
     * page is dirty.
     * The method ensures that the corresponding log
     * record has been written to disk prior to writing
     * the page to disk.
     */
    void flush() {
        lastAccessTime = System.nanoTime();
        if (isDirty()) {
            SimpleDB.logMgr().flush(logSequenceNumber);
            contents.write(blk);
            modifiedBy = -1;
        }
    }

    /**
     * CS4432-Project1: Update the last access time for this buffer
     *
     * Increases the buffer's pin count.
     */
    void pin() {
        lastAccessTime = System.nanoTime();
        pins++;
    }

    /**
     * CS4432-Project1: Update the last access time for this buffer
     *
     * Decreases the buffer's pin count.
     */
    void unpin() {
        lastAccessTime = System.nanoTime();
        pins--;
    }

    /**
     * Returns true if the buffer is currently pinned
     * (that is, if it has a nonzero pin count).
     * @return true if the buffer is pinned
     */
    boolean isPinned() {
        return pins > 0;
    }

    /**
     * Returns true if the buffer is dirty
     * due to a modification by the specified transaction.
     * @param txnum the id of the transaction
     * @return true if the transaction modified the buffer
     */
    boolean isModifiedBy(int txnum) {
        return txnum == modifiedBy;
    }

    /**
     * CS4432-Project1: Checks if the buffer is dirty
     * @return true if the buffer is dirty, false otherwise
     */
    boolean isDirty(){
        return modifiedBy >= 0;
    }

    /**
     * Reads the contents of the specified block into
     * the buffer's page.
     * If the buffer was dirty, then the contents
     * of the previous page are first written to disk.
     * @param b a reference to the data block
     */
    void assignToBlock(Block b) {
        flush();
        blk = b;
        contents.read(blk);
        pins = 0;
    }

    /**
     * Initializes the buffer's page according to the specified formatter,
     * and appends the page to the specified file.
     * If the buffer was dirty, then the contents
     * of the previous page are first written to disk.
     * @param filename the name of the file
     * @param fmtr a page formatter, used to initialize the page
     */
    void assignToNew(String filename, PageFormatter fmtr) {
        flush();
        fmtr.format(contents);
        blk = contents.append(filename);
        pins = 0;
    }

    /**
     * CS4432-Project1: Reports the last updated time for use with replacement policies
     * The time is represented in nanoseconds since the UNIX Epoch
     */
    public long getLastAccessTime(){
        return lastAccessTime;
    }

    /**
     * CS4432-Project1: Returns the ref (second chance) bit of a buffer,
     * which is used by the clock replacement policy.
     */
    public boolean hasSecondChance() {
        return refBit;
    }

    /**
     * CS4432-Project1: Sets the ref (second chance) but of a buffer.
     * @param refBit the second chance bit
     */
    public void setSecondChance(boolean refBit) {
        this.refBit = refBit;
    }

    /**
     * CS4432-Project1: Custom toString method to provide details about this buffer.
     * @return String representation of this buffer
     */
    @Override
    public String toString() {
        return "Buffer{" +
                "ID=" + ID +
                ", contents=" + contents +
                ", blk=" + blk +
                ", pins=" + pins +
                ", modifiedBy=" + modifiedBy +
                ", logSequenceNumber=" + logSequenceNumber +
                ", lastAccessTime=" + lastAccessTime +
                ", refBit=" + refBit +
                '}';
    }
}