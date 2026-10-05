# SisterLove South Africa

## Project Overview

**SisterLove South Africa** is a web application developed to provide the organisation with a professional digital platform for communicating its work, engaging with the community and supporting online interactions.

The application brings together public-facing information and interactive functionality such as donations, contact enquiries, events, partnerships, volunteering, user accounts and administrative functionality.

The system was developed using **ASP.NET Core MVC**, **C#**, **Entity Framework Core** and **Microsoft SQL Server**, with security and structured data flow considered throughout the application.

---

## Project Links

| Resource | Link |
|---|---|
| **GitHub Repository** | [WIL-SisterLove](https://github.com/TechMoveInovators/WIL-SisterLove) |
| **Azure App Service** | [Open SisterLove South Africa](https://sisterlove-sa-2026-cjh9gedtdhhebudu.southafricanorth-01.azurewebsites.net/) |
| **Official Domain** | [sisterloveorg.co.za](https://sisterloveorg.co.za) |
| **YouTube Demonstration** | **ADD YOUTUBE LINK HERE** |

### YouTube Demonstration

A video demonstration of the completed application can be accessed below:

**YouTube:** [Watch the SisterLove South Africa Application Demonstration](ADD-YOUTUBE-LINK-HERE)

---

# Purpose of the Application

The purpose of the SisterLove South Africa application is to provide a central online platform where users can:

- Learn about SisterLove South Africa
- View the organisation's work and track record
- Explore partnerships and opportunities
- Make donations
- Submit contact enquiries
- Express interest in volunteering
- Submit funding enquiries
- View events
- Create and manage user accounts
- Access functionality according to their assigned role

The application also provides administrative functionality for managing information and user-submitted data.

---

# Main Application Features

## Home Page

The Home page is the main entry point of the application.

It provides:

- SisterLove South Africa branding
- Main website navigation
- An introduction to the organisation
- Calls to action
- Links to the organisation's work
- Links to support and engagement functionality

---

## About Us

The About Us section provides information about SisterLove South Africa and introduces users to the organisation, its purpose and its work.

The section is designed to give visitors a clear understanding of the organisation before they interact with the rest of the website.

---

## Our Work

The Our Work section communicates the organisation's programmes, initiatives and areas of impact.

This section helps users understand the practical work carried out by SisterLove South Africa and how the organisation engages with communities.

---

## Track Record

The Track Record section presents the organisation's previous work and experience.

It provides visitors and potential partners with information that demonstrates the organisation's history and experience.

---

## Partnerships

The Partnerships section supports engagement with organisations, funders, sponsors and other potential partners.

It provides information about partnership opportunities and encourages organisations to work with SisterLove South Africa.

---

# Get Involved

## Donations

The application provides a donation workflow for users who want to support SisterLove South Africa.

The donation process follows this structure:

```text
Donation Form
      ↓
DonateController
      ↓
IDonationService
      ↓
DonationService
      ↓
Donation Entity
      ↓
ApplicationDbContext
      ↓
Entity Framework Core
      ↓
SQL Server
      ↓
Donation Result
      ↓
Thank You Page
