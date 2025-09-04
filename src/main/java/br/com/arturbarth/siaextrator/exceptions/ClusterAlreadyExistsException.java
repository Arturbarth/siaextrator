package br.com.arturbarth.siaextrator.exceptions;

public class ClusterAlreadyExistsException extends RuntimeException {
    public ClusterAlreadyExistsException(String message) {
        super(message);
    }
}