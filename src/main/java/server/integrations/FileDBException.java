package server.integrations;


public class FileDBException extends Exception{

    FileDBException(String reason) {
        super(reason);
    }

    FileDBException(String reason, Throwable rootCause) {
        super(reason, rootCause);
    }
}