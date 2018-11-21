package io.zeebe.logstreams.rocksdb;

import java.util.Arrays;
import java.util.List;
import org.rocksdb.DBOptions;

public class TestStateDb extends ZbStateDb {
  private static final byte[] TEST_STATE_PREFIX = "testState".getBytes();
  private static final List<ZbStateDescriptor> STATE_DESCRIPTORS =
      Arrays.asList(
          new ZbStateDescriptor<>(TestState::new, TestState.getDescriptors(TEST_STATE_PREFIX)));

  @Override
  protected DBOptions createOptions() {
    return new DBOptions();
  }

  @Override
  protected List<ZbStateDescriptor> getStateDescriptors() {
    return STATE_DESCRIPTORS;
  }
}
