package sudoku;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Place for your code.
 */
public class RobustSudokuSolver {

	/**
	 * @return names of the authors and their student IDs (1 per line).
	 */
	public String authors() {
		return "Alan Kin Shing Tsin #10537124";
	}

	/**
	 * Performs constraint satisfaction on the given Sudoku board using Arc Consistency and Domain Splitting.
	 * 
	 * @param board the 2d int array representing the Sudoku board. Zeros indicate unfilled cells.
	 * @return the solved Sudoku board
	 */
	public int[][] solve(int[][] board) throws Exception {
		
		int[] solution                = new int[81];
		ArrayList<Integer>[] domain   = new ArrayList[81];
		int temp = 0 ;
		
		// Convert to 1D array for easier processing
		for(int i = 0; i < 9; i++) {
			
			for(int j = 0; j < 9; j++ ) {
				solution[temp++] = board[i][j];
			}
			
		}
		
		// Initialize ArrayList
		for(int i = 0; i < 81; i++) {
			domain[i] = new ArrayList<Integer>();
		}
		
		// Initialize domains. If cell is 0, then domain is { 1,2,3,4,5,6,7,8,9 } else domain is the initial value
		for(int i = 0; i < solution.length; i++ ) {
			
			if(solution[i] == 0 ) {
				
				for( int j = 1; j <= 9; j++ )
					domain[i].add(j);
				
			}

			else {
				domain[i].add(solution[i]);
			}
			
		}
		
		// Call arcConsistency to solve the board
		arcConsistency(solution, domain);
		
		// Check for invalid board
		for(int i = 0; i < 81 ; i++) {
			
			// If any domain is empty, then the board is invalid
			if(domain[i].isEmpty()) {
				throw new Exception("This board is invalid.");
			}
			
		}
		
		
		for(int i = 0; i < 81 ; i++) {
			
			if((solution[i] == 0) && (domain[i].size() == 1)) {
				solution[i] = domain[i].get(0);
			}
			
		}
		
		// If board is solved with arc consistency alone, return the solved board
		if(isBoardSolved(solution)) {
			// Convert back to 2D array
			temp = 0 ;
			for(int x = 0; x < 9; x++) {
				for(int y = 0; y < 9; y++ ) {
					board[x][y] = solution[temp++];
				}
			}
			return board;
		}
		
		// Otherwise do arc consistency with domain splitting
		solution  = arcConsistencyWithDomainSplitting(solution, domain);
		
		// If solution was found, return the solved board
		if(solution != null) {
			temp = 0 ;
			
			for(int x = 0; x < 9; x++) {
				for(int y = 0; y < 9; y++ ) {
					board[x][y] = solution[temp++];
				}
			}
		}
		
		// Otherwise there is no solution
		else {
		  throw new Exception("This board has no solution.");
		}
		
		return board;
	}
	
	private int[] arcConsistencyWithDomainSplitting(int[] solution, ArrayList<Integer>[] domain) throws Exception {
		
		int splitThis                 	 = 0;
		int splitHere                    = 0;
		ArrayList<Integer>[] leftDomain  = new ArrayList[81]; // First half of split domain
		ArrayList<Integer>[] rightDomain = new ArrayList[81]; // Other half of split doamin
		boolean leftOK                   = true; // Checks for domain length 0, if so then becomes false
		boolean rightOK                  = true; // Checks for domain length 0, if so then becomes false
		boolean assigned                 = false;
		boolean equal                    = true;
		int temp             		     = 0;
		int[] solutionLeft               = new int[81]; // Solution board generated from left domain split
		int[] solutionRight              = new int[81]; // Solution board generated from right domain split
		
		// Initialize arrays and lists
		for(int i = 0; i < 81; i++) {
			solutionLeft[i]  = solution[i];
			solutionRight[i] = solution[i];
			leftDomain[i]  	 = new ArrayList<Integer>();
			rightDomain[i] 	 = new ArrayList<Integer>();
		}
		
		for(int i = 0; i < 81; i++) {
			copyArrayList(leftDomain[i],  domain[i]) ;
			copyArrayList(rightDomain[i], domain[i]) ;
		}
		
		// Pick domain to split
		for(int i = 0; i < 81; i++) {
			
			// If domain has more than 1 number
			if(domain[i].size() > 1) {
				splitThis = i;
				assigned = true;
			}
			
			// If domain is empty return null
			else if(domain[i].size() == 0) 
				return null ;
		}
		
		// If no domains were chosen to split, then all of them are equal to 1
		// If so, then call arcConsistency to check for correctness and return the board if it is correct
		if(!assigned) {
			arcConsistency(solution, domain) ;
			
			for(int i = 0; i < 81 ; i++) {
				if(domain[i].size() == 0) {
					return null ;
				}
			}
			
			for(int i = 0; i < 81 ; i++) {
				if((solution[i] == 0) && (domain[i].size() == 1)) {
					solution[i] = domain[i].get(0);
				}
			}
			
			return solution;
		}
		
		// Split domain and get the middle
		splitHere = domain[splitThis].size() / 2;
		temp      = splitHere;
		
		// leftDomain has first half
		while (leftDomain[splitThis].size() > splitHere) {
			leftDomain[splitThis].remove(leftDomain[splitThis].size() - 1);
		}

		// rightDomain has second half
		while (temp > 0) {
			rightDomain[splitThis].remove(0);
			temp--;
		}

		// Call arcConsistency on left domain
		arcConsistency(solutionLeft, leftDomain);

		// Check if any empty domains of length 0
		for(int i = 0; i < 81 ; i++) {
			if(leftDomain[i].isEmpty()) {
				leftOK = false;
			}
		}
		
		// Fill solutionLeft with new answers
		for(int i = 0; i < 81 ; i++) {
			
			if((solutionLeft[i] == 0) && (leftDomain[i].size() == 1)) {
				solutionLeft[i] = leftDomain[i].get(0);
			}
			
		}
		
		// Call arcConsistency on right domain
		arcConsistency(solutionRight, rightDomain);

		// check if any domain is length 0 -- i.e. any domain that is empty
		for( int i = 0; i < 81 ; i++) {
			if(rightDomain[i].isEmpty()) {
				rightOK = false;
			}
		}
		
		// Fill solutionRight with new answers
		for(int i = 0; i < 81 ; i++) {
			
			if((solutionRight[i] == 0) && (rightDomain[i].size() == 1)) {
				solutionRight[i] = rightDomain[i].get(0);
			}
			
		}
		
		// Recursion on left and right domains until domain is empty
		if(leftOK) {
			solutionLeft = arcConsistencyWithDomainSplitting( solutionLeft, leftDomain );
		}
		
		else {
			solutionLeft = null;
		}
		
		if(rightOK) {
			solutionRight = arcConsistencyWithDomainSplitting( solutionRight, rightDomain );
		}
		
		else {
			solutionRight = null;
		}
		
		// Compares left and right solution, if they are not equal then more than one solution exists
		if (isBoardSolved(solutionLeft)  &&  isBoardSolved(solutionRight)) {
			for(int i = 0; i < solutionLeft.length ; i++) {
				
				if( solutionLeft[i] != solutionRight[i]) {
					equal = false; 
				}
				
			}
			
			if(!equal) {
				throw new Exception("This board has more than one solution.");
			}
			
		}
		
		// Return solutions
		if( isBoardSolved(solutionLeft)) {
			return solutionLeft;
		}
		
		if(isBoardSolved(solutionRight)) {
			return solutionRight;
		}

		// If no solutions found, return null
		return null;
		
	}
	
	private void arcConsistency(int[] solution, ArrayList<Integer>[] domain) {
		LinkedList<Arc> toDoArcs = new LinkedList<Arc>();
		ArrayList<Integer> consistent;
		int curr_cell, other_cell;
		Iterator<Integer> itr1;
		Iterator<Integer> itr2;
		Integer tempOther;
		Arc tempArc ;
		Integer temp;

		// Fill to do list
		for (int i = 0; i < 81; i ++) {
			fillToDoList(toDoArcs, i) ;
		}

		// Loop while to do list is not empty
		while(toDoArcs.size() > 0) {
			
			tempArc    = toDoArcs.remove();
			curr_cell  = tempArc.currentCell;
			other_cell = tempArc.otherCell;
			consistent = new ArrayList<Integer>();
			itr1 = domain[curr_cell].iterator();

			// Loop through all the current cell's values
			while(itr1.hasNext()) {				
				temp = itr1.next();
				itr2 = domain[ other_cell ].iterator();
				
				// If empty, add the current cell's value to consistent
				if (domain[other_cell].isEmpty()) {
					consistent.add(temp);
				}

				// While there is still value for other cell
				while(true) {
					if(itr2.hasNext()) {
						tempOther = itr2.next();
						
						// If constraint achieved, add to consistent and break
						if( !temp.equals(tempOther)) {
							
							consistent.add(temp);
							break;
							
						}
						
					}
					
					else {
						break;
					}
					
				}
			}
			
			// If something was removed from the domain, then re-add all dependents to current cell and set new domain
			if(consistent.size() != domain[curr_cell].size()) {
				
				reAddToDoList(toDoArcs, curr_cell);
				copyArrayList(domain[curr_cell], consistent);
				
			}
			
		}
		
	}
	
	// Initial filling of the to do list with all arcs that need to be checked
	private void fillToDoList(LinkedList<Arc> toDoArcs, int i) {
		
		// Get row and column of position i
		int row = getRow(i);
		int column = getColumn(i);
		
		for (int j = 0; j < 9 ; j++) {
			
			// Add all other cells in the same row as i
			if (j != row) {
				toDoArcs.add(new Arc(i, getPosition(column, j)));
			}
			
			// Add all other cells in the same column as i
			if ( j != column ) {
				toDoArcs.add(new Arc(i, getPosition(j, row)));
			}
			
		}
		
		int column_block = column / 3;
		int row_block = row / 3;
		
		for (int m = column_block * 3 ; m < column_block * 3 + 3 ; ++m) {
			for (int n = row_block * 3 ; n < row_block * 3 + 3 ; ++n) {
				
				// If not the same coordinate, add to TDA
				if (m != column && n != row) {
					toDoArcs.add(new Arc(i, getPosition(m, n)));
				}
				
			}
		}		
	}
	
	// Refill the to do list with arcs that were affected by i
	private void reAddToDoList(LinkedList<Arc> toDoArcs, int i) {
		
		// Get row and column of position i
		int row = getRow(i) ;
		int column = getColumn(i) ;
		
		for (int y = 0; y < 9 ; y++) {
			
			// Add all other cells in the same row as i
			if (y != row) {
				toDoArcs.add( new Arc( getPosition( column, y ), i));
			}
			
			// Add all other cells in the same column as i
			if (y != column) {
				toDoArcs.add( new Arc( getPosition( y, row ),i));
			}
			
		}

		int column_block = column / 3;
		int row_block = row / 3;
		
		for (int m = column_block * 3 ; m < column_block * 3 + 3 ; ++m) {
			for (int n = row_block * 3 ; n < row_block * 3 + 3 ; ++n) {
				
				// If not the same coordinate, add to TDA
				if (m != column && n != row) {
					toDoArcs.add(new Arc(getPosition(m, n), i));
				}
				
			}
		}
	}
	
	private int getRow(int x) {
		return (int) Math.floor(x / 9);
	}
	
	private int getColumn(int x) {
		return x % 9;
	} 
	
	private int getPosition(int x, int y) {

		int pos = 0;
		
		for(int t = 0; t < 9; t++) {
			for(int r = 0; r < 9; r ++) {
				
				if (y == t && x == r) {
					return pos;
				}
				
				else {
					pos++;
				}
				
			}
		}
		
		return pos;
	}
	
	private void copyArrayList(ArrayList<Integer> oldDomain, ArrayList<Integer> newDomain) {
		Iterator<Integer> itr = newDomain.iterator();
		Integer x             = 0;
		
		oldDomain.clear();
		
		while(itr.hasNext()) {
			x = itr.next();
			oldDomain.add(x);			
		}		
	}
	
	// Checks the board for 0's. If no 0's found then the board is solved
	private boolean isBoardSolved(int[] solution) {
		
		if(solution == null) {
			return false;
		}
		
		for(int x = 0; x < solution.length; x++) {
			
			if( solution[ x ] == 0 ) {
				return false;
			}
			
		}
		
		return true;
	}
	
	// Class that defines an Arc
	public class Arc {
		
		int currentCell ;
		int otherCell ;
		
		public Arc(int a, int b) {
			currentCell = a;
			otherCell = b;
		}
	}
	
}
