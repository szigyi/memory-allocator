package hu.szigyi.memoryallocator.manager.impl;

import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import hu.szigyi.memoryallocator.manager.MemoryManager;
import hu.szigyi.memoryallocator.model.DataBlock;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class HeavyReleasePatternBenchmarkTest {

    private static final Logger LOG = LoggerFactory.getLogger(HeavyReleasePatternBenchmarkTest.class);

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private void runTest(int size, MemoryManager memoryManager) {
        Set<DataBlock> dbs = new HashSet<>();
        int requestedBlocks = 5;

        for (int i = 0; i < size % requestedBlocks; i++) {
            DataBlock dataBlock = memoryManager.allocate(requestedBlocks);
            dbs.add(dataBlock);

            if (i % 10 == 0) {
                int j = 0;
                Iterator<DataBlock> iterator = dbs.iterator();
                while (iterator.hasNext()) {
                    DataBlock db = iterator.next();
                    memoryManager.release(db);
                    iterator.remove();
                    if (j == 9) {
                        break;
                    }
                    j++;
                }
            }
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
