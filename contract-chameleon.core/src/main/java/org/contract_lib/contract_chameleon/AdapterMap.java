
package org.contract_lib.contract_chameleon;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdapterMap<T extends AdapterId> implements Iterable<java.util.Map.Entry<String, T>> {

  private final HashMap<String, T> adapters = new HashMap<String, T>();

  public AdapterMap(Class<T> type) {
    ServiceLoader<T> loader = ServiceLoader.load(type, ClassLoader.getSystemClassLoader());
    for (T a : loader) {
      String adapterName = a.getAdapterName();
      adapters.put(adapterName, a);
    }
  }

  public T get(String id) {
    return adapters.get(id);
    //TODO: Add error handling
  }

  public Set<String> adapterNames() {
    return adapters.keySet();
  }

  public String adapterList() {
    String adapterList = sortedElements()
        .map(this::printListElement)
        .collect(Collectors.joining(System.lineSeparator()));
    return "Adapters" + System.lineSeparator() + adapterList;
  }

  public Stream<Map.Entry<String, T>> sortedElements() {
    return this.adapters
        .entrySet()
        .stream()
        .sorted(Comparator.comparing(Map.Entry::getKey));
  }

  @Override
  public Iterator<Map.Entry<String, T>> iterator() {
    return adapters.entrySet().iterator();
  }

  private String printListElement(Map.Entry<String, T> element) {
    return "▶︎ " + element.getKey();
  }
}
