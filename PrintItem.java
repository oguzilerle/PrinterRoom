public class PrintItem implements Comparable<PrintItem>
{
    private int         printDuration; // in milliseconds
    private PrintType   type;
    private int         id;

    @Override
    public int compareTo(PrintItem o) {
        return this.id - o.id;
    }

    public enum PrintType
    {
        INSTRUCTOR,
        STUDENT
    };

    public PrintItem(int printDuration, PrintType t, int id)
    {
        this.printDuration = printDuration;
        this.type = t;
        this.id = id;
    }

    /**
     * 'Prints' the item (actually does nothing, only does sleeping)
     * @return true if print event is succeeded, return false when sleep operation is interrupted
     */
    public boolean print()
    {
        try
        {
            Thread.sleep(printDuration);
        }
        // Interrupted exception may occur when main thread tries to kill
        // the entire system
        catch (InterruptedException e) { return false; }
        return true;
    }

    @Override
    public String toString()
    {
        return String.format("{%s, ID:%03d, %03dms}",
                             (type == PrintType.STUDENT) ? "Student   " : "Instructor",
                             id, printDuration);
    }

    PrintType getPrintType()
    {
        return type;
    }

    int getPrintDuration()
    {
        return printDuration;
    }
}
