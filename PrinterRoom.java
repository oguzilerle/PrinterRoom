import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PrinterRoom
{
    private class Printer implements Runnable
    {
        IMPMCQueue<PrintItem> queue;
        private int id;

        public Printer(int id, IMPMCQueue<PrintItem> roomQueue)
        {
            this.queue = roomQueue;
            this.id = id;
            SyncLogger.Instance().Log(SyncLogger.ThreadType.CONSUMER, id,
                                      String.format(SyncLogger.FORMAT_PRINTER_LAUNCH, id));
        }

        @Override
        public void run() {
            while (true)
            {
                try {
                    PrintItem item = queue.Consume();
                    item.print();
                    SyncLogger.Instance().Log(SyncLogger.ThreadType.CONSUMER, id,
                            String.format(SyncLogger.FORMAT_PRINT_DONE, item));
                }
                catch (QueueIsClosedExecption e) {
                    break;
                }
            }
            SyncLogger.Instance().Log(SyncLogger.ThreadType.CONSUMER, id,
                    String.format(SyncLogger.FORMAT_TERMINATING, id));
        }
    }

    private IMPMCQueue<PrintItem> roomQueue;
    private final List<Printer> printers;

    public PrinterRoom(int printerCount, int maxElementCount)
    {
        // Instantiating the shared queue
        roomQueue = new PrinterQueue(maxElementCount);

        // Let's try streams
        // Printer creation automatically launches its thread
        printers = Collections.unmodifiableList(IntStream.range(0, printerCount)
                                                         .mapToObj(i -> new Printer(i, roomQueue))
                                                         .collect(Collectors.toList()));
        // Printers are launched using the same queue

        for (Printer p : printers) {
            new Thread(p).start();
        }
    }

    public boolean SubmitPrint(PrintItem item, int producerId)
    {
        // TODO: Implement
        try {
            SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, producerId,
                    String.format(SyncLogger.FORMAT_ADD, item));
            roomQueue.Add(item);
            return true;
        }
        catch (QueueIsClosedExecption e)
        {
            SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, producerId,
                    String.format(SyncLogger.FORMAT_ROOM_CLOSED, item));
            return false;
        }
    }

    public void CloseRoom()
    {
        // TODO: Implement
        roomQueue.CloseQueue();
    }
}
