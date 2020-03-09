package ancillary;

public class UnknownCommand extends Exception {

    public UnknownCommand() {
        super(">>> Unknown command! <<<");
    }
}
