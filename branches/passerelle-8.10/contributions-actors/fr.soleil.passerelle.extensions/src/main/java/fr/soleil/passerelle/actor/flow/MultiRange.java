package fr.soleil.passerelle.actor.flow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MultiRange {
	private List<Range> rangesList;
	private Range currentRange;
	private Iterator<Range> rangeIter;
	private double currentValue = 0;
	public MultiRange(){	
		rangesList = new ArrayList<Range>();
		
	}
	public void clear(){
		rangesList.clear();
	}
	public List<Range> getRangesList() {
		return rangesList;
	}

	public void setRangesList(List<Range> rangesList) {
		this.rangesList = rangesList;
	}
	
	public void add(Range range){
		rangesList.add(range);
	}
	public void initialize(){
		for (Iterator<Range> iter = rangesList.iterator(); iter.hasNext();) {
			Range element = iter.next();
			element.initialize();
		}
		rangeIter =  rangesList.iterator();
		// iterator is positioned to first range
		currentRange = this.next();
	}
	
	public boolean hasNext(){
		return rangeIter.hasNext();
	}
	public boolean hasNextValue(){
		return currentRange.hasNext() || this.hasNext();
	}
	public Range next(){
		currentRange = (Range)rangeIter.next();
		return currentRange;
	}
	
	public double nextValue(){	
		if(currentRange.hasNext()){
			currentValue = currentRange.next();
		}else{
			//get next value of next range
			this.next();
			if(currentRange.hasNext())
				currentValue = currentRange.next();
		}
		return currentValue;
	}
	
	public String toString()
	{	
		StringBuffer buffer = new StringBuffer("");
		for (Iterator<Range> iter = rangesList.iterator(); iter.hasNext();) {
			Range element = iter.next();
			buffer.append("Range " +element + "\r\n");
		}
		return buffer.toString(); 
	}

	public Range getCurrentRange() {
		return currentRange;
	}
}
