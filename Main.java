import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Main
{
   static class Producer implements Runnable
   {
       // TODO: You may want to implement this class to test your code
       private final int jobsToSubmit;
       private final int sleepDuration;
       private final int id;
       private final float studentJobProbability;
       private final int jobBaseDurationInMs;
       private PrinterRoom room;

       private Thread myThread;

       public Producer(PrinterRoom room, int id, int jobCount, int sleepDur, int jobBaseDurationInMs, float studentJobProb){
           this.room = room;
           this.id = id;
           this.jobsToSubmit  = jobCount;
           this.sleepDuration = sleepDur;
           this.studentJobProbability = studentJobProb;
           this.jobBaseDurationInMs = jobBaseDurationInMs;
           SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, id, "Created");

           myThread = new Thread(this);
           myThread.start();
       }

       public void join()
       {
           // TODO: Provide a thread join functionality for the main thread
           try {
               myThread.join();
           } catch (InterruptedException e) {
           }
       }

       @Override
       public void run() {
           int submittedCount = 0;

           try {
               Thread.sleep((long)(sleepDuration));
           } catch (InterruptedException e) {}

           while(submittedCount < jobsToSubmit)
           {
               float rand = ThreadLocalRandom.current().nextFloat(0.0f, 1.0f);
               var type = PrintItem.PrintType.INSTRUCTOR;
               if(rand < studentJobProbability){
                   type = PrintItem.PrintType.STUDENT;
               }

               float perctOffset = ThreadLocalRandom.current().nextFloat(-0.25f, 1.5f);
               int offset = (int)Math.floor(jobBaseDurationInMs * perctOffset);

               PrintItem itemToSubmit = new PrintItem(jobBaseDurationInMs + offset, type, id);
               if(room.SubmitPrint(itemToSubmit, id)){
//                   SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, id, "_dork_ SUBMITTED " + itemToSubmit);
                   submittedCount++;
               }
               else{
                   // room was closed, decide what to do.
                   // this basic imp just terminates. can try adding few more for further testing.
                   SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, id, String.format(SyncLogger.FORMAT_ROOM_CLOSED, itemToSubmit));
                   break;
               }
               try {
                   Thread.sleep((long)(sleepDuration));
               } catch (InterruptedException e) {}
           }

           SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, id, "Producer: " + id + " is terminating.");
       }
   }

    public static void main(String args[]) throws InterruptedException
    {
        PrinterRoom room = new PrinterRoom(5, 5);

        int producerCount = 6;
        ArrayList<Producer> producers = new ArrayList<>();
        for(int i = 0; i < producerCount; i +=2){
            producers.add(new Producer(room, i, 10, 100*(i+1), 1000,0.70f));
            producers.add(new Producer(room, i+1, 10, 500, 200,0.15f));
        }

        int roomCloseDelayInSeconds = 15;
        Thread.sleep((long)(roomCloseDelayInSeconds * 1000));

        // Log before close
        SyncLogger.Instance().Log(SyncLogger.ThreadType.MAIN_THREAD, 0, "Closing Room");
        room.CloseRoom();

        for (var prod: producers) {
            prod.join();
        }

        // original comment: This should print only after all elements are closed (here we wait 3 seconds so it should be immediate)
        SyncLogger.Instance().Log(SyncLogger.ThreadType.MAIN_THREAD, 0, "Room is Closed");
    }
}