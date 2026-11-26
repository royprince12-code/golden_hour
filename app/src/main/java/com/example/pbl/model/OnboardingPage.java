package com.example.pbl.model;

public class OnboardingPage {
    private String title;
    private String subtitle;
    private String body;
    private int iconRes;

    public OnboardingPage(String title, String subtitle, String body, int iconRes) {
        this.title = title;
        this.subtitle = subtitle;
        this.body = body;
        this.iconRes = iconRes;
    }

    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getBody() { return body; }
    public int getIconRes() { return iconRes; }
}
