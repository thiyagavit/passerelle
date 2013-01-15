package com.isencia.passerelle.editor.common.model;

import java.io.Serializable;

import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.util.NamedObj;

public class Link implements Serializable {

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Link other = (Link) obj;
    
    if (_head == null && _tail == null) {
      if (other._head != null || other._tail != null){
        return false;
      }
    }else{
      if (_head == null || _tail == null) {
        if (_head == null){
          if (!_tail.equals(other._head) && !_tail.equals(other._tail)){
            return false;
          }
        }else{
          if (!_head.equals(other._head) && !_head.equals(other._tail)){
            return false;
          }
        }
      }else{
        if (!(_head.equals(other._head) && _tail.equals(other._tail)) && !(_tail.equals(other._head) && _head.equals(other._tail))){
          return false;
        }
      }
      return true;
    }
     
    return true;
  }

  public String getTitle() {
		if (_head instanceof NamedObj && _tail instanceof NamedObj) {
			return ((NamedObj) _head).getName() + "_"
					+ ((NamedObj) _tail).getName();
		}
		if (_relation == null)
			return "";
		return _relation.getName();
	}

	public Link() {
		super();
	}

	public Link(Object head, Object tail, ComponentRelation relation) {
		super();
		_head = head;
		_tail = tail;
		_relation = relation;
	}

	public Object getHead() {
		return _head;
	}

	public ComponentRelation getRelation() {
		return _relation;
	}

	public Object getTail() {
		return _tail;
	}

	public void setHead(Object head) {
		_head = head;
	}

	public void setRelation(ComponentRelation relation) {
		_relation = relation;
	}

	public void setTail(Object tail) {
		_tail = tail;
	}

	public String toString() {

		return "Link" + ((NamedObj) _head).getFullName() + "_"
				+ ((NamedObj) _tail).getFullName() + "_"
				+ ((NamedObj) _relation).getFullName();
	}

	private Object _head;

	private Object _tail;

	private ComponentRelation _relation;
}
