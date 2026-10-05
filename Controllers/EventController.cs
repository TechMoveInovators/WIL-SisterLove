using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SisterLoveSA.Models;
using Sisterlove.Data;
using Sisterlove.Models.Entities;

namespace SisterLoveSA.Controllers
{
    public class EventController : Controller
    {
        private readonly ApplicationDbContext _context;
        private readonly ILogger<EventController> _logger;

        public EventController(
            ApplicationDbContext context,
            ILogger<EventController> logger)
        {
            _context = context;
            _logger = logger;
        }

        // GET: /Event
        // Public page - anyone can view events.
        [AllowAnonymous]
        [HttpGet]
        public async Task<IActionResult> Index()
        {
            var events = await _context.Events
                .AsNoTracking()
                .Include(e => e.CreatedByUser)
                .OrderBy(e => e.EventDate)
                .Select(e => new EventViewModel
                {
                    Id = e.Id,
                    EventName = e.EventName,
                    Description = e.Description,
                    EventDate = e.EventDate,
                    Venue = e.Venue,
                    CreatedByUserId = e.CreatedByUserId
                })
                .ToListAsync();

            return View(events);
        }

        // GET: /Event/Create
        // Only authenticated users can create events.
        [Authorize]
        [HttpGet]
        public IActionResult Create()
        {
            var model = new EventViewModel
            {
                EventDate = DateTime.Now
            };

            return View(model);
        }

        // POST: /Event/Create
        [Authorize]
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(EventViewModel model)
        {
            if (!ModelState.IsValid)
            {
                return View(model);
            }

            try
            {
                // Get the authenticated user's email from the login claim.
                var email = User.FindFirstValue(ClaimTypes.Email);

                if (string.IsNullOrWhiteSpace(email))
                {
                    _logger.LogWarning(
                        "Authenticated user attempted to create an event without an email claim.");

                    return Challenge();
                }

                // Find the real user in the database.
                var currentUser = await _context.Users
                    .FirstOrDefaultAsync(u => u.Email == email);

                if (currentUser == null)
                {
                    _logger.LogWarning(
                        "No database user found for authenticated email {Email}.",
                        email);

                    return Forbid();
                }

                var newEvent = new Event
                {
                    EventName = model.EventName,
                    Description = model.Description,
                    EventDate = model.EventDate,
                    Venue = model.Venue,

                    // IMPORTANT:
                    // The user ID comes from the database,
                    // NOT from the submitted form.
                    CreatedByUserId = currentUser.Id,

                    CreatedAt = DateTime.UtcNow
                };

                _context.Events.Add(newEvent);
                await _context.SaveChangesAsync();

                TempData["SuccessMessage"] =
                    "Event created successfully.";

                return RedirectToAction(nameof(Index));
            }
            catch (Exception ex)
            {
                _logger.LogError(
                    ex,
                    "Error creating event.");

                ModelState.AddModelError(
                    "",
                    "Unable to create the event. Please try again.");

                return View(model);
            }
        }

        // GET: /Event/Edit/5
        // Only authenticated users can edit events.
        [Authorize]
        [HttpGet]
        public async Task<IActionResult> Edit(int id)
        {
            var eventItem = await _context.Events
                .AsNoTracking()
                .FirstOrDefaultAsync(e => e.Id == id);

            if (eventItem == null)
            {
                return NotFound();
            }

            var model = new EventViewModel
            {
                Id = eventItem.Id,
                EventName = eventItem.EventName,
                Description = eventItem.Description,
                EventDate = eventItem.EventDate,
                Venue = eventItem.Venue,

                // Display the existing creator.
                CreatedByUserId = eventItem.CreatedByUserId
            };

            return View(model);
        }

        // POST: /Event/Edit
        [Authorize]
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(EventViewModel model)
        {
            if (!ModelState.IsValid)
            {
                return View(model);
            }

            try
            {
                var eventItem = await _context.Events
                    .FirstOrDefaultAsync(e => e.Id == model.Id);

                if (eventItem == null)
                {
                    return NotFound();
                }

                // Only update fields that the user is allowed
                // to change.
                eventItem.EventName = model.EventName;
                eventItem.Description = model.Description;
                eventItem.EventDate = model.EventDate;
                eventItem.Venue = model.Venue;

                // DO NOT update CreatedByUserId from the form.
                // The original creator remains associated
                // with the event.

                await _context.SaveChangesAsync();

                TempData["SuccessMessage"] =
                    "Event updated successfully.";

                return RedirectToAction(nameof(Index));
            }
            catch (Exception ex)
            {
                _logger.LogError(
                    ex,
                    "Error updating event with ID {EventId}.",
                    model.Id);

                ModelState.AddModelError(
                    "",
                    "Unable to update the event. Please try again.");

                return View(model);
            }
        }

        // GET: /Event/Delete/5
        // Only authenticated users can access the delete confirmation.
        [Authorize]
        [HttpGet]
        public async Task<IActionResult> Delete(int id)
        {
            var eventItem = await _context.Events
                .AsNoTracking()
                .FirstOrDefaultAsync(e => e.Id == id);

            if (eventItem == null)
            {
                return NotFound();
            }

            var model = new EventViewModel
            {
                Id = eventItem.Id,
                EventName = eventItem.EventName,
                Description = eventItem.Description,
                EventDate = eventItem.EventDate,
                Venue = eventItem.Venue,
                CreatedByUserId = eventItem.CreatedByUserId
            };

            return View(model);
        }

        // POST: /Event/Delete
        [Authorize]
        [HttpPost]
        [ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            try
            {
                var eventItem = await _context.Events
                    .FirstOrDefaultAsync(e => e.Id == id);

                if (eventItem == null)
                {
                    return NotFound();
                }

                _context.Events.Remove(eventItem);

                await _context.SaveChangesAsync();

                TempData["SuccessMessage"] =
                    "Event deleted successfully.";

                return RedirectToAction(nameof(Index));
            }
            catch (Exception ex)
            {
                _logger.LogError(
                    ex,
                    "Error deleting event with ID {EventId}.",
                    id);

                TempData["ErrorMessage"] =
                    "Unable to delete the event. Please try again.";

                return RedirectToAction(nameof(Index));
            }
        }
    }
}