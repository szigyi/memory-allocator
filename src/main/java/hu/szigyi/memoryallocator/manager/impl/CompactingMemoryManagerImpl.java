package hu.szigyi.memoryallocator.manager.impl;

import hu.szigyi.memoryallocator.manager.MemoryManager;
import hu.szigyi.memoryallocator.model.ContiguousDataBlock;
import hu.szigyi.memoryallocator.model.DataBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author szabolcs on 28/09/2017.
 */
public class CompactingMemoryManagerImpl implements MemoryManager {

    private static final Logger LOG = LoggerFactory.getLogger(CompactingMemoryManagerImpl.class);

    private ReentrantLock lock;

    private ReentrantLock compactingLock;

    private static final int BLOCK_SIZE = 1024;

    private final int maxSize;

    private byte[][] storage;

    private int endPointer;

    private int free;

    private List<ContiguousDataBlock> allocatedBlocks;

    public CompactingMemoryManagerImpl(int sizeOfStorage) {
        compactingLock = new ReentrantLock();
        lock = new ReentrantLock();
        // tracking datablocks for compacting, move them
        allocatedBlocks = new ArrayList<>();
        storage = new byte[sizeOfStorage][BLOCK_SIZE];
        // store it to know when the endPointer is overflow
        maxSize = sizeOfStorage - 1;
        // tracking the contiguous blocks end
        endPointer = -1;
        // tracking the free space in the whole storage to quickly reject request if does not fit
        free = sizeOfStorage;

        LOG.info("MemoryManager is created with size: {}", sizeOfStorage);
    }

    @Override
    public DataBlock allocate(int numBlocksRequired) {
        if (numBlocksRequired < 1) {
            throw new IllegalArgumentException("Requested size of blocks is invalid!");
        }

        lock.lock();

        if (free < numBlocksRequired) {
            lock.unlock();
            throw new RuntimeException("There is no available storage to allocate the requested number of blocks!");
        }

        int remainingSpaceAtEnd = maxSize - endPointer;

        if (remainingSpaceAtEnd < numBlocksRequired) {
            // there is no enough space at the end for contiguous block
            // but there are enough space in the whole storage
            // therefore compacting is necessary
            endPointer = compacting();
        }
        // there should be enough space at the end to allocate for the new datablock
        int startIndex = endPointer + 1;
        int endIndex = startIndex + numBlocksRequired - 1;
        ContiguousDataBlockImpl db = new ContiguousDataBlockImpl(storage, startIndex, endIndex, compactingLock);
        allocatedBlocks.add(db);

        endPointer += numBlocksRequired;
        free -= numBlocksRequired;

        lock.unlock();

        LOG.info("Allocated: start:{}, end:{}", db.getStart(), db.getEnd());
        return db;
    }

    private int compacting() {
        compactingLock.lock();
        // we know all the allocated, not released datablocks
        Iterator<ContiguousDataBlock> iterator = allocatedBlocks.iterator();
        // calculating the new endPointer by shifting the allocated datablocks to left
        int newEndPointer = -1;
        while (iterator.hasNext()) {
            ContiguousDataBlock db = iterator.next();
            Integer firstIndex = db.getStart();
            if (newEndPointer < firstIndex - 1) {
                // there is at least one free space beforehand
                // therefore shifting the datablock
                byte[] data = db.read();
                int size = db.getEnd() - db.getStart();
                int startIndex = newEndPointer + 1;
                int endIndex = startIndex + size;
                LOG.info("Shifting: [{},{}]->[{},{}]", db.getStart(), db.getEnd(), startIndex, endIndex);
                db.setStart(startIndex);
                db.setEnd(endIndex);
                db.write(data);
            }

            Integer lastIndex = db.getEnd();
            newEndPointer = lastIndex;
        }
        compactingLock.unlock();
        return newEndPointer;
    }

    @Override
    public void release(DataBlock dataBlock) {
        ContiguousDataBlock db = (ContiguousDataBlock) dataBlock;
        int size = db.getEnd() - db.getStart();

        lock.lock();

        // Make sure cannot reuse this datablock
        dataBlock.setReleased();
        free += size;
        // Compacting phase will take care to actually release the space
        allocatedBlocks.remove(dataBlock);

        lock.unlock();

        LOG.info("Released: start:{}, end:{}", db.getStart(), db.getEnd());
    }
}
