package hu.szigyi.memoryallocator.manager.impl;

import hu.szigyi.memoryallocator.manager.MemoryManager;
import hu.szigyi.memoryallocator.model.DataBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingMemoryManagerImpl implements MemoryManager {

    private static final Logger LOG = LoggerFactory.getLogger(BlockingMemoryManagerImpl.class);

    private static final int BLOCK_SIZE = 1024;

    private ReentrantLock lock;

    private byte[][] storage;

    private boolean[] allocatedFlag;

    private int free;

    public BlockingMemoryManagerImpl(int sizeOfStorage) {
        lock = new ReentrantLock();
        free = sizeOfStorage;
        storage = new byte[sizeOfStorage][BLOCK_SIZE];
        // this data structure helps tracking the allocated, free blocks
        allocatedFlag = new boolean[sizeOfStorage];
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

        List<Integer> indices = new ArrayList<>();
        // we know that enough free space in the storage
        // we have to find them (fragmented) and this is how it tracks it
        int alreadyAllocated = 0;
        for (int i = 0; i < allocatedFlag.length; i++) {
            boolean allocated = allocatedFlag[i];
            if (!allocated) {
                allocatedFlag[i] = true;
                // it stores the indices of the free spaces (fragmented)
                indices.add(i);
                alreadyAllocated++;
            }
            if (alreadyAllocated == numBlocksRequired) {
                break;
            }
        }
        free -= numBlocksRequired;
        lock.unlock();
        LOG.info("Allocated: {}", indices);

        DataBlock block = new FragmentedDataBlockImpl(storage, indices);

        return block;
    }

    @Override
    public void release(DataBlock dataBlock) {
        dataBlock.setReleased();
        List<Integer> indices = ((FragmentedDataBlockImpl) dataBlock).getIndices();

        lock.lock();

        for (Integer i : indices) {
            allocatedFlag[i] = false;
        }
        free += indices.size();

        lock.unlock();

        LOG.info("Released: {}", indices);
    }
}
