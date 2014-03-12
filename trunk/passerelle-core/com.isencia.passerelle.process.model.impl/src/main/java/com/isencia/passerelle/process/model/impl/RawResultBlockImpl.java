package com.isencia.passerelle.process.model.impl;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.isencia.passerelle.process.model.RawResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.ResultItemFromRawBuilder;
import com.isencia.passerelle.process.model.Task;

@Entity
@DiscriminatorValue("RAW_RESULTBLOCK")
public class RawResultBlockImpl extends ResultBlockImpl implements RawResultBlock {
  private static final long serialVersionUID = 1L;
  public static final String RESULT_ITEM_FROM_RAW_BUILDER_ATT_NAME = "ResultItemFromRawBuilderName";

  // treemap to have natural ordering on keys
  @Transient
  private Map<String, ResultItem<?>> generatedResultItems = new TreeMap<String, ResultItem<?>>();

  @Transient
  private Set<String> generationFlags = new HashSet<String>();

  public RawResultBlockImpl() {
  }

  public RawResultBlockImpl(Task task, String type) {
    super(task, type);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.isencia.passerelle.process.model.RawResultBlock#putRawResultItem(com.isencia.passerelle.process.model.ResultItem
   * , com.isencia.passerelle.process.model.ResultItemFromRawBuilder)
   */
  public ResultItem<?> putRawResultItem(ResultItem<?> rawResultitem, ResultItemFromRawBuilder builder) {
    testBuilderExistence(builder);
    setResultItemBuilder(rawResultitem, builder);
    return super.putItem(rawResultitem);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.isencia.passerelle.process.model.impl.ResultBlockImpl#getResultItemMap()
   */
  @Override
  protected Map<String, ResultItem<?>> getResultItemMap() {
    Map<String, ResultItem<?>> resultItemMap = super.getResultItemMap();
    synchronized (generationFlags) {
      for (String key : resultItemMap.keySet()) {
        if (!generationFlags.contains(key)) {
          generateAndAddResultItemsFor(resultItemMap.get(key));
          generationFlags.add(key);
        }
      }
    }
    // linked hashmap so that the insertion order is kept, want to first show the result items from DB, followed by
    // generated ones
    Map<String, ResultItem<?>> merged = new LinkedHashMap<String, ResultItem<?>>();
    merged.putAll(resultItemMap);
    merged.putAll(generatedResultItems);
    return merged;
  }

  private void generateAndAddResultItemsFor(ResultItem<?> rawResultItem) {
    ResultItemFromRawBuilder resultItemBuilder = getResultItemBuilder(rawResultItem);
    generatedResultItems.putAll(resultItemBuilder.transformRawToResultItems(rawResultItem));
  }

  private void setResultItemBuilder(ResultItem<?> rawResultitem, ResultItemFromRawBuilder builder) {
    new ResultItemAttributeImpl(rawResultitem, RESULT_ITEM_FROM_RAW_BUILDER_ATT_NAME, builder.getName());
  }

  private ResultItemFromRawBuilder getResultItemBuilder(ResultItem<?> rawResultitem) {
    String resultBuilderName = rawResultitem.getAttribute(RESULT_ITEM_FROM_RAW_BUILDER_ATT_NAME).getValueAsString();
    return ResultItemFromRawBuilderRegistryImpl.getInstance().getBuilderByName(resultBuilderName);
  }

  private void testBuilderExistence(ResultItemFromRawBuilder builder) {
    ResultItemFromRawBuilderRegistryImpl.getInstance().getBuilderByName(builder.getName());
  }
}
