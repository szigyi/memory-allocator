package hu.szigyi.memoryallocator.manager.impl;

import hu.szigyi.memoryallocator.manager.MemoryManager;
import hu.szigyi.memoryallocator.model.ContiguousDataBlock;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompactingMemoryManagerImplTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MemoryManager memoryManager;

    @Test
    public void whenSizeOfBlocksIsInvalid() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        int size = 1;
        int requestedBlocks = 0;
        memoryManager = new CompactingMemoryManagerImpl(size);

        memoryManager.allocate(requestedBlocks);
    }

    @Test
    public void whenThereIsNoAvailableStorage_thenThrowsException() throws Exception {
        thrown.expect(RuntimeException.class);
        int size = 3;
        int requestedBlocks = 2;
        memoryManager = new CompactingMemoryManagerImpl(size);

        memoryManager.allocate(requestedBlocks);
        memoryManager.allocate(requestedBlocks);
    }

    @Test
    public void whenRequestedSizeBiggerThenStorage_thenThrowsException() throws Exception {
        thrown.expect(RuntimeException.class);
        int size = 1;
        int requestedBlocks = 2;
        memoryManager = new CompactingMemoryManagerImpl(size);

        memoryManager.allocate(requestedBlocks);
    }

    @Test
    public void whenAllocated_thenIndicesAreContinuous() throws Exception {
        int size = 5;
        int requestedBlocks = 2;
        memoryManager = new CompactingMemoryManagerImpl(size);

        ContiguousDataBlock db1 = (ContiguousDataBlock) memoryManager.allocate(requestedBlocks);
        ContiguousDataBlock db2 = (ContiguousDataBlock) memoryManager.allocate(requestedBlocks);

        assertEquals(0, db1.getStart());
        assertEquals(1, db1.getEnd());

        assertEquals(2, db2.getStart());
        assertEquals(3, db2.getEnd());
    }

    @Test
    public void whenBlocksAreMinimumLength_thenIndicesAreContinuous() throws Exception {
        int size = 3;
        int requestedBlocks = 1;
        memoryManager = new CompactingMemoryManagerImpl(size);

        ContiguousDataBlock db1 = (ContiguousDataBlock) memoryManager.allocate(requestedBlocks);
        ContiguousDataBlock db2 = (ContiguousDataBlock) memoryManager.allocate(requestedBlocks);
        ContiguousDataBlock db3 = (ContiguousDataBlock) memoryManager.allocate(requestedBlocks);

        assertEquals(0, db1.getStart());
        assertEquals(0, db1.getEnd());

        assertEquals(1, db2.getStart());
        assertEquals(1, db2.getEnd());

        assertEquals(2, db3.getStart());
        assertEquals(2, db3.getEnd());
    }

    @Test
    public void whenOverAllocated_thenDoesNotLeak() throws Exception {
        int size = 2;
        int requestedBlocks = 3;
        memoryManager = new CompactingMemoryManagerImpl(size);

        try {
            memoryManager.allocate(requestedBlocks);
        } catch (RuntimeException e) {

        }

        ContiguousDataBlock db2 = (ContiguousDataBlock) memoryManager.allocate(2);

        assertEquals(0, db2.getStart());
        assertEquals(1, db2.getEnd());
    }

    @Test
    public void whenReleasedAndThereIsFreeSpaceAtTheEnd_thenReleasedSpaceNotTouched() throws Exception {
        int size = 6;
        int requestedBlocks = 2;
        memoryManager = new CompactingMemoryManagerImpl(size);

        ContiguousDataBlock db1 = (ContiguousDataBlock) memoryManager.allocate(requestedBlocks);
        ContiguousDataBlock db2 = (ContiguousDataBlock) memoryManager.allocate(requestedBlocks);

        memoryManager.release(db2);

        ContiguousDataBlock db3 = (ContiguousDataBlock) memoryManager.allocate(requestedBlocks);

        assertEquals(0, db1.getStart());
        assertEquals(1, db1.getEnd());

        assertEquals(2, db2.getStart());
        assertEquals(3, db2.getEnd());

        assertEquals(4, db3.getStart());
        assertEquals(5, db3.getEnd());
    }

    @Test
    public void whenReleasedAndThereIsNoSpaceAtTheEnd_thenCompacting() throws Exception {
        int size = 7;
        int requestedBlocks = 2;
        memoryManager = new CompactingMemoryManagerImpl(size);

        ContiguousDataBlock db1 = (ContiguousDataBlock) memoryManager.allocate(requestedBlocks);
        ContiguousDataBlock db2 = (ContiguousDataBlock) memoryManager.allocate(3);
        ContiguousDataBlock db3 = (ContiguousDataBlock) memoryManager.allocate(requestedBlocks);

        memoryManager.release(db2);

        // Compacting before allocate
        ContiguousDataBlock db4 = (ContiguousDataBlock) memoryManager.allocate(requestedBlocks);

        assertEquals(0, db1.getStart());
        assertEquals(1, db1.getEnd());

        assertTrue(db2.isReleased());

        assertEquals(2, db3.getStart());
        assertEquals(3, db3.getEnd());

        assertEquals(4, db4.getStart());
        assertEquals(5, db4.getEnd());
    }

    @Test
    public void whenReleasedAndThereIsNoEnoughSpaceAtTheEnd_thenCompacting() throws Exception {
        int size = 8;
        int requestedBlocks = 2;
        memoryManager = new CompactingMemoryManagerImpl(size);

        ContiguousDataBlock db1 = (ContiguousDataBlock) memoryManager.allocate(requestedBlocks);
        ContiguousDataBlock db2 = (ContiguousDataBlock) memoryManager.allocate(3);
        ContiguousDataBlock db3 = (ContiguousDataBlock) memoryManager.allocate(requestedBlocks);

        memoryManager.release(db2);

        // Compacting before allocate
        ContiguousDataBlock db4 = (ContiguousDataBlock) memoryManager.allocate(requestedBlocks);

        assertEquals(0, db1.getStart());
        assertEquals(1, db1.getEnd());

        assertTrue(db2.isReleased());

        assertEquals(2, db3.getStart());
        assertEquals(3, db3.getEnd());

        assertEquals(4, db4.getStart());
        assertEquals(5, db4.getEnd());
    }

}