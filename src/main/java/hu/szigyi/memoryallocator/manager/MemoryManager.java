package hu.szigyi.memoryallocator.manager;


import hu.szigyi.memoryallocator.model.DataBlock;

public interface MemoryManager {

    DataBlock allocate(int numBlocksRequired);

    void release(DataBlock dataBlock);
}
