/**
 * 
 */
package edu.usc.iiw.crawler;

import edu.usc.iiw.storage.DBEnvWrapper;
import edu.usc.iiw.storage.DBWrapper;

/**
 * Disk based Blocking Queue to store all the crawled URLs
 * 
 * @author 
 *
 */
public class BlockingDiskQueue {

	Integer enqueuePointer;
	Integer dequeuePointer;
	private boolean closed = false;
	public DBWrapper urlDiskQueue;

	/**
	 * Initiliaze the urlQueue Database connection object
	 * @param dbew
	 */
	public BlockingDiskQueue(DBEnvWrapper dbew) {
		this.urlDiskQueue = dbew.getUrlQueue();
		this.enqueuePointer = 0;
		this.dequeuePointer = 0;		
	}

	/**
	 * Insert the url string to the BerkleyDB with the enqueuePointer as key
	 * @param url
	 */
	synchronized public void enqueue(String url) {
		/*if (closed) {
			throw new ClosedException();
		}*/
		try {
			urlDiskQueue.insert(enqueuePointer, url);
			enqueuePointer += 1;
			// notify();
		} catch (Exception e) {
			System.out.println("Error adding "+url+ " to UrlQueue on disk");
			e.printStackTrace();
		}
	}
	
	synchronized boolean isEmpty() {
		return dequeuePointer == enqueuePointer;
	}

	/**
	 * Remove the url from the BerkleyDB and update the dequeue Pointer
	 * @return url which was dequeued
	 * @throws Exception 
	 */
	synchronized public String dequeue() throws Exception {
		/*if (!closed && dequeuePointer == enqueuePointer) {
			try {
				wait();
				// kill the thread if queue is empty and current thread is
				// waiting on it while 
				if (closed == true) {
					return null;
				}
			} catch (InterruptedException e) {
				// ignore
			}
			return null;
		}*/
		
		/*if (list.size() == 0) {
			return null;
		}*/
		if(dequeuePointer == enqueuePointer)
			return null;
		else {
			// delete from BerkleyDB and return the deleted url
			String url = null;
			url = (String) urlDiskQueue.get(dequeuePointer);
			urlDiskQueue.delete(dequeuePointer);
			dequeuePointer += 1;
			return url;
		}
	}

	synchronized public int size() {
		return enqueuePointer - dequeuePointer;
	}

	/**
	 * Close the blocking queue once the server has been shutdown and kill all
	 * waiting threads by notifying them.
	 */
	synchronized public void close() {
		closed = true;
		notifyAll();
	}

	synchronized public void open() {
		closed = false;
	}

	public static class ClosedException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		ClosedException() {
			super("Queue closed.");
		}
	}
}
