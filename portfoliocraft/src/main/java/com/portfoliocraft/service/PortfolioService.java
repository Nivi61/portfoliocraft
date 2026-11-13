package com.portfoliocraft.service;

import com.portfoliocraft.dto.UserData;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{(\\w+)\\}\\}");
    private static final String NOT_PROVIDED = "<p class=\"muted\">Not provided</p>";

    private final String template;

    public PortfolioService() throws IOException {
        this.template = new ClassPathResource("templates/portfolio.html")
                .getContentAsString(StandardCharsets.UTF_8);
    }

    public String generate(UserData u) {
        String name  = orDefault(u.name(), "Professional");
        String title = orDefault(u.title(), "Developer");
        String bio   = orDefault(u.bio(), "A passionate professional.");

        List<String> skills = nzList(u.skills());
        List<UserData.Experience> exp = nzList(u.experience());
        List<UserData.Education> edu = nzList(u.education());
        List<UserData.Project> proj = nzList(u.projects());

        Map<String, String> v = new HashMap<>();
        v.put("NAME", esc(name));
        v.put("TITLE", esc(title));
        v.put("BIO", esc(bio));
        v.put("AVATAR", avatar(u.photo(), name));
        v.put("GITHUB_URL", safeUrl(u.github()));
        v.put("LINKEDIN_URL", safeUrl(u.linkedin()));
        v.put("SKILLS", skillsHtml(skills));
        v.put("EXPERIENCE", experienceHtml(exp));
        v.put("EDUCATION", educationHtml(edu));
        v.put("PROJECTS", projectsHtml(proj));
        v.put("CONTACT", contactHtml(nz(u.email()), nz(u.phone()), nz(u.location()),
                                     u.github(), u.linkedin()));
        v.put("SKILL_COUNT", String.valueOf(skills.size()));
        v.put("EXP_COUNT", String.valueOf(exp.size()));
        v.put("PROJ_COUNT", String.valueOf(proj.size()));
        v.put("YEAR", String.valueOf(Year.now().getValue()));

        return render(v);
    }

    // Single-pass replacement, so injected user text containing {{...}} is never re-processed
    private String render(Map<String, String> vars) {
        Matcher m = PLACEHOLDER.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String value = vars.getOrDefault(m.group(1), "");
            m.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    // ---------- Sections ----------

    private String skillsHtml(List<String> skills) {
        if (skills.isEmpty()) return NOT_PROVIDED;
        StringBuilder sb = new StringBuilder("<div class=\"pills\">");
        for (String s : skills) {
            sb.append("<span class=\"pill\">").append(esc(s)).append("</span>");
        }
        return sb.append("</div>").toString();
    }

    private String experienceHtml(List<UserData.Experience> list) {
        if (list.isEmpty()) return NOT_PROVIDED;
        StringBuilder sb = new StringBuilder();
        for (var e : list) {
            sb.append(String.format("""
                <div class="card">
                  <h3>%s</h3>
                  <div style="color:#2563eb;margin:.2rem 0">%s</div>
                  <div class="muted" style="font-size:.85rem">%s &ndash; %s</div>
                  <p class="muted" style="margin-top:.6rem">%s</p>
                </div>
                """, esc(e.company()), esc(e.role()), esc(e.start()), esc(e.end()), esc(e.description())));
        }
        return sb.toString();
    }

    private String educationHtml(List<UserData.Education> list) {
        if (list.isEmpty()) return NOT_PROVIDED;
        StringBuilder sb = new StringBuilder();
        for (var e : list) {
            sb.append(String.format("""
                <div class="card">
                  <h3>%s</h3>
                  <p class="muted">%s in %s</p>
                  <span class="badge">%s</span>
                </div>
                """, esc(e.school()), esc(e.degree()), esc(e.field()), esc(e.end())));
        }
        return sb.toString();
    }

    private String projectsHtml(List<UserData.Project> list) {
        if (list.isEmpty()) return NOT_PROVIDED;
        StringBuilder sb = new StringBuilder("<div class=\"grid\">");
        for (var p : list) {
            String tech = Arrays.stream(nz(p.tech()).split(","))
                    .map(String::trim)
                    .filter(t -> !t.isEmpty())
                    .map(t -> "<span class=\"pill\">" + esc(t) + "</span>")
                    .collect(Collectors.joining());

            StringBuilder links = new StringBuilder();
            if (!nz(p.link()).isEmpty()) {
                links.append("<a href=\"").append(safeUrl(p.link()))
                     .append("\" target=\"_blank\" rel=\"noopener\">Live Site</a>");
            }
            if (!nz(p.github()).isEmpty()) {
                links.append("<a href=\"").append(safeUrl(p.github()))
                     .append("\" target=\"_blank\" rel=\"noopener\">GitHub</a>");
            }

            sb.append(String.format("""
                <div class="proj">
                  <h3>%s</h3>
                  <p class="muted" style="margin:.5rem 0 .8rem">%s</p>
                  <div class="tech">%s</div>
                  <div class="links" style="margin-top:.9rem">%s</div>
                </div>
                """, esc(p.name()), esc(p.desc()), tech, links));
        }
        return sb.append("</div>").toString();
    }

    private String contactHtml(String email, String phone, String location,
                               String github, String linkedin) {
        StringBuilder sb = new StringBuilder();
        if (!email.isEmpty())    sb.append(row("Email", "mailto:" + esc(email), email));
        if (!phone.isEmpty())    sb.append(row("Phone", "tel:" + esc(phone), phone));
        if (!location.isEmpty()) sb.append(row("Location", null, location));
        if (!nz(github).isEmpty())   sb.append(row("GitHub", safeUrl(github), nz(github)));
        if (!nz(linkedin).isEmpty()) sb.append(row("LinkedIn", safeUrl(linkedin), nz(linkedin)));

        if (!email.isEmpty()) {
            sb.append("<p style=\"text-align:center;margin-top:2rem\"><a class=\"btn solid\" href=\"mailto:")
              .append(esc(email)).append("\">Send Email</a></p>");
        }
        return sb.toString();
    }

    private String row(String label, String href, String text) {
        String value = href == null
                ? esc(text)
                : "<a href=\"" + href + "\"" + (href.startsWith("http") ? " target=\"_blank\" rel=\"noopener\"" : "")
                  + ">" + esc(text) + "</a>";
        return "<div class=\"contact-row\"><span class=\"muted\">" + label + "</span>" + value + "</div>";
    }

    private String avatar(String photo, String name) {
        String box = "width:120px;height:120px;border-radius:50%;border:3px solid #2563eb;"
                   + "margin:0 auto 1.5rem;";
        if (nz(photo).startsWith("data:image/")) {
            return "<img src=\"" + HtmlUtils.htmlEscape(photo) + "\" alt=\"" + esc(name)
                 + "\" style=\"" + box + "object-fit:cover;display:block\">";
        }
        String initials = Arrays.stream(name.trim().split("\\s+"))
                .limit(2)
                .filter(w -> !w.isEmpty())
                .map(w -> w.substring(0, 1).toUpperCase())
                .collect(Collectors.joining());
        return "<div style=\"" + box + "display:flex;align-items:center;justify-content:center;"
             + "font-size:2.5rem;font-weight:700;color:#2563eb;background:#111111;\">"
             + esc(initials) + "</div>";
    }

    // ---------- Helpers ----------

    private static String nz(String s) {
        return s == null ? "" : s.trim();
    }

    private static String orDefault(String s, String fallback) {
        String t = nz(s);
        return t.isEmpty() ? fallback : t;
    }

    private static <T> List<T> nzList(List<T> list) {
        return list == null ? List.of() : list.stream().filter(Objects::nonNull).toList();
    }

    private static String esc(String s) {
        return HtmlUtils.htmlEscape(nz(s));
    }

    // Allows only http(s), mailto, tel. Adds https:// to bare domains like "github.com/me".
    private static String safeUrl(String raw) {
        String u = nz(raw);
        if (u.isEmpty()) return "#";
        if (!u.matches("(?i)^(https?://|mailto:|tel:).*") && !u.contains(":")) {
            u = "https://" + u;
        }
        return u.matches("(?i)^(https?://|mailto:|tel:).*") ? HtmlUtils.htmlEscape(u) : "#";
    }
}
