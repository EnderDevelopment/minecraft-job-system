package com.enderdevelopment.plugin;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

public final class JobManager {
    private final Map<String, Job> jobs = new LinkedHashMap<>();

    public Job create(String id, String title, String description, String employer) {
        String key = normalize(id);
        Job job = new Job(key, title, description, employer);
        jobs.put(key, job);
        return job;
    }

    public Job get(String id) { return jobs.get(normalize(id)); }
    public Job remove(String id) { return jobs.remove(normalize(id)); }
    public Collection<Job> all() { return jobs.values(); }
    public boolean contains(String id) { return jobs.containsKey(normalize(id)); }

    public void load(File file) {
        jobs.clear();
        if (!file.exists()) return;
        YamlConfiguration data = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = data.getConfigurationSection("jobs");
        if (section == null) return;
        for (String id : section.getKeys(false)) {
            String path = "jobs." + id + ".";
            String title = data.getString(path + "title");
            String description = data.getString(path + "description");
            String employer = data.getString(path + "employer");
            if (title == null || description == null || employer == null) continue;
            Job job = new Job(normalize(id), title, description, employer,
                    new java.util.LinkedHashSet<>(data.getStringList(path + "applicants")),
                    new java.util.LinkedHashSet<>(data.getStringList(path + "employees")));
            jobs.put(job.getId(), job);
        }
    }

    public void save(File file) throws IOException {
        YamlConfiguration data = new YamlConfiguration();
        for (Job job : jobs.values()) {
            String path = "jobs." + job.getId() + ".";
            data.set(path + "title", job.getTitle());
            data.set(path + "description", job.getDescription());
            data.set(path + "employer", job.getEmployer());
            data.set(path + "applicants", job.getApplicants().stream().toList());
            data.set(path + "employees", job.getEmployees().stream().toList());
        }
        data.save(file);
    }

    private String normalize(String id) { return id.toLowerCase(Locale.ROOT); }
}
