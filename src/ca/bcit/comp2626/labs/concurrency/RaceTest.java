package ca.bcit.comp2626.labs.concurrency;

interface DIterator<T> {
    boolean isEmpty();

    boolean hasNext();

    boolean hasPrevious();

    T next();

    T previous();
}

class DLinkedList<T extends Comparable<T>> {
    private DNode head;

    private class DNode {
        T data;
        DNode previous, next;

        DNode(T d) {
            data = d;
        }
    }

    public void clear() {
        head = null;
    }

    public synchronized boolean insert(T d) {
        try {
            DNode temp = new DNode(d);
            DNode cur = head;
            DNode prev = head;
            // 1. empty list case
            if (head == null) {
                head = temp;
                return true;
            }
            // 2. non-empty list, find position
            while ((cur.next != null) && (cur.data.compareTo(d) < 0)) {
                prev = cur;
                cur = cur.next;
            }
            // 3. value exists in list already - fail
            if (cur.data.compareTo(d) == 0) {
                return false;
            }
            // 4. single node in list
            if (cur == prev) {
                // 5. single node < new node
                if (cur.data.compareTo(d) < 0) {
                    cur.next = temp;
                    temp.previous = cur;
                    return true;
                }
                // 6. single node > new node
                temp.next = cur;
                cur.previous = temp;
                head = temp;
                return true;
            }
            // 7. multiple nodes in list

            if (cur.data.compareTo(d) > 0) {
                prev.next = temp;
                temp.next = cur;
            } else {
                temp.next = cur.next;
                cur.next = temp;
            }
            // 8. check if being added at the start of the list
            // if it is there is no previous node and the head of list
            // needs to change
            if (cur.previous != null)
                cur.previous = temp;
            else
                head = temp;
            temp.previous = prev;
        } catch (Exception e) {
        }
        return true;
    }

    public DIterator<T> iterator() {
        return new DIterator<T>() {
            DNode cur = head;

            public boolean isEmpty() {
                if (cur != null)
                    return false;
                return true;
            }

            public boolean hasNext() {
                return cur.next != null;
            }

            public boolean hasPrevious() {
                return cur.previous != null;
            }

            public T next() {
                T d = cur.data;
                cur = cur.next;
                return d;
            }

            public T previous() {
                T d = cur.data;
                cur = cur.previous;
                return d;
            }
        };
    }
}

// CREATE YOUR RUNNABLE CLASS(ES) HERE FOR THREADING

class MyThread extends Thread{
    
    private static DLinkedList<Integer> list;
    
    private int[] numbers;
    
    public MyThread(int [] numbers, DLinkedList<Integer> list){
        this.numbers = numbers;
        MyThread.list = list;
    }
    
    public void run(){
        for(int i = 0; i < numbers.length;i++){
            list.insert(numbers[i]);
        }
    }
}




public class RaceTest {
    private static DLinkedList<Integer> list = new DLinkedList<Integer>();
    
    public static void main(String[] args) throws InterruptedException {
        int[] prime1 = {47,13,23,17};//for Thread1
        
        int[] prime2 = {5,19,37,7};//for Thread2

        // make threads and launch them
        MyThread r1 = new MyThread(prime1, list);
        MyThread r2 = new MyThread(prime2, list);
        
        r1.start();
        r2.start();

        // make sure you WAIT for Thread1 and Thread2 to complete before
        
        r1.join();
        r2.join();
        
        // attempting to print
        print(list);// result should display missing data, data out of order,
                    // different each time
    }

    public static <P extends Comparable<P>> void print(DLinkedList<P> list) {
        DIterator<P> i = list.iterator();
        while (!i.isEmpty())
            System.out.print("" + i.next() + " ");
        System.out.println("");
    }

    public static <P extends Comparable<P>> void printR(DLinkedList<P> list) {
        DIterator<P> i = list.iterator();
        while (i.hasNext())
            i.next();
        while (!i.isEmpty())
            System.out.print("" + i.previous() + " ");
        System.out.println("");
    }
}
