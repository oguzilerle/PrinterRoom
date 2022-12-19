import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main
{
   static class Producer implements Runnable
   {

       // TODO: You may want to implement this class to test your code
       private PrinterRoom room;
       private int producerID;

       private PrintItem.PrintType type;

         public Producer(PrinterRoom room, int id, PrintItem.PrintType type)
         {
              this.room = room;
              this.producerID = id;
              this.type = type;
             SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, producerID,
                     String.format(SyncLogger.FORMAT_PRODUCER_LAUNCH, producerID));
         }

       @Override
       public void run() {
            while (true)
            {
                try {
                    PrintItem item = new PrintItem(1000, type, producerID);
                    if(!room.SubmitPrint(item, producerID))
                    {
                        SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, producerID,
                                String.format(SyncLogger.FORMAT_ROOM_CLOSED, item));
                        break;
                    }
                }
                catch (QueueIsClosedExecption e) {
                    break;
                }
            }
       }
   }

    public static void main(String args[]) throws InterruptedException
    {
        List<Producer> producers = new ArrayList<>();
        List<PrintItem> items = new ArrayList<>();
        PrinterRoom room = new PrinterRoom(2, 8);
        while(true)
        {
            /*Producer p1 = new Producer(room, 1, PrintItem.PrintType.STUDENT);
            Producer p2 = new Producer(room, 2, PrintItem.PrintType.INSTRUCTOR);

            producers.add(p1);
            producers.add(p2);

            for (Producer p : producers) {
                new Thread(p).start();
            }*/
            items.add(new PrintItem(1000, PrintItem.PrintType.STUDENT, 1));
            items.add(new PrintItem(1000, PrintItem.PrintType.STUDENT, 2));
            items.add(new PrintItem(1000, PrintItem.PrintType.INSTRUCTOR, 3));
            items.add(new PrintItem(1000, PrintItem.PrintType.STUDENT, 4));
            items.add(new PrintItem(1000, PrintItem.PrintType.STUDENT, 5));
            items.add(new PrintItem(1000, PrintItem.PrintType.INSTRUCTOR, 6));
            items.add(new PrintItem(1000, PrintItem.PrintType.STUDENT, 7));
            items.add(new PrintItem(1000, PrintItem.PrintType.STUDENT, 8));
            items.add(new PrintItem(1000, PrintItem.PrintType.INSTRUCTOR, 9));
            items.add(new PrintItem(1000, PrintItem.PrintType.STUDENT, 10));
            items.add(new PrintItem(1000, PrintItem.PrintType.STUDENT, 11));

            for (PrintItem item : items) {
                if (!room.SubmitPrint(item, item.getId())) {
                    break;
                }
                Thread.sleep((long)(200));
            }
            break;
        }

        // Wait a little we are doing produce on the same thread that will do the close
        // actual tests won't do this.
        Thread.sleep((long)(3 * 1000));
        // Log before close
        SyncLogger.Instance().Log(SyncLogger.ThreadType.MAIN_THREAD, 0,
                                  "Closing Room");
        room.CloseRoom();
        for (PrintItem item : items) {
            if (!room.SubmitPrint(item, 1)) {
                break;
            }
        }
        // This should print only after all elements are closed (here we wait 3 seconds so it should be immediate)
        SyncLogger.Instance().Log(SyncLogger.ThreadType.MAIN_THREAD, 0,
                                  "Room is Closed");
    }
}