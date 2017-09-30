package hu.szigyi.memoryallocator.manager.impl;

import hu.szigyi.memoryallocator.model.ContiguousDataBlock;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class ContiguousDataBlockImpl implements ContiguousDataBlock {

    private final byte[][] storage;
    private int start;
    private int end;
    private boolean released;
    private ReentrantLock lock;
    private ReentrantLock compactingLock;

    ContiguousDataBlockImpl(byte[][] storage, int start, int end, ReentrantLock compactingLock) {
        lock = new ReentrantLock();
        Objects.requireNonNull(storage);
        this.storage = storage;
        this.start = start;
        this.end = end;
        this.compactingLock = compactingLock;
    }

    @Override
    public void setReleased() {
        lock.lock();
        this.released = true;
        lock.unlock();
    }

    @Override
    public boolean isReleased() {
        return released;
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public void setStart(int start) {
        this.start = start;
    }

    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public byte[] read() {
        if (released) {
            throw new RuntimeException("This resource is already released!");
        }
        compactingLock.lock();
        lock.lock();
        // do read
        lock.unlock();
        compactingLock.unlock();
        return new byte[0];
    }

    @Override
    public boolean write(byte[] data) {
        if (released) {
            throw new RuntimeException("This resource is already released!");
        }
        compactingLock.lock();
        lock.lock();
        // do write
        lock.unlock();
        compactingLock.unlock();
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContiguousDataBlockImpl that = (ContiguousDataBlockImpl) o;

        if (start != that.start) return false;
        return end == that.end;
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        return result;
    }

    @Override
    public String toString() {
        return "ContiguousDataBlockImpl{" +
                "start=" + start +
                ", end=" + end +
                ", released=" + released +
                '}';
    }
}
