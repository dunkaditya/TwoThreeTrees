import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.util.regex.*;


class Node {
   String guide;
   int value;
   // guide points to max key in subtree rooted at node
}

class InternalNode extends Node {
   Node child0, child1, child2;
   // child0 and child1 are always non-null
   // child2 is null iff node has only 2 children
}

class LeafNode extends Node {
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

public class augtwothree {

   public static void main(String[] args) throws IOException {
       
       TwoThreeTree planets = new TwoThreeTree();
       Scanner sc = new Scanner(new File("test3.in"));
       BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out, "ASCII"), 4096);
       
       int num = Integer.parseInt(sc.nextLine());
       for(int i = 0; i < num; i++) {
           
           String[] s = (sc.nextLine()).split(" ");
           if(s[0].equals("1")) {
               insert(s[1], Integer.parseInt(s[2]), planets);
           }
           if(s[0].equals("2")) {
               //make sure range is right
               String left = s[2];
               String right = s[1];
               if(s[1].compareTo(s[2]) <= 0) {
                   
                   left = s[1];
                   right = s[2];
               }
               addRange(planets.root, left, right, planets.height, "", Integer.parseInt(s[3]));
           }
           if(s[0].equals("3")) {
               out.write(Integer.toString(lookup(planets.root, s[1], planets.height, 0)));
               out.newLine();
           }
       }
       out.flush();
   }
   
   static void addRange(Node p, String left, String right, int height, String lo, int add) {
       
       if(height == 0) {
           
           if(p.guide.compareTo(left) >= 0 && p.guide.compareTo(right) <= 0) {
               
               p.value += add;
           }
           return;
       }
       else {
    	   InternalNode q = (InternalNode)p;
           String hi = q.guide;
           if(right.compareTo(lo) <= 0) {
               return;
           }
           if(left.compareTo(hi) > 0) {
               return;
           }
           if(left.compareTo(lo) <= 0 && right.compareTo(hi) >= 0) {
               q.value += add;
               return;
           }
           addRange(q.child0, left, right, height-1, lo, add);
           addRange(q.child1, left, right, height-1, q.child0.guide, add);
           if(q.child2 != null) {
               addRange(q.child2, left, right, height-1, q.child1.guide, add);
           }  
       }     
   }
   
   static int lookup(Node p, String key, int height, int effVal) {
       
       if(height == 0) {
           if(p.guide.equals(key)) {
               return effVal + p.value;
           }
           else
               return -1;
       }
       else {
           InternalNode q = (InternalNode)p;
           if (key.compareTo(q.child0.guide) <= 0) {
               return lookup(q.child0, key, height-1, effVal + q.value);
           }
           else if (key.compareTo(q.child1.guide) <= 0 || q.child2 == null) {
               return lookup(q.child1, key, height-1, effVal + q.value);
           }
           else {
               return lookup(q.child2, key, height-1, effVal + q.value);
           }
       }
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
         
         if(q.value != 0) {
             q.child0.value += q.value;
             q.child1.value += q.value;
             if(q.child2 != null)
                 q.child2.value += q.value;
             q.value = 0;
         }

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