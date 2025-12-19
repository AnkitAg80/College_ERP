package edu.univ.erp.domain;

 //Corresponds to the branches table in erpdb.
public class Branch {
    private int branchId;
    private String branchCode;
    private String branchName;

    public Branch() {}

    public Branch(int branchId, String branchCode, String branchName) {
        this.branchId = branchId;
        this.branchCode = branchCode;
        this.branchName = branchName;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    @Override
    public String toString() {
        return branchName + " (" + branchCode + ")";
    }
}