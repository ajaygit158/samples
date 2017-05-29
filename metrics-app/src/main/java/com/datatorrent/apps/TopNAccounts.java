package com.datatorrent.apps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.lang3.mutable.MutableInt;

import com.datatorrent.api.AutoMetric;
import com.datatorrent.api.DefaultInputPort;
import com.datatorrent.common.util.BaseOperator;
import com.datatorrent.common.util.Pair;

/**
 * Created by chinmay on 29/5/17.
 */
public class TopNAccounts extends BaseOperator
{
  @AutoMetric
  private Collection<Collection<Pair<String, Object>>> topN = new ArrayList<>();

  private Map<String, MutableInt> collectives = new HashMap<>();

  public final transient DefaultInputPort<Object> in = new DefaultInputPort<Object>()
  {
    @Override
    public void process(Object t)
    {
      PojoEvent tuple = (PojoEvent)t;
      String name = tuple.getName();
      int amount = tuple.getAmount();

      MutableInt i = collectives.get(name);
      if (i == null) {
        collectives.put(name, new MutableInt(amount));
      } else {
        i.add(amount);
      }
    }
  };

  @Override
  public void beginWindow(long windowId)
  {
    super.beginWindow(windowId);

//    topN.clear();
//    Collection<Pair<String, Object>> val = new ArrayList<>();
//    val.add(new Pair<String, Object>("hashtag", "hello1"));
//    val.add(new Pair<String, Object>("count", new Random().nextInt(100)));
//
//    Collection<Pair<String, Object>> val1 = new ArrayList<>();
//    val1.add(new Pair<String, Object>("hashtag", "hello2"));
//    val1.add(new Pair<String, Object>("count", new Random().nextInt(100)));
//
//    topN.add(val);
//    topN.add(val1);
//
    Map<String, Integer> unsortedMap = new HashMap<>();
    for (Map.Entry<String, MutableInt> entry : collectives.entrySet()) {
      unsortedMap.put(entry.getKey(), entry.getValue().getValue());
    }

    Map<String, Integer> sortedMap = sortByValue(unsortedMap);
    topN.clear();
    if (sortedMap.size() <= 0) {
      sortedMap.put("NA1", new Integer(1));
      sortedMap.put("NA2", new Integer(2));
    }

    int count = 5;
    for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
      Collection<Pair<String, Object>> row = new ArrayList<>();
      row.add(new Pair<String, Object>("Name", entry.getKey()));
      row.add(new Pair<String, Object>("Value", entry.getValue()));
      topN.add(row);
      if (count-- <= 0) {
        break;
      }
    }

    System.out.println("TOP N Size : " + topN.size());
  }

  public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> unsortMap) {

    List<Map.Entry<K, V>> list =
      new LinkedList<Map.Entry<K, V>>(unsortMap.entrySet());

    Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
      public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
        return (o2.getValue()).compareTo(o1.getValue());
      }
    });

    Map<K, V> result = new LinkedHashMap<K, V>();
    for (Map.Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }

    return result;
  }
}