package br.com.arturbarth.siaextrator.exceptions;

public class ClusterNotFoundException extends RuntimeException {
    public ClusterNotFoundException(String message) {
        super(message);
    }
}