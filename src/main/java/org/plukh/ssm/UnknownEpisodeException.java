package org.plukh.ssm;

public class UnknownEpisodeException extends Exception {
    public UnknownEpisodeException(int episode) {
        super("Unknown episode #" + episode);
    }

    public UnknownEpisodeException(String message) {
        super(message);
    }
}
