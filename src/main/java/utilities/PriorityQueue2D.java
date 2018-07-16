package utilities;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

/**************************************************************************************************
 * Class that implements a 2-dimensional priority queue: The items in the queue have two unrelated
 * orderings, X and Y. For a given p, we can extract the object with the Y-greatest entry that is
 * not X-greater than p. The class basically consists of two TreeSets:
 *     - xySortedElements: TreeSet containing all elements added to the priority queue ordered in
 *     ascending X-dimension and, for equal X, in descending Y-dimension
 *     - uncoveredElements: TreeSet containing a subset of the elements at xySortedElements such
 *     that no other elements at xySortedElements are X-less while being also Y-greater or equal.
 *     These elements are, at the same time, X-sorted and Y-sorted.
 *
 * Objects inserted into PriorityQueue2D must implement the interface PriorityQueue2D.XYComparator
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class PriorityQueue2D<E> implements Iterable<E>, Serializable {

	//------------------//
	//----- Fields -----//
	//------------------//

	private TreeSet<E>       	xySortedElements; // X-sorted set of elements with reverse Y-sorting for equal X
	private TreeSet<E>          uncoveredElements; // X-sorted set of uncovered elements
	private XYComparator<E>     comparator;

	//------------------------//
	//----- Constructors -----//
	//------------------------//

	public PriorityQueue2D(XYComparator<E> comparator) {
		this.comparator = comparator;
		xySortedElements = new TreeSet<>(new XYComparatorClass());
		uncoveredElements = new TreeSet<>(new XComparatorClass());
	}

	//----------------------//
	//----- Subclasses -----//
	//----------------------//

	/**
	 * Interface for the XYComparator, to be implemented by the objects to be inserted in the PriorityQueue2D
	 */
	public interface XYComparator<T> extends Serializable {
		/**
		 * @return -1 or 1 if arg0 is, respectively, X-less than or X-greater than arg1 solving the arg0 == arg1 case by
		 * reverse comparing along the Y dimension and comparing their Id's if they also have the same Y-measure
		 */
		int XYCompare(T arg0, T arg1);
		/**
		 * @return -1, 0 or 1 if arg0 is, respectively, X-less than, X-equal to, or X-greater than arg1
		 */
		int XCompare(T arg0, T arg1);
        /**
         * @return -1, 0 or 1 if arg0 is, respectively, Y-less than, Y-equal to, or Y-greater than arg1
         */
        int YCompare(T arg0, T arg1);
	}

	/**
	 * Class to encapsulate the XYCompare method at XYComparator such that it can be passed as an argument to the
	 * TreeSet constructor
	 */
	public class XYComparatorClass implements Comparator<E> {
		public int compare(E arg0, E arg1) { return comparator.XYCompare(arg0, arg1); }
	}

    /**
     * Class to encapsulate the XCompare method at XYComparator such that it can be passed as an argument to the
     * TreeSet constructor
     */
    public class XComparatorClass implements Comparator<E> {
        public int compare(E arg0, E arg1) { return comparator.XCompare(arg0, arg1); }
    }

	/**
	 * Iterator through the XY-sorted elements of xySortedElements. This needs to be re-implemented here in order to
	 * override the remove method so as to remove the given element also from uncoveredElements set
	 */
	public class Iter implements Iterator<E> {
		// Fields
		Iterator<E> it;
		E last;
		// Constructors
		Iter() { it = xySortedElements.iterator(); }
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
			if (last != null) removeFromUncovered(last);
		}
	}

	//-------------------//
	//----- Methods -----//
	//-------------------//

	/**
	 * Adds the new element to the XY-sorted TreeSet, xySortedElements
	 *
	 * @param element Object to be added
	 */

	public void add(E element) {
		// Add element to the XY-sorted TreeSet
		xySortedElements.add(element);
	}

	/**
	 * Fill uncoveredElements TreeSet from the xySortedElements TreeSet
	 */
	public void sortPriorities() {
		E element;
		E lastElementAdded = null; // Initialising with null here just to avoid warning of possible non-initialisation
        // First, clear uncovered elements remaining from previous time steps
        uncoveredElements.clear();
		// Iterate over the elements at xySortedElements
		Iterator<E> iterator = new Iter();
		// By definition, the first element at xySortedElements, X-least element which is also Y-greatest for equal X,
		// is uncovered
		if (iterator.hasNext()) {
			element = iterator.next();
			uncoveredElements.add(element);
			lastElementAdded = element;
		}
		// Continue iterating through the rest of elements at xySortedElements...
		while (iterator.hasNext()) {
			element = iterator.next();
			// ...and adding them to the uncoveredElements set only if they are strictly Y-greater than the last element
			// added
			if (comparator.YCompare(element, lastElementAdded) == 1) {
				uncoveredElements.add(element);
				lastElementAdded = element;
			}
		}
	}

	/**
	 * Find the Y-greatest element that is not X-greater than xGreatestBoundary
	 *
	 * @param xGreatestBoundary Element that defines the X value we can't go above
	 */
	public E peek(E xGreatestBoundary) {
		return uncoveredElements.floor(xGreatestBoundary);
	}

	/**
	 * Removes element both from the xySortedElements and the uncoveredElements TreeSets
	 *
	 * @param element Element to remove
	 */
	public void remove(E element) {
		xySortedElements.remove(element);
		removeFromUncovered(element);
	}

	/**
	 * Removes element from the uncoveredElements TreeSet. Removing an uncovered element may uncover other elements,
	 * which then need to be added to the uncoveredElements container. Potentially new uncovered elements are those that
	 * lie in the xySortedElements TreeSet strictly between the element to be removed and the next uncovered element.
	 *
	 * @param element Element to remove (must be an uncovered element)
	 */
	private void removeFromUncovered(E element) {
		// If element is not uncovered, do nothing, otherwise, remove element from uncoveredElements and continue
		if (!uncoveredElements.remove(element)) return;
		// If it was the last element within the PriorityQueue2D, do nothing, otherwise, continue
		if(xySortedElements.size() == 0) return;
		// Find the next uncovered element, i.e., the least uncovered element strictly greater than the removed element
		E nextHigher = uncoveredElements.higher(element);
		// Find the previous uncovered element, i.e., the greatest uncovered element strictly less than the removed
		// element and store it as initial lastElementAdded
		E lastElementAdded = uncoveredElements.lower(element);
		// If there is no previous uncovered element (nextLower is null), then add the new first element at
		// xySortedElements as uncovered, as the X-least element which is also Y-greatest for equal X is always
		// uncovered, and store it as initial lastElementAdded
		if (lastElementAdded == null) {
			lastElementAdded = xySortedElements.first();
			uncoveredElements.add(lastElementAdded);
		}
		// If there is no next uncovered element (nextHigher is null)...
		if (nextHigher == null) {
			// ...then loop through all the elements of the xySortedElements set which are greater than the removed
			// element...
			for (E e: xySortedElements.tailSet(element, false)) {
				// ...adding them to the uncoveredElements set only if they are strictly Y-greater than the last element
				// added
				if (comparator.YCompare(e, lastElementAdded) == 1) {
					uncoveredElements.add(e);
					lastElementAdded = e;
				}
			}
			// Otherwise...
		} else {
			// ...loop through the elements of the xySortedElements set which are greater than the removed element and
			// less than the next higher uncovered element...
			for (E e: xySortedElements.subSet(element, false, nextHigher, false)) {
				// ...adding them to the uncoveredElements set only if they are strictly Y-greater than the last element
				// added
				if (comparator.YCompare(e, lastElementAdded) == 1) {
					uncoveredElements.add(e);
					lastElementAdded = e;
				}
			}
		}
	}

	public int size() { return xySortedElements.size(); }

	public void clear() {
		uncoveredElements.clear();
		xySortedElements.clear();
	}

	@Override
	public Iter iterator() { return this.new Iter(); }
}
