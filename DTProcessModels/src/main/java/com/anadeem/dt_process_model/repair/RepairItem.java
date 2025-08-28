package com.anadeem.dt_process_model.repair;

/**
 * Model class representing repair-list.js repair item that needs to be processed
 */
public class RepairItem {

    private String repairId;
    private String surface;
    private int severity;
    private int priority;
    private String defectType;
    private String status;

    public RepairItem() {
    }

    public String getRepairId() {
        return repairId;
    }

    public void setRepairId(String repairId) {
        this.repairId = repairId;
    }

    public String getSurface() {
        return surface;
    }

    public void setSurface(String surface) {
        this.surface = surface;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getDefectType() {
        return defectType;
    }

    public void setDefectType(String defectType) {
        this.defectType = defectType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "RepairItem{" +
                "repairId='" + repairId + '\'' +
                ", surface='" + surface + '\'' +
                ", severity=" + severity +
                ", priority=" + priority +
                ", defectType='" + defectType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}