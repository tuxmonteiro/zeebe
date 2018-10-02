package io.zeebe.broker.spike;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import io.zeebe.test.util.AutoCloseableRule;

public class NewElementInstanceStateTest {


  @Rule public TemporaryFolder folder = new TemporaryFolder();

  @Rule public AutoCloseableRule closeables = new AutoCloseableRule();

  private NewElementInstanceState elementInstanceState;

  @Before
  public void setUp() throws Exception {
    final DbBuilder builder = new DbBuilder(folder.newFolder("db"));
    NewElementInstanceState.declareStores(builder);
    builder.open();

    elementInstanceState = new NewElementInstanceState(builder);

    // TODO: close some things
  }

  @Test
  public void shouldMaintainParentChildHierarchy()
  {
    // given
    final ElementInstanceValue parentInstance = elementInstanceState.newInstance(1, "parent");

    final ElementInstanceValue child1 = elementInstanceState.newInstance(parentInstance, 2, "child1");
    elementInstanceState.newInstance(parentInstance, 3, "child2");
    elementInstanceState.newInstance(parentInstance, 4, "child3");
    elementInstanceState.newInstance(child1, 5, "grandChild1");

    // when
    final List<ElementInstanceValue> children = elementInstanceState.getChildren(parentInstance.getKey());
    assertThat(children).hasSize(3);
    assertThat(children).extracting(c -> c.getKey(), c -> c.getValue())
      .containsExactlyInAnyOrder(tuple(2L, "child1"), tuple(3L, "child2"), tuple(4L, "child3"));
  }

}
