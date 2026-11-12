package com.enderdevelopment.plugin;

import java.util.LinkedHashSet;
import java.util.Set;

public final class Job {
    private final String id;
    private String title;
    private String description;
    private final String employer;
    private final Set<String> applicants;
    private final Set<String> employees;

    public Job(String id, String title, String description, String employer) {
        this(id, title, description, employer, new LinkedHashSet<>(), new LinkedHashSet<>());
    }

    public Job(String id, String title, String description, String employer, Set<String> applicants, Set<String> employees) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.employer = employer;
        this.applicants = new LinkedHashSet<>(applicants);
        this.employees = new LinkedHashSet<>(employees);
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getEmployer() { return employer; }
    public Set<String> getApplicants() { return applicants; }
    public Set<String> getEmployees() { return employees; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
}
