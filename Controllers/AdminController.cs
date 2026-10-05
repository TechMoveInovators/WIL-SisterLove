using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Sisterlove.Data;
using SisterLoveSA.Models;

namespace SisterLoveSA.Controllers

{
    [Authorize(Roles = "Administrator")]
    public class AdminController : Controller
    {
        private readonly ApplicationDbContext _context;

        public AdminController(ApplicationDbContext context)
        {
            _context = context;
        }

        // ============================================================
        // ADMIN DASHBOARD
        // ============================================================

        [HttpGet]
        public async Task<IActionResult> Index()
        {
            var model = new AdminDashboardViewModel
            {
                TotalDonations = await _context.Donations.CountAsync(),

                TotalVolunteerApplications =
                    await _context.VolunteerApplications.CountAsync(),

                TotalFundingEnquiries =
                    await _context.FundingEnquiries.CountAsync(),

                TotalContactEnquiries =
                    await _context.ContactEnquiries.CountAsync(),

                TotalProgrammes =
                    await _context.Programmes.CountAsync(),

                TotalPartnerships =
                    await _context.Partnerships.CountAsync(),

                TotalNewsArticles =
                    await _context.NewsArticles.CountAsync(),

                TotalLegacyProjects =
                    await _context.LegacyProjects.CountAsync(),

                TotalEvents =
                    await _context.Events.CountAsync()
            };

            return View(model);
        }

        // ============================================================
        // DONATIONS
        // ============================================================

        [HttpGet]
        public async Task<IActionResult> Donations()
        {
            var donations = await _context.Donations
                .OrderByDescending(d => d.CreatedAt)
                .ToListAsync();

            return View(donations);
        }

        // ============================================================
        // VOLUNTEER APPLICATIONS
        // ============================================================

        [HttpGet]
        public async Task<IActionResult> VolunteerApplications()
        {
            var applications = await _context.VolunteerApplications
                .OrderByDescending(v => v.SubmittedAt)
                .ToListAsync();

            return View(applications);
        }

        // ============================================================
        // FUNDING ENQUIRIES
        // ============================================================

        [HttpGet]
        public async Task<IActionResult> FundingEnquiries()
        {
            var enquiries = await _context.FundingEnquiries
                .OrderByDescending(f => f.SubmittedAt)
                .ToListAsync();

            return View(enquiries);
        }

        // ============================================================
        // CONTACT ENQUIRIES
        // ============================================================

        [HttpGet]
        public async Task<IActionResult> ContactEnquiries()
        {
            var enquiries = await _context.ContactEnquiries
                .OrderByDescending(c => c.SubmittedAt)
                .ToListAsync();

            return View(enquiries);
        }
    }
}