package com.portfoliocraft.dto;

import java.util.List;

public record UserData(
        String name,
        String title,
        String bio,
        String email,
        String phone,
        String location,
        String github,
        String linkedin,
        String photo,
        List<String> skills,
        List<Experience> experience,
        List<Education> education,
        List<Project> projects
) {
    public record Experience(String role, String company, String start, String end, String description) {}
    public record Education(String degree, String field, String school, String end) {}
    public record Project(String name, String desc, String tech, String link, String github) {}
}
