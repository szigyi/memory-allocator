package hu.szigyi.memoryallocator.manager.impl;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import hu.szigyi.memoryallocator.manager.MemoryManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllocationBenchmarkTest {

    private static final Logger LOG = LoggerFactory.getLogger(AllocationBenchmarkTest.class);

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private void runTest(int size, MemoryManager memoryManager) {
        int requestedBlocks = 5;

        for (int i = 0; i < size % requestedBlocks; i++) {
            memoryManager.allocate(requestedBlocks);
        }
    }

    @Test
    public void ArrayBasedMemoryManagerImpl() throws Exception {
        int size = 500_000;
        MemoryManager memoryManager = new ArrayBasedMemoryManagerImpl(size);

        runTest(size, memoryManager);
    }

    @Test
    public void SetBasedMemoryManagerImpl() throws Exception {
        int size = 500_000;
        MemoryManager memoryManager = new SetBasedMemoryManagerImpl(size);

        runTest(size, memoryManager);
    }

    @Test
    public void CompactingMemoryManagerImpl() throws Exception {
        int size = 500_000;
        MemoryManager memoryManager = new CompactingMemoryManagerImpl(size);

        runTest(size, memoryManager);
    }

    @Test
    public void SetTrackerBasedMemoryManagerImpl() throws Exception {
        int size = 500_000;
        MemoryManager memoryManager = new SetTrackerBasedMemoryManagerImpl(size);

        runTest(size, memoryManager);
    }
}
