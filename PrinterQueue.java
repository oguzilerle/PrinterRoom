import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PrinterQueue implements IMPMCQueue<PrintItem>
{
    Queue<PrintItem> teachersQueue;
    Queue<PrintItem> studentsQueue;

    private ReentrantLock queueLock;
    private Semaphore entryLock;
    private int lengthOfQueue;
    private boolean isClosed;
    private Condition queueIsEmpty;
    public PrinterQueue(int maxElementCount)
    {
        // TODO: Implement
        this.teachersQueue = new PriorityQueue<>(maxElementCount);
        this.studentsQueue = new PriorityQueue<>(maxElementCount);

        this.queueLock = new ReentrantLock();
        this.entryLock = new Semaphore(maxElementCount, true);

        queueIsEmpty = queueLock.newCondition();

        this.lengthOfQueue = 0;
        this.isClosed = false;
    }

    @Override
    public void Add(PrintItem data) throws QueueIsClosedExecption {
        try {
            this.entryLock.acquire();
            this.queueLock.lock();

            if (isClosed) throw new QueueIsClosedExecption();

            if (data.getPrintType() == PrintItem.PrintType.INSTRUCTOR) this.teachersQueue.add(data);
            else this.studentsQueue.add(data);

            this.lengthOfQueue++;
            this.queueIsEmpty.signal();
        }
        catch (InterruptedException e) {}
        finally {
            this.queueLock.unlock();
        }
    }

    @Override
    public PrintItem Consume() throws QueueIsClosedExecption {

        this.queueLock.lock();
        if (isClosed && lengthOfQueue == 0)
        {
            this.queueLock.unlock();
            throw new QueueIsClosedExecption();
        }
        PrintItem removed;
        if (this.teachersQueue.isEmpty() && this.studentsQueue.isEmpty())
        {
            try {
                this.queueIsEmpty.await();
                if (isClosed && lengthOfQueue == 0)
                {
                    this.queueLock.unlock();
                    throw new QueueIsClosedExecption();
                }
            }
            catch (InterruptedException e) {}
        }

        if (teachersQueue.isEmpty() && !studentsQueue.isEmpty()) removed = studentsQueue.remove();
        else removed = teachersQueue.remove();

        lengthOfQueue--;
        if (isClosed)
        {
            for (int i = 0; i < entryLock.getQueueLength(); i++)
            {
                this.entryLock.release();
            }
        }
        else this.entryLock.release();

        queueLock.unlock();
        return removed;
    }

    @Override
    public int RemainingSize() {
        return this.entryLock.availablePermits();
    }

    @Override
    public void CloseQueue()
    {
        this.queueLock.lock();
        this.isClosed = true;
        this.queueIsEmpty.signalAll();
        this.queueLock.unlock();
    }
}
