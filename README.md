# PortfolioCraft

PortfolioCraft is a Spring Boot web app that generates a single-file, responsive portfolio website. Fill in a form with your details, and the app places them into a template and gives you an HTML file you can download and host anywhere.

## Features

- **Multi-step builder** for personal info, photo, skills, experience, education, and projects
- **Profile photo support**, embedded in the generated page (initials are shown if no photo is uploaded)
- **Live preview** of the generated portfolio inside the builder
- **One-click download** of a standalone `.html` file
- **Safe output**: user input is HTML-escaped, and links are limited to `http`, `https`, `mailto`, and `tel`
- **Responsive design** with a mobile menu and scroll animations
- **No API keys or external AI services** required

## Tech Stack

| Layer    | Technology                                  |
|----------|---------------------------------------------|
| Backend  | Java 17, Spring Boot 3.3.5                  |
| Build    | Maven                                       |
| Frontend | HTML5, CSS3, vanilla JavaScript             |
| Fonts    | Google Fonts (Syne, Inter, Instrument Sans) |
| Deploy   | Docker, Render                              |

## Project Structure

```
portfoliocraftAI/
├── .gitignore
├── README.md
└── portfoliocraft/
    ├── Dockerfile
    ├── pom.xml
    └── src/main/
        ├── java/com/portfoliocraft/
        │   ├── PortfolioApplication.java      # Spring Boot entry point
        │   ├── controller/
        │   │   └── PortfolioController.java   # Routes: /, /builder, /generate
        │   ├── dto/
        │   │   ├── GenerateRequest.java       # Wrapper for incoming JSON
        │   │   └── UserData.java              # Personal, skills, experience, education, projects
        │   └── service/
        │       └── PortfolioService.java      # Fills the template and escapes user input
        └── resources/
            ├── application.properties         # Server port setting
            ├── templates/
            │   └── portfolio.html             # Portfolio template with {{PLACEHOLDERS}}
            └── static/
                ├── home.html                  # Landing page
                └── builder.html               # Multi-step form and preview
```

## How It Works

1. The user fills in the form on `/builder`.
2. The browser sends the data as JSON to `POST /generate`.
3. `PortfolioService` loads `templates/portfolio.html` and replaces each `{{PLACEHOLDER}}` (such as `{{NAME}}`, `{{SKILLS}}`, `{{PROJECTS}}`) with escaped values.
4. The filled HTML is returned as `{ "html": "..." }` and shown in a preview.
5. The user downloads the page as a standalone HTML file.

## Routes

| Method | Path        | Description                              |
|--------|-------------|------------------------------------------|
| GET    | `/`         | Landing page (`home.html`)               |
| GET    | `/builder`  | Portfolio builder form (`builder.html`)  |
| POST   | `/generate` | Accepts user data and returns the HTML   |

## Running Locally

**Requirements:** JDK 17 or newer, and Maven 3.8+ (or use the IDE's built-in Maven).

```bash
cd portfoliocraft
mvn spring-boot:run
```

Open http://localhost:5000 in your browser.

You can also open the `portfoliocraft` folder in IntelliJ IDEA or VS Code and run `PortfolioApplication.java`.

## Customizing the Design

Edit `src/main/resources/templates/portfolio.html` to change the look of generated portfolios. Java only fills in the placeholders, so you can change the HTML and CSS without touching the Java code. If you add a new placeholder, also add it in `PortfolioService.generate()`.

## Deployment

The project includes a `Dockerfile` that builds the jar with Maven and runs it on Java 17.

**Render:**

1. Create a new **Web Service** and connect this repository.
2. Choose **Docker** as the language.
3. Set **Root Directory** to `portfoliocraft`.
4. Click **Deploy web service**.

Render sets the `PORT` variable automatically, and the app reads it through `server.port=${PORT:5000}`. No environment variables are required.

## Notes

- The free Render plan sleeps after a period of inactivity, so the first visit may take about a minute.
- Generated portfolios are static files. They don't store any data on the server.

