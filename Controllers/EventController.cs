using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Sisterlove.Data;
using Sisterlove.Models.Entities;
using Sisterlove.Services;
using SisterLoveSA.Models;
using SisterLoveSA.Services;
using System.Security.Claims;

namespace SisterLoveSA.Controllers
{
    public class EventController : Controller
    {
        private readonly ApplicationDbContext _context;
        private readonly IVolunteerService _volunteerService;
        private readonly ILogger<EventController> _logger;

        public EventController(
            ApplicationDbContext context,
            IVolunteerService volunteerService,
            ILogger<EventController> logger)
        {
            _context = context;
            _volunteerService = volunteerService;
            _logger = logger;
        }

        // GET: /Event
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

        // GET: /Event/Volunteer
        [AllowAnonymous]
        [HttpGet]
        public IActionResult Volunteer()
        {
            return View(new VolunteerViewModel());
        }

        // POST: /Event/Volunteer
        [AllowAnonymous]
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Volunteer(VolunteerViewModel model)
        {
            if (!ModelState.IsValid)
            {
                return View(model);
            }

            var success = await _volunteerService.SubmitVolunteerApplicationAsync(model);

            if (success)
            {
                TempData["SuccessMessage"] = "Thank you! Your volunteer application has been submitted.";
                return RedirectToAction(nameof(Index));
            }

            ModelState.AddModelError("", "Unable to submit volunteer application. Please try again.");
            return View(model);
        }

        // GET: /Event/Create
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
                var userIdClaim = User.FindFirstValue(ClaimTypes.NameIdentifier);

                if (!int.TryParse(userIdClaim, out var userId))
                {
                    _logger.LogWarning("Authenticated user attempted to create an event without a valid user ID claim.");
                    return Challenge();
                }

                var newEvent = new Event
                {
                    EventName = model.EventName,
                    Description = model.Description,
                    EventDate = model.EventDate,
                    Venue = model.Venue,
                    CreatedByUserId = userId,
                    CreatedAt = DateTime.UtcNow
                };

                _context.Events.Add(newEvent);
                await _context.SaveChangesAsync();

                TempData["SuccessMessage"] = "Event created successfully.";
                return RedirectToAction(nameof(Index));
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error creating event.");
                ModelState.AddModelError("", "Unable to create the event. Please try again.");
                return View(model);
            }
        }

        // GET: /Event/Edit/5
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

                eventItem.EventName = model.EventName;
                eventItem.Description = model.Description;
                eventItem.EventDate = model.EventDate;
                eventItem.Venue = model.Venue;

                await _context.SaveChangesAsync();

                TempData["SuccessMessage"] = "Event updated successfully.";
                return RedirectToAction(nameof(Index));
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error updating event with ID {EventId}.", model.Id);
                ModelState.AddModelError("", "Unable to update the event. Please try again.");
                return View(model);
            }
        }

        // GET: /Event/Delete/5
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

                TempData["SuccessMessage"] = "Event deleted successfully.";
                return RedirectToAction(nameof(Index));
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error deleting event with ID {EventId}.", id);
                TempData["ErrorMessage"] = "Unable to delete the event. Please try again.";
                return RedirectToAction(nameof(Index));
            }
        }
    }
}