package housing;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.function.Consumer;

/***
 * A 2-dimensional priority queue: The items in the queue have two unrelated orderings:
 * X and Y. For a given p, we can extract the object with the Y-greatest entry that is
 * not X-greater than p.
 * 
 * Extraction has amortised complexity of O(sqrt(N))
 * Insertion has complexity O(log(N))
 *  
 *  Objects inserted into PriorityQueue2D must implement the interface
 *  PriorityQueue2D::Comparable
 *  
 * @author daniel
 *
 */
//public class PriorityQueue2D<E extends PriorityQueue2D.Comparable<E>> {
public class PriorityQueue2D<E> implements Iterable<E> {
	public interface Comparable<T> {
		/***
		 * @return (-1, 0, 1) if this is (less than, equal to, greater than) other
		 */
		public int XCompareTo(T other);
		public int YCompareTo(T other);
	}

	public interface XYComparator<T> {
		/***
		 * @return (-1, 0, 1) if arg0 is (less than, equal to, greater than) arg1
		 */
		public int XCompare(T arg0, T arg1);
		public int YCompare(T arg0, T arg1);
	}

	public PriorityQueue2D(XYComparator<E> iComparator) {
		comparator = iComparator;
		uncoveredElements = new TreeSet<E>(new Comparator<E>() {
			@Override
			public int compare(E arg0, E arg1) {
				return(comparator.XCompare(arg0,arg1));
			}

		});
		ySortedElements = new TreeSet<E>(new Comparator<E>() {
			@Override
			public int compare(E arg0, E arg1) {
				return(comparator.YCompare(arg0,arg1));
			}

		});
	}

	/***
	public PriorityQueue2D() {
		uncoveredElements = new TreeSet<E extends PriorityQueue2D.Comparable<E>>(new Comparator<E>() {
			@Override
			public int compare(E arg0, E arg1) {
				return(arg0.XCompareTo(arg1));
			}

		});
		ySortedElements = new TreeSet<E>(new Comparator<E>() {
			@Override
			public int compare(E arg0, E arg1) {
				return(arg0.YCompareTo(arg1));
			}

		});
	}
	***/
	
	public boolean add(E element) {
		ySortedElements.add(element);
		if(isUncovered(element)) {
			uncoveredElements.add(element);
			// remove any members of uncoveredElements that are covered by the new element
			E nextHigher = uncoveredElements.higher(element);
			while(nextHigher != null && comparator.YCompare(element, nextHigher) == 1) {
				uncoveredElements.remove(nextHigher);
				nextHigher = uncoveredElements.higher(element);
			}
		}
		return(true);
	}
	
	/***
	 * Finds and removes the object that is the Y-greatest entry that is not 
	 * X-greater than xGreatestBoundary.
	 * 
	 * @param xGreatestBoundary - object that defines the X value we can't go above
	 * @return the Y-greatest entry that is not X-greater than xGreatestBoundary.
	 */
	public E poll(E xGreatestBoundary) {
		E head = peek(xGreatestBoundary);
		if(head == null) return(null);
		removeUncovered(head);
		return(head);
	}

	/***
	 * Finds the object that is the Y-greatest entry that is not 
	 * X-greater than xGreatestBoundary, leaving the object in the collection.
	 * 
	 * @param xGreatestBoundary - object that defines the X value we can't go above
	 * @return the Y-greatest entry that is not X-greater than xGreatestBoundary.
	 */
	public E peek(E xGreatestBoundary) {
		return(uncoveredElements.floor(xGreatestBoundary));		
	}
	
	@SuppressWarnings("unchecked")
	public boolean remove(Object element) {
		if(uncoveredElements.contains(element)) {
			removeUncovered((E)element);
		} else {
			ySortedElements.remove(element);
		}
		return(true);
	}
	
	/***
	 * Removes element. Element must be an uncovered member of
	 * this set. Removing an uncovered element may uncover other elements,
	 * which then need to be added to the uncoveredElements container. This
	 * is done 
	 * 
	 * @param element Element to remove (must be an uncovered member of this set).
	 */
	protected void removeUncovered(E element) {
		uncoveredElements.remove(element);
		ySortedElements.remove(element);
		if(ySortedElements.size() == 0) return;
		boolean inclusive = false;
		E nextxLower = uncoveredElements.lower(element);
		if(nextxLower == null) { // removing the lowest uncovered element
			inclusive = true;
			nextxLower = ySortedElements.first();
			if(comparator.YCompare(element, nextxLower) == -1) { // y-least element doesn't cover anything
				return;
			}
		}
		E nextxHigher = uncoveredElements.higher(element);
		if(nextxHigher == null) { // removing the highest uncovered element (must be tail of ySortedElements)
			nextxHigher = ySortedElements.last();
			uncoveredElements.add(nextxHigher);
		}
		for(E e : ySortedElements.subSet(nextxLower, inclusive, element, true).descendingSet()) {
			if(comparator.XCompare(e, nextxHigher) == -1) {
				uncoveredElements.add(e);
				nextxHigher = e;
			}
		}
	}
	
	/***
	 * An element, a, is said to be "covered" by and element, b, iff
	 * b is Y-greater than a and b is X-less than a.
	 * 
	 * By construction, if a is covered by an element is must also
	 * be covered by an uncovered element.
	 * 
	 * @param element
	 * @return true if there doesn't exist an element in the
	 * queue that is both Y-greater and X-less than the given
	 * element.
	 */
	public boolean isUncovered(E element) {
		E nextLower = uncoveredElements.lower(element);
		if(nextLower == null) {
			return(true);
		}
		if(comparator.YCompare(nextLower, element) == -1) {
			return(true);
		}
		return(false);
	}
	
	public int size() {return(ySortedElements.size());}
	public int uncoveredSize() {return(uncoveredElements.size());}
	public boolean contains(Object element) {return(ySortedElements.contains(element));}
	public void clear() {
		uncoveredElements.clear();
		ySortedElements.clear();
	}

	@Override
	public Iterator<E> iterator() {
		return this.new Iter();
	}
	
	public class Iter implements Iterator<E> {
		public Iter() {
			it = PriorityQueue2D.this.ySortedElements.iterator();
		}
		
		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public E next() {
			last = it.next();
			return last;
		}

		@Override
		public void remove() {
			it.remove();
			PriorityQueue2D.this.uncoveredElements.remove(last);
		}

		@Override
		public void forEachRemaining(Consumer<? super E> action) {
			it.forEachRemaining(action);
		}
		
		Iterator<E> it;
		E last;
	}
	//////////////////////////////////////////////
	
	TreeSet<E> uncoveredElements; // x-sorted
	TreeSet<E> ySortedElements;
	XYComparator<E> comparator;
}
