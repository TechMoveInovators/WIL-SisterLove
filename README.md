

readme = r"""# SisterLove South Africa

![SisterLove South Africa Home Page](docs/images/homepage.png)

## About the Project

**SisterLove South Africa** is a web application developed to support the digital presence and community engagement of SisterLove South Africa. The application provides visitors, donors, volunteers, partners and administrators with a central platform to learn about the organisation, explore its work, get involved, make donations, submit enquiries and manage relevant information.

The application combines a modern responsive front end with ASP.NET Core MVC, Entity Framework Core and SQL Server database integration. The project also includes authentication, authorisation, data validation, secure form handling and administrative functionality.

---

## Project Links

### GitHub Repository

[View the SisterLove South Africa GitHub Repository](https://github.com/TechMoveInovators/WIL-SisterLove)

### Live Application

> **Live Application:** Add the deployed application URL here.

Example:

`https://your-deployed-sisterlove-app.com`

### API / Swagger

> **API / Swagger:** Add the deployed API or Swagger URL here if applicable.

Example:

`https://your-api-url/swagger`

### Local Application

The application can be run locally from Visual Studio using the HTTPS launch profile.

Current local development address:

`https://localhost:7203`

> The local URL may change depending on the launch settings and Visual Studio configuration.

---

# Application Overview

The SisterLove South Africa application is designed around several main areas of functionality.

## 1. Home Page

The Home page introduces SisterLove South Africa and communicates the organisation's purpose and community-focused work.

It provides:

- SisterLove South Africa branding
- Main navigation
- Organisation introduction
- Calls to action
- Links to explore the organisation's work
- Partnership and support options
- Responsive visual design

### Screenshot

![Home Page](docs/images/homepage.png)

---

## 2. About Us

The About Us section provides information about SisterLove South Africa, its purpose and its community-focused approach.

It helps visitors understand:

- Who SisterLove South Africa is
- The organisation's goals
- Its community impact
- Its focus areas
- Its approach to social and health-related initiatives

**Screenshot:**  
Add a screenshot here:

`docs/images/about-us.png`

---

## 3. Our Work

The Our Work section presents the organisation's programmes, initiatives and areas of impact.

The section is intended to help users understand the practical work carried out by SisterLove South Africa.

It can include:

- Community programmes
- Youth empowerment
- Health initiatives
- Gender justice work
- Education and awareness
- Community engagement

**Screenshot:**  
Add:

`docs/images/our-work.png`

---

## 4. Track Record

The Track Record section communicates the organisation's experience, previous work and impact.

This area helps demonstrate that the organisation has an established history and experience rather than being a new initiative.

**Screenshot:**  
Add:

`docs/images/track-record.png`

---

## 5. Partnerships

The Partnerships section provides information for organisations that want to collaborate with SisterLove South Africa.

It supports the organisation's relationship with:

- Community partners
- Funders
- Organisations
- Sponsors
- Strategic partners

**Screenshot:**  
Add:

`docs/images/partnerships.png`

---

# Get Involved

The application provides several ways for users to engage with SisterLove South Africa.

## 6. Donate

The Donate section allows users to submit donation information.

The donation workflow is:

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
SQL Server
      ↓
DonationResult
      ↓
Thank You Page
```

Donation information includes:

- Donor name
- Email address
- Donation amount
- Donation type
- Optional message
- Transaction ID
- Date/time of the donation

The system creates a transaction identifier and stores the donation using Entity Framework Core.

**Screenshot:**  
Add:

`docs/images/donate.png`

---

## 7. Contact

The Contact section allows visitors to send enquiries to SisterLove South Africa.

The form collects:

- Full name
- Email address
- Phone number
- Subject
- Message
- Volunteer interest

The current workflow is:

```text
Contact Form
      ↓
ContactController
      ↓
IContactService
      ↓
ContactService
      ↓
ContactEnquiry
      ↓
ApplicationDbContext
      ↓
SQL Server
```

The form uses server-side validation and anti-forgery protection.

**Screenshot:**  
Add:

`docs/images/contact.png`

---

## 8. Volunteer Applications

The application supports volunteer engagement and provides functionality for managing volunteer applications.

Volunteer information can be submitted through the application and managed through the administrative area.

**Screenshot:**  
Add:

`docs/images/volunteer.png`

---

## 9. Funding Enquiries

The application includes functionality for funding-related enquiries.

This provides a dedicated workflow for organisations or potential funders who want to communicate with SisterLove South Africa.

**Screenshot:**  
Add:

`docs/images/funding-enquiry.png`

---

# User Accounts and Authentication

The application includes database-backed user authentication.

Users log in using:

- Email address
- Password

The authentication workflow is:

```text
Login Form
      ↓
AccountController
      ↓
Users Table
      ↓
BCrypt Password Verification
      ↓
Role Lookup
      ↓
Claims
      ↓
Authentication Cookie
      ↓
Authenticated User
```

The application uses claims including:

- User ID
- Full name
- Email address
- Role
- Phone number where available

The authenticated user's database ID is stored in the `NameIdentifier` claim.

This allows other parts of the application to identify the currently logged-in user without trusting an ID submitted by the browser.

---

# User Profile

Authenticated users can access **My Account**.

The profile functionality allows users to:

- View their name
- View their email address
- View their membership date
- Update their full name
- Update their phone number
- Sign out securely

Profile updates are saved to the database.

The logout process uses:

- HTTP POST
- Anti-forgery token
- Cookie sign-out

---

# Events

The Events section allows users to view upcoming events.

Events are stored in the database and displayed in date order.

The event workflow is:

```text
Authenticated User
      ↓
NameIdentifier Claim
      ↓
User.Id
      ↓
EventController
      ↓
Event Entity
      ↓
ApplicationDbContext
      ↓
SQL Server
```

Event functionality includes:

- Viewing events
- Creating events
- Editing events
- Deleting events
- Event descriptions
- Event dates
- Event venues
- Creator information

A key security improvement is that the event creator ID is obtained from the authenticated user's claim rather than being trusted from the submitted form.

---

# Administration

The application includes an administrative area for managing relevant organisation information.

The administrative functionality provides access to areas such as:

- Donations
- Funding enquiries
- Volunteer applications
- Content and organisation information

Administrator access is based on the user's database role.

The authentication system loads the user's role from the database and places it into the authentication claims.

---

# Security

Security was considered throughout the application.

## Authentication

The application uses ASP.NET Core Cookie Authentication.

Security measures include:

- Database-backed authentication
- BCrypt password verification
- Authentication cookies
- User ID claims
- Role claims
- Login validation
- Secure logout
- Return URL validation

## Authorisation

Protected controller actions use:

```csharp
[Authorize]
```

Public pages can use:

```csharp
[AllowAnonymous]
```

This prevents unauthenticated users from accessing protected functionality.

## Anti-Forgery Protection

POST actions that change data use:

```csharp
[ValidateAntiForgeryToken]
```

Forms include:

```cshtml
@Html.AntiForgeryToken()
```

This provides protection against cross-site request forgery (CSRF).

## Input Validation

View models use data annotation validation such as:

```csharp
[Required]
[EmailAddress]
[StringLength]
[Phone]
```

This provides validation before data is processed.

## Cookie Security

Authentication cookies are configured with security settings including:

- `HttpOnly`
- `SameSite`
- Authentication expiry
- Sliding expiration

---

# Data Flow and Logic

The project uses a structured MVC data flow.

A typical database-backed workflow is:

```text
User
 ↓
View
 ↓
Controller
 ↓
Service
 ↓
Entity
 ↓
ApplicationDbContext
 ↓
Entity Framework Core
 ↓
SQL Server
```

For example, the Contact workflow follows:

```text
Contact Form
 ↓
ContactController
 ↓
IContactService
 ↓
ContactService
 ↓
ContactEnquiry
 ↓
ApplicationDbContext
 ↓
SQL Server
```

The Donation workflow follows:

```text
Donation Form
 ↓
DonateController
 ↓
DonationService
 ↓
Donation
 ↓
ApplicationDbContext
 ↓
SQL Server
```

This separation makes the application easier to maintain and test.

---

# Database

The application uses **Entity Framework Core** for database access.

The `ApplicationDbContext` contains entities including:

- Roles
- Users
- Donations
- Events
- Programmes
- Partnerships
- Legacy Projects
- News Articles
- Media
- Volunteer Applications
- Funding Enquiries
- Contact Enquiries

Relationships between entities are configured in `ApplicationDbContext`.

Examples include:

- User → Role
- Event → User
- Programme → User
- Partnership → User
- News Article → User
- Media → Programme
- Media → News Article
- Media → Legacy Project
- Contact Enquiry → User
- Funding Enquiry → User
- Volunteer Application → User

---

# Technologies Used

## Front End

- HTML5
- CSS3
- JavaScript
- Razor Views
- Responsive web design
- Font Awesome icons

## Back End

- C#
- ASP.NET Core MVC
- Entity Framework Core
- .NET
- REST/API integration where applicable

## Database

- Microsoft SQL Server
- Entity Framework Core
- Code First migrations

## Authentication and Security

- ASP.NET Core Cookie Authentication
- BCrypt password hashing
- Claims-based authentication
- Role-based authorisation
- Anti-forgery tokens
- Model validation

## Development Tools

- Visual Studio
- Git
- GitHub
- SQL Server
- Swagger/API tools where applicable

---

# Project Structure

A simplified project structure is:

```text
WIL-SisterLove
│
├── Controllers
│   ├── AccountController.cs
│   ├── ContactController.cs
│   ├── DonateController.cs
│   ├── EventController.cs
│   └── ...
│
├── Models
│   ├── Entities
│   │   ├── User.cs
│   │   ├── Role.cs
│   │   ├── Event.cs
│   │   ├── Donation.cs
│   │   ├── ContactEnquiry.cs
│   │   └── ...
│   │
│   ├── ContactViewModel.cs
│   ├── LoginViewModel.cs
│   └── ...
│
├── Services
│   ├── ContactService.cs
│   ├── DonationService.cs
│   ├── IContactService.cs
│   ├── IDonationService.cs
│   └── ...
│
├── Data
│   └── ApplicationDbContext.cs
│
├── Views
│   ├── Account
│   ├── Contact
│   ├── Donate
│   ├── Event
│   ├── Admin
│   └── Shared
│
├── wwwroot
│   ├── css
│   ├── js
│   └── images
│
├── Migrations
│
├── Program.cs
└── README.md
```

---

# Screenshots

The following section should contain screenshots of the completed application.

## Home Page

![Home Page](docs/images/homepage.png)

## About Us

![About Us](docs/images/about-us.png)

## Our Work

![Our Work](docs/images/our-work.png)

## Partnerships

![Partnerships](docs/images/partnerships.png)

## Track Record

![Track Record](docs/images/track-record.png)

## Donate

![Donate](docs/images/donate.png)

## Contact

![Contact](docs/images/contact.png)

## Login

![Login](docs/images/login.png)

## My Account

![My Account](docs/images/profile.png)

## Events

![Events](docs/images/events.png)

## Administration

![Administration](docs/images/admin.png)

---

# How to Run the Project

## Prerequisites

Install:

- Visual Studio
- .NET SDK compatible with the project
- SQL Server
- SQL Server Management Studio or another SQL Server management tool
- Git

## Clone the Repository

```bash
git clone https://github.com/TechMoveInovators/WIL-SisterLove.git
```

Then open the project in Visual Studio.

## Database Setup

1. Configure the SQL Server connection string in the project's configuration.
2. Ensure SQL Server is running.
3. Apply the Entity Framework Core migrations.
4. Run the application.

## Run the Application

In Visual Studio:

1. Open the solution.
2. Restore NuGet packages.
3. Build the solution.
4. Start the application using the HTTPS profile.

The local application will normally be available at:

```text
https://localhost:7203
```

---

# Testing

The application should be tested across its major workflows.

Recommended tests include:

### Authentication Tests

- Valid login
- Invalid email
- Invalid password
- Empty login fields
- Administrator login
- Normal user login
- Logout
- Accessing protected pages while logged out

### Contact Tests

- Valid contact submission
- Invalid email
- Missing name
- Missing message
- Message length validation
- Database persistence

### Donation Tests

- Valid donation
- Invalid amount
- Required fields
- Transaction creation
- Database persistence
- Thank-you page

### Event Tests

- View events
- Create event
- Edit event
- Delete event
- Unauthenticated access
- Creator ID assignment

---

# GitHub and Version Control

The project is maintained using Git and GitHub.

Repository:

[https://github.com/TechMoveInovators/WIL-SisterLove](https://github.com/TechMoveInovators/WIL-SisterLove)

Development work is committed using structured commit messages to provide a clear history of changes and collaboration.

Example:

```bash
git add .
git commit -m "Updated authentication security and data flow"
git push origin Master
```

---

# Application Goals

The main goals of the SisterLove South Africa application are to:

- Provide a professional online presence
- Communicate SisterLove South Africa's work
- Support community engagement
- Allow visitors to make enquiries
- Support donations
- Encourage volunteering
- Support partnerships and funding enquiries
- Provide secure user accounts
- Provide administration functionality
- Store application data reliably in SQL Server
- Provide a maintainable MVC architecture

---

# Conclusion

The SisterLove South Africa application brings together a responsive web interface, database-driven functionality, authentication, security controls and administrative features into one platform.

The project demonstrates the use of:

- ASP.NET Core MVC
- C#
- Entity Framework Core
- SQL Server
- Cookie Authentication
- BCrypt password verification
- Claims-based authentication
- Authorisation
- Secure form handling
- Anti-forgery protection
- Service-based application logic
- Git and GitHub

The result is a structured application that supports the organisation's public-facing information, community engagement and internal management requirements.

---

## Project Team

**SisterLove South Africa WIL Project**

Repository: [TechMoveInovators/WIL-SisterLove](https://github.com/TechMoveInovators/WIL-SisterLove)

> Add the names and student numbers of all team members here before submitting the final repository.
"""

# Use pypandoc as required for markdown generation.
out = Path("/mnt/data/SisterLove_README.md")
pypandoc.convert_text(readme, "md", format="md", outputfile=str(out), extra_args=["--standalone"])

print(f"Created: {out}")
