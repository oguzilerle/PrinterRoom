import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class PrinterQueue implements IMPMCQueue<PrintItem>
{
    // TODO: This is all yours
    Queue<PrintItem> teachersQueue;
    Queue<PrintItem> studentsQueue;

    private ReentrantLock queueLock;
    private Semaphore entryLock;
    private int lengthOfQueue;

    private boolean isClosed;
    public PrinterQueue(int maxElementCount)
    {
        // TODO: Implement
        teachersQueue = new PriorityQueue<>(maxElementCount);
        studentsQueue = new PriorityQueue<>(maxElementCount);

        queueLock = new ReentrantLock();
        entryLock = new Semaphore(maxElementCount, true);

        lengthOfQueue = 0;
        isClosed = false;
    }

    @Override
    public void Add(PrintItem data) throws QueueIsClosedExecption {
        if (isClosed) throw new QueueIsClosedExecption();
        try {
            entryLock.acquire();
            queueLock.lock();
            if (data.getPrintType() == PrintItem.PrintType.INSTRUCTOR) teachersQueue.add(data);
            else studentsQueue.add(data);
            lengthOfQueue += 1;

        }
        catch (InterruptedException e) {}
        finally {
            queueLock.unlock();
        }
    }

    @Override
    public PrintItem Consume() throws QueueIsClosedExecption {

        queueLock.lock();
        if (isClosed && lengthOfQueue == 0)
        {
            queueLock.unlock();
            throw new QueueIsClosedExecption();
        }
        PrintItem removed;
        if (teachersQueue.isEmpty()) removed = studentsQueue.remove();
        else removed = teachersQueue.remove();
        lengthOfQueue -= 1;
        entryLock.release();
        queueLock.unlock();
        return removed;
    }

    @Override
    public int RemainingSize() {
        queueLock.lock();
        int size = lengthOfQueue;
        queueLock.unlock();
        return size;
    }

    @Override
    public void CloseQueue()
    {
        isClosed = true;
    }
}
