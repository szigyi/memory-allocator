package hu.szigyi.memoryallocator.manager.impl;

import hu.szigyi.memoryallocator.manager.MemoryManager;
import hu.szigyi.memoryallocator.model.DataBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SetTrackerBasedMemoryManagerImpl implements MemoryManager {

    private static final Logger LOG = LoggerFactory.getLogger(SetTrackerBasedMemoryManagerImpl.class);

    private static final int BLOCK_SIZE = 1024;

    private ReentrantLock lock;

    private byte[][] storage;

    private Set<Integer> allocatedFlag;

    private Set<Integer> unAllocatedFlag;

    private int free;

    public SetTrackerBasedMemoryManagerImpl(int sizeOfStorage) {
        lock = new ReentrantLock();
        free = sizeOfStorage;
        storage = new byte[sizeOfStorage][BLOCK_SIZE];
        // this data structure helps tracking the allocated and unallocated blocks
        allocatedFlag = new HashSet<>(sizeOfStorage);
        unAllocatedFlag = IntStream.range(0, sizeOfStorage).boxed().collect(Collectors.toSet());
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
        List<Integer> indices = flopFlagsAfterRemove(numBlocksRequired);
        indicesRemove(indices);

        free -= numBlocksRequired;
        lock.unlock();
        LOG.debug("Allocated: {}", indices);

        DataBlock block = new FragmentedDataBlockImpl(storage, indices);

        return block;
    }

    private List<Integer> flopFlagsIteratorRemove(int numBlocksRequired) {
        List<Integer> indices = new ArrayList<>();
        // we know that enough free space in the storage
        // we have to find them (fragmented) and this is how it tracks it
        int alreadyAllocated = 0;
        Iterator<Integer> unallocatedIterator = unAllocatedFlag.iterator();
        while (unallocatedIterator.hasNext() && alreadyAllocated != numBlocksRequired) {
            Integer i = unallocatedIterator.next();
            unallocatedIterator.remove();
            allocatedFlag.add(i);
            indices.add(i);
            alreadyAllocated++;
        }
        return indices;
    }

    private List<Integer> flopFlagsAfterRemove(int numBlocksRequired) {
        List<Integer> indices = new ArrayList<>();
        // we know that enough free space in the storage
        // we have to find them (fragmented) and this is how it tracks it
        int alreadyAllocated = 0;

        for (Integer i : unAllocatedFlag) {
            allocatedFlag.add(i);
            indices.add(i);
            alreadyAllocated++;

            if (alreadyAllocated == numBlocksRequired) {
                break;
            }
        }
        return indices;
    }

    private void indicesRemove(List<Integer> indices) {
        for (Integer index : indices) {
            unAllocatedFlag.remove(index);
        }
    }

    @Override
    public void release(DataBlock dataBlock) {
        dataBlock.setReleased();
        List<Integer> indices = ((FragmentedDataBlockImpl) dataBlock).getIndices();

        lock.lock();

        for (Integer i : indices) {
            allocatedFlag.remove(i);
            unAllocatedFlag.add(i);
        }
        free += indices.size();

        lock.unlock();

        LOG.debug("Released: {}", indices);
    }
}
