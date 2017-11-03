package utilities;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

/**************************************************************************************************
 * Class that implements a 2-dimensional priority queue: The items in the queue have two unrelated
 * orderings, X and Y. For a given p, we can extract the object with the Y-greatest entry that is
 * not X-greater than p
 *
 * Extraction has amortised complexity of O(sqrt(N))
 * Insertion has complexity O(log(N))
 *
 * Objects inserted into PriorityQueue2D must implement the interface PriorityQueue2D.XYComparator
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class PriorityQueue2D<E> implements Iterable<E>, Serializable {
	private static final long serialVersionUID = -2371013046862291303L;

    //------------------//
    //----- Fields -----//
    //------------------//

    private TreeSet<E>          uncoveredElements; // x-sorted (price). Elements such that one cannot find cheaper for the same or higher quality/yield
    private TreeSet<E>          ySortedElements; // y-sorted (quality normal buyers, yield for BTL investors)
    private XYComparator<E>     comparator;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    public PriorityQueue2D(XYComparator<E> iComparator) {
        comparator = iComparator;
        uncoveredElements = new TreeSet<>(this.new XComparator());
        ySortedElements = new TreeSet<>(this.new YComparator());
    }

    //----------------------//
    //----- Subclasses -----//
    //----------------------//

    /**
     * Interface for the XYComparator, to be implemented by the objects to be inserted in the PriorityQueue2D
     */
	public interface XYComparator<T> extends Serializable {
		/**
		 * @return -1 or 1 if arg0 is, respectively, less than or greater than arg1 (arg0 == arg1 is solved by comparing
         *         object Id's)
		 */
        int XCompare(T arg0, T arg1);
		int YCompare(T arg0, T arg1);
	}

	public class XComparator implements Comparator<E>, Serializable {
		private static final long serialVersionUID = -264394909715934581L;
		public int compare(E arg0, E arg1) { return comparator.XCompare(arg0, arg1); }
	}

	public class YComparator implements Comparator<E>, Serializable {
		private static final long serialVersionUID = 175936399605372278L;
		public int compare(E arg0, E arg1) { return comparator.YCompare(arg0, arg1); }
	}

    /**
     * Iterator through the y-sorted elements of the PriorityQueue2D (i.e., through the elements of ySortedElements)
     */
    public class Iter implements Iterator<E> {
        // Fields
        Iterator<E> it;
        public E last;
        // Constructors
        Iter() { it = PriorityQueue2D.this.ySortedElements.iterator(); }
        // Methods
        @Override
        public boolean hasNext() { return it.hasNext(); }
        @Override
        public E next() {
            last = it.next();
            return last;
        }
        @Override
        public void remove() {
            it.remove();
            if(last != null) PriorityQueue2D.this.removeFromUncovered(last);
        }
    }

    //-------------------//
    //----- Methods -----//
    //-------------------//
	
	public boolean add(E element) {
		ySortedElements.add(element);
		if(isUncovered(element)) {
			uncoveredElements.add(element);
			// Remove any members of uncoveredElements that are covered by the new element
			E nextHigher = uncoveredElements.higher(element);
			while(nextHigher != null && comparator.YCompare(element, nextHigher) == 1) {
				uncoveredElements.remove(nextHigher);
				nextHigher = uncoveredElements.higher(element);
			}
		}
		return true;
	}
	
	/**
	 * Finds and removes the object that is the Y-greatest entry that is not X-greater than xGreatestBoundary.
	 * 
	 * @param xGreatestBoundary - object that defines the X value we can't go above
	 * @return the Y-greatest entry that is not X-greater than xGreatestBoundary.
	 */
	public E poll(E xGreatestBoundary) {
		E head = peek(xGreatestBoundary);
		if(head == null) return(null);
		ySortedElements.remove(head);
		removeFromUncovered(head);
		return(head);
	}

	/**
	 * Finds the object that is the Y-greatest entry that is not  X-greater than xGreatestBoundary, leaving the object
     * in the collection.
	 * 
	 * @param xGreatestBoundary - object that defines the X value we can't go above
	 * @return the Y-greatest entry that is not X-greater than xGreatestBoundary.
	 */
	public E peek(E xGreatestBoundary) {
		return(uncoveredElements.floor(xGreatestBoundary));
	}
	
	@SuppressWarnings("unchecked")
	public boolean remove(E element) {
		ySortedElements.remove(element);
		removeFromUncovered(element);
		return(true);
	}
	
	/**
	 * Removes element from the set of uncovered elements. Removing an uncovered element may uncover other elements,
	 * which then need to be added to the uncoveredElements container. Potentially uncovered elements are the ones that
     * were covered by the removed element but not covered by the element's x-neighbours in the set of uncovered
     * elements
	 * 
	 * @param element Element to remove (must be an uncovered member of this set)
	 */
	private void removeFromUncovered(E element) {
		if(!uncoveredElements.remove(element)) return;
		if(ySortedElements.size() == 0) return;
		boolean inclusive = false;
		E nextxLower = uncoveredElements.lower(element);
		if(nextxLower == null) { // we're removing the x-lowest uncovered element
			inclusive = true;
			nextxLower = ySortedElements.first();
			if(comparator.YCompare(element, nextxLower) == -1) { // element was the y-least element, which doesn't cover anything
				return;
			}
		}
		E nextxHigher = uncoveredElements.higher(element);
		if(nextxHigher == null) { // removing the highest uncovered element (must be the last element of ySortedElements)
			nextxHigher = ySortedElements.last();
			uncoveredElements.add(nextxHigher);
		}
		if(comparator.YCompare(nextxLower, element) == 1) {
			System.out.println("From = " + nextxLower + " to = " + element + " compare = "
                    + comparator.YCompare(nextxLower, element));
		}
		for(E e : ySortedElements.subSet(nextxLower, inclusive, element, true).descendingSet()) {
			if(comparator.XCompare(e, nextxHigher) == -1) {
				uncoveredElements.add(e);
				nextxHigher = e;
			}
		}
	}
	
	/**
	 * An element, a, is said to be "covered" by and element, b, if and only if b is Y-greater than a and b is X-less
     * than a
	 *
	 * By construction, if a is covered by an element it must also be covered by an uncovered element
	 * 
	 * @param element element to check if it is covered by existing uncovered elements
	 * @return true if there doesn't exist an element in the queue that is both Y-greater and X-less than the given
     *         element
	 */
	private boolean isUncovered(E element) {
        E nextLower = uncoveredElements.lower(element);
        return nextLower == null || comparator.YCompare(nextLower, element) == -1;
    }
	
	public int size() { return ySortedElements.size(); }
	public int uncoveredSize() { return uncoveredElements.size(); }
	public boolean contains(E element) { return ySortedElements.contains(element); }
	public void clear() {
		uncoveredElements.clear();
		ySortedElements.clear();
	}

	@Override
	public Iter iterator() { return this.new Iter(); }
}
