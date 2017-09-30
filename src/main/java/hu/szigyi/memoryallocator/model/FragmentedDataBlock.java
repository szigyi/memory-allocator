package hu.szigyi.memoryallocator.model;

import java.util.List;

public interface FragmentedDataBlock extends DataBlock {

    List<Integer> getIndices();
}
