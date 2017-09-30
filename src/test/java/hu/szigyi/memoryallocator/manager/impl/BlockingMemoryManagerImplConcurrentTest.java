package hu.szigyi.memoryallocator.manager.impl;

import hu.szigyi.memoryallocator.manager.MemoryManager;
import hu.szigyi.memoryallocator.model.DataBlock;
import hu.szigyi.memoryallocator.model.FragmentedDataBlock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class BlockingMemoryManagerImplConcurrentTest {

    private MemoryManager memoryManager;

    @Test
    public void whenMultipleThreadAllocate_thenShouldNotAllocateSameIndex() {
        int size = 100;
        int requestedBlocks1 = 5;
        int requestedBlocks2 = 5;
        memoryManager = new BlockingMemoryManagerImpl(size);

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

        blocks1.forEach(block -> blocks1Indices.addAll(((FragmentedDataBlock) block).getIndices()));
        blocks2.forEach(block -> blocks2Indices.addAll(((FragmentedDataBlock) block).getIndices()));

        assertThat(blocks1Indices, hasSize(15));
        assertThat(blocks2Indices, hasSize(15));
        assertThat(blocks1Indices, not(containsInAnyOrder(blocks2Indices.toArray())));

        System.out.println(blocks1Indices);
        System.out.println(blocks2Indices);
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
}
