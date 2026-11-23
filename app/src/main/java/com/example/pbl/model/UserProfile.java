package com.example.pbl.model;

public class UserProfile {
    private String name;
    private String phone;
    private String bloodGroup;
    private String allergies;
    private String medicalConditions;

    public UserProfile() {
    }

    public UserProfile(String name, String phone, String bloodGroup, String allergies, String medicalConditions) {
        this.name = name;
        this.phone = phone;
        this.bloodGroup = bloodGroup;
        this.allergies = allergies;
        this.medicalConditions = medicalConditions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getMedicalConditions() {
        return medicalConditions;
    }

    public void setMedicalConditions(String medicalConditions) {
        this.medicalConditions = medicalConditions;
    }
}
