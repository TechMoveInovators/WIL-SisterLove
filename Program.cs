using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.EntityFrameworkCore;
using Sisterlove.Data;
using Sisterlove.Services;
using SisterLoveSA.Services;

var builder = WebApplication.CreateBuilder(args);

// ============================================================
// SERVICES
// ============================================================

// MVC
builder.Services.AddControllersWithViews();

// Application Services
builder.Services.AddScoped<IDonationService, DonationService>();
builder.Services.AddScoped<IContactService, ContactService>();

// ============================================================
// DATABASE
// ============================================================

builder.Services.AddDbContext<ApplicationDbContext>(options =>
    options.UseSqlServer(
        builder.Configuration.GetConnectionString("DefaultConnection")));

// ============================================================
// SESSION
// ============================================================

builder.Services.AddDistributedMemoryCache();

builder.Services.AddSession(options =>
{
    // Session expires after 30 minutes of inactivity
    options.IdleTimeout = TimeSpan.FromMinutes(30);

    // Prevent JavaScript from accessing the session cookie
    options.Cookie.HttpOnly = true;

    // Required for session to work
    options.Cookie.IsEssential = true;

    // Helps protect against certain cross-site request attacks
    options.Cookie.SameSite = SameSiteMode.Lax;
});

// ============================================================
// AUTHENTICATION
// ============================================================

builder.Services.AddAuthentication(
    CookieAuthenticationDefaults.AuthenticationScheme)
    .AddCookie(
        CookieAuthenticationDefaults.AuthenticationScheme,
        options =>
        {
            // Page users are redirected to when they are
            // not authenticated
            options.LoginPath = "/Account/Login";

            // Page users are redirected to when they are
            // authenticated but do not have permission
            options.AccessDeniedPath = "/Account/AccessDenied";

            // Name of the authentication cookie
            options.Cookie.Name = "SisterLove.Auth";

            // Prevent JavaScript from accessing the
            // authentication cookie
            options.Cookie.HttpOnly = true;

            // Helps protect against cross-site request attacks
            options.Cookie.SameSite = SameSiteMode.Lax;

            // HTTPS in production, HTTP allowed during
            // local development
            options.Cookie.SecurePolicy =
                CookieSecurePolicy.SameAsRequest;

            // Authentication expires after 30 minutes
            options.ExpireTimeSpan = TimeSpan.FromMinutes(30);

            // Extend the authentication period when the
            // authenticated user remains active
            options.SlidingExpiration = true;
        });

//API INTEGRATION

builder.Services.AddHttpClient("NodeApi", client =>
{
    client.BaseAddress = new Uri(builder.Configuration["ApiSettings:BaseUrl"] ?? "http://localhost:4000/api");
});

builder.Services.AddScoped<IVolunteerService, VolunteerService>();

// ============================================================
// BUILD APPLICATION
// ============================================================

var app = builder.Build();



// ============================================================
// ERROR HANDLING
// ============================================================

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");

    // Adds HTTP Strict Transport Security in production
    app.UseHsts();
}

// ============================================================
// MIDDLEWARE PIPELINE
// ============================================================

// Redirect HTTP requests to HTTPS
app.UseHttpsRedirection();

// Allow CSS, JavaScript, images and other static files
app.UseStaticFiles();

// Enable routing
app.UseRouting();

// ============================================================
// AUTHENTICATION & AUTHORIZATION
// ============================================================

// Identify the logged-in user
app.UseAuthentication();

// Check whether the logged-in user has permission
// to access the requested resource
app.UseAuthorization();

// ============================================================
// SESSION
// ============================================================

app.UseSession();

// ============================================================
// MVC ROUTING
// ============================================================

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=Index}/{id?}");

// ============================================================
// START APPLICATION
// ============================================================

app.Run();