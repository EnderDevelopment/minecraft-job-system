package com.enderdevelopment.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class JobCommand implements CommandExecutor, TabCompleter {
    private final JobManager jobs;
    private final Main plugin;

    public JobCommand(Main plugin, JobManager jobs) {
        this.plugin = plugin;
        this.jobs = jobs;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return help(sender);
        String action = args[0].toLowerCase(Locale.ROOT);
        if (action.equals("list")) {
            if (jobs.all().isEmpty()) { sender.sendMessage("There are no open jobs yet."); return true; }
            sender.sendMessage("Open jobs:");
            jobs.all().forEach(job -> sender.sendMessage("- " + job.getId() + ": " + job.getTitle() + " (" + job.getEmployees().size() + " employees)"));
            return true;
        }
        if (action.equals("create")) {
            if (!(sender instanceof Player player)) { sender.sendMessage("Only players can create jobs."); return true; }
            if (args.length < 4) return usage(sender, "/job create <id> <title> <description>");
            String id = args[1].toLowerCase(Locale.ROOT);
            if (!id.matches("[a-z0-9_-]{1,24}")) { sender.sendMessage("Job IDs may contain only letters, numbers, _ and -, up to 24 characters."); return true; }
            if (jobs.contains(id)) { sender.sendMessage("That job ID already exists."); return true; }
            jobs.create(id, join(args, 2, 3), join(args, 3, args.length), player.getUniqueId().toString());
            save();
            sender.sendMessage("Created job " + id + ".");
            return true;
        }
        if (action.equals("info")) {
            if (args.length < 2) return usage(sender, "/job info <id>");
            Job job = jobs.get(args[1]);
            if (job == null) { sender.sendMessage("Job not found."); return true; }
            sender.sendMessage(job.getTitle() + " [" + job.getId() + "] — " + job.getDescription());
            sender.sendMessage("Employer: " + name(job.getEmployer()) + " | Employees: " + names(job.getEmployees()) + " | Applicants: " + job.getApplicants().size());
            return true;
        }
        if (action.equals("apply")) {
            if (!(sender instanceof Player player)) { sender.sendMessage("Only players can apply."); return true; }
            if (args.length < 2) return usage(sender, "/job apply <id>");
            Job job = jobs.get(args[1]);
            if (job == null) { sender.sendMessage("Job not found."); return true; }
            String uuid = player.getUniqueId().toString();
            if (job.getEmployer().equals(uuid) || job.getEmployees().contains(uuid)) { sender.sendMessage("You already manage or work at this job."); return true; }
            if (!job.getApplicants().add(uuid)) { sender.sendMessage("You have already applied."); return true; }
            save();
            player.sendMessage("Application submitted for " + job.getTitle() + ".");
            notifyEmployer(job, "New application for " + job.getTitle() + " from " + player.getName() + ". Use /job applications " + job.getId() + ".");
            return true;
        }
        if (action.equals("applications")) {
            if (args.length < 2) return usage(sender, "/job applications <id>");
            Job job = jobs.get(args[1]);
            if (!owned(sender, job)) return true;
            if (job.getApplicants().isEmpty()) { sender.sendMessage("No applications for " + job.getTitle() + "."); return true; }
            sender.sendMessage("Applicants for " + job.getTitle() + ":");
            job.getApplicants().forEach(id -> sender.sendMessage("- " + name(id) + " (" + id + ")"));
            sender.sendMessage("Use /job accept <job> <player> or /job reject <job> <player>.");
            return true;
        }
        if (action.equals("accept") || action.equals("reject")) {
            if (args.length < 3) return usage(sender, "/job " + action + " <id> <player>");
            Job job = jobs.get(args[1]);
            if (!owned(sender, job)) return true;
            String applicant = findApplicant(job, args[2]);
            if (applicant == null) { sender.sendMessage("That player has not applied to this job."); return true; }
            job.getApplicants().remove(applicant);
            if (action.equals("accept")) {
                job.getEmployees().add(applicant);
                sender.sendMessage("Accepted " + name(applicant) + " for " + job.getTitle() + ".");
                notifyPlayer(applicant, "Your application for " + job.getTitle() + " was accepted.");
            } else {
                sender.sendMessage("Rejected " + name(applicant) + " for " + job.getTitle() + ".");
                notifyPlayer(applicant, "Your application for " + job.getTitle() + " was declined.");
            }
            save();
            return true;
        }
        if (action.equals("edit")) {
            if (args.length < 4) return usage(sender, "/job edit <id> <title|description> <text>");
            Job job = jobs.get(args[1]);
            if (!owned(sender, job)) return true;
            String value = join(args, 3, args.length);
            if (args[2].equalsIgnoreCase("title")) job.setTitle(value);
            else if (args[2].equalsIgnoreCase("description")) job.setDescription(value);
            else return usage(sender, "Field must be title or description.");
            save();
            sender.sendMessage("Updated " + args[2].toLowerCase(Locale.ROOT) + " for " + job.getId() + ".");
            return true;
        }
        if (action.equals("delete")) {
            if (args.length < 2) return usage(sender, "/job delete <id>");
            Job job = jobs.get(args[1]);
            if (!owned(sender, job)) return true;
            jobs.remove(job.getId());
            save();
            sender.sendMessage("Deleted job " + job.getId() + ".");
            return true;
        }
        if (action.equals("quit")) {
            if (!(sender instanceof Player player)) { sender.sendMessage("Only players can quit a job."); return true; }
            if (args.length < 2) return usage(sender, "/job quit <id>");
            Job job = jobs.get(args[1]);
            if (job == null || !job.getEmployees().remove(player.getUniqueId().toString())) { sender.sendMessage("You do not work at that job."); return true; }
            save();
            sender.sendMessage("You quit " + job.getTitle() + ".");
            notifyEmployer(job, player.getName() + " has quit " + job.getTitle() + ".");
            return true;
        }
        if (action.equals("mine")) {
            if (!(sender instanceof Player player)) { sender.sendMessage("Only players have jobs."); return true; }
            String id = player.getUniqueId().toString();
            List<String> lines = new ArrayList<>();
            jobs.all().forEach(job -> { if (job.getEmployer().equals(id)) lines.add("Employer: " + job.getId() + " (" + job.getTitle() + ")"); if (job.getEmployees().contains(id)) lines.add("Employee: " + job.getId() + " (" + job.getTitle() + ")"); if (job.getApplicants().contains(id)) lines.add("Application: " + job.getId() + " (" + job.getTitle() + ")"); });
            sender.sendMessage(lines.isEmpty() ? "You have no jobs or applications." : String.join("\n", lines));
            return true;
        }
        return help(sender);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return filter(List.of("list", "info", "create", "edit", "delete", "apply", "applications", "accept", "reject", "quit", "mine"), args[0]);
        if (args.length == 2 && Set.of("info", "edit", "delete", "apply", "applications", "accept", "reject", "quit").contains(args[0].toLowerCase(Locale.ROOT))) return filter(jobs.all().stream().map(Job::getId).toList(), args[1]);
        if (args.length == 3 && Set.of("accept", "reject").contains(args[0].toLowerCase(Locale.ROOT))) { Job job = jobs.get(args[1]); if (job != null) return filter(job.getApplicants().stream().map(this::name).toList(), args[2]); }
        if (args.length == 3 && args[0].equalsIgnoreCase("edit")) return filter(List.of("title", "description"), args[2]);
        return List.of();
    }

    private boolean owned(CommandSender sender, Job job) {
        if (job == null) { sender.sendMessage("Job not found."); return false; }
        if (sender.hasPermission("jobs.admin")) return true;
        if (!(sender instanceof Player player) || !job.getEmployer().equals(player.getUniqueId().toString())) { sender.sendMessage("Only this job's employer can do that."); return false; }
        return true;
    }

    private String findApplicant(Job job, String input) {
        for (String id : job.getApplicants()) if (id.equalsIgnoreCase(input) || name(id).equalsIgnoreCase(input)) return id;
        return null;
    }

    private void notifyEmployer(Job job, String message) {
        Player player = Bukkit.getPlayer(UUID.fromString(job.getEmployer()));
        if (player != null) player.sendMessage(message);
    }

    private void notifyPlayer(String id, String message) {
        Player player = Bukkit.getPlayer(UUID.fromString(id));
        if (player != null) player.sendMessage(message);
    }

    private String name(String id) {
        try { return Bukkit.getOfflinePlayer(UUID.fromString(id)).getName() == null ? id : Bukkit.getOfflinePlayer(UUID.fromString(id)).getName(); }
        catch (IllegalArgumentException exception) { return id; }
    }

    private String names(Set<String> ids) { return ids.isEmpty() ? "none" : ids.stream().map(this::name).collect(Collectors.joining(", ")); }
    private String join(String[] args, int start, int end) { return String.join(" ", java.util.Arrays.copyOfRange(args, start, end)); }
    private List<String> filter(List<String> values, String prefix) { return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))).toList(); }
    private void save() { plugin.saveJobs(); }
    private boolean usage(CommandSender sender, String text) { sender.sendMessage(text); return true; }
    private boolean help(CommandSender sender) { sender.sendMessage("Jobs: /job list, info, create, edit, delete, apply, applications, accept, reject, quit, mine"); return true; }
}
