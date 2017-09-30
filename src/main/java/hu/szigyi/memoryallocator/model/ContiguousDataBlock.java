package hu.szigyi.memoryallocator.model;

public interface ContiguousDataBlock extends DataBlock {

    int getStart();

    void setStart(int start);

    int getEnd();

    void setEnd(int end);
}
