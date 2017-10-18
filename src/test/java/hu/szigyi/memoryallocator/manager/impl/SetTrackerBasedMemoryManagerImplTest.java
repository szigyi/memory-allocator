package hu.szigyi.memoryallocator.manager.impl;

import hu.szigyi.memoryallocator.manager.MemoryManager;
import hu.szigyi.memoryallocator.model.FragmentedDataBlock;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class SetTrackerBasedMemoryManagerImplTest {

    private static final Logger LOG = LoggerFactory.getLogger(SetTrackerBasedMemoryManagerImplTest.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MemoryManager memoryManager;

    @Test
    public void whenSizeOfBlocksIsInvalid() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        int size = 1;
        int requestedBlocks = 0;
        memoryManager = new SetTrackerBasedMemoryManagerImpl(size);

        memoryManager.allocate(requestedBlocks);
    }

    @Test
    public void whenThereIsNoAvailableStorage_thenThrowsException() throws Exception {
        thrown.expect(RuntimeException.class);
        int size = 3;
        int requestedBlocks = 2;
        memoryManager = new SetTrackerBasedMemoryManagerImpl(size);

        memoryManager.allocate(requestedBlocks);
        memoryManager.allocate(requestedBlocks);
    }

    @Test
    public void whenRequestedSizeBiggerThenStorage_thenThrowsException() throws Exception {
        thrown.expect(RuntimeException.class);
        int size = 1;
        int requestedBlocks = 2;
        memoryManager = new SetTrackerBasedMemoryManagerImpl(size);

        memoryManager.allocate(requestedBlocks);
    }

    @Test
    public void whenAllocated_thenIndicesAreContinuous() throws Exception {
        int size = 5;
        int requestedBlocks = 2;
        memoryManager = new SetTrackerBasedMemoryManagerImpl(size);

        FragmentedDataBlock db1 = (FragmentedDataBlock) memoryManager.allocate(requestedBlocks);
        FragmentedDataBlock db2 = (FragmentedDataBlock) memoryManager.allocate(requestedBlocks);

        assertEquals(Arrays.asList(0, 1), db1.getIndices());
        assertEquals(Arrays.asList(2, 3), db2.getIndices());
    }

    @Test
    public void whenOverAllocated_thenDoesNotLeak() throws Exception {
        int size = 2;
        int requestedBlocks = 3;
        memoryManager = new SetTrackerBasedMemoryManagerImpl(size);

        try {
            memoryManager.allocate(requestedBlocks);
        } catch (RuntimeException e) {

        }

        FragmentedDataBlock db2 = (FragmentedDataBlock) memoryManager.allocate(2);

        assertEquals(Arrays.asList(0, 1), db2.getIndices());
    }

    @Test
    public void whenReleased_thenFreeSpaceReused() throws Exception {
        int size = 6;
        int requestedBlocks = 2;
        memoryManager = new SetTrackerBasedMemoryManagerImpl(size);

        FragmentedDataBlock db1 = (FragmentedDataBlock) memoryManager.allocate(requestedBlocks);
        FragmentedDataBlock db2 = (FragmentedDataBlock) memoryManager.allocate(requestedBlocks);
        FragmentedDataBlock db3 = (FragmentedDataBlock) memoryManager.allocate(requestedBlocks);

        memoryManager.release(db2);

        FragmentedDataBlock db4 = (FragmentedDataBlock) memoryManager.allocate(requestedBlocks);

        assertEquals(Arrays.asList(0, 1), db1.getIndices());
        assertEquals(Arrays.asList(2, 3), db2.getIndices());
        assertEquals(Arrays.asList(4, 5), db3.getIndices());
        assertEquals(Arrays.asList(2, 3), db4.getIndices());
    }

    @Test
    public void whenReleased_thenFragmentedFreeSpaceReused() throws Exception {
        int size = 10;
        int requestedBlocks = 2;
        memoryManager = new SetTrackerBasedMemoryManagerImpl(size);

        FragmentedDataBlock db1 = (FragmentedDataBlock) memoryManager.allocate(requestedBlocks);
        FragmentedDataBlock db2 = (FragmentedDataBlock) memoryManager.allocate(3);
        FragmentedDataBlock db3 = (FragmentedDataBlock) memoryManager.allocate(requestedBlocks);

        memoryManager.release(db2);

        FragmentedDataBlock db4 = (FragmentedDataBlock) memoryManager.allocate(requestedBlocks);
        FragmentedDataBlock db5 = (FragmentedDataBlock) memoryManager.allocate(requestedBlocks);

        assertEquals(Arrays.asList(0, 1), db1.getIndices());
        assertEquals(Arrays.asList(2, 3, 4), db2.getIndices());
        assertEquals(Arrays.asList(5, 6), db3.getIndices());
        assertEquals(Arrays.asList(2, 3), db4.getIndices());
        assertEquals(Arrays.asList(4, 7), db5.getIndices());
    }

}