package com.isencia.passerelle.process.model.event;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.runtime.SimpleEvent;

public abstract class AbstractResultItemEventImpl<V extends Serializable> extends SimpleEvent implements ResultItem<V> {
  private static final long serialVersionUID = 3619256178951715634L;
  private Map<String, Attribute> attributes = new HashMap<String, Attribute>();
  private String colour;
  private V value;

  protected AbstractResultItemEventImpl(String topic, V value, Date creationTS, Long duration) {
    super(topic,creationTS,duration);
    this.value = value;
  }
  
  @Override
  public Attribute getAttribute(String name) {
    return attributes.get(name);
  }
  
  @Override
  public Iterator<String> getAttributeNames() {
    return attributes.keySet().iterator();
  }
  
  @Override
  public Set<Attribute> getAttributes() {
    return new HashSet<Attribute>(attributes.values());
  }

  @Override
  public String getColour() {
    return colour;
  }

  @Override
  public Long getId() {
    return null;
  }

  @Override
  public Integer getLevel() {
    return null;
  }

  @Override
  public String getName() {
    return getTopic();
  }

  @Override
  public ResultBlock getResultBlock() {
    return null;
  }

  @Override
  public String getScope() {
    return getType();
  }

  @Override
  public String getType() {
    if (getResultBlock() == null) {
      return null;
    }
    return getResultBlock().getType();
  }

  @Override
  public String getUnit() {
    return null;
  }

  @Override
  public V getValue() {
    return value;
  }

  @Override
  public String getValueAsString() {
    return value!=null?value.toString():null;
  }

  @Override
  public Attribute putAttribute(Attribute attribute) {
    return attributes.put(attribute.getName(), attribute);
  }
  
  @Override
  public void setColour(String colour) {
    this.colour = colour;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
    result = prime * result + ((colour == null) ? 0 : colour.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractResultItemEventImpl<?> other = (AbstractResultItemEventImpl<?>) obj;
    if (attributes == null) {
      if (other.attributes != null)
        return false;
    } else if (!attributes.equals(other.attributes))
      return false;
    if (colour == null) {
      if (other.colour != null)
        return false;
    } else if (!colour.equals(other.colour))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "AbstractResultItemEventImpl [" + super.toString() + ", value=" + value + "]";
  }
}
