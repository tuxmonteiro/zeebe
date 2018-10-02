package io.zeebe.broker.spike;

import java.util.List;
import java.util.stream.Collectors;

public class NewElementInstanceState {

  private final LongKey longKey = new LongKey();
  private final LongValue longValue = new LongValue();
  private final ValueStore<LongKey, ElementInstanceValue> elementInstances;
  private final MultiValueStore<LongKey, LongValue> parentChildHierarchy;

  public NewElementInstanceState(DbBuilder dbBuilder)
  {
    elementInstances = dbBuilder.getValueStore("element-instances", LongKey.class, ElementInstanceValue.class);
    parentChildHierarchy = dbBuilder.getMultiValueStore("element-instance-hierarchy", LongKey.class, LongValue.class);

    // example for a sequence:
//    Sequence versionSequence = dbBuilder.getSequence("versions", 0, 1);
//    final long nextVersion = versionSequence.nextValue();
  }

  public static void declareStores(DbBuilder db)
  {
    db.declareStore("element-instances");
    db.declareStore("element-instance-hierarchy");
  }

  public ElementInstanceValue getInstance(long key)
  {
    longKey.set(key);
    return elementInstances.get(longKey);
  }

  public ElementInstanceValue newInstance(long key, String value)
  {
    return newInstance(null, key, value);
  }

  public ElementInstanceValue newInstance(ElementInstanceValue parent, long key, String value)
  {
    final ElementInstanceValue elementInstanceValue = new ElementInstanceValue();

    longKey.set(key);
    elementInstanceValue.setKey(key);
    elementInstanceValue.setValue(value);
    elementInstances.put(longKey, elementInstanceValue);

    if (parent != null)
    {
      longKey.set(parent.getKey());
      longValue.set(key);
      parentChildHierarchy.add(longKey, longValue);
    }

    return elementInstanceValue;
  }

  public List<ElementInstanceValue> getChildren(long scopeKey)
  {
    longKey.set(scopeKey);
    final List<LongValue> childElements = parentChildHierarchy.get(longKey);

    return childElements.stream()
        .map(k -> {
          longKey.set(k.get());
          return elementInstances.get(longKey);
        })
        .collect(Collectors.toList());
  }


}
