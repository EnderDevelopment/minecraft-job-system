# Minecraft Job Management Plugin

JobSystemPlus is a Minecraft job management plugin for Paper servers. It lets players create and browse jobs, apply for positions, review applications, accept or reject applicants, and manage employment with commands.

## Install

1. Use a Paper server running Minecraft 1.21.11 or a compatible version, with Java 21.
2. Build the project with Maven using `mvn package`.
3. Copy the generated `target/plugin.jar` into the server’s `plugins` folder.
4. Restart the server. Job data is stored in `plugins/Jobs/jobs.yml`.

## Use

- Run `/job list` to browse jobs, or `/job info <id>` to view a job’s details.
- Create a job with `/job create <id> <title> <description>`.
- Apply with `/job apply <id>`. Use `/job mine` to see your jobs and applications.
- As the employer, view applications with `/job applications <id>`, then use `/job accept <id> <player>` or `/job reject <id> <player>`.
- Edit a job with `/job edit <id> title <text>` or `/job edit <id> description <text>`. Remove one with `/job delete <id>`.
- Employees can leave with `/job quit <id>`.
- Server operators with the `jobs.admin` permission can manage any job.

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/d7a4f08f-837b-4fff-9d49-f3bed8d4d2f2?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=d7a4f08f-837b-4fff-9d49-f3bed8d4d2f2
