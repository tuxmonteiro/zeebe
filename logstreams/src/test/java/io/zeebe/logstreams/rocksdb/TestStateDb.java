package io.zeebe.logstreams.rocksdb;

import java.util.Collections;
import java.util.List;

public class TestStateDb extends ZbStateDb {
  private static final byte[] TEST_STATE_PREFIX = "testState".getBytes();
  private static final List<ZbStateDescriptor> STATE_DESCRIPTORS =
      Collections.singletonList(
          new ZbStateDescriptor<>(TestState::new, TestState.getDescriptors(TEST_STATE_PREFIX)));

  private final TestState testState;

  public TestStateDb(String path, boolean reopen) {
    super(path, reopen);

    testState = (TestState) states.get(0);
  }

  @Override
  protected List<ZbStateDescriptor> getStateDescriptors() {
    return STATE_DESCRIPTORS;
  }

  public TestState getTestState() {
    return testState;
  }
}
