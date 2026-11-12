package com.enderdevelopment.plugin;

import java.io.File;
import java.io.IOException;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private JobManager jobManager;

    @Override
    public void onEnable() {
        jobManager = new JobManager();
        File dataFile = new File(getDataFolder(), "jobs.yml");
        if (dataFile.exists()) {
            jobManager.load(dataFile);
        }
        JobCommand executor = new JobCommand(this, jobManager);
        if (getCommand("job") == null) {
            getLogger().severe("The job command is missing from plugin.yml.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getCommand("job").setExecutor(executor);
        getCommand("job").setTabCompleter(executor);
        getLogger().info("Jobs enabled with " + jobManager.all().size() + " saved jobs.");
    }

    public void saveJobs() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().severe("Could not create the plugin data folder.");
            return;
        }
        try {
            jobManager.save(new File(getDataFolder(), "jobs.yml"));
        } catch (IOException exception) {
            getLogger().severe("Could not save jobs: " + exception.getMessage());
        }
    }

    @Override
    public void onDisable() {
        if (jobManager != null) {
            saveJobs();
        }
    }
}
