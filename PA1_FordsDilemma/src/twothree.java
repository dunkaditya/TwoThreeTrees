import java.io.*;
import java.util.*;


class Node {
   String guide;
   // guide points to max key in subtree rooted at node
}

class InternalNode extends Node {
   Node child0, child1, child2;
   // child0 and child1 are always non-null
   // child2 is null iff node has only 2 children
}

class LeafNode extends Node {
   // guide points to the key

   int value;
}

class TwoThreeTree {
   Node root;
   int height;

   TwoThreeTree() {
      root = null;
      height = -1;
   }
}

class WorkSpace {
// this class is used to hold return values for the recursive doInsert
// routine (see below)

   Node newNode;
   int offset;
   boolean guideChanged;
   Node[] scratch;
}

public class twothree {

   public static void main(String[] args) throws IOException {
	   
	   TwoThreeTree planets = new TwoThreeTree();
	   Scanner sc = new Scanner(new File("test0.in"));
	   BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out, "ASCII"), 4096);
	   
	   int num = Integer.parseInt(sc.nextLine());
	   for(int i = 0; i < num; i++) {
		   
		   String[] s = (sc.nextLine()).split(" ");
		   insert(s[0], Integer.parseInt(s[1]), planets);
	   }
	   
	   int num2 = Integer.parseInt(sc.nextLine());
	   for(int i = 0; i < num2; i++) {
		   
		   String[] s = (sc.nextLine()).split(" ");
		   //make sure range is right
		   String left = s[1];
		   String right = s[0];
		   if(s[0].compareTo(s[1]) <= 0) {
			   
			   left = s[0];
			   right = s[1];
		   }
		   
		   printRange(planets.root, left, right, planets.height, "0", out);
	   }	   
   }
   
   static void recursivePrint(Node p, int height){

	    if (height == 0){
	    	
			LeafNode p1 = (LeafNode)p;
	    	System.out.println(p1.guide + " " + p1.value);
	    	return;

	    } 
	    InternalNode p1 = (InternalNode)p;
        recursivePrint(p1.child0, height-1);
        /* Here print node's left value */
        recursivePrint(p1.child1, height-1);
        if (p1.child2 != null)
        	recursivePrint(p1.child2, height-1);

	}
   
   static void printRange(Node p, String left, String right, int height, String lo, BufferedWriter br) throws IOException {

	   //if we are at a leaf node
	   if(height == 0) {
		   //check if it's in the range
		   if(p.guide.compareTo(left) >= 0 && p.guide.compareTo(right) <= 0) {
			   
			   LeafNode p1 = (LeafNode)p;
			   String content = p.guide + " " + Integer.toString(p1.value);
//			   System.out.println((content));
			   br.write(content);
		   }
		   return;
	   }
	   
	   InternalNode p1 = (InternalNode)p;
	   String hi = p1.guide;
	   if(right.compareTo(lo) < 0)
		   return;
	   if(left.compareTo(hi) > 0)
		   return;
	   printRange(p1.child0, left, right, height-1, lo, br);
	   printRange(p1.child1, left, right, height-1, p1.child0.guide, br);
	   if(p1.child2 != null)
		   printRange(p1.child2, left, right, height -1, p1.child1.guide, br);
	   
   }
   
   static void insert(String key, int value, TwoThreeTree tree) {
   // insert a key value pair into tree (overwrite existsing value
   // if key is already present)

      int h = tree.height;

      if (h == -1) {
          LeafNode newLeaf = new LeafNode();
          newLeaf.guide = key;
          newLeaf.value = value;
          tree.root = newLeaf; 
          tree.height = 0;
      }
      else {
         WorkSpace ws = doInsert(key, value, tree.root, h);

         if (ws != null && ws.newNode != null) {
         // create a new root

            InternalNode newRoot = new InternalNode();
            if (ws.offset == 0) {
               newRoot.child0 = ws.newNode; 
               newRoot.child1 = tree.root;
            }
            else {
               newRoot.child0 = tree.root; 
               newRoot.child1 = ws.newNode;
            }
            resetGuide(newRoot);
            tree.root = newRoot;
            tree.height = h+1;
         }
      }
   }

   static WorkSpace doInsert(String key, int value, Node p, int h) {
   // auxiliary recursive routine for insert

      if (h == 0) {
         // we're at the leaf level, so compare and 
         // either update value or insert new leaf

         LeafNode leaf = (LeafNode) p; //downcast
         int cmp = key.compareTo(leaf.guide);

         if (cmp == 0) {
            leaf.value = value; 
            return null;
         }

         // create new leaf node and insert into tree
         LeafNode newLeaf = new LeafNode();
         newLeaf.guide = key; 
         newLeaf.value = value;

         int offset = (cmp < 0) ? 0 : 1;
         // offset == 0 => newLeaf inserted as left sibling
         // offset == 1 => newLeaf inserted as right sibling

         WorkSpace ws = new WorkSpace();
         ws.newNode = newLeaf;
         ws.offset = offset;
         ws.scratch = new Node[4];

         return ws;
      }
      else {
         InternalNode q = (InternalNode) p; // downcast
         int pos;
         WorkSpace ws;

         if (key.compareTo(q.child0.guide) <= 0) {
            pos = 0; 
            ws = doInsert(key, value, q.child0, h-1);
         }
         else if (key.compareTo(q.child1.guide) <= 0 || q.child2 == null) {
            pos = 1;
            ws = doInsert(key, value, q.child1, h-1);
         }
         else {
            pos = 2; 
            ws = doInsert(key, value, q.child2, h-1);
         }

         if (ws != null) {
            if (ws.newNode != null) {
               // make ws.newNode child # pos + ws.offset of q

               int sz = copyOutChildren(q, ws.scratch);
               insertNode(ws.scratch, ws.newNode, sz, pos + ws.offset);
               if (sz == 2) {
                  ws.newNode = null;
                  ws.guideChanged = resetChildren(q, ws.scratch, 0, 3);
               }
               else {
                  ws.newNode = new InternalNode();
                  ws.offset = 1;
                  resetChildren(q, ws.scratch, 0, 2);
                  resetChildren((InternalNode) ws.newNode, ws.scratch, 2, 2);
               }
            }
            else if (ws.guideChanged) {
               ws.guideChanged = resetGuide(q);
            }
         }

         return ws;
      }
   }


   static int copyOutChildren(InternalNode q, Node[] x) {
   // copy children of q into x, and return # of children

      int sz = 2;
      x[0] = q.child0; x[1] = q.child1;
      if (q.child2 != null) {
         x[2] = q.child2; 
         sz = 3;
      }
      return sz;
   }

   static void insertNode(Node[] x, Node p, int sz, int pos) {
   // insert p in x[0..sz) at position pos,
   // moving existing entries to the right

      for (int i = sz; i > pos; i--)
         x[i] = x[i-1];

      x[pos] = p;
   }

   static boolean resetGuide(InternalNode q) {
   // reset q.guide, and return true if it changes.

      String oldGuide = q.guide;
      if (q.child2 != null)
         q.guide = q.child2.guide;
      else
         q.guide = q.child1.guide;

      return q.guide != oldGuide;
   }


   static boolean resetChildren(InternalNode q, Node[] x, int pos, int sz) {
   // reset q's children to x[pos..pos+sz), where sz is 2 or 3.
   // also resets guide, and returns the result of that

      q.child0 = x[pos]; 
      q.child1 = x[pos+1];

      if (sz == 3) 
         q.child2 = x[pos+2];
      else
         q.child2 = null;

      return resetGuide(q);
   }
}

