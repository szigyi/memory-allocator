package hu.szigyi.memoryallocator.manager.impl;

import hu.szigyi.memoryallocator.manager.MemoryManager;
import hu.szigyi.memoryallocator.model.ContiguousDataBlock;
import hu.szigyi.memoryallocator.model.DataBlock;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class CompactingMemoryManagerImplConcurrentTest {

    private static final Logger LOG = LoggerFactory.getLogger(CompactingMemoryManagerImplConcurrentTest.class);

    private MemoryManager memoryManager;

    @Test
    public void whenMultipleThreadAllocate_thenShouldNotAllocateSameIndex() {
        int size = 100;
        int requestedBlocks1 = 5;
        int requestedBlocks2 = 5;
        memoryManager = new CompactingMemoryManagerImpl(size);

        List<DataBlock> blocks1 = new ArrayList<>();
        List<DataBlock> blocks2 = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        TestClient client1 = new TestClient(memoryManager, blocks1, requestedBlocks1);
        TestClient client2 = new TestClient(memoryManager, blocks2, requestedBlocks2);

        executor.execute(client1);
        executor.execute(client2);

        executor.shutdown();
        while (!executor.isTerminated()) {

        }

        List<Integer> blocks1Indices = new ArrayList<>();
        List<Integer> blocks2Indices = new ArrayList<>();


        blocks1.forEach(block -> {
            int startIndex = ((ContiguousDataBlock) block).getStart();
            int endIndex = ((ContiguousDataBlock) block).getEnd();
            blocks1Indices.addAll(listOfIntegerFromBounds(startIndex, endIndex));
        });
        blocks2.forEach(block -> {
            int startIndex = ((ContiguousDataBlock) block).getStart();
            int endIndex = ((ContiguousDataBlock) block).getEnd();
            blocks2Indices.addAll(listOfIntegerFromBounds(startIndex, endIndex));
        });

        assertThat(blocks1Indices, hasSize(15));
        assertThat(blocks2Indices, hasSize(15));
        assertThat(blocks1Indices, not(containsInAnyOrder(blocks2Indices.toArray())));

        LOG.info(blocks1Indices.toString());
        LOG.info(blocks2Indices.toString());
    }

    @Test
    public void whenCompacting_thenShouldNotCauseDeadLock() {
        int size = 35;
        int requestedBlocks1 = 5;
        int requestedBlocks2 = 5;
        memoryManager = new CompactingMemoryManagerImpl(size);

        List<DataBlock> blocks1 = new ArrayList<>();
        List<DataBlock> blocks2 = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        TestClientCompacting client1 = new TestClientCompacting(memoryManager, blocks1, requestedBlocks1);
        TestClientCompacting client2 = new TestClientCompacting(memoryManager, blocks2, requestedBlocks2);

        executor.execute(client1);
        executor.execute(client2);

        executor.shutdown();
        while (!executor.isTerminated()) {

        }

        List<Integer> blocks1Indices = new ArrayList<>();
        List<Integer> blocks2Indices = new ArrayList<>();


        blocks1.forEach(block -> {
            int startIndex = ((ContiguousDataBlock) block).getStart();
            int endIndex = ((ContiguousDataBlock) block).getEnd();
            blocks1Indices.addAll(listOfIntegerFromBounds(startIndex, endIndex));
        });
        blocks2.forEach(block -> {
            int startIndex = ((ContiguousDataBlock) block).getStart();
            int endIndex = ((ContiguousDataBlock) block).getEnd();
            blocks2Indices.addAll(listOfIntegerFromBounds(startIndex, endIndex));
        });

        assertThat(blocks1Indices, hasSize(15));
        assertThat(blocks2Indices, hasSize(15));
        assertThat(blocks1Indices, not(containsInAnyOrder(blocks2Indices.toArray())));

        LOG.info(blocks1Indices.toString());
        LOG.info(blocks2Indices.toString());
    }

    class TestClient implements Runnable {

        private MemoryManager memoryManager;

        private List<DataBlock> blocks;

        private int requestedBlocks;

        public TestClient(MemoryManager memoryManager, List<DataBlock> blocks, int requestedBlocks) {
            this.memoryManager = memoryManager;
            this.blocks = blocks;
            this.requestedBlocks = requestedBlocks;
        }

        @Override
        public void run() {
            DataBlock db1 = memoryManager.allocate(requestedBlocks);
            DataBlock db2 = memoryManager.allocate(requestedBlocks);
            DataBlock db3 = memoryManager.allocate(requestedBlocks);

            blocks.add(db1);
            blocks.add(db2);
            blocks.add(db3);
        }
    }

    class TestClientCompacting implements Runnable {

        private MemoryManager memoryManager;

        private List<DataBlock> blocks;

        private int requestedBlocks;

        public TestClientCompacting(MemoryManager memoryManager, List<DataBlock> blocks, int requestedBlocks) {
            this.memoryManager = memoryManager;
            this.blocks = blocks;
            this.requestedBlocks = requestedBlocks;
        }

        @Override
        public void run() {
            DataBlock db1 = memoryManager.allocate(requestedBlocks);
            DataBlock db2 = memoryManager.allocate(requestedBlocks);
            DataBlock db3 = memoryManager.allocate(requestedBlocks);

            memoryManager.release(db2);

            DataBlock db4 = memoryManager.allocate(requestedBlocks);

            blocks.add(db1);
//            blocks.add(db2);
            blocks.add(db3);
            blocks.add(db4);
        }
    }

    private List<Integer> listOfIntegerFromBounds(int startIndex, int endIndex) {
        return IntStream.range(startIndex, endIndex + 1).boxed().collect(Collectors.toList());
    }
}
