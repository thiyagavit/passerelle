package com.isencia.passerelle.process.model;

import java.util.Set;

/**
 * A Task is a kind of internal request, to execute one step in a process.
 * 
 * Contrary to a plain Request, which is just a plain container for storing the info
 * which triggered the process, Tasks (may) have associated results represented in blocks of key/value items.
 * 
 * @author delerw
 *
 */
public interface Task extends Request {

  /**
   * 
   * @return
   */
	Request getParentRequest();
	
	/**
	 * 
	 * @param block
	 * @return true if the block was added successfully
	 */
	boolean addResultBlock(ResultBlock block);
	
	/**
	 * @return the results that have been gathered by this task
	 */
	Set<ResultBlock> getResultBlocks();
}
