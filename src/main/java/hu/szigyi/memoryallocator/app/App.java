package hu.szigyi.memoryallocator.app;

import hu.szigyi.memoryallocator.manager.MemoryManager;
import hu.szigyi.memoryallocator.manager.impl.CompactingMemoryManagerImpl;
import hu.szigyi.memoryallocator.manager.impl.SetTrackerBasedMemoryManagerImpl;
import hu.szigyi.memoryallocator.model.DataBlock;

/**
 * @author szabolcs on 18/10/2017.
 */
public class App {

    public static void main(String[] args) {
        int repeatRun = 1000;
        int size = 500_000;
        int requestedBlocks = 5;

//        MemoryManager memoryManager = new SetTrackerBasedMemoryManagerImpl(size);
        MemoryManager memoryManager = new CompactingMemoryManagerImpl(size);
        for (int i = 0; i < repeatRun; i++) {
            runApp(memoryManager, size, requestedBlocks);
        }
    }

    private static void runApp(MemoryManager memoryManager, int size, int requestedBlocks) {
        for (int i = 0; i < size / requestedBlocks; i++) {
            DataBlock block = memoryManager.allocate(requestedBlocks);
            memoryManager.release(block);
        }
    }
}
