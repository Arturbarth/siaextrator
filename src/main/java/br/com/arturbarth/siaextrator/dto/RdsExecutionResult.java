package br.com.arturbarth.siaextrator.dto;

import java.util.List;
import java.util.Map;

public class RdsExecutionResult {
    
    private List<Map<String, Object>> data;
    private List<String> columnNames;
    private long rowCount;
    private long executionTimeMs;
    private String errorMessage;
    private boolean successful;
    
    // Construtor principal
    public RdsExecutionResult(List<Map<String, Object>> data, List<String> columnNames, long rowCount) {
        this.data = data;
        this.columnNames = columnNames;
        this.rowCount = rowCount;
        this.successful = true;
    }
    
    // Construtor para erro
    public RdsExecutionResult(String errorMessage) {
        this.errorMessage = errorMessage;
        this.successful = false;
        this.rowCount = 0;
    }
    
    // Construtor vazio
    public RdsExecutionResult() {
        this.successful = false;
        this.rowCount = 0;
    }
    
    // Getters and Setters
    public List<Map<String, Object>> getData() {
        return data;
    }
    
    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
    
    public List<String> getColumnNames() {
        return columnNames;
    }
    
    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }
    
    public long getRowCount() {
        return rowCount;
    }
    
    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
    
    // Métodos utilitários
    public boolean hasData() {
        return data != null && !data.isEmpty();
    }
    
    public int getColumnCount() {
        return columnNames != null ? columnNames.size() : 0;
    }
    
    public boolean isEmpty() {
        return rowCount == 0;
    }
    
    @Override
    public String toString() {
        return "RdsExecutionResult{" +
                "rowCount=" + rowCount +
                ", executionTimeMs=" + executionTimeMs +
                ", successful=" + successful +
                ", columnCount=" + getColumnCount() +
                ", hasData=" + hasData() +
                '}';
    }
    
}
