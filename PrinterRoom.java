import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PrinterRoom
{
    private class Printer implements Runnable
    {
        private IMPMCQueue<PrintItem> queue;
        private int id;

        public Printer(int id, IMPMCQueue<PrintItem> roomQueue)
        {
            this.queue = roomQueue;
            this.id = id;
            SyncLogger.Instance().Log(SyncLogger.ThreadType.MAIN_THREAD, id,
                                      String.format(SyncLogger.FORMAT_PRINTER_LAUNCH, id));
        }

        @Override
        public void run() {
            while (true)
            {
                try {
                    PrintItem item = queue.Consume();
                    SyncLogger.Instance().Log(SyncLogger.ThreadType.CONSUMER, id,
                            String.format(SyncLogger.FORMAT_PRINT_DONE, item));
                    item.print();
                }
                catch (QueueIsClosedExecption e) {
                    SyncLogger.Instance().Log(SyncLogger.ThreadType.CONSUMER, id,
                            String.format(SyncLogger.FORMAT_TERMINATING, id));
                    break;
                }
            }
        }
    }

    private IMPMCQueue<PrintItem> roomQueue;
    private final List<Printer> printers;

    public PrinterRoom(int printerCount, int maxElementCount)
    {
        roomQueue = new PrinterQueue(maxElementCount);
        printers = Collections.unmodifiableList(IntStream.range(0, printerCount)
                                                         .mapToObj(i -> new Printer(i, roomQueue))
                                                         .collect(Collectors.toList()));
        for (Printer p : printers) {
            new Thread(p).start();
        }
    }

    public boolean SubmitPrint(PrintItem item, int producerId)
    {
        SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, producerId,
                String.format(SyncLogger.FORMAT_ADD, item));
        // TODO: Implement
        try {
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
