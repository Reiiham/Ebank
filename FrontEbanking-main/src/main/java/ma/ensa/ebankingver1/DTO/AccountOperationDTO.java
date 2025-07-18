package ma.ensa.ebankingver1.DTO;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import ma.ensa.ebankingver1.model.OperationType;

import java.util.Date;

public class AccountOperationDTO {
    private  Long id;
    private Date operationDate;
    private double amount;
    @Enumerated(EnumType.STRING)
    private OperationType type;
    private String description;
    //setters and getters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Date getOperationDate() {
        return operationDate;
    }
    public void setOperationDate(Date operationDate) {
        this.operationDate = operationDate;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public OperationType getType() {
        return type;
    }
    public void setType(OperationType type) {
        this.type = type;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

}
