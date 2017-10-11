package hu.szigyi.memoryallocator.manager.impl;

import hu.szigyi.memoryallocator.manager.MemoryManager;
import hu.szigyi.memoryallocator.model.DataBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class SetBasedMemoryManagerImpl implements MemoryManager {

    private static final Logger LOG = LoggerFactory.getLogger(SetBasedMemoryManagerImpl.class);

    private static final int BLOCK_SIZE = 1024;

    private ReentrantLock lock;

    private byte[][] storage;

    private Set<Integer> allocatedFlag;

    private int free;

    public SetBasedMemoryManagerImpl(int sizeOfStorage) {
        lock = new ReentrantLock();
        free = sizeOfStorage;
        storage = new byte[sizeOfStorage][BLOCK_SIZE];
        // this data structure helps tracking the allocated blocks
        // FIXME why not tracking the remaining free spaces?
        allocatedFlag = new HashSet<>(sizeOfStorage);
        LOG.debug("MemoryManager is created with size: {}", sizeOfStorage);
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
        // FIXME iterating over the storage is not efficient
        for (int i = 0; i < storage.length; i++) {
            boolean allocated = allocatedFlag.contains(i);
            if (!allocated) {
                allocatedFlag.add(i);
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
        LOG.debug("Allocated: {}", indices);

        DataBlock block = new FragmentedDataBlockImpl(storage, indices);

        return block;
    }

    @Override
    public void release(DataBlock dataBlock) {
        dataBlock.setReleased();
        List<Integer> indices = ((FragmentedDataBlockImpl) dataBlock).getIndices();

        lock.lock();

        for (Integer i : indices) {
            allocatedFlag.remove(i);
        }
        free += indices.size();

        lock.unlock();

        LOG.debug("Released: {}", indices);
    }
}
