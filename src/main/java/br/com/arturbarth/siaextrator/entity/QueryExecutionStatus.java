package br.com.arturbarth.siaextrator.entity;

public enum QueryExecutionStatus {
    PENDING("Pendente"),
    RUNNING("Executando"),
    COMPLETED("Concluído"),
    FAILED("Falhou"),
    CANCELLED("Cancelado");
    
    private final String description;
    
    QueryExecutionStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
