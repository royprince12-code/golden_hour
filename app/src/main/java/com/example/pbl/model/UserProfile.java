package com.example.pbl.model;

public class UserProfile {
    private String name;
    private String phone;
    private String bloodGroup;
    private String allergies;
    private String medicalConditions;
    private String age;
    private String heightWeight;
    private String medications;
    private String surgeries;
    private String organDonor;
    private String notes;

    public UserProfile() {
    }

    public UserProfile(String name, String phone, String bloodGroup, String allergies, String medicalConditions) {
        this.name = name;
        this.phone = phone;
        this.bloodGroup = bloodGroup;
        this.allergies = allergies;
        this.medicalConditions = medicalConditions;
    }

    public UserProfile(String name, String phone, String bloodGroup, String allergies, String medicalConditions,
                       String age, String heightWeight, String medications, String surgeries, String organDonor, String notes) {
        this.name = name;
        this.phone = phone;
        this.bloodGroup = bloodGroup;
        this.allergies = allergies;
        this.medicalConditions = medicalConditions;
        this.age = age;
        this.heightWeight = heightWeight;
        this.medications = medications;
        this.surgeries = surgeries;
        this.organDonor = organDonor;
        this.notes = notes;
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

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getHeightWeight() {
        return heightWeight;
    }

    public void setHeightWeight(String heightWeight) {
        this.heightWeight = heightWeight;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getSurgeries() {
        return surgeries;
    }

    public void setSurgeries(String surgeries) {
        this.surgeries = surgeries;
    }

    public String getOrganDonor() {
        return organDonor;
    }

    public void setOrganDonor(String organDonor) {
        this.organDonor = organDonor;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
