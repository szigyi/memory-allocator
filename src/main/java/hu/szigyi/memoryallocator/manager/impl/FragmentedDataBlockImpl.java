package hu.szigyi.memoryallocator.manager.impl;

import hu.szigyi.memoryallocator.model.FragmentedDataBlock;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class FragmentedDataBlockImpl implements FragmentedDataBlock {

    private final byte[][] storage;
    private final List<Integer> indices;
    private boolean released;
    private ReentrantLock lock;

    FragmentedDataBlockImpl(byte[][] storage, List<Integer> indices) {
        lock = new ReentrantLock();
        Objects.requireNonNull(storage);
        Objects.requireNonNull(indices);
        this.storage = storage;
        this.indices = indices;
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
    public List<Integer> getIndices() {
        return indices;
    }

    @Override
    public byte[] read() {
        lock.lock();
        if (released) {
            throw new RuntimeException("This resource is already released!");
        }
        lock.unlock();
        return new byte[0];
    }

    @Override
    public boolean write(byte[] data) {
        lock.lock();
        if (released) {
            throw new RuntimeException("This resource is already released!");
        }
        lock.unlock();
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FragmentedDataBlockImpl dataBlock = (FragmentedDataBlockImpl) o;

        return indices != null ? indices.equals(dataBlock.indices) : dataBlock.indices == null;
    }

    @Override
    public int hashCode() {
        return indices != null ? indices.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "FragmentedDataBlockImpl{" +
                "indices=" + indices +
                ", released=" + released +
                '}';
    }
}
