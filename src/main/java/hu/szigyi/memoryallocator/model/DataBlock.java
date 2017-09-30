package hu.szigyi.memoryallocator.model;

public interface DataBlock {

    void setReleased();

    boolean isReleased();

    byte[] read();

    boolean write(byte[] data);
}
